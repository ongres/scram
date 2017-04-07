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


package com.ongres.scram.common;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ScramStringFormattingTest {
    private static final String[] VALUES_NO_CHARS_TO_BE_ESCAPED = new String[] { "asdf", "''--%%21", "   ttt???" };
    private static final String[] VALUES_TO_BE_ESCAPED = new String[] {
            ",",    "=",    "a,b",      "===",          "a=",   ",=,",          "=2C",      "=3D"
    };
    private static final String[] ESCAPED_VALUES = new String[] {
            "=2C",  "=3D",  "a=2Cb",    "=3D=3D=3D",    "a=3D", "=2C=3D=2C",    "=3D2C",    "=3D3D"
    };
    private static final String[] INVALID_SASL_NAMES = new String[] { "=", "as,df", "a=b", "   ttt???=2D" };

    @Test
    public void toSaslNameNoCharactersToBeEscaped() {
        for(String s : VALUES_NO_CHARS_TO_BE_ESCAPED) {
            assertEquals(s, ScramStringFormatting.toSaslName(s));
        }
    }

    @Test
    public void toSaslNameWithCharactersToBeEscaped() {
        for(int i = 0; i < VALUES_TO_BE_ESCAPED.length; i++) {
            assertEquals(ESCAPED_VALUES[i], ScramStringFormatting.toSaslName(VALUES_TO_BE_ESCAPED[i]));
        }
    }

    @Test
    public void fromSaslNameNoCharactersToBeEscaped() {
        for(String s : VALUES_NO_CHARS_TO_BE_ESCAPED) {
            assertEquals(s, ScramStringFormatting.fromSaslName(s));
        }
    }

    @Test
    public void fromSaslNameWithCharactersToBeUnescaped() {
        for(int i = 0; i < ESCAPED_VALUES.length; i++) {
            assertEquals(VALUES_TO_BE_ESCAPED[i], ScramStringFormatting.fromSaslName(ESCAPED_VALUES[i]));
        }
    }

    @Test
    public void fromSaslNameWithInvalidCharacters() {
        int n = 0;
        for(String s : INVALID_SASL_NAMES) {
            try {
                assertEquals(s, ScramStringFormatting.fromSaslName(s));
            } catch (IllegalArgumentException e) {
                n++;
            }
        }

        assertTrue("Not all values produced IllegalArgumentException", n == INVALID_SASL_NAMES.length);
    }
}
