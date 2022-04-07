/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.gssapi;

import com.ongres.scram.common.util.AbstractCharAttributeValue;

/**
 * Parse and write GS2 Attribute-Value pairs.
 */
public class Gs2AttributeValue extends AbstractCharAttributeValue {
    public Gs2AttributeValue(Gs2Attributes attribute, String value) {
        super(attribute, value);
    }

    public static StringBuffer writeTo(StringBuffer sb, Gs2Attributes attribute, String value) {
        return new Gs2AttributeValue(attribute, value).writeTo(sb);
    }

    /**
     * Parses a potential Gs2AttributeValue String.
     * @param value The string that contains the Attribute-Value pair (where value is optional).
     * @return The parsed class, or null if the String was null.
     * @throws IllegalArgumentException If the String is an invalid Gs2AttributeValue
     */
    public static Gs2AttributeValue parse(String value) throws IllegalArgumentException {
        if(null == value) {
            return null;
        }

        if(value.length() < 1 || value.length() == 2 || (value.length() > 2 && value.charAt(1) != '=')) {
            throw new IllegalArgumentException("Invalid Gs2AttributeValue");
        }

        return new Gs2AttributeValue(
                Gs2Attributes.byChar(value.charAt(0)),
                value.length() > 2 ? value.substring(2) : null
        );
    }
}
