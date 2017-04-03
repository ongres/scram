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


import com.ongres.scram.common.util.AbstractCharAttributeValue;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;


/**
 * Parse and write SCRAM Attribute-Value pairs.
 */
public class SCRAMAttributeValue extends AbstractCharAttributeValue {
    public SCRAMAttributeValue(SCRAMAttributes attribute, String value) {
        super(attribute, checkNotNull(value, "value"));
    }

    public static StringBuffer writeTo(StringBuffer sb, SCRAMAttributes attribute, String value) {
        return new SCRAMAttributeValue(attribute, value).writeTo(sb);
    }

    /**
     * Parses a potential SCRAMAttributeValue String.
     * @param value The string that contains the Attribute-Value pair.
     * @return The parsed class, or null if the String was null.
     * @throws IllegalArgumentException If the String is an invalid SCRAMAttributeValue
     */
    public static SCRAMAttributeValue parse(String value) throws IllegalArgumentException {
        if(null == value) {
            return null;
        }

        if(value.length() < 3 || value.charAt(1) != '=') {
            throw new IllegalArgumentException("Invalid SCRAMAttributeValue '" + value + "'");
        }

        return new SCRAMAttributeValue(SCRAMAttributes.byChar(value.charAt(0)), value.substring(2));
    }
}
