/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

/**
 * Simple methods similar to Precondition class. Avoid importing full library.
 */
public class Preconditions {
    /**
     * Checks that the argument is not null.
     * @param value The value to be checked
     * @param valueName The name of the value that is checked in the method
     * @param <T> The type of the value
     * @return The same value passed as argument
     * @throws IllegalArgumentException If value is null
     */
    public static <T> T checkNotNull(T value, String valueName) throws IllegalArgumentException {
        if(null == value) {
            throw new IllegalArgumentException("Null value for '" + valueName + "'");
        }

        return value;
    }

    /**
     * Checks that the String is not null and not empty
     * @param value The String to check
     * @param valueName The name of the value that is checked in the method
     * @return The same String passed as argument
     * @throws IllegalArgumentException If value is null or empty
     */
    public static String checkNotEmpty(String value, String valueName) throws IllegalArgumentException {
        if(checkNotNull(value, valueName).isEmpty()) {
            throw new IllegalArgumentException("Empty string '" + valueName + "'");
        }

        return value;
    }

    /**
     * Checks that the argument is valid, based in a check boolean condition.
     * @param check The boolean check
     * @param valueName The name of the value that is checked in the method
     * @throws IllegalArgumentException
     */
    public static void checkArgument(boolean check, String valueName) throws IllegalArgumentException {
        if(! check) {
            throw new IllegalArgumentException("Argument '" + valueName + "' is not valid");
        }
    }

    /**
     * Checks that the integer argument is positive.
     * @param value The value to be checked
     * @param valueName The name of the value that is checked in the method
     * @return The same value passed as argument
     * @throws IllegalArgumentException If value is null
     */
    public static int gt0(int value, String valueName) throws IllegalArgumentException {
        if(value <= 0) {
            throw new IllegalArgumentException("'" + valueName + "' must be positive");
        }

        return value;
    }
}
