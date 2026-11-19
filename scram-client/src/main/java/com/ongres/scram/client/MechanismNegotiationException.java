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
 * <p>In 3.2.x, the {@code build()} failures that 3.2 already reported, such as no known
 * mechanism advertised, still throw {@link IllegalArgumentException}; this exception is thrown
 * only for failures that a {@link ChannelBindingPolicy} other than the default introduces.
 *
 * @since 3.2.1
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
