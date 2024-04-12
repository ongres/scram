/*
 * Copyright (c) 2024 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

open module test.scram {
    requires com.ongres.scram.client;

    requires transitive org.junit.jupiter.engine;
    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;
}