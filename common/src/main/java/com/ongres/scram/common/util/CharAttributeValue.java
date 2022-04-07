/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

/**
 * Augments a {@link CharAttribute} with a String value and the method(s) to write its data to a StringBuffer.
 */
public interface CharAttributeValue extends CharAttribute, StringWritable {
    /**
     * Returns the value associated with the {@link CharAttribute}
     * @return The String value or null if no value is associated
     */
    String getValue();
}
