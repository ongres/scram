/*
 * Copyright (c) 2026 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.client;

/**
 * Signals that an error occurred during the negotiation, validation, or
 * enforcement of SASL SCRAM Channel Binding.
 *
 * <p>This exception typically indicates a mismatch between the client's configured
 * {@code ChannelBindingPolicy} and the server's advertised mechanisms, or a failure
 * in processing cryptographic channel binding data (e.g., TLS server endpoint data).
 *
 * @since 3.3
 */
public class ChannelBindingException extends MechanismNegotiationException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@code ChannelBindingException} with the specified detail message.
   *
   * @param detail the detail message explaining the cause of the negotiation failure.
   */
  public ChannelBindingException(String detail) {
    super(detail);
  }

}
