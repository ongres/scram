/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkArgument;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;
import static com.ongres.scram.common.util.Preconditions.gt0;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import com.ongres.scram.common.exception.ScramInterruptedException;
import com.ongres.scram.common.exception.ScramRuntimeException;
import org.jetbrains.annotations.NotNull;

/**
 * Utility static methods for cryptography related tasks.
 */
final class CryptoUtil {

  /**
   * The interval at which the PBKDF2 loop checks for thread interruption.
   *
   * <p>Checking the thread state via {@link Thread#isInterrupted()} on every
   * iteration requires a native JVM call, which significantly degrades hashing
   * throughput. This stride value batches iterations, allowing the loop to execute
   * rapidly while remaining responsive to shutdown signals.
   *
   * <p><b>Note:</b> This value MUST be a power of two. This allows the compiler
   * to use a highly optimized bitwise AND operation {@code (i & (STRIDE - 1))}
   * rather than a slower modulo operator.
   */
  private static final int INTERRUPT_CHECK_STRIDE = 1024;

  private CryptoUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Compute the "Hi" function for SCRAM.
   *
   * {@code
   *      Hi(str, salt, i):
   *
   *      U1   := HMAC(str, salt + INT(1))
   *      U2   := HMAC(str, U1)
   *      ...
   *      Ui-1 := HMAC(str, Ui-2)
   *      Ui   := HMAC(str, Ui-1)
   *
   *      Hi := U1 XOR U2 XOR ... XOR Ui
   *
   *       where "i" is the iteration count, "+" is the string concatenation
   *       operator, and INT(g) is a 4-octet encoding of the integer g, most
   *       significant octet first.
   *
   *       Hi() is, essentially, PBKDF2 [RFC2898] with HMAC() as the
   *       pseudorandom function (PRF) and with dkLen == output length of
   *       HMAC() == output length of H().
   * }
   *
   * @param mac The Mac instance to use
   * @param password The char array to compute the Hi function
   * @param salt The salt
   * @param iterationCount The number of iterations
   * @return The bytes of the computed Hi value
   * @throws ScramRuntimeException if unsupported key for Mac algorithm, or if
   *           thread is interrupted
   */
  static byte[] hi(Mac mac, char[] password, byte[] salt, int iterationCount) {
    checkNotNull(mac, "mac");
    checkNotNull(password, "password");
    checkNotNull(salt, "salt");
    checkArgument(salt.length != 0, "salt");
    gt0(iterationCount, "iterationCount");
    try {
      byte[] pwBytes = passwordToUtf8Bytes(password);
      try {
        mac.init(new SecretKeySpec(pwBytes, mac.getAlgorithm()));
      } finally {
        Arrays.fill(pwBytes, (byte) 0);
      }
    } catch (InvalidKeyException ex) {
      throw new ScramRuntimeException(
          String.format(Locale.ROOT, "Platform error: unsupported key for %s algorithm",
              mac.getAlgorithm()),
          ex);
    }

    mac.update(salt);
    // The 4-octet encoding of the integer 1 INT(1).
    mac.update((byte) 0);
    mac.update((byte) 0);
    mac.update((byte) 0);
    mac.update((byte) 1);

    byte[] ui = mac.doFinal();
    byte[] result = Arrays.copyOf(ui, ui.length);
    boolean success = false;

    try {
      for (int i = 2; i <= iterationCount; i++) {
        if ((i & (INTERRUPT_CHECK_STRIDE - 1)) == 0 && Thread.currentThread().isInterrupted()) {
          throw new ScramInterruptedException("PBKDF2 computation interrupted at iteration " + i);
        }
        mac.update(ui);
        mac.doFinal(ui, 0);

        for (int j = 0; j < result.length; j++) {
          result[j] ^= ui[j];
        }
      }

      success = true;
      return result;
    } catch (ShortBufferException e) {
      throw new AssertionError("Buffer sized by Mac.doFinal() is suddenly too short", e);
    } finally {
      Arrays.fill(ui, (byte) 0);
      if (!success) {
        Arrays.fill(result, (byte) 0);
      }
    }
  }

  /**
   * Convert password to UTF-8 bytes and secure clear the backing array.
   *
   * @param password The password to convert
   * @return The UTF-8 bytes of the password
   */
  private static byte[] passwordToUtf8Bytes(char[] password) {
    ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
    try {
      byte[] pwBytes = new byte[bb.remaining()];
      bb.get(pwBytes);
      return pwBytes;
    } finally {
      // Wipe of the intermediate buffer
      if (bb.hasArray()) {
        Arrays.fill(bb.array(), bb.arrayOffset(), bb.arrayOffset() + bb.capacity(), (byte) 0);
      } else {
        // Fallback just in case a future JDK returns a DirectBuffer here
        bb.clear();
        while (bb.hasRemaining()) {
          bb.put((byte) 0);
        }
      }
    }
  }

  /**
   * Computes the HMAC of a given message.
   *
   * {@code
   * HMAC(key, str): Apply the HMAC keyed hash algorithm (defined in
   * [RFC2104]) using the octet string represented by "key" as the key
   * and the octet string "str" as the input string.  The size of the
   * result is the hash result size for the hash function in use.  For
   * example, it is 20 octets for SHA-1 (see [RFC3174]).
   * }
   *
   * @param secretKeySpec A key of the given algorithm
   * @param mac A MAC instance of the given algorithm
   * @param message The message to compute the HMAC
   * @return The bytes of the computed HMAC value
   * @throws ScramRuntimeException unsupported key for HMAC algorithm
   */
  static byte[] hmac(SecretKeySpec secretKeySpec, Mac mac, byte[] message) {
    try {
      mac.init(secretKeySpec);
    } catch (InvalidKeyException ex) {
      throw new ScramRuntimeException(
          String.format(Locale.ROOT, "Platform error: unsupported key for %s algorithm",
              mac.getAlgorithm()),
          ex);
    }
    return mac.doFinal(message);
  }

  /**
   * Computes a byte-by-byte xor operation.
   * {@code
   * XOR: Apply the exclusive-or operation to combine the octet string
   * on the left of this operator with the octet string on the right of
   * this operator.  The length of the output and each of the two
   * inputs will be the same for this use.
   * }
   *
   * @param value1 first value to apply xor
   * @param value2 second value to apply xor
   * @return xor operation
   */
  static byte @NotNull [] xor(byte @NotNull [] value1, byte @NotNull [] value2) {
    checkNotNull(value1, "value1");
    checkNotNull(value2, "value2");
    checkArgument(value1.length == value2.length, "Both values must have the same length");

    byte[] result = new byte[value1.length];
    for (int i = 0; i < value1.length; i++) {
      result[i] = (byte) (value1[i] ^ value2[i]);
    }

    return result;
  }

  /**
   * Generates a random salt. Normally the output is encoded to Base64.
   *
   * @param saltSize The length of the salt, in bytes
   * @param random The SecureRandom to use
   * @return The bye[] representing the salt
   * @throws IllegalArgumentException if the saltSize is not positive, or if random is null
   */
  static byte @NotNull [] salt(int saltSize, @NotNull SecureRandom random) {
    gt0(saltSize, "saltSize");
    checkNotNull(random, "random");
    byte[] randomSalt = new byte[saltSize];
    random.nextBytes(randomSalt);
    return randomSalt;
  }
}
