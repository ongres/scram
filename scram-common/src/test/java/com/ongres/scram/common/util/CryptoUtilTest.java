/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;

import static org.junit.Assert.fail;

public class CryptoUtilTest {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Test(expected = IllegalArgumentException.class)
    public void nonceInvalidSize1() {
        CryptoUtil.nonce(0, SECURE_RANDOM);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonceInvalidSize2() {
        CryptoUtil.nonce(-1, SECURE_RANDOM);
    }

    @Test
    public void nonceValid() {
        int nNonces = 1000;
        int nonceMaxSize = 100;
        Random random = new Random();

        // Some more random testing
        for(int i = 0; i < nNonces; i++) {
            for(char c : CryptoUtil.nonce(random.nextInt(nonceMaxSize) + 1, SECURE_RANDOM).toCharArray()) {
                if(c == ',' || c < (char) 33 || c > (char) 126) {
                    fail("Character c='" + c + "' is not allowed on a nonce");
                }
            }
        }
    }
}
