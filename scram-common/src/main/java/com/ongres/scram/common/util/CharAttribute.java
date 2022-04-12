/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

/**
 * Represents an attribute (a key name) that is represented by a single char.
 */
public interface CharAttribute {

  /**
   * Return the char used to represent this attribute.
   *
   * @return The character of the attribute
   */
  char getChar();

}
