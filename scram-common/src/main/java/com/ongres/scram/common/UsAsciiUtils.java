/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import java.nio.CharBuffer;

/**
 * Utility to remove non-printable characters from the US-ASCII String.
 */
final class UsAsciiUtils {

  private UsAsciiUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Removes non-printable characters from the US-ASCII String.
   *
   * @param value The original String
   * @return The possibly modified String, without non-printable US-ASCII characters.
   * @throws IllegalArgumentException If the String is null or contains non US-ASCII characters.
   */
  static char[] toPrintable(final char[] value) {
    checkNotNull(value, "value");
    CharBuffer charBuffer = CharBuffer.allocate(value.length);
    for (char ch : value) {
      if (ch >= 127) {
        throw new IllegalArgumentException(
            "value contains character '" + ch + "' which is non US-ASCII");
      } else if (ch >= 32) {
        charBuffer.put(ch);
      }
    }
    // Flip the buffer to prepare for reading
    charBuffer.flip();
    char[] charArray = new char[charBuffer.remaining()];
    charBuffer.get(charArray);
    return charArray;
  }

  /**
   * Removes non-printable characters from the US-ASCII String.
   *
   * @param value The original String
   * @return The possibly modified String, without non-printable US-ASCII characters.
   * @throws IllegalArgumentException If the String is null or contains non US-ASCII characters.
   */
  static String toPrintable(final String value) {
    char[] charArray = checkNotNull(value, "value").toCharArray();
    return new String(toPrintable(charArray));
  }
}
