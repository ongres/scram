/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

/**
 * StringPreparations enumerations to use in SCRAM.
 */
public enum StringPreparation {

  /**
   * Implementation of StringPreparation that performs no preparation. Non US-ASCII characters will
   * produce an exception.
   *
   * <p>Even though the <a href="https://tools.ietf.org/html/rfc5802">[RFC5802]</a> is not very
   * clear about it, this implementation will normalize non-printable US-ASCII characters similarly
   * to what SASLprep does (i.e., removing them).
   */
  NO_PREPARATION {
    @Override
    char[] doNormalize(char[] value) {
      return UsAsciiUtils.toPrintable(value);
    }
  },
  /**
   * Implementation of StringPreparation that performs {@code SASLprep} preparation. UTF-8 byte
   * sequences that are prohibited by the SASLprep algorithm will produce an exception.
   */
  SASL_PREPARATION {
    @Override
    char[] doNormalize(char[] value) {
      return ScramStringFormatting.SASL_PREP.prepareStored(value);
    }
  },
  /**
   * Implementation of StringPreparation that performs {@code SASLprep} preparation for PostgreSQL.
   *
   * <blockquote>The SCRAM specification dictates that the password is also in UTF-8, and is
   * processed with the {@code SASLprep} algorithm. PostgreSQL, however, does not require UTF-8 to
   * be used for the password. When a user's password is set, it is processed with SASLprep as if it
   * was in UTF-8, regardless of the actual encoding used. However, if it is not a legal UTF-8 byte
   * sequence, or it contains UTF-8 byte sequences that are prohibited by the SASLprep algorithm,
   * the raw password will be used without SASLprep processing, instead of throwing an error. This
   * allows the password to be normalized when it is in UTF-8, but still allows a non-UTF-8 password
   * to be used, and doesn't require the system to know which encoding the password is
   * in.</blockquote>
   *
   * @see <a href="https://www.postgresql.org/docs/current/sasl-authentication.html">PostgreSQL
   *      SCRAM-SHA-256 Authentication</a>
   */
  POSTGRESQL_PREPARATION {
    @Override
    char[] doNormalize(char[] value) {
      try {
        return ScramStringFormatting.SASL_PREP.prepareStored(value);
      } catch (IllegalArgumentException ex) {
        // the raw password will be used without SASLprep processing
        return value;
      }
    }
  };

  abstract char[] doNormalize(char[] value);

  /**
   * Normalize acording the selected preparation.
   *
   * @param value array of chars to normalize
   * @return the normalized array of chars
   * @throws IllegalArgumentException if the string is null or empty
   */
  public char[] normalize(char[] value) {
    if (null == value || value.length == 0) {
      throw new IllegalArgumentException("Empty string for value");
    }

    char[] normalized = doNormalize(value);

    if (null == normalized || normalized.length == 0) {
      throw new IllegalArgumentException("null or empty value after normalization");
    }

    return normalized;
  }
}
