/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

/**
 * Construct and write generic CharAttribute-Value pairs.
 *
 * Concrete sub-classes should also provide a static parse(String) creation method.
 */
public class AbstractCharAttributeValue extends AbstractStringWritable implements CharAttributeValue {
    private final CharAttribute charAttribute;
    private final String value;

    public AbstractCharAttributeValue(CharAttribute charAttribute, String value) throws IllegalArgumentException {
        this.charAttribute = checkNotNull(charAttribute, "attribute");
        if(null != value && value.isEmpty()) {
            throw new IllegalArgumentException("Value should be either null or non-empty");
        }
        this.value = value;
    }

    @Override
    public char getChar() {
        return charAttribute.getChar();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public StringBuffer writeTo(StringBuffer sb) {
        sb.append(charAttribute.getChar());

        if(null != value) {
            sb.append('=').append(value);
        }

        return sb;
    }
}
