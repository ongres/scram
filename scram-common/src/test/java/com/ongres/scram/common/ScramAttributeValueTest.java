/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.RfcExampleSha1.CLIENT_FINAL_MESSAGE_PROOF;
import static com.ongres.scram.common.RfcExampleSha1.CLIENT_NONCE;
import static com.ongres.scram.common.RfcExampleSha1.FULL_NONCE;
import static com.ongres.scram.common.RfcExampleSha1.GS2_HEADER_BASE64;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_FINAL_MESSAGE;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_ITERATIONS;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_SALT;
import static com.ongres.scram.common.RfcExampleSha1.USER;
import static com.ongres.scram.common.ScramAttributes.CLIENT_PROOF;
import static com.ongres.scram.common.ScramAttributes.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ongres.scram.common.exception.ScramParseException;
import org.junit.jupiter.api.Test;

class ScramAttributeValueTest {

  private static final Object NULL = null; // Skip validation for null analisys

  @Test
  void constructorDoesNotAllowNullValue() {
    assertThrows(IllegalArgumentException.class,
        () -> new ScramAttributeValue(USERNAME, (String) NULL),
        "A null value must throw an IllegalArgumentException");
  }

  @Test
  void parseIllegalValuesStructure() {
    String[] values = new String[] {
        null, "", "asdf", "asdf=a", CLIENT_PROOF.getChar() + "=", CLIENT_PROOF.getChar() + ",a"
    };
    int n = 0;
    for (String value : values) {
      try {
        assertNotNull(ScramAttributeValue.parse(value));
      } catch (ScramParseException e) {
        n++;
      }
    }

    assertEquals(values.length, n, "Not every illegal value thrown ScramParseException");
  }

  @Test
  void parseIllegalValuesInvalidSCRAMAttibute() {
    // SCRAM allows for extensions. If a new attribute is supported and its value has been used
    // below,
    // test will fail and will need to be fixed
    String[] values = new String[] {"z=asdfasdf", "!=value"};

    for (String value : values) {
      assertThrows(ScramParseException.class, () -> ScramAttributeValue.parse(value));
    }
  }

  @Test
  void parseLegalValues() throws ScramParseException {
    String[] legalValues = new String[] {
        CLIENT_PROOF.getChar() + "=" + "proof",
        USERNAME.getChar() + "=" + "username",
        "n=" + USER,
        "r=" + CLIENT_NONCE,
        "r=" + FULL_NONCE,
        "s=" + SERVER_SALT,
        "i=" + SERVER_ITERATIONS,
        "c=" + GS2_HEADER_BASE64,
        "p=" + CLIENT_FINAL_MESSAGE_PROOF,
        SERVER_FINAL_MESSAGE,
    };
    for (String value : legalValues) {
      assertNotNull(ScramAttributeValue.parse(value));
    }

    assertNotNull(ScramAttributeValue.parse("e=unsupported-channel-binding-type"));
  }
}
