/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.stringprep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import com.ongres.stringprep.Profile;
import com.ongres.stringprep.Stringprep;
import org.junit.jupiter.api.Test;

class SaslPrepTest {

  private static final Profile saslPrep = Stringprep.getProvider("SASLprep");

  @Test
  void rfc4013Examples() throws IOException {
    // Taken from https://tools.ietf.org/html/rfc4013#section-3
    assertEquals("IX", saslPrep.prepareStored("I\u00ADX"));
    assertEquals("user", saslPrep.prepareStored("user"));
    assertEquals("USER", saslPrep.prepareStored("USER"));
    assertEquals("a", saslPrep.prepareStored("\u00AA"));
    assertEquals("IX", saslPrep.prepareStored("\u2168"));
    try {
      saslPrep.prepareStored("\u0007");
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Prohibited ASCII control \"0x0007\"", e.getMessage());
    }
    try {
      saslPrep.prepareStored("\u0627\u0031");
      fail("Should thow IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("RandALCat character is not the first and the last character", e.getMessage());
    }
  }
}
