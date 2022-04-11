/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.SecureRandom;
import java.util.Random;

import org.junit.jupiter.api.Test;

class CryptoUtilTest {
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  @Test
  void nonceInvalidSize1() {
    assertThrows(IllegalArgumentException.class, () -> CryptoUtil.nonce(0, SECURE_RANDOM));
  }

  @Test
  void nonceInvalidSize2() {
    assertThrows(IllegalArgumentException.class, () -> CryptoUtil.nonce(-1, SECURE_RANDOM));
  }

  @Test
  void nonceValid() {
    int nonces = 1000;
    int nonceMaxSize = 100;
    Random random = new Random();

    // Some more random testing
    for (int i = 0; i < nonces; i++) {
      for (char c : CryptoUtil.nonce(random.nextInt(nonceMaxSize) + 1, SECURE_RANDOM)
          .toCharArray()) {
        if (c == ',' || c < (char) 33 || c > (char) 126) {
          fail("Character c='" + c + "' is not allowed on a nonce");
        }
      }
    }
  }
}
