/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.castNonNull;
import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import com.ongres.scram.common.exception.ScramParseException;
import org.jetbrains.annotations.NotNull;

/**
 * Parse and write SCRAM Attribute-Value pairs.
 */
class ScramAttributeValue extends AbstractCharAttributeValue<ScramAttributes> {

  public ScramAttributeValue(@NotNull ScramAttributes attribute, @NotNull String value) {
    super(attribute, checkNotNull(value, "value"));
  }

  @Override
  public final @NotNull String getValue() {
    return castNonNull(super.getValue());
  }

  /**
   * Parses a potential ScramAttributeValue String.
   *
   * @param value The string that contains the Attribute-Value pair.
   * @return The parsed class
   * @throws ScramParseException If the argument is empty or an invalid Attribute-Value
   */
  public static @NotNull ScramAttributeValue parse(@NotNull String value) throws ScramParseException {
    if (value == null || value.length() < 3 || value.charAt(1) != '=') {
      throw new ScramParseException("Invalid ScramAttributeValue '" + value + "'");
    }
    return new ScramAttributeValue(ScramAttributes.byChar(value.charAt(0)), value.substring(2));
  }
}
