/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class Base64Test {

  @Test
  void rfcTest() {
    assertEquals("", new String(Base64.getDecoder().decode(""), StandardCharsets.UTF_8));
    assertEquals("f", new String(Base64.getDecoder().decode("Zg=="), StandardCharsets.UTF_8));
    assertEquals("fo", new String(Base64.getDecoder().decode("Zm8="), StandardCharsets.UTF_8));
    assertEquals("foo", new String(Base64.getDecoder().decode("Zm9v"), StandardCharsets.UTF_8));
    assertEquals("foob",
        new String(Base64.getDecoder().decode("Zm9vYg=="), StandardCharsets.UTF_8));
    assertEquals("fooba",
        new String(Base64.getDecoder().decode("Zm9vYmE="), StandardCharsets.UTF_8));
    assertEquals("foobar",
        new String(Base64.getDecoder().decode("Zm9vYmFy"), StandardCharsets.UTF_8));
    assertEquals("", Base64.getEncoder().encodeToString("".getBytes(StandardCharsets.UTF_8)));
    assertEquals("Zg==", Base64.getEncoder().encodeToString("f".getBytes(StandardCharsets.UTF_8)));
    assertEquals("Zm8=", Base64.getEncoder().encodeToString("fo".getBytes(StandardCharsets.UTF_8)));
    assertEquals("Zm9v",
        Base64.getEncoder().encodeToString("foo".getBytes(StandardCharsets.UTF_8)));
    assertEquals("Zm9vYg==",
        Base64.getEncoder().encodeToString("foob".getBytes(StandardCharsets.UTF_8)));
    assertEquals("Zm9vYmE=",
        Base64.getEncoder().encodeToString("fooba".getBytes(StandardCharsets.UTF_8)));
    assertEquals("Zm9vYmFy",
        Base64.getEncoder().encodeToString("foobar".getBytes(StandardCharsets.UTF_8)));
  }
}
