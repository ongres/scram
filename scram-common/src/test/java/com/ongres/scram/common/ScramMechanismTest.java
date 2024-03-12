/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ScramMechanismTest {

  private static final byte[] EMPTY_KEY = new byte[32];

  @ParameterizedTest
  @ValueSource(strings = {"SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"})
  void testIanaScramMechanisms(@NotNull String name) {
    assertNotNull(ScramMechanism.byName(name));
  }

  @ParameterizedTest
  @EnumSource(ScramMechanism.class)
  void testNameConvention(ScramMechanism scramMechanism) {
    // Note that SASL mechanism names are limited to 20 octets, which means that only
    // hash function names with lengths shorter or equal to 9 octets
    // (20-length("SCRAM-")-length("-PLUS") can be used.
    String name = scramMechanism.getName();
    assertTrue(name.startsWith("SCRAM-"), "name should start with SCRAM-");
    if (scramMechanism.isPlus()) {
      assertTrue(name.endsWith("-PLUS"), "name should end with -PLUS");
    } else {
      assertFalse(name.endsWith("-PLUS"), "name should not end with -PLUS");
    }

    String hashName = name.replace("SCRAM-", "").replace("-PLUS", "");
    assertEquals(scramMechanism.getHashAlgorithmName(), hashName);
    assertTrue(hashName.length() <= 9);
  }

  @ParameterizedTest
  @ValueSource(strings = {"SCRAM-SHA", "SHA-1-PLUS", "SCRAM-SHA-256-", "SCRAM-SHA-256-PLUS!"})
  @EmptySource
  void byNameInvalid(@NotNull String name) {
    assertNull(ScramMechanism.byName(name));
  }

  @ParameterizedTest
  @NullSource
  void byNullName(@NotNull String name) {
    assertThrows(IllegalArgumentException.class, () -> ScramMechanism.byName(name));
  }

  @ParameterizedTest
  @EnumSource(ScramMechanism.class)
  void testHashSupportedByJvm(ScramMechanism scramMechanism) {
    byte[] digest = scramMechanism.digest(new byte[0]);
    assertNotNull(digest, "got a null digest");
    assertEquals(scramMechanism.getKeyLength() / 8, digest.length);
  }

  @ParameterizedTest
  @MethodSource("provideSupportedMechanisms")
  void testHmacSupportedByJvm(@NotNull String mechanism) {
    ScramMechanism scramMechanism = ScramMechanism.byName(mechanism);
    assertNotNull(scramMechanism);
    byte[] hmac = scramMechanism.hmac(EMPTY_KEY, new byte[0]);
    assertNotNull(hmac, "got a null HMAC");
    assertEquals(scramMechanism.getKeyLength() / 8, hmac.length);
  }

  private static @NotNull List<@NotNull String> provideSupportedMechanisms() {
    return ScramMechanism.supportedMechanisms();
  }

}
