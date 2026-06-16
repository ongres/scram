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
 * {@link ChannelBindingPolicy} and the server's advertised mechanisms, or a failure
 * in processing cryptographic channel binding data (e.g., TLS server endpoint data).
 *
 * @since 3.3
 */
public class ChannelBindingException extends MechanismNegotiationException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@code ChannelBindingException} with the specified detail message.
   *
   * @param message A String containing details about the exception
   */
  public ChannelBindingException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code ChannelBindingException} with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
   * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method).
   *        (A null value is permitted,and indicates that the cause is nonexistent or unknown.)
   */
  public ChannelBindingException(String message, Throwable cause) {
    super(message, cause);
  }
}
