/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ongres.scram.common.Gs2AttributeValue;
import com.ongres.scram.common.Gs2Attributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

class Gs2AttributeValueTest {

  @ParameterizedTest
  @NullSource
  @EnumSource(value = Gs2Attributes.class, names = {"CHANNEL_BINDING_REQUIRED", "AUTHZID"})
  void constructorNotAllowsNullValuesForCbAuthzId(Gs2Attributes attrib) {
    assertThrows(IllegalArgumentException.class,
        () -> new Gs2AttributeValue(attrib, null));
  }

  @ParameterizedTest
  @EnumSource(value = Gs2Attributes.class, names = {"CLIENT_NOT", "CLIENT_YES_SERVER_NOT"})
  void constructorAllowsNullValuesForClientnClienty(Gs2Attributes attrib) {
    Gs2AttributeValue gs2 = assertDoesNotThrow(() -> new Gs2AttributeValue(attrib, null));
    assertNotNull(gs2);
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
