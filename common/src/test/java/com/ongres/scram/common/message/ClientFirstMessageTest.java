/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.message;

import com.ongres.scram.common.exception.ScramParseException;
import com.ongres.scram.common.gssapi.Gs2CbindFlag;
import org.junit.Test;

import static com.ongres.scram.common.RfcExampleSha1.CLIENT_NONCE;
import static org.junit.Assert.*;

public class ClientFirstMessageTest  {

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid1() {
        assertNotNull(new ClientFirstMessage(null, "a", CLIENT_NONCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid2() {
        assertNotNull(
                new ClientFirstMessage(Gs2CbindFlag.CLIENT_NOT, null, "cbind", "a", CLIENT_NONCE)
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid3() {
        assertNotNull(
            new ClientFirstMessage(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, null, "cbind", "a", CLIENT_NONCE)
       );
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid4() {
        assertNotNull(new ClientFirstMessage(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, null, null, "a", CLIENT_NONCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid5() {
        assertNotNull(new ClientFirstMessage(Gs2CbindFlag.CLIENT_NOT, "authzid", null, null, CLIENT_NONCE));
    }

    private void assertClientFirstMessage(String expected, ClientFirstMessage clientFirstMessage) {
        assertEquals(expected, clientFirstMessage.writeTo(new StringBuffer()).toString());
    }

    @Test
    public void writeToValidValues() {
        assertClientFirstMessage(
                "n,,n=user,r=" + CLIENT_NONCE,
                new ClientFirstMessage("user", CLIENT_NONCE)
        );
        assertClientFirstMessage(
                "y,,n=user,r=" + CLIENT_NONCE,
                new ClientFirstMessage(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, null, null, "user", CLIENT_NONCE)
        );
        assertClientFirstMessage(
                "p=blah,,n=user,r=" + CLIENT_NONCE,
                new ClientFirstMessage(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED,  null, "blah", "user", CLIENT_NONCE)
        );
        assertClientFirstMessage(
                "p=blah,a=authzid,n=user,r=" + CLIENT_NONCE,
                new ClientFirstMessage(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "authzid", "blah", "user", CLIENT_NONCE)
        );
    }

    @Test
    public void parseFromValidValues() throws ScramParseException {
        ClientFirstMessage m1 = ClientFirstMessage.parseFrom("n,,n=user,r=" + CLIENT_NONCE);
        assertTrue(
                ! m1.isChannelBinding() && m1.getChannelBindingFlag() == Gs2CbindFlag.CLIENT_NOT
                && null == m1.getAuthzid() && "user".equals(m1.getUser()) && CLIENT_NONCE.equals(m1.getNonce())
        );

        ClientFirstMessage m2 = ClientFirstMessage.parseFrom("y,,n=user,r=" + CLIENT_NONCE);
        assertTrue(
                ! m2.isChannelBinding() && m2.getChannelBindingFlag() == Gs2CbindFlag.CLIENT_YES_SERVER_NOT
                        && null == m2.getAuthzid() && "user".equals(m2.getUser()) && CLIENT_NONCE.equals(m2.getNonce())
        );

        ClientFirstMessage m3 = ClientFirstMessage.parseFrom("y,a=user2,n=user,r=" + CLIENT_NONCE);
        assertTrue(
                ! m3.isChannelBinding() && m3.getChannelBindingFlag() == Gs2CbindFlag.CLIENT_YES_SERVER_NOT
                        && null != m3.getAuthzid() && "user2".equals(m3.getAuthzid())
                        && "user".equals(m3.getUser()) && CLIENT_NONCE.equals(m3.getNonce())
        );

        ClientFirstMessage m4 = ClientFirstMessage.parseFrom("p=channel,a=user2,n=user,r=" + CLIENT_NONCE);
        assertTrue(
                m4.isChannelBinding() && m4.getChannelBindingFlag() == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED
                        && null != m4.getChannelBindingName() && "channel".equals(m4.getChannelBindingName())
                        && null != m4.getAuthzid() && "user2".equals(m4.getAuthzid())
                        && "user".equals(m4.getUser()) && CLIENT_NONCE.equals(m4.getNonce())
        );
    }

    @Test
    public void parseFromInvalidValues() {
        String[] invalidValues = new String[] {
                "n,,r=user,r=" + CLIENT_NONCE, "n,,z=user,r=" + CLIENT_NONCE, "n,,n=user", "n,", "n,,", "n,,n=user,r", "n,,n=user,r="
        };

        int n = 0;
        for(String s : invalidValues) {
            try {
                assertNotNull(ClientFirstMessage.parseFrom(s));
            } catch (ScramParseException e) {
                n++;
            }
        }

        assertEquals(invalidValues.length, n);
    }
}
