/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.gssapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class Gs2HeaderTest {
  private static final String[] VALID_GS2HEADER_STRINGS = new String[] {
      "n,", "y,", "n,a=blah", "p=cb,", "p=cb,a=b"
  };
  private static final Gs2Header[] VALID_GS_2_HEADERS = new Gs2Header[] {
      new Gs2Header(Gs2CbindFlag.CLIENT_NOT),
      new Gs2Header(Gs2CbindFlag.CLIENT_YES_SERVER_NOT),
      new Gs2Header(Gs2CbindFlag.CLIENT_NOT, null, "blah"),
      new Gs2Header(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "cb"),
      new Gs2Header(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "cb", "b")
  };

  private void assertGS2Header(String expected, Gs2Header gs2Header) {
    assertEquals(expected, gs2Header.writeTo(new StringBuffer()).toString());
  }

  @Test
  void constructorValid() {
    for (int i = 0; i < VALID_GS2HEADER_STRINGS.length; i++) {
      assertGS2Header(VALID_GS2HEADER_STRINGS[i], VALID_GS_2_HEADERS[i]);
    }
  }

  @Test
  void constructorInvalid1() {
    assertThrows(IllegalArgumentException.class,
        () -> new Gs2Header(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, null));
  }

  @Test
  void constructorInvalid2() {
    assertThrows(IllegalArgumentException.class,
        () -> new Gs2Header(Gs2CbindFlag.CLIENT_NOT, "blah"));
  }

  @Test
  void constructorInvalid3() {
    assertThrows(IllegalArgumentException.class,
        () -> new Gs2Header(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, "blah"));
  }

  @Test
  void constructorInvalid4() {
    assertThrows(IllegalArgumentException.class,
        () -> new Gs2Header(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, null, "b"));
  }

  @Test
  void parseFromInvalid() {
    String[] invalids = new String[] {"Z,", "n,Z=blah", "p,", "n=a,"};
    int n = 0;
    for (String invalid : invalids) {
      try {
        Gs2Header.parseFrom(invalid);
        System.out.println(invalid);
      } catch (IllegalArgumentException e) {
        n++;
      }
    }

    assertEquals(invalids.length, n);
  }

  @Test
  void parseFromValid() {
    for (int i = 0; i < VALID_GS2HEADER_STRINGS.length; i++) {
      assertGS2Header(
          VALID_GS_2_HEADERS[i].writeTo(new StringBuffer()).toString(),
          Gs2Header.parseFrom(VALID_GS2HEADER_STRINGS[i]));
    }
  }
}
