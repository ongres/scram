/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.gssapi;

import com.ongres.scram.common.ScramAttributes;
import com.ongres.scram.common.util.CharAttribute;

/**
 * Possible values of a GS2 Attribute.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
public enum Gs2Attributes implements CharAttribute {

  /**
   * Channel binding attribute. Client doesn't support channel binding.
   */
  CLIENT_NOT(Gs2CbindFlag.CLIENT_NOT.getChar()),

  /**
   * Channel binding attribute. Client does support channel binding but thinks the server does not.
   */
  CLIENT_YES_SERVER_NOT(Gs2CbindFlag.CLIENT_YES_SERVER_NOT.getChar()),

  /**
   * Channel binding attribute. Client requires channel binding. The selected channel binding
   * follows "p=".
   */
  CHANNEL_BINDING_REQUIRED(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED.getChar()),

  /**
   * SCRAM attribute. This attribute specifies an authorization identity.
   */
  AUTHZID(ScramAttributes.AUTHZID.getChar());

  private final char flag;

  Gs2Attributes(char flag) {
    this.flag = flag;
  }

  @Override
  public char getChar() {
    return flag;
  }

  public static Gs2Attributes byChar(char c) {
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

  public static Gs2Attributes byGS2CbindFlag(Gs2CbindFlag cbindFlag) {
    return byChar(cbindFlag.getChar());
  }
}
