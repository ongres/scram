/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

/**
 * Basic implementation of the StringWritable interface, that overrides the toString() method.
 */
public abstract class AbstractStringWritable implements StringWritable {

  @Override
  public String toString() {
    return writeTo(new StringBuffer()).toString();
  }

}
