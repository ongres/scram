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


import com.ongres.scram.common.util.AbstractCharAttributeValue;

/**
 * Parse and write GS2 Attribute-Value pairs.
 */
public class GS2AttributeValue extends AbstractCharAttributeValue {
    public GS2AttributeValue(GS2Attributes attribute, String value) {
        super(attribute, value);
    }

    public static StringBuffer writeTo(StringBuffer sb, GS2Attributes attribute, String value) {
        return new GS2AttributeValue(attribute, value).writeTo(sb);
    }

    /**
     * Parses a potential GS2AttributeValue String.
     * @param value The string that contains the Attribute-Value pair (where value is optional).
     * @return The parsed class, or null if the String was null.
     * @throws IllegalArgumentException If the String is an invalid GS2AttributeValue
     */
    public static GS2AttributeValue parse(String value) throws IllegalArgumentException {
        if(null == value) {
            return null;
        }

        if(value.length() < 1 || value.length() == 2 || (value.length() > 2 && value.charAt(1) != '=')) {
            throw new IllegalArgumentException("Invalid GS2AttributeValue");
        }

        return new GS2AttributeValue(
                GS2Attributes.byChar(value.charAt(0)),
                value.length() > 2 ? value.substring(2) : null
        );
    }
}
