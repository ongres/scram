/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractCharAttributeValueTest {
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
    public void constructorNullAttribute() {
        try {
            assertNotNull(new AbstractCharAttributeValue((CharAttribute) null, "value"));
        } catch(IllegalArgumentException e) {
            return;
        }

        fail("IllegalArgumentException must be thrown if the CharAttribute is null");
    }

    @Test
    public void constructorEmptyValue() {
        try {
            assertNotNull(new AbstractCharAttributeValue(new MockCharAttribute('c'), ""));
        } catch(IllegalArgumentException e) {
            return;
        }

        fail("IllegalArgumentException must be thrown if the value is empty");
    }

    @Test
    public void writeToNonNullValues() {
        String[] legalValues = new String[] { "a", "----", "value" };
        char c = 'c';
        for(String s : legalValues) {
            assertEquals(
                    "" + c + '=' + s,
                    new AbstractCharAttributeValue(new MockCharAttribute(c), s).toString()
            );
        }
    }

    @Test
    public void writeToNullValue() {
        char c = 'd';
        assertEquals(
                "" + c,
                new AbstractCharAttributeValue(new MockCharAttribute(c), null).toString()
        );
    }

    @Test
    public void writeToEscapedValues() {
        char c = 'a';
        MockCharAttribute mockCharAttribute = new MockCharAttribute(c);
        String[] values = new String[]   {  "a=b",      "c,a",      ",",    "=,",       "=,,="          };
        for(int i = 0; i < values.length; i++) {
            assertEquals(
                    "" + c + '=' + values[i],
                    new AbstractCharAttributeValue(mockCharAttribute, values[i]).toString()
            );
        }
    }
}
