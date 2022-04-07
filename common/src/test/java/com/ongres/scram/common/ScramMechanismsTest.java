/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ScramMechanismsTest {
    @Test
    public void TestHashSupportedByJVM() {
        byte[] digest;
        for(ScramMechanisms scramMechanism : ScramMechanisms.values()) {
            digest = scramMechanism.digest(new byte[0]);
            assertNotNull("got a null digest", digest);
        }
    }

    @Test
    public void TestHMACSupportedByJVM() {
        byte[] hmac;
        for(ScramMechanisms scramMechanism : ScramMechanisms.values()) {
            hmac = scramMechanism.hmac(new byte[] { 0 }, new byte[0]);
            assertNotNull("got a null HMAC", hmac);
        }
    }
    
    private interface Predicate<T> {
        boolean test(T t);
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
                count
        );
    }

    @Test
    public void byNameValid() {
        testNames(
                new String[] { "SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS" },
                new Predicate<ScramMechanisms>() {
                    @Override
                    public boolean test(ScramMechanisms scramMechanisms) {
                      return scramMechanisms != null;
                    }
                  }
        );
    }

    @Test
    public void byNameInvalid() {
        testNames(
                new String[] { "SCRAM-SHA", "SHA-1-PLUS", "SCRAM-SHA-256-", "SCRAM-SHA-256-PLUS!" },
                new Predicate<ScramMechanisms>() {
                    @Override
                    public boolean test(ScramMechanisms scramMechanisms) {
                      return scramMechanisms == null;
                    }
                  }
        );
    }

    private void selectMatchingMechanismTest(ScramMechanisms scramMechanisms, boolean channelBinding, String... names) {
        assertEquals(
                scramMechanisms, ScramMechanisms.selectMatchingMechanism(channelBinding, names)
        );
    }

    @Test
    public void selectMatchingMechanism() {
        selectMatchingMechanismTest(
                ScramMechanisms.SCRAM_SHA_1, false,
                "SCRAM-SHA-1"
        );
        selectMatchingMechanismTest(
                ScramMechanisms.SCRAM_SHA_256_PLUS, true,
                "SCRAM-SHA-256-PLUS"
        );
        selectMatchingMechanismTest(
                ScramMechanisms.SCRAM_SHA_256, false,
                "SCRAM-SHA-1", "SCRAM-SHA-256"
        );
        selectMatchingMechanismTest(
                ScramMechanisms.SCRAM_SHA_256, false,
                "SCRAM-SHA-1", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"
        );
        selectMatchingMechanismTest(
                ScramMechanisms.SCRAM_SHA_1_PLUS, true,
                "SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256"
        );
        selectMatchingMechanismTest(
                ScramMechanisms.SCRAM_SHA_256_PLUS, true,
                "SCRAM-SHA-1", "SCRAM-SHA-1-PLUS", "SCRAM-SHA-256", "SCRAM-SHA-256-PLUS"
        );
        selectMatchingMechanismTest(
                null, true,
                "SCRAM-SHA-1", "SCRAM-SHA-256"
        );
    }
}
