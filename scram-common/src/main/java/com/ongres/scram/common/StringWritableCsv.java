/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import java.util.Arrays;

import com.ongres.scram.common.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to generate Comma Separated Values of StringWritables.
 */
final class StringWritableCsv {

  private StringWritableCsv() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Write a sequence of StringWritableCsv to a StringBuffer. Null StringWritables are not printed,
   * but separator is still used. Separator is a comma (',')
   *
   * @param sb The sb to write to
   * @param values Zero or more attribute-value pairs to write
   * @return The same sb, with data filled in (if any)
   * @throws IllegalArgumentException If sb is null
   */
  static @NotNull StringBuilder writeTo(@NotNull StringBuilder sb,
      @Nullable StringWritable... values) {
    checkNotNull(sb, "sb");
    if (null == values || values.length == 0) {
      return sb;
    }

    if (null != values[0]) {
      Preconditions.castNonNull(values[0]).writeTo(sb);
    }
    int i = 1;
    while (i < values.length) {
      sb.append(',');
      if (null != values[i]) {
        Preconditions.castNonNull(values[i]).writeTo(sb);
      }
      i++;
    }

    return sb;
  }

  /**
   * Parse a String with a StringWritableCsv into its composing Strings represented as Strings. No
   * validation is performed on the individual attribute-values returned.
   *
   * @param value The String with the set of attribute-values
   * @param n Number of entries to return (entries will be null of there were not enough). 0 means
   *          unlimited
   * @param offset How many entries to skip before start returning
   * @return An array of Strings which represent the individual attribute-values
   * @throws IllegalArgumentException If value is null or either n or offset are negative
   */
  static @NotNull String @NotNull [] parseFrom(@NotNull String value, int n, int offset) {
    checkNotNull(value, "value");
    if (n < 0 || offset < 0) {
      throw new IllegalArgumentException("Limit and offset have to be >= 0");
    }

    if (value.isEmpty()) {
      return new String[0];
    }

    String[] split = value.split(",", -1);
    if (split.length < offset) {
      throw new IllegalArgumentException("Not enough items for the given offset");
    }

    return Arrays.copyOfRange(
        split,
        offset,
        (n == 0 ? split.length : n) + offset);
  }

  /**
   * Parse a String with a StringWritableCsv into its composing Strings represented as Strings. No
   * validation is performed on the individual attribute-values returned. Elements are returned
   * starting from the first available attribute-value.
   *
   * @param value The String with the set of attribute-values
   * @param n Number of entries to return (entries will be null of there were not enough). 0 means
   *          unlimited
   * @return An array of Strings which represent the individual attribute-values
   * @throws IllegalArgumentException If value is null or n is negative
   */
  static @NotNull String @NotNull [] parseFrom(@NotNull String value, int n) {
    return parseFrom(value, n, 0);
  }

  /**
   * Parse a String with a StringWritableCsv into its composing Strings represented as Strings. No
   * validation is performed on the individual attribute-values returned. All the available
   * attribute-values will be returned.
   *
   * @param value The String with the set of attribute-values
   * @return An array of Strings which represent the individual attribute-values
   * @throws IllegalArgumentException If value is null
   */
  static @NotNull String @NotNull [] parseFrom(@NotNull String value) {
    return parseFrom(value, 0, 0);
  }
}
