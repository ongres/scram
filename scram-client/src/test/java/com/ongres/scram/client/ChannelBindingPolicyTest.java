/*
 * Copyright (c) 2026 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ChannelBindingPolicyTest {

  @ParameterizedTest(name = "''{0}'' -> DISABLE")
  @ValueSource(strings = {"disable", "DISABLE", "Disable"})
  void ofDisable(String value) {
    assertEquals(ChannelBindingPolicy.DISABLE, ChannelBindingPolicy.of(value));
  }

  @ParameterizedTest(name = "''{0}'' -> ALLOW")
  @ValueSource(strings = {"allow", "ALLOW", "Allow", "prefer", "PREFER", "Prefer"})
  void ofAllow(String value) {
    assertEquals(ChannelBindingPolicy.ALLOW, ChannelBindingPolicy.of(value));
  }

  @ParameterizedTest(name = "''{0}'' -> REQUIRE")
  @ValueSource(strings = {"require", "REQUIRE", "Require"})
  void ofRequire(String value) {
    assertEquals(ChannelBindingPolicy.REQUIRE, ChannelBindingPolicy.of(value));
  }

  @Test
  void ofUnknownValueThrows() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> ChannelBindingPolicy.of("unknown"));
    assertEquals("Invalid channel binding value: unknown", ex.getMessage());
  }
}
