/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

/**
 * Utility to remove non-printable characters from the US-ASCII String.
 */
public class UsAsciiUtils {

  /**
   * Removes non-printable characters from the US-ASCII String.
   *
   * @param value The original String
   * @return The possibly modified String, without non-printable US-ASCII characters.
   * @throws IllegalArgumentException If the String is null or contains non US-ASCII characters.
   */
  public static String toPrintable(String value) throws IllegalArgumentException {
    checkNotNull(value, "value");

    char[] printable = new char[value.length()];
    int i = 0;
    for (char chr : value.toCharArray()) {
      int c = chr;
      if (c < 0 || c >= 127) {
        throw new IllegalArgumentException(
            "value contains character '" + chr + "' which is non US-ASCII");
      } else if (c >= 32) {
        printable[i++] = chr;
      }
    }

    return i == value.length() ? value : new String(printable, 0, i);
  }
}
