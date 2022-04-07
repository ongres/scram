/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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
