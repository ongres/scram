/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ongres.scram.common.ScramAttributeValue;
import com.ongres.scram.common.ScramAttributes;
import com.ongres.scram.common.gssapi.Gs2AttributeValue;
import com.ongres.scram.common.gssapi.Gs2Attributes;
import org.junit.jupiter.api.Test;

class StringWritableCsvTest {
  private static final String[] ONE_ARG_VALUES =
      new String[] {"c=channel", "i=4096", "a=authzid", "n"};
  private static final String SEVERAL_VALUES_STRING = "n,,n=user,r=fyko+d2lbbFgONRv9qkxdawL";

  @Test
  void writeToNullOrEmpty() {
    assertTrue(StringWritableCsv.writeTo(new StringBuffer()).length() == 0);
    assertTrue(
        StringWritableCsv.writeTo(new StringBuffer(), new CharAttributeValue[] {}).length() == 0);
  }

  @Test
  void writeToOneArg() {
    CharAttributeValue[] pairs = new CharAttributeValue[] {
        new ScramAttributeValue(ScramAttributes.CHANNEL_BINDING, "channel"),
        new ScramAttributeValue(ScramAttributes.ITERATION, "" + 4096),
        new Gs2AttributeValue(Gs2Attributes.AUTHZID, "authzid"),
        new Gs2AttributeValue(Gs2Attributes.CLIENT_NOT, null)
    };

    for (int i = 0; i < pairs.length; i++) {
      assertEquals(ONE_ARG_VALUES[i],
          StringWritableCsv.writeTo(new StringBuffer(), pairs[i]).toString());
    }
  }

  @Test
  void writeToSeveralArgs() {
    assertEquals(
        SEVERAL_VALUES_STRING,
        StringWritableCsv.writeTo(
            new StringBuffer(),
            new Gs2AttributeValue(Gs2Attributes.CLIENT_NOT, null),
            null,
            new ScramAttributeValue(ScramAttributes.USERNAME, "user"),
            new ScramAttributeValue(ScramAttributes.NONCE, "fyko+d2lbbFgONRv9qkxdawL")

        ).toString());
  }

  @Test
  void parseFromEmpty() {
    assertArrayEquals(new String[] {}, StringWritableCsv.parseFrom(""));
  }

  @Test
  void parseFromOneArgWithLimitsOffsets() {
    for (String s : ONE_ARG_VALUES) {
      assertArrayEquals(new String[] {s}, StringWritableCsv.parseFrom(s));
    }

    int[] numberEntries = new int[] {0, 1};
    for (int n : numberEntries) {
      for (String s : ONE_ARG_VALUES) {
        assertArrayEquals(new String[] {s}, StringWritableCsv.parseFrom(s, n));
      }
    }
    for (String s : ONE_ARG_VALUES) {
      assertArrayEquals(new String[] {s, null, null}, StringWritableCsv.parseFrom(s, 3));
    }

    for (int n : numberEntries) {
      for (String s : ONE_ARG_VALUES) {
        assertArrayEquals(new String[] {s}, StringWritableCsv.parseFrom(s, n, 0));
      }
    }
    for (String s : ONE_ARG_VALUES) {
      assertArrayEquals(new String[] {s, null, null}, StringWritableCsv.parseFrom(s, 3, 0));
    }

    for (int n : numberEntries) {
      for (String s : ONE_ARG_VALUES) {
        assertArrayEquals(new String[] {null}, StringWritableCsv.parseFrom(s, n, 1));
      }
    }
  }

  @Test
  void parseFromSeveralArgsWithLimitsOffsets() {
    assertArrayEquals(
        new String[] {"n", "", "n=user", "r=fyko+d2lbbFgONRv9qkxdawL"},
        StringWritableCsv.parseFrom(SEVERAL_VALUES_STRING));

    assertArrayEquals(
        new String[] {"n", ""},
        StringWritableCsv.parseFrom(SEVERAL_VALUES_STRING, 2));

    assertArrayEquals(
        new String[] {"", "n=user"},
        StringWritableCsv.parseFrom(SEVERAL_VALUES_STRING, 2, 1));

    assertArrayEquals(
        new String[] {"r=fyko+d2lbbFgONRv9qkxdawL", null},
        StringWritableCsv.parseFrom(SEVERAL_VALUES_STRING, 2, 3));

    assertArrayEquals(
        new String[] {null, null},
        StringWritableCsv.parseFrom(SEVERAL_VALUES_STRING, 2, 4));

    assertArrayEquals(
        new String[] {"n", "", "n=user", "r=fyko+d2lbbFgONRv9qkxdawL", null},
        StringWritableCsv.parseFrom(SEVERAL_VALUES_STRING, 5));
  }
}
