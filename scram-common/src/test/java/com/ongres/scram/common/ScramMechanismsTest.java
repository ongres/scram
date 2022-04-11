/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class ScramMechanismsTest {

  @Test
  void testHashSupportedByJvm() {
    byte[] digest;
    for (ScramMechanisms scramMechanism : ScramMechanisms.values()) {
      digest = scramMechanism.digest(new byte[0]);
      assertNotNull(digest, "got a null digest");
    }
  }

  @Test
  void testHmacSupportedByJvm() {
    byte[] hmac;
    for (ScramMechanisms scramMechanism : ScramMechanisms.values()) {
      hmac = scramMechanism.hmac(new byte[] {0}, new byte[0]);
      assertNotNull(hmac, "got a null HMAC");
    }
  }

  private void testNames(String[] names, Predicate<ScramMechanisms> predicate) {
    int count = 0;
    for (String name : names) {
      if (predicate.test(ScramMechanisms.byName(name))) {
        count++;
      }
    }
    assertEquals(
        names.length,
        count);
  }

  @Test
  void byNameValid() {
    testNames(
        new String[] {"SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"},
        new Predicate<ScramMechanisms>() {
          @Override
          public boolean test(ScramMechanisms scramMechanisms) {
            return scramMechanisms != null;
          }
        });
  }

  @Test
  void byNameInvalid() {
    testNames(
        new String[] {"SCRAM-SHA", "SHA-1-PLUS", "SCRAM-SHA-256-", "SCRAM-SHA-256-PLUS!"},
        new Predicate<ScramMechanisms>() {
          @Override
          public boolean test(ScramMechanisms scramMechanisms) {
            return scramMechanisms == null;
          }
        });
  }

  private void selectMatchingMechanismTest(ScramMechanisms scramMechanisms, boolean channelBinding,
      String... names) {
    assertEquals(
        scramMechanisms, ScramMechanisms.selectMatchingMechanism(channelBinding, names));
  }

  @Test
  void selectMatchingMechanism() {
    selectMatchingMechanismTest(
        ScramMechanisms.SCRAM_SHA_1, false,
        "SCRAM-SHA-1");
    selectMatchingMechanismTest(
        ScramMechanisms.SCRAM_SHA_256_PLUS, true,
        "SCRAM-SHA-256-PLUS");
    selectMatchingMechanismTest(
        ScramMechanisms.SCRAM_SHA_256, false,
        "SCRAM-SHA-1", "SCRAM-SHA-256");
    selectMatchingMechanismTest(
        ScramMechanisms.SCRAM_SHA_256, false,
        "SCRAM-SHA-1", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS");
    selectMatchingMechanismTest(
        ScramMechanisms.SCRAM_SHA_1_PLUS, true,
        "SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256");
    selectMatchingMechanismTest(
        ScramMechanisms.SCRAM_SHA_256_PLUS, true,
        "SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS");
    selectMatchingMechanismTest(
        null, true,
        "SCRAM-SHA-1", "SCRAM-SHA-256");
  }
}
