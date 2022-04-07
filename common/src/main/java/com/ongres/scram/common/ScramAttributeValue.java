/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.util.AbstractCharAttributeValue;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

/**
 * Parse and write SCRAM Attribute-Value pairs.
 */
public class ScramAttributeValue extends AbstractCharAttributeValue {
    public ScramAttributeValue(ScramAttributes attribute, String value) {
        super(attribute, checkNotNull(value, "value"));
    }

    public static StringBuffer writeTo(StringBuffer sb, ScramAttributes attribute, String value) {
        return new ScramAttributeValue(attribute, value).writeTo(sb);
    }

    /**
     * Parses a potential ScramAttributeValue String.
     * @param value The string that contains the Attribute-Value pair.
     * @return The parsed class
     * @throws ScramParseException If the argument is empty or an invalid Attribute-Value
     */
    public static ScramAttributeValue parse(String value)
    throws ScramParseException {
        if(null == value || value.length() < 3 || value.charAt(1) != '=') {
            throw new ScramParseException("Invalid ScramAttributeValue '" + value + "'");
        }

        return new ScramAttributeValue(ScramAttributes.byChar(value.charAt(0)), value.substring(2));
    }
}
