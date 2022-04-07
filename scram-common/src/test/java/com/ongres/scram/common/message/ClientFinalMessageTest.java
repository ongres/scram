/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.message;

import com.ongres.scram.common.RfcExampleSha1;
import com.ongres.scram.common.gssapi.Gs2CbindFlag;
import com.ongres.scram.common.gssapi.Gs2Header;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClientFinalMessageTest {
    @Test
    public void writeToWithoutProofValid() {
        StringBuffer sb = ClientFinalMessage.writeToWithoutProof(
                new Gs2Header(Gs2CbindFlag.CLIENT_NOT), null, RfcExampleSha1.FULL_NONCE
        );

        assertEquals(RfcExampleSha1.CLIENT_FINAL_MESSAGE_WITHOUT_PROOF, sb.toString());
    }
}
