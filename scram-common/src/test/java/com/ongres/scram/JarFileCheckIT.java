/*
 * Copyright (c) 2024 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JarFileCheckIT {

  private static JarFile jarFile;
  private static Path buildJarPath;

  @BeforeAll
  static void beforeAll() throws IOException {
    buildJarPath = Paths.get(System.getProperty("buildJar"));
    assertTrue(Files.exists(buildJarPath));
    jarFile = new JarFile(buildJarPath.toFile(), true);
  }

  @AfterAll
  static void afterAll() throws IOException {
    jarFile.close();
  }

  @Test
  void checkLicense() throws IOException {
    JarEntry jarLicense = jarFile.getJarEntry("META-INF/LICENSE");
    assertNotNull(jarLicense, "LICENSE file should be present in the final JAR file");
    try (InputStream is = jarFile.getInputStream(jarLicense);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8))) {
      String line = reader.readLine();
      assertEquals("Copyright (c) 2017 OnGres, Inc.", line);
    }
  }

  @Test
  void checkMultiReleaseManifest() throws IOException {
    Attributes mainAttributes = jarFile.getManifest().getMainAttributes();
    String multiReleaseValue = mainAttributes.getValue(new Attributes.Name("Multi-Release"));
    assertNotNull(multiReleaseValue);
    assertEquals("true", multiReleaseValue);
  }

  @Test
  void checkModuleInfoPresent() throws IOException {
    JarEntry jarModuleInfo = jarFile.getJarEntry("META-INF/versions/9/module-info.class");
    ModuleDescriptor moduleDescriptor = ModuleDescriptor.read(jarFile.getInputStream(jarModuleInfo));
    assertNotNull(moduleDescriptor);
    assertEquals("com.ongres.scram.common", moduleDescriptor.name());
  }
}
