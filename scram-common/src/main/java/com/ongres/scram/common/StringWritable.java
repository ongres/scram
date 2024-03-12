/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract class to denote classes which can write to a StringBuffer.
 */
abstract class StringWritable {

  /**
   * Write the class information to the given StringBuffer.
   *
   * @param sb Where to write the data.
   * @return The same StringBuffer.
   */
  abstract @NotNull StringBuilder writeTo(@NotNull StringBuilder sb);

}
