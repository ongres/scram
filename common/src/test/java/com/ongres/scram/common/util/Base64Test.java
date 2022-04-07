/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
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
