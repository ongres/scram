/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static com.ongres.scram.common.util.Preconditions.checkArgument;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.ongres.scram.common.exception.ScramRuntimeException;

/**
 * Utility static methods for cryptography related tasks.
 */
public final class CryptoUtil {

  private static final int MIN_ASCII_PRINTABLE_RANGE = 0x21;
  private static final int MAX_ASCII_PRINTABLE_RANGE = 0x7e;
  private static final int EXCLUDED_CHAR = ','; // 0x2c

  private CryptoUtil() {
    throw new IllegalStateException("Utility class");
  }

  private static class SecureRandomHolder {
    private static final SecureRandom INSTANCE = new SecureRandom();
  }

  /**
   * Generates a random string (called a 'nonce'), composed of ASCII printable characters, except
   * comma (',').
   *
   * @param size The length of the nonce, in characters/bytes
   * @param random The SecureRandom to use
   * @return The String representing the nonce
   * @throws IllegalArgumentException if the size is not positive
   */
  public static String nonce(int size, SecureRandom random) {
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be positive");
    }

    char[] chars = new char[size];
    int r;
    for (int i = 0; i < size;) {
      r = random.nextInt(MAX_ASCII_PRINTABLE_RANGE - MIN_ASCII_PRINTABLE_RANGE + 1)
          + MIN_ASCII_PRINTABLE_RANGE;
      if (r != EXCLUDED_CHAR) {
        chars[i++] = (char) r;
      }
    }

    return new String(chars);
  }

  /**
   * Generates a random string (called a 'nonce'), composed of ASCII printable characters, except
   * comma (','). It uses a default SecureRandom instance.
   *
   * @param size The length of the nonce, in characters/bytes
   * @return The String representing the nonce
   */
  public static String nonce(int size) {
    return nonce(size, SecureRandomHolder.INSTANCE);
  }

  /**
   * Compute the "Hi" function for SCRAM.
   *
   * {@code
   * Hi(str, salt, i):
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
   * @param secretKeyFactory The SecretKeyFactory to generate the SecretKey
   * @param keyLength The length of the key (in bits)
   * @param value The char array to compute the Hi function
   * @param salt The salt
   * @param iterations The number of iterations
   * @return The bytes of the computed Hi value
   * @throws ScramRuntimeException if unsupported PBEKeySpec
   */
  public static byte[] hi(
      SecretKeyFactory secretKeyFactory, int keyLength, char[] value, byte[] salt, int iterations) {
    try {
      PBEKeySpec spec = new PBEKeySpec(value, salt, iterations, keyLength);
      SecretKey key = secretKeyFactory.generateSecret(spec);
      return key.getEncoded();
    } catch (InvalidKeySpecException e) {
      throw new ScramRuntimeException("Platform error: unsupported PBEKeySpec");
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
  public static byte[] hmac(SecretKeySpec secretKeySpec, Mac mac, byte[] message) {
    try {
      mac.init(secretKeySpec);
    } catch (InvalidKeyException e) {
      throw new ScramRuntimeException("Platform error: unsupported key for HMAC algorithm");
    }

    return mac.doFinal(message);
  }

  /**
   * Computes a byte-by-byte xor operation.
   *
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
  public static byte[] xor(byte[] value1, byte[] value2) {
    checkNotNull(value1, "value1");
    checkNotNull(value2, "value2");
    checkArgument(value1.length == value2.length, "Both values must have the same length");

    byte[] result = new byte[value1.length];
    for (int i = 0; i < value1.length; i++) {
      result[i] = (byte) (value1[i] ^ value2[i]);
    }

    return result;
  }
}
