/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClientFinalMessageTest {

  @Test
  void writeToWithoutProofValid() {
    StringBuilder sb = ClientFinalMessage.withoutProof(new StringBuilder(),
        new Gs2Header(Gs2CbindFlag.CLIENT_NOT), null, RfcExampleSha1.FULL_NONCE);

    assertEquals(RfcExampleSha1.CLIENT_FINAL_MESSAGE_WITHOUT_PROOF, sb.toString());
  }

}
