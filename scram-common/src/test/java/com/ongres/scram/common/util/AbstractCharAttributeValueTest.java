/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class AbstractCharAttributeValueTest {
  private class MockCharAttribute implements CharAttribute {
    private final char c;

    public MockCharAttribute(char c) {
      this.c = c;
    }

    @Override
    public char getChar() {
      return c;
    }
  }

  @Test
  void constructorNullAttribute() {
    try {
      assertNotNull(new AbstractCharAttributeValue((CharAttribute) null, "value"));
    } catch (IllegalArgumentException e) {
      return;
    }

    fail("IllegalArgumentException must be thrown if the CharAttribute is null");
  }

  @Test
  void constructorEmptyValue() {
    try {
      assertNotNull(new AbstractCharAttributeValue(new MockCharAttribute('c'), ""));
    } catch (IllegalArgumentException e) {
      return;
    }

    fail("IllegalArgumentException must be thrown if the value is empty");
  }

  @Test
  void writeToNonNullValues() {
    String[] legalValues = new String[] {"a", "----", "value"};
    char c = 'c';
    for (String s : legalValues) {
      assertEquals(
          "" + c + '=' + s,
          new AbstractCharAttributeValue(new MockCharAttribute(c), s).toString());
    }
  }

  @Test
  void writeToNullValue() {
    char c = 'd';
    assertEquals(
        "" + c,
        new AbstractCharAttributeValue(new MockCharAttribute(c), null).toString());
  }

  @Test
  void writeToEscapedValues() {
    char c = 'a';
    MockCharAttribute mockCharAttribute = new MockCharAttribute(c);
    String[] values = new String[] {"a=b", "c,a", ",", "=,", "=,,="};
    for (int i = 0; i < values.length; i++) {
      assertEquals(
          "" + c + '=' + values[i],
          new AbstractCharAttributeValue(mockCharAttribute, values[i]).toString());
    }
  }
}
