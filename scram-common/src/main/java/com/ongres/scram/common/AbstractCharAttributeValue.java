/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.util.Preconditions.checkNotNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Construct and write generic CharAttribute-Value pairs.
 *
 * <p>Concrete sub-classes should also provide a static parse(String) creation method.
 */
abstract class AbstractCharAttributeValue<T extends CharSupplier> extends StringWritable {

  private final char charAttribute;
  private final @Nullable String value;

  protected AbstractCharAttributeValue(@NotNull T charAttribute, @Nullable String value) {
    if (null != value && value.isEmpty()) {
      throw new IllegalArgumentException("Value should be either null or non-empty");
    }
    this.charAttribute = checkNotNull(charAttribute, "attribute").getChar();
    this.value = value;
  }

  public final char getChar() {
    return charAttribute;
  }

  public @Nullable String getValue() {
    return value;
  }

  @Override
  StringBuilder writeTo(StringBuilder sb) {
    sb.append(charAttribute);
    if (null != value) {
      sb.append('=').append(value);
    }
    return sb;
  }

}
