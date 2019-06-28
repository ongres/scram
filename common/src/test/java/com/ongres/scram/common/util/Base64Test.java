/*
 * Copyright 2019, OnGres.
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

package com.ongres.scram.common.util;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.ongres.scram.common.bouncycastle.base64.Base64;

public class Base64Test {

  @Test
  public void rfcTest() {
    Assert.assertEquals("", new String(Base64.decode(""), StandardCharsets.UTF_8));
    Assert.assertEquals("f", new String(Base64.decode("Zg=="), StandardCharsets.UTF_8));
    Assert.assertEquals("fo", new String(Base64.decode("Zm8="), StandardCharsets.UTF_8));
    Assert.assertEquals("foo", new String(Base64.decode("Zm9v"), StandardCharsets.UTF_8));
    Assert.assertEquals("foob", new String(Base64.decode("Zm9vYg=="), StandardCharsets.UTF_8));
    Assert.assertEquals("fooba", new String(Base64.decode("Zm9vYmE="), StandardCharsets.UTF_8));
    Assert.assertEquals("foobar", new String(Base64.decode("Zm9vYmFy"), StandardCharsets.UTF_8));
    Assert.assertEquals("", Base64.toBase64String("".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("Zg==", Base64.toBase64String("f".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("Zm8=", Base64.toBase64String("fo".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("Zm9v", Base64.toBase64String("foo".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("Zm9vYg==", Base64.toBase64String("foob".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("Zm9vYmE=", Base64.toBase64String("fooba".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("Zm9vYmFy", Base64.toBase64String("foobar".getBytes(StandardCharsets.UTF_8)));
  }
}
