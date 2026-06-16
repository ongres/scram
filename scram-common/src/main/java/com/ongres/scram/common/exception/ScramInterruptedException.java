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
   * @param message A String containing details about the exception
   */
  public ScramInterruptedException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code ScramInterruptedException} with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
   * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method).
   *        (A null value is permitted,and indicates that the cause is nonexistent or unknown.)
   */
  public ScramInterruptedException(String message, Throwable cause) {
    super(message, cause);
  }
}
