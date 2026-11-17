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
 * {@code ChannelBindingPolicy}, the server's advertised mechanisms, and the channel binding
 * data set on the client.
 *
 * @since 3.2.1
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
