/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UsAsciiUtilsTest {

  @ParameterizedTest
  @NullSource
  void toPrintableNull(String value) {
    assertThrows(IllegalArgumentException.class, () -> UsAsciiUtils.toPrintable(value),
        () -> "Calling with null value must throw IllegalArgumentException");
  }

  @ParameterizedTest
  @ValueSource(strings = {"abcdé", "ñ", "€", "Наташа", (char) 127 + ""})
  void toPrintableNonASCII(String value) {
    assertThrows(IllegalArgumentException.class, () -> UsAsciiUtils.toPrintable(value),
        () -> "String(s) with non-ASCII characters not throwing IllegalArgumentException");
  }

  @ParameterizedTest
  @CsvSource(value = {" u , u ", "a" + (char) 12 + ",a", (char) 0 + "ttt" + (char) 31 + ",ttt"},
      ignoreLeadingAndTrailingWhitespace = false)
  void toPrintableNonPrintable(String original, String expected) {
    assertEquals(expected, UsAsciiUtils.toPrintable(original));
  }

  @Test
  void toPrintableAllPrintable() {
    List<String> values = new ArrayList<String>();
    values.addAll(Arrays.asList(
        new String[] {(char) 33 + "", "user", "!", "-,.=?", (char) 126 + ""}));
    for (int c = 33; c < 127; c++) {
      values.add("---" + (char) c + "---");
    }

    for (String s : values) {
      assertEquals(s, UsAsciiUtils.toPrintable(s),
          "All printable String '" + s + "' not returning the same value");
    }
  }
}
