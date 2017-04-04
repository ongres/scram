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


package com.ongres.scram.common.util;


/**
 * Helper class to generate Comma Separated Values of {@link CharAttributeValue}s
 */
public class CharAttributeValueCSV {
    /**
     * Write a sequence of {@link CharAttributeValueCSV}s to a StringBuffer.
     * Null {@link CharAttributeValueCSV}s are not printed, but separator is still used.
     * Separator is a comma (',')
     * @param sb The sb to write to
     * @param values Zero or more attribute-value pairs to write
     * @return The same sb, with data filled in (if any)
     * @throws IllegalArgumentException If sb is null
     */
    public static StringBuffer writeTo(StringBuffer sb, CharAttributeValue... values) throws IllegalArgumentException {
        return sb;
    }

    /**
     * Parse a String with a {@link CharAttributeValueCSV} into its composing {@link CharAttributeValue}s
     * represented as Strings. No validation is performed on the individual attribute-values returned.
     * @param value The String with the set of attribute-values
     * @param limit Maximum number of entries to return. 0 means unlimited
     * @param offset How many entries to skip before start returning
     * @return An array of Strings which represent the individual attribute-values
     * @throws IllegalArgumentException If value is null or either limit or offset are negative
     */
    public static String[] parseFrom(String value, int limit, int offset) throws IllegalArgumentException {
        return null;
    }

    /**
     * Parse a String with a {@link CharAttributeValueCSV} into its composing {@link CharAttributeValue}s
     * represented as Strings. No validation is performed on the individual attribute-values returned.
     * Elements are returned starting from the first available attribute-value.
     * @param value The String with the set of attribute-values
     * @param limit Maximum number of entries to return. 0 means unlimited
     * @return An array of Strings which represent the individual attribute-values
     * @throws IllegalArgumentException If value is null or limit is negative
     */
    public static String[] parseFrom(String value, int limit)  throws IllegalArgumentException {
        return parseFrom(value, limit, 0);
    }

    /**
     * Parse a String with a {@link CharAttributeValueCSV} into its composing {@link CharAttributeValue}s
     * represented as Strings. No validation is performed on the individual attribute-values returned.
     * All the available attribute-values will be returned.
     * @param value The String with the set of attribute-values
     * @return An array of Strings which represent the individual attribute-values
     * @throws IllegalArgumentException If value is null
     */
    public static String[] parseFrom(String value) throws IllegalArgumentException{
        return parseFrom(value, 0, 0);
    }
}
