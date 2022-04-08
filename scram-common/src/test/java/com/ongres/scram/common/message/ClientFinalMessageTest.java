/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ongres.scram.common.RfcExampleSha1;
import com.ongres.scram.common.gssapi.Gs2CbindFlag;
import com.ongres.scram.common.gssapi.Gs2Header;
import org.junit.jupiter.api.Test;

class ClientFinalMessageTest {
  @Test
  void writeToWithoutProofValid() {
    StringBuffer sb = ClientFinalMessage.writeToWithoutProof(
        new Gs2Header(Gs2CbindFlag.CLIENT_NOT), null, RfcExampleSha1.FULL_NONCE);

    assertEquals(RfcExampleSha1.CLIENT_FINAL_MESSAGE_WITHOUT_PROOF, sb.toString());
  }
}
