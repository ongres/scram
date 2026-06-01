/*
 * Copyright (c) 2026 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

import com.ongres.scram.common.exception.ScramRuntimeException;

/**
 * Signals that an error occurred during the SASL SCRAM mechanism negotiation,
 * capability handshake, or policy enforcement phase.
 *
 * <p>This exception typically indicates a structural mismatch between the mechanisms
 * advertised by the server and those supported or allowed by the client, such as
 * a failure to find a mutually acceptable authentication variant or a breakdown in
 * channel binding requirements.
 *
 * @since 3.3
 */
public class MechanismNegotiationException extends ScramRuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@code MechanismNegotiationException} with the specified detail message.
   *
   * @param detail the detail message explaining the cause of the negotiation failure.
   */
  public MechanismNegotiationException(String detail) {
    super(detail);
  }

}
