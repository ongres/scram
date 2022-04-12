/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.gssapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class Gs2AttributeValueTest {
  @Test
  void constructorAllowsNullValue() {
    try {
      assertNotNull(new Gs2AttributeValue(Gs2Attributes.CHANNEL_BINDING_REQUIRED, null));
    } catch (IllegalArgumentException e) {
      fail("A null value is valid and cannot throw an IllegalArgumentException");
    }
  }

  @Test
  void parseNullValue() {
    assertNull(Gs2AttributeValue.parse(null));
  }

  @Test
  void parseIllegalValuesStructure() {
    String[] values =
        new String[] {"", "as", "asdfjkl", Gs2Attributes.CHANNEL_BINDING_REQUIRED.getChar() + "="};
    int n = 0;
    for (String value : values) {
      try {
        assertNotNull(Gs2AttributeValue.parse(value));
      } catch (IllegalArgumentException e) {
        n++;
      }
    }

    assertEquals(values.length, n, "Not every illegal value thrown IllegalArgumentException");
  }

  @Test
  void parseIllegalValuesInvalidGS2Attibute() {
    String[] values = new String[] {"z=asdfasdf", "i=value"};

    int n = 0;
    for (String value : values) {
      try {
        assertNotNull(Gs2AttributeValue.parse(value));
      } catch (IllegalArgumentException e) {
        n++;
      }
    }

    assertEquals(values.length, n, "Not every illegal value thrown IllegalArgumentException");
  }

  @Test
  void parseLegalValues() {
    String[] values = new String[] {"n", "y", "p=value", "a=authzid"};
    for (String value : values) {
      assertNotNull(Gs2AttributeValue.parse(value));
    }
  }
}
