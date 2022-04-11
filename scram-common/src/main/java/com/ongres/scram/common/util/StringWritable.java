/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

/**
 * Interface to denote classes which can write to a StringBuffer.
 */
public interface StringWritable {

  /**
   * Write the class information to the given StringBuffer.
   *
   * @param sb Where to write the data.
   * @return The same StringBuffer.
   */
  StringBuffer writeTo(StringBuffer sb);

}
