/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Class with static methods that provide support for converting to/from salNames.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7: Formal
 *      Syntax</a>
 */
public final class ScramStringFormatting {

  private ScramStringFormatting() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Given a value-safe-char (normalized UTF-8 String), return one where characters ',' and '=' are
   * represented by '=2C' or '=3D', respectively.
   *
   * @param value The value to convert so saslName
   * @return The saslName, with caracter escaped (if any)
   */
  public static String toSaslName(String value) {
    if (null == value || value.isEmpty()) {
      return value;
    }

    int comma = 0;
    int equal = 0;
    char[] originalChars = value.toCharArray();

    // Fast path
    for (char c : originalChars) {
      if (',' == c) {
        comma++;
      } else if ('=' == c) {
        equal++;
      }
    }
    if (comma == 0 && equal == 0) {
      return value;
    }

    // Replace chars
    char[] saslChars = new char[originalChars.length + comma * 2 + equal * 2];
    int i = 0;
    for (char c : originalChars) {
      if (',' == c) {
        saslChars[i++] = '=';
        saslChars[i++] = '2';
        saslChars[i++] = 'C';
      } else if ('=' == c) {
        saslChars[i++] = '=';
        saslChars[i++] = '3';
        saslChars[i++] = 'D';
      } else {
        saslChars[i++] = c;
      }
    }

    return new String(saslChars);
  }

  /**
   * Given a saslName, return a non-escaped String.
   *
   * @param value The saslName
   * @return The saslName, unescaped
   * @throws IllegalArgumentException If a ',' character is present, or a '=' not followed by either
   *         '2C' or '3D'
   */
  public static String fromSaslName(String value) throws IllegalArgumentException {
    if (null == value || value.isEmpty()) {
      return value;
    }

    int equal = 0;
    char[] orig = value.toCharArray();

    // Fast path
    for (int i = 0; i < orig.length; i++) {
      if (orig[i] == ',') {
        throw new IllegalArgumentException("Invalid ',' character present in saslName");
      }
      if (orig[i] == '=') {
        equal++;
        if (i + 2 > orig.length - 1) {
          throw new IllegalArgumentException("Invalid '=' character present in saslName");
        }
        if (!(orig[i + 1] == '2' && orig[i + 2] == 'C'
            || orig[i + 1] == '3' && orig[i + 2] == 'D')) {
          throw new IllegalArgumentException(
              "Invalid char '=" + orig[i + 1] + orig[i + 2] + "' found in saslName");
        }
      }
    }
    if (equal == 0) {
      return value;
    }

    // Replace characters
    char[] replaced = new char[orig.length - equal * 2];

    for (int r = 0, o = 0; r < replaced.length; r++) {
      if ('=' == orig[o]) {
        if (orig[o + 1] == '2' && orig[o + 2] == 'C') {
          replaced[r] = ',';
        } else if (orig[o + 1] == '3' && orig[o + 2] == 'D') {
          replaced[r] = '=';
        }
        o += 3;
      } else {
        replaced[r] = orig[o];
        o += 1;
      }
    }

    return new String(replaced);
  }

  public static String base64Encode(byte[] value) throws IllegalArgumentException {
    return Base64.getEncoder().encodeToString(checkNotNull(value, "value"));
  }

  public static String base64Encode(String value) throws IllegalArgumentException {
    return base64Encode(checkNotEmpty(value, "value").getBytes(StandardCharsets.UTF_8));
  }

  public static byte[] base64Decode(String value) throws IllegalArgumentException {
    return Base64.getDecoder().decode(checkNotEmpty(value, "value"));
  }
}
