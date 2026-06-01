/*
 * Copyright (c) 2026 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.ongres.scram.common.exception.ScramInterruptedException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HiFunctionTest {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  // Character sets for different edge cases
  private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final String SPECIAL = "!@#$%^&*()-_=+[{]}\\|;:'\",.<>/?`~ ";
  private static final String UNICODE_EMOJI = "🚀🔥🛡️🔑";
  private static final String UNICODE_EXTENDED = "こんにちは世界"; // "Hello World" in Japanese
  private static final String COMBINED_POOL = ALPHANUMERIC + SPECIAL + UNICODE_EXTENDED + UNICODE_EMOJI;
  private static final int[] VALID_CODE_POINTS = COMBINED_POOL.codePoints().toArray();

  private static char[] generateRandom(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomCodePoint = VALID_CODE_POINTS[SECURE_RANDOM.nextInt(VALID_CODE_POINTS.length)];
      sb.appendCodePoint(randomCodePoint);
    }
    return sb.toString().toCharArray();
  }

  /**
   * ScramMechanism x Random Passwords (of different lengths) x Iteration Counts.
   */
  static Stream<Arguments> scramTestMatrix() {
    return Stream.of(ScramMechanism.values())
        .filter(m -> !m.isPlus())
        .flatMap(mechanism -> SECURE_RANDOM.ints(1, 200).limit(15).boxed()
            .flatMap(pwLength -> Stream.of(1, 4096, 10_000, 25_000)
                .map(iter -> Arguments.of(mechanism, pwLength, iter))));
  }

  @ParameterizedTest(name = "{0} | PW Len: {1} | Iter: {2}")
  @MethodSource("scramTestMatrix")
  void testAlgorithmCorrectness(ScramMechanism mechanism, int pwLength, int iterations) {
    String hmacAlgorithm = mechanism.getHmacAlgorithmName();
    char[] password = StringPreparation.POSTGRESQL_PREPARATION.normalize(generateRandom(pwLength));
    int randomSaltSize = ThreadLocalRandom.current().nextInt(1, 200);
    byte[] salt = CryptoUtil.salt(randomSaltSize, SECURE_RANDOM);
    try {
      Mac mac = Mac.getInstance(hmacAlgorithm);
      byte[] actual = CryptoUtil.hi(mac, password, salt, iterations);

      // Verify against standard PBKDF2 (which 'hi' is based on)
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2With" + hmacAlgorithm);
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, mac.getMacLength() * 8);
      byte[] expected = factory.generateSecret(spec).getEncoded();

      assertArrayEquals(expected, actual, "Hi mismatch for " + hmacAlgorithm);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      Assumptions.abort("Skipping: " + hmacAlgorithm + " not supported by current Provider.");
      return;
    }
  }

  @Test
  void testNullHandling() throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    char[] pass = "pw".toCharArray();
    byte[] salt = CryptoUtil.salt(24, new SecureRandom());

    assertAll("Null checks",
        () -> assertThrows(IllegalArgumentException.class,
            () -> CryptoUtil.hi(null, pass, salt, 1)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> CryptoUtil.hi(mac, null, salt, 1)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> CryptoUtil.hi(mac, pass, null, 1)));
  }

  @Test
  void testInvalidInputs() throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    char[] pass = "pw".toCharArray();
    byte[] salt = CryptoUtil.salt(16, new SecureRandom());

    assertThrows(IllegalArgumentException.class,
        () -> CryptoUtil.hi(mac, pass, new byte[0], 100));
    assertThrows(IllegalArgumentException.class,
        () -> CryptoUtil.hi(mac, pass, salt, 0));
  }

  @Test
  void testInterruptionBehavior() throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    char[] password = "long-running-task".toCharArray();
    byte[] salt = CryptoUtil.salt(24, new SecureRandom());
    int iterations = 1_000_000;

    try {
      Thread.currentThread().interrupt();

      ScramInterruptedException exception = assertThrows(ScramInterruptedException.class,
          () -> CryptoUtil.hi(mac, password, salt, iterations));

      assertTrue(exception.getMessage().contains("PBKDF2 computation interrupted"),
          "Expected interruption error but got: " + exception.getMessage());
    } finally {
      // CRITICAL: Clear the interrupt flag so it doesn't bleed into other tests!
      // Thread.interrupted() returns the flag state AND resets it to false.
      Thread.interrupted();
    }
  }

  @Test
  void testInterruptionMidExecution() throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    char[] password = "long-running-task".toCharArray();
    byte[] salt = CryptoUtil.salt(24, new SecureRandom());
    int iterations = 50_000_000; // High enough to stay in the loop

    AtomicReference<Throwable> actualError = new AtomicReference<>();
    CountDownLatch threadStarted = new CountDownLatch(1);
    CountDownLatch threadFinished = new CountDownLatch(1);

    Thread worker = new Thread(() -> {
      threadStarted.countDown(); // 1. Signal that we are alive
      try {
        CryptoUtil.hi(mac, password, salt, iterations);
      } catch (Throwable t) {
        actualError.set(t); // 4. Capture the exception safely
      } finally {
        threadFinished.countDown();
      }
    });

    worker.start();
    // 2. Wait until the thread has actually started executing
    threadStarted.await();
    // Give it a tiny fraction of a second to enter the PBKDF2 loop
    Thread.sleep(50);
    // 3. Fire the interrupt mid-flight
    worker.interrupt();
    // 5. Wait for the thread to shut down (with a timeout so tests never hang forever)
    boolean finishedCleanly = threadFinished.await(2, TimeUnit.SECONDS);
    assertTrue(finishedCleanly, "The thread hung and did not respect the interrupt signal!");
    // 6. Verify the captured exception
    Throwable caught = actualError.get();
    assertNotNull(caught, "Expected an exception, but the task completed normally.");
    assertInstanceOf(ScramInterruptedException.class, caught,
        "Expected ScramInterruptedException but got: " + caught.getClass().getName());
  }

  @Test
  void testHmacSHA3_512() throws Exception {
    // Derived key generated via "golang.org/x/crypto/pbkdf2"
    String expectedHex = "AVe/93um8jge6rPdfKvlV11zTpETcH56COQ+GToe4F3tv+99LYkIYYY4rLFbjbAz+6mdbZXVBbphDmgN2o3LSQ==";
    byte[] expected = Base64.getDecoder().decode(expectedHex);

    // Gracefully skip on JVMs (like Java 8) that lack native SHA-3 support
    Mac mac;
    try {
      mac = Mac.getInstance("HmacSHA3-512");
    } catch (NoSuchAlgorithmException e) {
      Assumptions.abort("Skipping test: JVM does not support HmacSHA3-512");
      return;
    }

    char[] password = "my_super_secret_password".toCharArray();
    byte[] salt = "my_random_salt_value".getBytes(StandardCharsets.UTF_8);
    int iterations = 100_000;
    byte[] actual = CryptoUtil.hi(mac, password, salt, iterations);

    assertArrayEquals(expected, actual, "Hi mismatch for HmacSHA3-512");
  }

}
