/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;
import static com.ongres.scram.common.util.Preconditions.gt0;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import com.ongres.scram.common.exception.ScramRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * SCRAM Mechanisms supported by this library. At least, {@code SCRAM-SHA-1} and
 * {@code SCRAM-SHA-256} are provided, since both the hash and the HMAC implementations are provided
 * by the Java JDK version 8 or greater.
 *
 * <p>{@link java.security.MessageDigest}: "Every implementation of the Java platform is required to
 * support the following standard MessageDigest algorithms: {@code SHA-1}, {@code SHA-256}".
 *
 * <p>{@link javax.crypto.Mac}: "Every implementation of the Java platform is required to support
 * the following standard Mac algorithms: {@code HmacSHA1}, {@code HmacSHA256}".
 *
 * @see <a href="https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml#scram"> SASL
 *      SCRAM Family Mechanisms</a>
 */
public enum ScramMechanism {

  /**
   * SCRAM-SHA-1 mechanism, defined in RFC-5802.
   */
  SCRAM_SHA_1("SCRAM-SHA-1", "SHA-1", 160, "HmacSHA1", 4096),
  /**
   * SCRAM-SHA-1-PLUS mechanism, defined in RFC-5802.
   */
  SCRAM_SHA_1_PLUS("SCRAM-SHA-1-PLUS", "SHA-1", 160, "HmacSHA1", 4096),
  /**
   * SCRAM-SHA-224 mechanism, not defined in an RFC.
   */
  SCRAM_SHA_224("SCRAM-SHA-224", "SHA-224", 224, "HmacSHA224", 4096),
  /**
   * SCRAM-SHA-224-PLUS mechanism, not defined in an RFC.
   */
  SCRAM_SHA_224_PLUS("SCRAM-SHA-224-PLUS", "SHA-224", 224, "HmacSHA224", 4096),
  /**
   * SCRAM-SHA-256 mechanism, defined in RFC-7677.
   */
  SCRAM_SHA_256("SCRAM-SHA-256", "SHA-256", 256, "HmacSHA256", 4096),
  /**
   * SCRAM-SHA-256-PLUS mechanism, defined in RFC-7677.
   */
  SCRAM_SHA_256_PLUS("SCRAM-SHA-256-PLUS", "SHA-256", 256, "HmacSHA256", 4096),
  /**
   * SCRAM-SHA-384 mechanism, not defined in an RFC.
   */
  SCRAM_SHA_384("SCRAM-SHA-384", "SHA-384", 384, "HmacSHA384", 4096),
  /**
   * SCRAM-SHA-384-PLUS mechanism, not defined in an RFC.
   */
  SCRAM_SHA_384_PLUS("SCRAM-SHA-384-PLUS", "SHA-384", 384, "HmacSHA384", 4096),
  /**
   * SCRAM-SHA-512 mechanism.
   */
  SCRAM_SHA_512("SCRAM-SHA-512", "SHA-512", 512, "HmacSHA512", 10000),
  /**
   * SCRAM-SHA-512-PLUS mechanism.
   */
  SCRAM_SHA_512_PLUS("SCRAM-SHA-512-PLUS", "SHA-512", 512, "HmacSHA512", 10000);

  private static final @Unmodifiable Map<String, ScramMechanism> BY_NAME_MAPPING =
      Arrays.stream(values())
          .filter(ScramMechanism::isAlgorithmSupported)
          .collect(Collectors.collectingAndThen(
              Collectors.toMap(ScramMechanism::getName, Function.identity()),
              Collections::unmodifiableMap));
  private static final @Unmodifiable List<String> SUPPORTED_MECHANISMS = BY_NAME_MAPPING.keySet()
      .stream()
      .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

  private final @NotNull String mechanismName;
  private final @NotNull String hashAlgorithmName;
  private final int keyLength;
  private final @NotNull String hmacAlgorithmName;
  private final @NotNull String keyFactoryAlgorithmName;
  private final boolean channelBinding;
  private final int iterationCount;

  ScramMechanism(String name, String hashAlgorithmName, int keyLength, String hmacAlgorithmName,
      int iterationCount) {
    this.mechanismName = checkNotNull(name, "name");
    this.hashAlgorithmName = checkNotNull(hashAlgorithmName, "hashAlgorithmName");
    this.keyLength = gt0(keyLength, "keyLength");
    this.hmacAlgorithmName = checkNotNull(hmacAlgorithmName, "hmacAlgorithmName");
    this.keyFactoryAlgorithmName = "PBKDF2With" + hmacAlgorithmName;
    this.channelBinding = name.endsWith("-PLUS");
    this.iterationCount = gt0(iterationCount, "iterationCount");
  }

  /**
   * Method that returns the name of the hash algorithm. It is protected since should be of no
   * interest for direct users. The instance is supposed to provide abstractions over the algorithm
   * names, and are not meant to be directly exposed.
   *
   * @return The name of the hash algorithm
   */
  @NotNull
  String getHashAlgorithmName() {
    return hashAlgorithmName;
  }

  /**
   * Method that returns the name of the HMAC algorithm. It is protected since should be of no
   * interest for direct users. The instance is supposed to provide abstractions over the algorithm
   * names, and are not meant to be directly exposed.
   *
   * @return The name of the HMAC algorithm
   */
  @NotNull
  String getHmacAlgorithmName() {
    return hmacAlgorithmName;
  }

  /**
   * The name of the mechanism.
   *
   * <p>Must be a value registered under IANA: <a
   * href="https://www.iana.org/assignments/sasl-mechanisms/sasl-mechanisms.xhtml#scram"> SASL SCRAM
   * Family Mechanisms</a>
   *
   * @return The mechanism name
   */
  @NotNull
  public String getName() {
    return mechanismName;
  }

  /**
   * The mechanism {@code -PLUS} require channel binding.
   *
   * @return true if the mechanism requires channel binding
   */
  public boolean isPlus() {
    return channelBinding;
  }

  /**
   * Returns the length of the key length of the algorithm.
   *
   * @return The length (in bits)
   */
  int getKeyLength() {
    return keyLength;
  }

  int getIterationCount() {
    return iterationCount;
  }

  /**
   * Calculate a message digest, according to the algorithm of the SCRAM mechanism.
   *
   * @param message the message
   * @return The calculated message digest
   * @throws ScramRuntimeException If the algorithm is not provided by current JVM or any included
   *           implementations
   */
  byte @NotNull [] digest(byte @NotNull [] message) {
    try {
      return MessageDigest.getInstance(hashAlgorithmName).digest(message);
    } catch (NoSuchAlgorithmException e) {
      throw new ScramRuntimeException(
          "Hash algorithm " + hashAlgorithmName + " not present in current JVM", e);
    }
  }

  /**
   * Calculate the hmac of a key and a message, according to the algorithm of the SCRAM mechanism.
   *
   * @param key the key
   * @param message the message
   * @return The calculated message hmac instance
   * @throws ScramRuntimeException If the algorithm is not provided by current JVM or any included
   *           implementations
   */
  byte @NotNull [] hmac(byte @NotNull [] key, byte @NotNull [] message) {
    try {
      return CryptoUtil.hmac(new SecretKeySpec(key, hmacAlgorithmName),
          Mac.getInstance(hmacAlgorithmName), message);
    } catch (NoSuchAlgorithmException e) {
      throw new ScramRuntimeException(
          "HMAC algorithm " + hmacAlgorithmName + " not present in current JVM", e);
    }
  }

  /**
   * Compute the salted password.
   *
   * @param stringPreparation Type of preparation to perform in the string
   * @param password Password used
   * @param salt Salt used
   * @param iterationCount Number of iterations
   * @return The salted password
   * @throws ScramRuntimeException If the algorithm is not provided by current JVM or any included
   *           implementations
   */
  byte @NotNull [] saltedPassword(@NotNull StringPreparation stringPreparation,
      char @NotNull [] password, byte @NotNull [] salt, int iterationCount) {
    final char[] normalizedPassword = stringPreparation.normalize(password);
    try {
      return CryptoUtil.hi(
          SecretKeyFactory.getInstance(keyFactoryAlgorithmName),
          keyLength,
          normalizedPassword,
          salt,
          iterationCount);
    } catch (NoSuchAlgorithmException ex) {
      throw new ScramRuntimeException(
          "Unsupported " + keyFactoryAlgorithmName + " for " + mechanismName, ex);
    }
  }

  /**
   * Gets a SCRAM mechanism given its standard IANA name, supported by the Java security provider.
   *
   * @apiNote This will get only the mechanims supported by the Java security provider, if the
   *          configured security provider lacks the algorithm this method will return {@code null}.
   *
   * @param name The standard IANA full name of the mechanism.
   * @return An instance that contains the ScramMechanism if it was found, or null otherwise.
   */
  public static @Nullable ScramMechanism byName(@NotNull String name) {
    return BY_NAME_MAPPING.get(checkNotNull(name, "name"));
  }

  /**
   * List all the supported SCRAM mechanisms by this client implementation.
   *
   * @return A unmodifiable list of the IANA-registered, SCRAM supported mechanisms
   */
  @Unmodifiable
  public static @NotNull List<@NotNull String> supportedMechanisms() {
    return Collections.unmodifiableList(SUPPORTED_MECHANISMS);
  }

  private static boolean isAlgorithmSupported(@NotNull ScramMechanism mechanism) {
    try {
      MessageDigest.getInstance(mechanism.hashAlgorithmName);
      Mac.getInstance(mechanism.hmacAlgorithmName);
      SecretKeyFactory.getInstance(mechanism.keyFactoryAlgorithmName);
      return true;
    } catch (NoSuchAlgorithmException e) {
      return false;
    }
  }

}
