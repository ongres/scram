/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.ongres.scram.common.ScramFunctions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NonceTest {
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  @ParameterizedTest
  @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
  void nonceInvalidSize(int size) {
    assertThrows(IllegalArgumentException.class, () -> ScramFunctions.nonce(size, SECURE_RANDOM));
  }

  @Test
  void nonceValid() throws NoSuchAlgorithmException {
    int nonces = 1000;
    int nonceMaxSize = 100;
    SecureRandom random = new SecureRandom();

    // Some more random testing
    for (int i = 0; i < nonces; i++) {
      final int size = SECURE_RANDOM.nextInt(nonceMaxSize) + 1;
      final String nonce = ScramFunctions.nonce(size, random);
      for (int j = 0; j < nonce.length(); j++) {
        char c = nonce.charAt(j);
        if (c == ',' || c < (char) 33 || c > (char) 126) {
          fail("Character c='" + c + "' is not allowed on a nonce");
        }
      }
      assertEquals(size, nonce.length());
    }
  }
}
