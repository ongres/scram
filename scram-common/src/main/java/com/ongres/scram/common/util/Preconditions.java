/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic utility methods used to validate data.
 *
 * @apiNote This is not part of the public API of the SCRAM library, it's provided as a helper
 *          utility and could be renamed or removed at any time.
 */
public final class Preconditions {

  private Preconditions() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Checks that the argument is not null.
   *
   * @param value The value to be checked
   * @param valueName The name of the value that is checked in the method
   * @param <T> The type of the value
   * @return The same value passed as argument
   * @throws IllegalArgumentException If value is null.
   */
  public static <T> @NotNull T checkNotNull(@Nullable T value, @NotNull String valueName) {
    if (null == value) {
      throw new IllegalArgumentException("Null value for '" + valueName + "'");
    }
    return value;
  }

  @SuppressWarnings("null")
  public static <T> @NotNull T castNonNull(@Nullable T ref) {
    assert ref != null : "Misuse of castNonNull: called with a null argument";
    return (@NotNull T) ref;
  }

  /**
   * Checks that the String is not null and not empty.
   *
   * @param value The String to check
   * @param valueName The name of the value that is checked in the method
   * @return The same String passed as argument
   * @throws IllegalArgumentException If value is null or empty
   */
  public static @NotNull String checkNotEmpty(@NotNull String value, @NotNull String valueName) {
    if (checkNotNull(value, valueName).isEmpty()) {
      throw new IllegalArgumentException("The value for '" + valueName + "' must not be empty");
    }
    return value;
  }

  /**
   * Checks that the char[] is not null and not empty.
   *
   * @param value The String to check
   * @param valueName The name of the value that is checked in the method
   * @return The same String passed as argument
   * @throws IllegalArgumentException If value is null or empty
   */
  public static char @NotNull [] checkNotEmpty(char @NotNull [] value, @NotNull String valueName) {
    if (checkNotNull(value, valueName).length == 0) {
      throw new IllegalArgumentException("The value for '" + valueName + "' must not be empty");
    }
    return value;
  }

  /**
   * Checks that the argument is valid, based in a check boolean condition.
   *
   * @param check The boolean check
   * @param valueName The name of the value that is checked in the method
   * @throws IllegalArgumentException if check is not valid
   */
  public static void checkArgument(boolean check, @NotNull String valueName) {
    if (!check) {
      throw new IllegalArgumentException("Argument '" + valueName + "' is not valid");
    }
  }

  /**
   * Checks that the argument is valid, based in a check boolean condition.
   *
   * @param check The boolean check
   * @param valueName The name of the value that is checked in the method
   * @param errMsg Detail of the error message
   * @throws IllegalArgumentException if check is not valid
   */
  public static void checkArgument(boolean check, @NotNull String valueName,
      @NotNull String errMsg) {
    if (!check) {
      throw new IllegalArgumentException("Argument '" + valueName + "' is not valid, " + errMsg);
    }
  }

  /**
   * Checks that the integer argument is positive.
   *
   * @param value The value to be checked
   * @param valueName The name of the value that is checked in the method
   * @return The same value passed as argument
   * @throws IllegalArgumentException If value is equal or less than 0
   */
  public static int gt0(int value, @NotNull String valueName) {
    if (value <= 0) {
      throw new IllegalArgumentException("'" + valueName + "' must be positive, was: " + value);
    }
    return value;
  }

  /**
   * Returns {@code true} if the given string is null or is the empty string.
   *
   * @param string a String reference to check
   * @return {@code true} if the string is null or the string is empty
   */
  public static boolean isNullOrEmpty(@Nullable String string) {
    return string == null || string.isEmpty();
  }

}
