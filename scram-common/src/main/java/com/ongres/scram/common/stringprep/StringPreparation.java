/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.stringprep;

/**
 * Interface for all possible String Preparations mechanisms.
 */
public interface StringPreparation {

  /**
   * Normalize a UTF-8 String according to this String Preparation rules.
   *
   * @param value The String to prepare
   * @return The prepared String
   * @throws IllegalArgumentException If the String to prepare is not valid.
   */
  String normalize(String value) throws IllegalArgumentException;
}
