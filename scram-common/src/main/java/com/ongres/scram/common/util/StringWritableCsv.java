/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import java.util.Arrays;

/**
 * Helper class to generate Comma Separated Values of {@link StringWritable}s.
 */
public class StringWritableCsv {

  private static void writeStringWritableToStringBuffer(StringWritable value, StringBuffer sb) {
    if (null != value) {
      value.writeTo(sb);
    }
  }

  /**
   * Write a sequence of {@link StringWritableCsv}s to a StringBuffer. Null {@link StringWritable}s
   * are not printed, but separator is still used. Separator is a comma (',')
   *
   * @param sb The sb to write to
   * @param values Zero or more attribute-value pairs to write
   * @return The same sb, with data filled in (if any)
   * @throws IllegalArgumentException If sb is null
   */
  public static StringBuffer writeTo(StringBuffer sb, StringWritable... values)
      throws IllegalArgumentException {
    checkNotNull(sb, "sb");
    if (null == values || values.length == 0) {
      return sb;
    }

    writeStringWritableToStringBuffer(values[0], sb);
    int i = 1;
    while (i < values.length) {
      sb.append(',');
      writeStringWritableToStringBuffer(values[i], sb);
      i++;
    }

    return sb;
  }

  /**
   * Parse a String with a {@link StringWritableCsv} into its composing Strings represented as
   * Strings. No validation is performed on the individual attribute-values returned.
   *
   * @param value The String with the set of attribute-values
   * @param n Number of entries to return (entries will be null of there were not enough). 0 means
   *        unlimited
   * @param offset How many entries to skip before start returning
   * @return An array of Strings which represent the individual attribute-values
   * @throws IllegalArgumentException If value is null or either n or offset are negative
   */
  public static String[] parseFrom(String value, int n, int offset)
      throws IllegalArgumentException {
    checkNotNull(value, "value");
    if (n < 0 || offset < 0) {
      throw new IllegalArgumentException("Limit and offset have to be >= 0");
    }

    if (value.isEmpty()) {
      return new String[0];
    }

    String[] split = value.split(",");
    if (split.length < offset) {
      throw new IllegalArgumentException("Not enough items for the given offset");
    }

    return Arrays.copyOfRange(
        split,
        offset,
        (n == 0 ? split.length : n) + offset);
  }

  /**
   * Parse a String with a {@link StringWritableCsv} into its composing Strings represented as
   * Strings. No validation is performed on the individual attribute-values returned. Elements are
   * returned starting from the first available attribute-value.
   *
   * @param value The String with the set of attribute-values
   * @param n Number of entries to return (entries will be null of there were not enough). 0 means
   *        unlimited
   * @return An array of Strings which represent the individual attribute-values
   * @throws IllegalArgumentException If value is null or n is negative
   */
  public static String[] parseFrom(String value, int n) throws IllegalArgumentException {
    return parseFrom(value, n, 0);
  }

  /**
   * Parse a String with a {@link StringWritableCsv} into its composing Strings represented as
   * Strings. No validation is performed on the individual attribute-values returned. All the
   * available attribute-values will be returned.
   *
   * @param value The String with the set of attribute-values
   * @return An array of Strings which represent the individual attribute-values
   * @throws IllegalArgumentException If value is null
   */
  public static String[] parseFrom(String value) throws IllegalArgumentException {
    return parseFrom(value, 0, 0);
  }
}
