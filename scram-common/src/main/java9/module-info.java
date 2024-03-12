/*
 * Copyright (C) 2024 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

module com.ongres.scram.common {
  requires transitive com.ongres.saslprep;
  exports com.ongres.scram.common;
  exports com.ongres.scram.common.exception;
  exports com.ongres.scram.common.util;
}