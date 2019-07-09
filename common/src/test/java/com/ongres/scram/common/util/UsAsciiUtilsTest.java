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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class UsAsciiUtilsTest {
    @Test
    public void toPrintableNull() {
        try {
            UsAsciiUtils.toPrintable(null);
        } catch(IllegalArgumentException ex) {
            return;
        }

        fail("Calling with null value must throw IllegalArgumentException");
    }

    @Test
    public void toPrintableNonASCII() {
        String[] nonASCIIStrings = new String[] { "abcdé", "ñ", "€", "Наташа", (char) 127 + "" };
        int n = 0;
        for(String s : nonASCIIStrings) {
            try {
                UsAsciiUtils.toPrintable(s);
            } catch(IllegalArgumentException ex) {
                n++;
            }
        }

        assertTrue(
                "String(s) with non-ASCII characters not throwing IllegalArgumentException",
                n == nonASCIIStrings.length
        );
    }

    @Test
    public void toPrintableNonPrintable() {
        String[] original = new String[] { " u ",   "a" + (char) 12,    (char) 0 + "ttt" + (char) 31 };
        String[] expected = new String[] { "u",     "a",                "ttt" };

        for(int i = 0; i < original.length; i++) {
            assertEquals("", expected[i], UsAsciiUtils.toPrintable(original[i]));
        }
    }

    @Test
    public void toPrintableAllPrintable() {
        List<String> values = new ArrayList<String>();
        values.addAll(Arrays.asList(
                new String[] { (char) 33 + "", "user", "!", "-,.=?", (char) 126 + "" })
        );
        for(int c = 33; c < 127; c++) {
            values.add("---" + (char) c + "---");
        }

        for(String s : values) {
            assertEquals(
                    "All printable String '" + s + "' not returning the same value",
                    s,
                    UsAsciiUtils.toPrintable(s)
            );
        }
    }
}
