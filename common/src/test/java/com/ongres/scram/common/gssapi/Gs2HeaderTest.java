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


package com.ongres.scram.common.gssapi;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class Gs2HeaderTest {
    private static final String[] VALID_GS2HEADER_STRINGS = new String[] {
            "n,", "y,", "n,a=blah", "p=cb,", "p=cb,a=b"
    };
    private static final Gs2Header[] VALID_GS_2_HEADERS = new Gs2Header[] {
            new Gs2Header(Gs2CbindFlag.CLIENT_NOT),
            new Gs2Header(Gs2CbindFlag.CLIENT_YES_SERVER_NOT),
            new Gs2Header(Gs2CbindFlag.CLIENT_NOT, null, "blah"),
            new Gs2Header(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "cb"),
            new Gs2Header(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, "cb", "b")
    };

    private void assertGS2Header(String expected, Gs2Header gs2Header) {
        assertEquals(expected, gs2Header.writeTo(new StringBuffer()).toString());
    }

    @Test
    public void constructorValid() {
        for(int i = 0; i < VALID_GS2HEADER_STRINGS.length; i++) {
            assertGS2Header(VALID_GS2HEADER_STRINGS[i], VALID_GS_2_HEADERS[i]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorInvalid1() {
        new Gs2Header(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorInvalid2() {
        new Gs2Header(Gs2CbindFlag.CLIENT_NOT, "blah");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorInvalid3() {
        new Gs2Header(Gs2CbindFlag.CLIENT_YES_SERVER_NOT, "blah");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorInvalid4() {
        new Gs2Header(Gs2CbindFlag.CHANNEL_BINDING_REQUIRED, null, "b");
    }

    @Test
    public void parseFromInvalid() {
        String[] invalids = new String[] { "Z,", "n,Z=blah", "p,", "n=a," };
        int n = 0;
        for(String invalid : invalids) {
            try {
                Gs2Header.parseFrom(invalid);
                System.out.println(invalid);
            } catch (IllegalArgumentException e) {
                n++;
            }
        }

        assertEquals(invalids.length, n);
    }

    @Test
    public void parseFromValid() {
        for(int i = 0; i < VALID_GS2HEADER_STRINGS.length; i++) {
            assertGS2Header(
                    VALID_GS_2_HEADERS[i].writeTo(new StringBuffer()).toString(),
                    Gs2Header.parseFrom(VALID_GS2HEADER_STRINGS[i])
            );
        }
    }
}
