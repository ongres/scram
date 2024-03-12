/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import org.jetbrains.annotations.NotNull;

/**
 * Basic implementation of the StringWritable interface, that overrides the toString() method.
 */
abstract class AbstractScramMessage extends StringWritable {

  /**
   * String representation of the SCRAM message.
   */
  @Override
  public final @NotNull String toString() {
    return writeTo(new StringBuilder(48)).toString();
  }

}
