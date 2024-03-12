/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parse and write GS2 Attribute-Value pairs.
 */
final class Gs2AttributeValue extends AbstractCharAttributeValue<Gs2Attributes> {

  Gs2AttributeValue(@NotNull Gs2Attributes attribute, @Nullable String value) {
    super(attribute, value);
    if (attribute.isRequiredValue()) {
      checkNotNull(value, "value");
    }
  }

  /**
   * Parses a potential Gs2AttributeValue String.
   *
   * @param value The string that contains the Attribute-Value pair (where value is optional).
   * @return The parsed class, or null if the String was null.
   * @throws IllegalArgumentException If the String is an invalid Gs2AttributeValue
   */
  @NotNull
  static Gs2AttributeValue parse(@NotNull String value) {
    if (value.isEmpty() || value.length() == 2 || value.length() > 2 && value.charAt(1) != '=') {
      throw new IllegalArgumentException("Invalid Gs2AttributeValue");
    }

    Gs2Attributes byChar = Gs2Attributes.byChar(value.charAt(0));
    String val = value.length() > 2 ? value.substring(2) : null;
    return new Gs2AttributeValue(byChar, val);
  }

}
