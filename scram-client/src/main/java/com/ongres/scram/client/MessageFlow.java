/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import com.ongres.scram.common.ClientFinalMessage;
import com.ongres.scram.common.ClientFirstMessage;
import com.ongres.scram.common.ServerFinalMessage;
import com.ongres.scram.common.ServerFirstMessage;
import com.ongres.scram.common.exception.ScramException;
import org.jetbrains.annotations.NotNull;

interface MessageFlow {
  @NotNull
  ClientFirstMessage clientFirstMessage();

  @NotNull
  ServerFirstMessage serverFirstMessage(@NotNull String serverFirstMessage) throws ScramException;

  @NotNull
  ClientFinalMessage clientFinalMessage();

  @NotNull
  ServerFinalMessage serverFinalMessage(@NotNull String serverFinalMessage) throws ScramException;

  enum Stage {
    NONE,
    CLIENT_FIRST,
    SERVER_FIRST,
    CLIENT_FINAL,
    SERVER_FINAL;
  }
}
