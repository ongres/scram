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

import static com.ongres.scram.common.SCRAMAttributes.CLIENT_PROOF;
import static com.ongres.scram.common.SCRAMAttributes.USERNAME;
import static org.junit.Assert.*;


public class SCRAMAttributeValueTest {
    @Test
    public void constructorDoesNotAllowNullValue() {
        try {
            assertNotNull(new SCRAMAttributeValue(SCRAMAttributes.USERNAME, null));
        } catch(IllegalArgumentException e) {
            return;
        }

        fail("A null value must throw an IllegalArgumentException");
    }

    @Test
    public void parseNullValue() {
        assertNull(SCRAMAttributeValue.parse(null));
    }

    @Test
    public void parseIllegalValuesStructure() {
        String[] values = new String[] {
                "", "asdf", "asdf=a", CLIENT_PROOF.getChar() + "=", CLIENT_PROOF.getChar() + ",a"
        };
        int n = 0;
        for(String value : values) {
            try {
                assertNotNull(SCRAMAttributeValue.parse(value));
            } catch(IllegalArgumentException e) {
                n++;
            }
        }

        assertEquals("Not every illegal value thrown IllegalArgumentException", values.length, n);
    }

    @Test
    public void parseIllegalValuesInvalidSCRAMAttibute() {
        // SCRAM allows for extensions. If a new attribute is supported and its value has been used below,
        // test will fail and will need to be fixed
        String[] values = new String[] { "z=asdfasdf", "!=value" };

        int n = 0;
        for(String value : values) {
            try {
                assertNotNull(SCRAMAttributeValue.parse(value));
            } catch(IllegalArgumentException e) {
                n++;
            }
        }

        assertEquals("Not every illegal value thrown IllegalArgumentException", values.length, n);
    }

    @Test
    public void parseLegalValues() {
        String[] legalValues = new String[] {
                CLIENT_PROOF.getChar() + "=" + "proof", USERNAME.getChar() + "=" + "username",
                "n=user", "r=fyko+d2lbbFgONRv9qkxdawL",
                "r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j", "s=QSXCR+Q6sek8bf92",
                "i=4096", "c=biws", "r=fyko+d2lbbFgONRv9qkxdawL3rfcNHYJY1ZVvWVs7j",
                "p=v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=", "v=rmF9pqV8S7suAoZWja4dJRkFsKQ="
        };

        for(String value : legalValues) {
            assertNotNull(SCRAMAttributeValue.parse(value));
        }
    }
}
