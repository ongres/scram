/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import com.ongres.scram.common.bouncycastle.base64.Base64;
import com.ongres.scram.common.stringprep.StringPreparations;
import org.junit.jupiter.api.Test;

class ScramFunctionsTest {
  private void assertBytesEqualsBase64(String expected, byte[] actual) {
    assertArrayEquals(Base64.decode(expected), actual);
  }

  @Test
  void hmac() throws UnsupportedEncodingException {
    String message = "The quick brown fox jumps over the lazy dog";
    byte[] key = "key".getBytes(StandardCharsets.UTF_8);

    assertBytesEqualsBase64(
        "3nybhbi3iqa8ino29wqQcBydtNk=",
        ScramFunctions.hmac(ScramMechanisms.SCRAM_SHA_1,
            message.getBytes(StandardCharsets.US_ASCII), key));
    assertBytesEqualsBase64(
        "97yD9DBThCSxMpjmqm+xQ+9NWaFJRhdZl0edvC0aPNg=",
        ScramFunctions.hmac(ScramMechanisms.SCRAM_SHA_256,
            message.getBytes(StandardCharsets.US_ASCII), key));
  }

  private byte[] generateSaltedPassword() {
    return ScramFunctions.saltedPassword(
        ScramMechanisms.SCRAM_SHA_1, StringPreparations.NO_PREPARATION, "pencil",
        Base64.decode("QSXCR+Q6sek8bf92"), 4096);
  }

  private byte[] generateSaltedPasswordSha256() {
    return ScramFunctions.saltedPassword(
        ScramMechanisms.SCRAM_SHA_256, StringPreparations.NO_PREPARATION, "pencil",
        Base64.decode("W22ZaJ0SNY7soEsUEjb6gQ=="), 4096);
  }

  @Test
  void saltedPassword() {
    assertBytesEqualsBase64("HZbuOlKbWl+eR8AfIposuKbhX30=", generateSaltedPassword());
  }

  @Test
  void saltedPasswordWithSaslPrep() {
    assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
        ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "\u2168\u3000a\u0300",
        Base64.decode("0BojBCBE6P2/N4bQ"), 6400));
    assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
        ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "\u00ADIX \u00E0",
        Base64.decode("0BojBCBE6P2/N4bQ"), 6400));
    assertBytesEqualsBase64("YniLes+b8WFMvBhtSACZyyvxeCc=", ScramFunctions.saltedPassword(
        ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "IX \u00E0",
        Base64.decode("0BojBCBE6P2/N4bQ"), 6400));
    assertBytesEqualsBase64("HZbuOlKbWl+eR8AfIposuKbhX30=", ScramFunctions.saltedPassword(
        ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION, "\u0070enc\u1806il",
        Base64.decode("QSXCR+Q6sek8bf92"), 4096));
    try {
      ScramFunctions.saltedPassword(
          ScramMechanisms.SCRAM_SHA_1, StringPreparations.SASL_PREPARATION,
          "\u2168\u3000a\u0300\u0007",
          Base64.decode("QSXCR+Q6sek8bf92"), 6400);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Prohibited character \u0007", e.getMessage());
    }
  }

  @Test
  void saltedPasswordSha256() {
    assertBytesEqualsBase64("xKSVEDI6tPlSysH6mUQZOeeOp01r6B3fcJbodRPcYV0=",
        generateSaltedPasswordSha256());
  }

  private byte[] generateClientKey() {
    return ScramFunctions.clientKey(ScramMechanisms.SCRAM_SHA_1, generateSaltedPassword());
  }

  private byte[] generateClientKeySha256() {
    return ScramFunctions.clientKey(ScramMechanisms.SCRAM_SHA_256, generateSaltedPasswordSha256());
  }

  @Test
  void clientKey() {
    assertBytesEqualsBase64("4jTEe/bDZpbdbYUrmaqiuiZVVyg=", generateClientKey());
  }

  @Test
  void clientKeySha256() {
    assertBytesEqualsBase64("pg/JI9Z+hkSpLRa5btpe9GVrDHJcSEN0viVTVXaZbos=",
        generateClientKeySha256());
  }

  private byte[] generateStoredKey() {
    return ScramFunctions.storedKey(ScramMechanisms.SCRAM_SHA_1, generateClientKey());
  }

  private byte[] generateStoredKeySha256() {
    return ScramFunctions.storedKey(ScramMechanisms.SCRAM_SHA_256, generateClientKeySha256());
  }

  @Test
  void storedKey() {
    assertBytesEqualsBase64("6dlGYMOdZcOPutkcNY8U2g7vK9Y=", generateStoredKey());
  }

  @Test
  void storedKeySha256() {
    assertBytesEqualsBase64("WG5d8oPm3OtcPnkdi4Uo7BkeZkBFzpcXkuLmtbsT4qY=",
        generateStoredKeySha256());
  }

  private byte[] generateServerKey() {
    return ScramFunctions.serverKey(ScramMechanisms.SCRAM_SHA_1, generateSaltedPassword());
  }

  private byte[] generateServerKeySha256() {
    return ScramFunctions.serverKey(ScramMechanisms.SCRAM_SHA_256, generateSaltedPasswordSha256());
  }

  @Test
  void serverKey() {
    assertBytesEqualsBase64("D+CSWLOshSulAsxiupA+qs2/fTE=", generateServerKey());
  }

  @Test
  void serverKeySha256() {
    assertBytesEqualsBase64("wfPLwcE6nTWhTAmQ7tl2KeoiWGPlZqQxSrmfPwDl2dU=",
        generateServerKeySha256());
  }

  private byte[] generateClientSignature() {
    return ScramFunctions.clientSignature(ScramMechanisms.SCRAM_SHA_1, generateStoredKey(),
        com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE);
  }

  private byte[] generateClientSignatureSha256() {
    return ScramFunctions.clientSignature(ScramMechanisms.SCRAM_SHA_256, generateStoredKeySha256(),
        com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE);
  }

  @Test
  void clientSignature() {
    assertBytesEqualsBase64("XXE4xIawv6vfSePi2ovW5cedthM=", generateClientSignature());
  }

  @Test
  void clientSignatureSha256() {
    assertBytesEqualsBase64("0nMSRnwopAqKfwXHPA3jPrPL+0qDeDtYFEzxmsa+G98=",
        generateClientSignatureSha256());
  }

  private byte[] generateClientProof() {
    return ScramFunctions.clientProof(generateClientKey(), generateClientSignature());
  }

  private byte[] generateClientProofSha256() {
    return ScramFunctions.clientProof(generateClientKeySha256(), generateClientSignatureSha256());
  }

  @Test
  void clientProof() {
    assertBytesEqualsBase64("v0X8v3Bz2T0CJGbJQyF0X+HI4Ts=", generateClientProof());
  }

  @Test
  void clientProofSha256() {
    assertBytesEqualsBase64("dHzbZapWIk4jUhN+Ute9ytag9zjfMHgsqmmiz7AndVQ=",
        generateClientProofSha256());
  }

  private byte[] generateServerSignature() {
    return ScramFunctions.serverSignature(ScramMechanisms.SCRAM_SHA_1, generateServerKey(),
        com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE);
  }

  private byte[] generateServerSignatureSha256() {
    return ScramFunctions.serverSignature(ScramMechanisms.SCRAM_SHA_256, generateServerKeySha256(),
        com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE);
  }

  @Test
  void serverSignature() {
    assertBytesEqualsBase64("rmF9pqV8S7suAoZWja4dJRkFsKQ=", generateServerSignature());
  }

  @Test
  void serverSignatureSha256() {
    assertBytesEqualsBase64("6rriTRBi23WpRR/wtup+mMhUZUn/dB5nLTJRsjl95G4=",
        generateServerSignatureSha256());
  }

  @Test
  void verifyClientProof() {
    assertTrue(
        ScramFunctions.verifyClientProof(
            ScramMechanisms.SCRAM_SHA_1, generateClientProof(), generateStoredKey(),
            com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE));
  }

  @Test
  void verifyClientProofSha256() {
    assertTrue(
        ScramFunctions.verifyClientProof(
            ScramMechanisms.SCRAM_SHA_256, generateClientProofSha256(), generateStoredKeySha256(),
            com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE));
  }

  @Test
  void verifyServerSignature() {
    assertTrue(
        ScramFunctions.verifyServerSignature(
            ScramMechanisms.SCRAM_SHA_1, generateServerKey(),
            com.ongres.scram.common.RfcExampleSha1.AUTH_MESSAGE, generateServerSignature()));
  }

  @Test
  void verifyServerSignatureSha256() {
    assertTrue(
        ScramFunctions.verifyServerSignature(
            ScramMechanisms.SCRAM_SHA_256, generateServerKeySha256(),
            com.ongres.scram.common.RfcExampleSha256.AUTH_MESSAGE,
            generateServerSignatureSha256()));
  }
}
