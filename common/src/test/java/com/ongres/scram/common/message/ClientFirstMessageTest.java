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


import com.ongres.scram.common.gssapi.Gs2CbindFlag;
import org.junit.Test;

import static org.junit.Assert.*;


public class ClientFirstMessageTest  {
    private static final String NONCE = "fyko+d2lbbFgONRv9qkxdawL";

    private void testBuilder(ClientFirstMessage.Builder builder) {
        assertNotNull("Builder returns a null object", builder.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid1() {
        testBuilder(new ClientFirstMessage.Builder(null, "a", NONCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid2() {
        testBuilder(
                new ClientFirstMessage.Builder(Gs2CbindFlag.CLIENT_NOT, "a", NONCE).channelBindingData("cbind")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid3() {
        testBuilder(
            new ClientFirstMessage.Builder(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, "a", NONCE)
                    .channelBindingData("cbind")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid4() {
        testBuilder(new ClientFirstMessage.Builder(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "a", NONCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInvalid5() {
        testBuilder(new ClientFirstMessage.Builder(Gs2CbindFlag.CLIENT_NOT, null, NONCE));
    }

    private void assertClientFirstMessage(String expected, ClientFirstMessage.Builder clientFirstMessageBuilder) {
        assertEquals(expected, clientFirstMessageBuilder.get().writeTo(new StringBuffer()).toString());
    }

    @Test
    public void writeToValidValues() {
        assertClientFirstMessage(
                "n,,n=user,r=" + NONCE,
                new ClientFirstMessage.Builder(Gs2CbindFlag.CLIENT_NOT, "user", NONCE)
        );
        assertClientFirstMessage(
                "y,,n=user,r=" + NONCE,
                new ClientFirstMessage.Builder(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, "user", NONCE)
        );
        assertClientFirstMessage(
                "p=blah,,n=user,r=" + NONCE,
                new ClientFirstMessage.Builder(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED,  "user", NONCE)
                .channelBindingData("blah")
        );
        assertClientFirstMessage(
                "p=blah,a=authzid,n=user,r=" + NONCE,
                new ClientFirstMessage.Builder(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "user", NONCE)
                .channelBindingData("blah")
                .authzid("authzid")
        );
    }

    @Test
    public void parseFromValidValues() {
        ClientFirstMessage m1 = ClientFirstMessage.parseFrom("n,,n=user,r=" + NONCE);
        assertTrue(
                ! m1.isChannelBinding() && m1.getChannelBindingFlag() == Gs2CbindFlag.CLIENT_NOT
                && ! m1.getAuthzid().isPresent() && "user".equals(m1.getUser()) && NONCE.equals(m1.getNonce())
        );

        ClientFirstMessage m2 = ClientFirstMessage.parseFrom("y,,n=user,r=" + NONCE);
        assertTrue(
                ! m2.isChannelBinding() && m2.getChannelBindingFlag() == Gs2CbindFlag.CLIENT_YES_SERVER_NOT
                        && ! m2.getAuthzid().isPresent() && "user".equals(m2.getUser()) && NONCE.equals(m2.getNonce())
        );

        ClientFirstMessage m3 = ClientFirstMessage.parseFrom("y,a=user2,n=user,r=" + NONCE);
        assertTrue(
                ! m3.isChannelBinding() && m3.getChannelBindingFlag() == Gs2CbindFlag.CLIENT_YES_SERVER_NOT
                        && m3.getAuthzid().isPresent() && "user2".equals(m3.getAuthzid().get())
                        && "user".equals(m3.getUser()) && NONCE.equals(m3.getNonce())
        );

        ClientFirstMessage m4 = ClientFirstMessage.parseFrom("p=channel,a=user2,n=user,r=" + NONCE);
        assertTrue(
                m4.isChannelBinding() && m4.getChannelBindingFlag() == Gs2CbindFlag.CHANNEL_BINDING_REQUIRED
                        && m4.getChannelBindingName().isPresent() && "channel".equals(m4.getChannelBindingName().get())
                        && m4.getAuthzid().isPresent() && "user2".equals(m4.getAuthzid().get())
                        && "user".equals(m4.getUser()) && NONCE.equals(m4.getNonce())
        );
    }

    @Test
    public void parseFromInvalidValues() {
        String[] invalidValues = new String[] {
                "n,,r=user,r=" + NONCE, "n,,z=user,r=" + NONCE, "n,,n=user", "n,", "n,,", "n,,n=user,r", "n,,n=user,r="
        };

        int n = 0;
        for(String s : invalidValues) {

            try {
                assertNotNull(ClientFirstMessage.parseFrom(s));
            } catch (IllegalArgumentException e) {
                n++;
            }
        }

        assertEquals(invalidValues.length, n);
    }
}
