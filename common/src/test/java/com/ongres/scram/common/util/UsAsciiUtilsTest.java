/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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
