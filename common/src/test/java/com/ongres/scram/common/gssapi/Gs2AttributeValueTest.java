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


package com.ongres.scram.common.gssapi;


import org.junit.Test;

import static org.junit.Assert.*;


public class Gs2AttributeValueTest {
    @Test
    public void constructorAllowsNullValue() {
        try {
            assertNotNull(new Gs2AttributeValue(Gs2Attributes.CHANNEL_BINDING_REQUIRED, null));
        } catch(IllegalArgumentException e) {
            fail("A null value is valid and cannot throw an IllegalArgumentException");
        }
    }

    @Test
    public void parseNullValue() {
        assertNull(Gs2AttributeValue.parse(null));
    }

    @Test
    public void parseIllegalValuesStructure() {
        String[] values = new String[] { "", "as", "asdfjkl",  Gs2Attributes.CHANNEL_BINDING_REQUIRED.getChar() + "=" };
        int n = 0;
        for(String value : values) {
            try {
                assertNotNull(Gs2AttributeValue.parse(value));
            } catch(IllegalArgumentException e) {
                n++;
            }
        }

        assertEquals("Not every illegal value thrown IllegalArgumentException", values.length, n);
    }

    @Test
    public void parseIllegalValuesInvalidGS2Attibute() {
        String[] values = new String[] { "z=asdfasdf", "i=value" };

        int n = 0;
        for(String value : values) {
            try {
                assertNotNull(Gs2AttributeValue.parse(value));
            } catch(IllegalArgumentException e) {
                n++;
            }
        }

        assertEquals("Not every illegal value thrown IllegalArgumentException", values.length, n);
    }

    @Test
    public void parseLegalValues() {
        String[] values = new String[] { "n", "y", "p=value", "a=authzid" };
        for(String value : values) {
            assertNotNull(Gs2AttributeValue.parse(value));
        }
    }
}
