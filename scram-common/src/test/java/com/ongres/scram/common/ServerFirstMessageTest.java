/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static com.ongres.scram.common.RfcExampleSha1.CLIENT_NONCE;
import static com.ongres.scram.common.RfcExampleSha1.SERVER_FIRST_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import com.ongres.scram.common.exception.ScramParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ServerFirstMessageTest {
  @Test
  void validConstructor() {
    ServerFirstMessage serverFirstMessage = new ServerFirstMessage(
        CLIENT_NONCE,
        "3rfcNHYJY1ZVvWVs7j",
        "QSXCR+Q6sek8bf92",
        4096);

    assertEquals(SERVER_FIRST_MESSAGE, serverFirstMessage.toString());
  }

  @Test
  void validParseFrom() throws ScramParseException {
    ServerFirstMessage serverFirstMessage =
        ServerFirstMessage.parseFrom(SERVER_FIRST_MESSAGE, CLIENT_NONCE);

    assertEquals(SERVER_FIRST_MESSAGE, serverFirstMessage.toString());
  }

  static Stream<String> invalidServerNonceSuffixes() {
    return Stream.of(
      "server,nonce", // printable ASCII except ","
        "server nonce", // space (0x20) is below the printable ASCII range
        "servernonce" + (char) 0x7F // DEL (0x7F) is above the printable ASCII range
    );
  }

  @ParameterizedTest(name = "nonce with invalid char rejected")
  @MethodSource("invalidServerNonceSuffixes")
  void invalidNonceCharactersRejected(String serverNonceSuffix) {
    String message = "r=" + CLIENT_NONCE + serverNonceSuffix + ",s=QSXCR+Q6sek8bf92,i=4096";
    assertThrows(ScramParseException.class,
        () -> ServerFirstMessage.parseFrom(message, CLIENT_NONCE));
  }
}
