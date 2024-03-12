/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import org.jetbrains.annotations.NotNull;

/**
 * Possible values of a GS2 Attribute.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
enum Gs2Attributes implements CharSupplier {

  /**
   * Channel binding attribute. Client doesn't support channel binding.
   */
  CLIENT_NOT(Gs2CbindFlag.CLIENT_NOT.getChar(), false),

  /**
   * Channel binding attribute. Client does support channel binding but thinks the server does not.
   */
  CLIENT_YES_SERVER_NOT(Gs2CbindFlag.CLIENT_YES_SERVER_NOT.getChar(), false),

  /**
   * Channel binding attribute. Client requires channel binding. The selected channel binding
   * follows "p=".
   */
  CHANNEL_BINDING_REQUIRED(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED.getChar(), true),

  /**
   * SCRAM attribute. This attribute specifies an authorization identity.
   */
  AUTHZID(ScramAttributes.AUTHZID.getChar(), true);

  private final char flag;
  private final boolean requiredValue;

  Gs2Attributes(char flag, boolean requiredValue) {
    this.flag = flag;
    this.requiredValue = requiredValue;
  }

  @Override
  public char getChar() {
    return flag;
  }

  boolean isRequiredValue() {
    return requiredValue;
  }

  @NotNull
  static Gs2Attributes byChar(char c) {
    switch (c) {
      case 'n':
        return CLIENT_NOT;
      case 'y':
        return CLIENT_YES_SERVER_NOT;
      case 'p':
        return CHANNEL_BINDING_REQUIRED;
      case 'a':
        return AUTHZID;
      default:
        throw new IllegalArgumentException("Invalid GS2Attribute character '" + c + "'");
    }
  }

  @NotNull
  static Gs2Attributes byGs2CbindFlag(Gs2CbindFlag cbindFlag) {
    return byChar(cbindFlag.getChar());
  }
}
