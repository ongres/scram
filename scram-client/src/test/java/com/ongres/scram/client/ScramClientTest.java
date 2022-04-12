/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import com.ongres.scram.common.ScramMechanisms;
import com.ongres.scram.common.stringprep.StringPreparations;
import com.ongres.scram.common.util.CryptoUtil;
import org.junit.jupiter.api.Test;

class ScramClientTest {
  @Test
  void getValid() {
    ScramClient client1 = ScramClient
        .channelBinding(ScramClient.ChannelBinding.NO)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1")
        .setup();
    ScramClient client2 = ScramClient
        .channelBinding(ScramClient.ChannelBinding.YES)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-256-PLUS")
        .nonceLength(64)
        .setup();
    ScramClient client3 = ScramClient
        .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
        .nonceSupplier(() -> CryptoUtil.nonce(36))
        .setup();
    ScramClient client4 = ScramClient
        .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectMechanismBasedOnServerAdvertisedCsv("SCRAM-SHA-1,SCRAM-SHA-256-PLUS")
        .secureRandomAlgorithmProvider("SHA1PRNG", "SUN")
        .nonceLength(64)
        .setup();
    ScramClient client5 = ScramClient
        .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectMechanismBasedOnServerAdvertisedCsv("SCRAM-SHA-1,SCRAM-SHA-256-PLUS")
        .secureRandomAlgorithmProvider("SHA1PRNG", null)
        .nonceLength(64)
        .setup();
    ScramClient client6 = ScramClient
        .channelBinding(ScramClient.ChannelBinding.NO)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectClientMechanism(ScramMechanisms.SCRAM_SHA_1)
        .setup();
    ScramClient client7 = ScramClient
        .channelBinding(ScramClient.ChannelBinding.YES)
        .stringPreparation(StringPreparations.NO_PREPARATION)
        .selectClientMechanism(ScramMechanisms.SCRAM_SHA_256_PLUS)
        .setup();

    for (ScramClient client : new ScramClient[] {
        client1, client2, client3, client4, client5, client6, client7
    }) {
      assertNotNull(client);
    }
  }

  @Test
  void getInvalid() {
    int n = 0;

    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.NO)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1-PLUS")
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.YES)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1-PLUS,SCRAM-SAH-256-PLUS")
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectMechanismBasedOnServerAdvertised("INVALID-SCRAM-MECHANISM")
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
          .nonceSupplier(null)
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
          .nonceLength(0)
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
          .secureRandomAlgorithmProvider("Invalid algorithm", null)
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectMechanismBasedOnServerAdvertised("SCRAM-SHA-1", "SCRAM-SHA-1-PLUS")
          .secureRandomAlgorithmProvider("SHA1PRNG", "Invalid provider")
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.YES)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectClientMechanism(ScramMechanisms.SCRAM_SHA_1)
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.NO)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectClientMechanism(ScramMechanisms.SCRAM_SHA_1_PLUS)
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }
    try {
      assertNotNull(ScramClient
          .channelBinding(ScramClient.ChannelBinding.IF_SERVER_SUPPORTS_IT)
          .stringPreparation(StringPreparations.NO_PREPARATION)
          .selectClientMechanism(ScramMechanisms.SCRAM_SHA_1)
          .setup());
    } catch (IllegalArgumentException e) {
      n++;
    }

    assertEquals(10, n);
  }

  @Test
  void supportedMechanismsTestAll() {
    String[] expecteds =
        new String[] {"SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"};
    Arrays.sort(expecteds);
    String[] actuals = ScramClient.supportedMechanisms().toArray(new String[0]);
    Arrays.sort(actuals);
    assertArrayEquals(
        expecteds,
        actuals);
  }
}
