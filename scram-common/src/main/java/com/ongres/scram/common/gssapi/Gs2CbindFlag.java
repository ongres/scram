/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.gssapi;

import com.ongres.scram.common.util.CharAttribute;

/**
 * Possible values of a GS2 Cbind Flag (channel binding; part of GS2 header). These values are sent
 * by the client, and so are interpreted from this perspective.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Formal Syntax</a>
 */
public enum Gs2CbindFlag implements CharAttribute {

  /**
   * Client doesn't support channel binding.
   */
  CLIENT_NOT('n'),

  /**
   * Client does support channel binding but thinks the server does not.
   */
  CLIENT_YES_SERVER_NOT('y'),

  /**
   * Client requires channel binding. The selected channel binding follows "p=".
   */
  CHANNEL_BINDING_REQUIRED('p');

  private final char flag;

  Gs2CbindFlag(char flag) {
    this.flag = flag;
  }

  @Override
  public char getChar() {
    return flag;
  }

  public static Gs2CbindFlag byChar(char c) {
    switch (c) {
      case 'n':
        return CLIENT_NOT;
      case 'y':
        return CLIENT_YES_SERVER_NOT;
      case 'p':
        return CHANNEL_BINDING_REQUIRED;
      default:
        throw new IllegalArgumentException("Invalid Gs2CbindFlag character '" + c + "'");
    }
  }
}
