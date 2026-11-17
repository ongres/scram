/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import com.ongres.scram.client.ChannelBindingPolicy;
import com.ongres.scram.client.ScramClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * {@code build()} failures that 3.2 reported as {@link IllegalArgumentException} keep that type and
 * message, so a caller that catches {@code IllegalArgumentException} around {@code build()} still
 * catches them.
 */
class BuilderFailureCompatibilityTest {

  @ParameterizedTest
  @EnumSource(ChannelBindingPolicy.class)
  void noKnownMechanismAdvertised(ChannelBindingPolicy policy) {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> builder(Collections.singletonList("SCRAM-UNKNOWN"))
            .channelBindingPolicy(policy)
            .build());
    assertEquals("Either a bare or plus mechanism must be present", ex.getMessage());
  }

  @Test
  void onlyPlusAdvertisedAndNoChannelBindingData() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> builder(Collections.singletonList("SCRAM-SHA-256-PLUS")).build());
    assertEquals("A non-PLUS mechanism was not advertised", ex.getMessage());
  }

  private static ScramClient.FinalBuildStage builder(List<String> mechanisms) {
    return ScramClient.builder()
        .advertisedMechanisms(mechanisms)
        .username("user")
        .password("pencil".toCharArray());
  }
}
