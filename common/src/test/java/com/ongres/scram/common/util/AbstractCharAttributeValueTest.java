/*
 * Copyright 2017, OnGres.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
