/*
 * Copyright (c) 2026 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

/**
 * Thrown when a SCRAM authentication process is interrupted, typically during
 * CPU-intensive PBKDF2 computation.
 *
 * <p>This class serves as a {@link ScramRuntimeException} wrapper for an
 * interruption signal. It is primarily used during cryptographic operations
 * where a checked {@link InterruptedException} cannot be propagated directly
 * due to API or functional interface constraints.
 *
 * @see Thread#isInterrupted()
 * @see InterruptedException
 * @see ScramRuntimeException
 *
 * @since 3.3
 */
public class ScramInterruptedException extends ScramRuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@code ScramInterruptedException} with the specified detail message.
   *
   * @param detail the detail message explaining the context of the interruption.
   */
  public ScramInterruptedException(String detail) {
    super(detail);
  }

  /**
   * Constructs a new {@code ScramInterruptedException} with the specified detail
   * message and the underlying cause.
   *
   * @param detail the detail message explaining the context.
   * @param ex the cause (which is saved for later retrieval by the {@link #getCause()} method).
   */
  public ScramInterruptedException(String detail, Throwable ex) {
    super(detail, ex);
  }
}
