/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScramStringFormattingTest {
  private static final String[] VALUES_NO_CHARS_TO_BE_ESCAPED =
      new String[] {"asdf", "''--%%21", "   ttt???"};
  private static final String[] VALUES_TO_BE_ESCAPED = new String[] {
      ",", "=", "a,b", "===", "a=", ",=,", "=2C", "=3D"
  };
  private static final String[] ESCAPED_VALUES = new String[] {
      "=2C", "=3D", "a=2Cb", "=3D=3D=3D", "a=3D", "=2C=3D=2C", "=3D2C", "=3D3D"
  };
  private static final String[] INVALID_SASL_NAMES =
      new String[] {"=", "as,df", "a=b", "   ttt???=2D"};

  @Test
  void toSaslNameNoCharactersToBeEscaped() {
    for (String s : VALUES_NO_CHARS_TO_BE_ESCAPED) {
      assertEquals(s, ScramStringFormatting.toSaslName(s));
    }
  }

  @Test
  void toSaslNameWithCharactersToBeEscaped() {
    for (int i = 0; i < VALUES_TO_BE_ESCAPED.length; i++) {
      assertEquals(ESCAPED_VALUES[i], ScramStringFormatting.toSaslName(VALUES_TO_BE_ESCAPED[i]));
    }
  }

  @Test
  void fromSaslNameNoCharactersToBeEscaped() {
    for (String s : VALUES_NO_CHARS_TO_BE_ESCAPED) {
      assertEquals(s, ScramStringFormatting.fromSaslName(s));
    }
  }

  @Test
  void fromSaslNameWithCharactersToBeUnescaped() {
    for (int i = 0; i < ESCAPED_VALUES.length; i++) {
      assertEquals(VALUES_TO_BE_ESCAPED[i], ScramStringFormatting.fromSaslName(ESCAPED_VALUES[i]));
    }
  }

  @Test
  void fromSaslNameWithInvalidCharacters() {
    int n = 0;
    for (String s : INVALID_SASL_NAMES) {
      try {
        assertEquals(s, ScramStringFormatting.fromSaslName(s));
      } catch (IllegalArgumentException e) {
        n++;
      }
    }

    assertTrue(n == INVALID_SASL_NAMES.length, "Not all values produced IllegalArgumentException");
  }
}
