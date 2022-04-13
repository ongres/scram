/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.stringprep;

import static com.ongres.scram.common.util.Preconditions.checkNotEmpty;

import com.ongres.scram.common.util.UsAsciiUtils;
import com.ongres.stringprep.Profile;
import com.ongres.stringprep.Stringprep;

/**
 * StringPreparations enumerations to use in SCRAM.
 */
public enum StringPreparations implements StringPreparation {

  /**
   * Implementation of StringPreparation that performs no preparation. Non US-ASCII characters will
   * produce an exception. Even though the <a
   * href="https://tools.ietf.org/html/rfc5802">[RFC5802]</a> is not very clear about it, this
   * implementation will normalize non-printable US-ASCII characters similarly to what SaslPrep does
   * (i.e., removing them).
   */
  NO_PREPARATION {
    @Override
    protected char[] doNormalize(char[] value) throws IllegalArgumentException {
      return UsAsciiUtils.toPrintable(String.valueOf(value)).toCharArray();
    }
  },
  /**
   * Implementation of StringPreparation that performs preparation. Non US-ASCII characters will
   * produce an exception. Even though the <a
   * href="https://tools.ietf.org/html/rfc5802">[RFC5802]</a> is not very clear about it, this
   * implementation will normalize as SaslPrep does.
   */
  SASL_PREPARATION {
    @Override
    protected char[] doNormalize(char[] value) throws IllegalArgumentException {
      return saslPrep.prepareStored(String.valueOf(value)).toCharArray();
    }
  };

  private static final Profile saslPrep = Stringprep.getProvider("SASLprep");

  protected abstract char[] doNormalize(char[] value) throws IllegalArgumentException;

  @Override
  public char[] normalize(char[] value) throws IllegalArgumentException {
    checkNotEmpty(value, "value");

    char[] normalized = doNormalize(value);

    if (null == normalized || normalized.length == 0) {
      throw new IllegalArgumentException("null or empty value after normalization");
    }

    return normalized;
  }
}
