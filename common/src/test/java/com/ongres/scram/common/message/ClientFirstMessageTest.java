/*
 * Copyright 2017, OnGres.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
