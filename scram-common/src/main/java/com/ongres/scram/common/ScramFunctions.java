/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.SecureRandom;

import com.ongres.scram.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * Utility functions (mostly crypto) for SCRAM.
 */
public final class ScramFunctions {

  private static final byte @NotNull [] CLIENT_KEY_HMAC_MESSAGE = "Client Key".getBytes(UTF_8);
  private static final byte @NotNull [] SERVER_KEY_HMAC_MESSAGE = "Server Key".getBytes(UTF_8);

  private ScramFunctions() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Compute the salted password, based on the given SCRAM mechanism, the String preparation
   * algorithm, the provided salt and the number of iterations.
   *
   * <pre>{@code
   *      SaltedPassword  := Hi(Normalize(password), salt, i)
   *  }</pre>
   *
   * @param scramMechanism The SCRAM mechanism
   * @param stringPreparation The String preparation
   * @param password The non-salted password
   * @param salt The bytes representing the salt
   * @param iterationCount The number of iterations
   * @return The salted password
   */
  public static byte @NotNull [] saltedPassword(@NotNull ScramMechanism scramMechanism,
      @NotNull StringPreparation stringPreparation, char @NotNull [] password,
      byte @NotNull [] salt, int iterationCount) {
    return scramMechanism.saltedPassword(stringPreparation, password, salt, iterationCount);
  }

  /**
   * Computes the HMAC of the message and key, using the given SCRAM mechanism.
   *
   * <pre>{@code
   *     HMAC(key, str)
   * }</pre>
   *
   * @param scramMechanism The SCRAM mechanism
   * @param message The message to compute the HMAC
   * @param key The key used to initialize the MAC
   * @return The computed HMAC
   */
  public static byte @NotNull [] hmac(@NotNull ScramMechanism scramMechanism, byte @NotNull [] key,
      byte @NotNull [] message) {
    return scramMechanism.hmac(key, message);
  }

  /**
   * Generates a client key, from the salted password.
   *
   * <pre>{@code
   *      ClientKey := HMAC(SaltedPassword, "Client Key")
   *  }</pre>
   *
   * @param scramMechanism The SCRAM mechanism
   * @param saltedPassword The salted password
   * @return The client key
   */
  public static byte[] clientKey(@NotNull ScramMechanism scramMechanism,
      byte @NotNull [] saltedPassword) {
    return hmac(scramMechanism, saltedPassword, CLIENT_KEY_HMAC_MESSAGE);
  }

  /**
   * Generates a server key, from the salted password.
   *
   * <pre>{@code
   *      ServerKey := HMAC(SaltedPassword, "Server Key")
   * }</pre>
   *
   * @param scramMechanism The SCRAM mechanism
   * @param saltedPassword The salted password
   * @return The server key
   */
  public static byte[] serverKey(@NotNull ScramMechanism scramMechanism,
      byte @NotNull [] saltedPassword) {
    return hmac(scramMechanism, saltedPassword, SERVER_KEY_HMAC_MESSAGE);
  }

  /**
   * Computes the hash function of a given value, based on the SCRAM mechanism hash function.
   *
   * <pre>{@code
   *     H(str)
   * }</pre>
   *
   * @param scramMechanism The SCRAM mechanism
   * @param message The message to hash
   * @return The hashed value
   */
  public static byte[] hash(@NotNull ScramMechanism scramMechanism, byte @NotNull [] message) {
    return scramMechanism.digest(message);
  }

  /**
   * Generates a stored key, from the salted password.
   *
   * <pre>{@code
   *      StoredKey := H(ClientKey)
   * }</pre>
   *
   * @param scramMechanism The SCRAM mechanism
   * @param clientKey The client key
   * @return The stored key
   */
  public static byte[] storedKey(@NotNull ScramMechanism scramMechanism,
      byte @NotNull [] clientKey) {
    return hash(scramMechanism, clientKey);
  }

  /**
   * Computes the SCRAM client signature.
   *
   * <pre>{@code
   *      ClientSignature := HMAC(StoredKey, AuthMessage)
   * }</pre>
   *
   * @param scramMechanism The SCRAM mechanism
   * @param storedKey The stored key
   * @param authMessage The auth message
   * @return The client signature
   */
  public static byte @NotNull [] clientSignature(@NotNull ScramMechanism scramMechanism,
      byte @NotNull [] storedKey, @NotNull String authMessage) {
    return hmac(scramMechanism, storedKey, authMessage.getBytes(UTF_8));
  }

  /**
   * Computes the SCRAM client proof to be sent to the server on the client-final-message.
   *
   * <pre>{@code
   *      ClientProof := ClientKey XOR ClientSignature
   * }</pre>
   *
   * @param clientKey The client key
   * @param clientSignature The client signature
   * @return The client proof
   */
  public static byte[] clientProof(byte @NotNull [] clientKey, byte @NotNull [] clientSignature) {
    return CryptoUtil.xor(clientKey, clientSignature);
  }

  /**
   * Compute the SCRAM server signature.
   *
   * <pre>{@code
   *      ServerSignature := HMAC(ServerKey, AuthMessage)
   * }</pre>
   *
   * @param scramMechanism The SCRAM mechanism
   * @param serverKey The server key
   * @param authMessage The auth message
   * @return The server signature
   */
  public static byte @NotNull [] serverSignature(@NotNull ScramMechanism scramMechanism,
      byte @NotNull [] serverKey, @NotNull String authMessage) {
    return hmac(scramMechanism, serverKey, authMessage.getBytes(UTF_8));
  }

  /**
   * Verifies that a provided client proof is correct.
   *
   * @param scramMechanism The SCRAM mechanism
   * @param clientProof The provided client proof
   * @param storedKey The stored key
   * @param authMessage The auth message
   * @return True if the client proof is correct
   */
  public static boolean verifyClientProof(
      @NotNull ScramMechanism scramMechanism, byte @NotNull [] clientProof,
      byte @NotNull [] storedKey, @NotNull String authMessage) {
    byte[] clientSignature = clientSignature(scramMechanism, storedKey, authMessage);
    byte[] clientKey = CryptoUtil.xor(clientSignature, clientProof);
    byte[] computedStoredKey = hash(scramMechanism, clientKey);
    return MessageDigest.isEqual(storedKey, computedStoredKey);
  }

  /**
   * Verifies that a provided server proof is correct.
   *
   * @param scramMechanism The SCRAM mechanism
   * @param serverKey The server key
   * @param authMessage The auth message
   * @param serverSignature The provided server signature
   * @return True if the server signature is correct
   */
  public static boolean verifyServerSignature(
      ScramMechanism scramMechanism, byte[] serverKey, String authMessage, byte[] serverSignature) {
    byte[] computedServerSignature = serverSignature(scramMechanism, serverKey, authMessage);
    return MessageDigest.isEqual(serverSignature, computedServerSignature);
  }

  /**
   * Generates a random string (called a 'nonce'), composed of ASCII printable characters, except
   * comma (',').
   *
   * @param nonceSize The length of the nonce, in characters/bytes
   * @param random The SecureRandom to use
   * @return The String representing the nonce
   * @throws IllegalArgumentException if the nonceSize is not positive, or if random is null
   */
  public static String nonce(int nonceSize, SecureRandom random) {
    Preconditions.gt0(nonceSize, "nonceSize");
    Preconditions.checkNotNull(random, "random");
    final StringBuilder nonceBuilder = new StringBuilder(nonceSize);
    while (nonceBuilder.length() < nonceSize) {
      int codePoint = random.nextInt(0x7E - 0x21 + 1) + 0x21;
      if (codePoint != ',') {
        nonceBuilder.append((char) codePoint);
      }
    }
    return nonceBuilder.toString();
  }

  /**
   * Generates a random salt that can be used to generate a salted password.
   *
   * @param saltSize The length of the salt, in bytes
   * @param random The SecureRandom to use
   * @return The bye[] representing the salt
   * @throws IllegalArgumentException if the saltSize is not positive, or if random is null
   */
  public static byte @NotNull [] salt(int saltSize, @NotNull SecureRandom random) {
    return CryptoUtil.salt(saltSize, random);
  }

  /**
   * The AuthMessage is computed by concatenating messages from the authentication exchange.
   *
   * <pre>{@code
   *      AuthMessage := client-first-message-bare + "," +
   *                                    server-first-message + "," +
   *                                    client-final-message-without-proof
   * }</pre>
   *
   * @param clientFirstMessage the {@link ClientFirstMessage ClientFirstMessage}
   * @param serverFirstMessage the {@link ServerFirstMessage ServerFirstMessage}
   * @param cbindData the channel binding data, or null
   * @return the AuthMessage
   */
  public static String authMessage(ClientFirstMessage clientFirstMessage,
      ServerFirstMessage serverFirstMessage, byte[] cbindData) {
    StringBuilder sb = clientFirstMessage.clientFirstMessageBare(new StringBuilder(96))
        .append(',').append(serverFirstMessage).append(',');
    ClientFinalMessage.withoutProof(sb, clientFirstMessage.getGs2Header(),
        cbindData, serverFirstMessage.getNonce());
    return sb.toString();
  }

}
