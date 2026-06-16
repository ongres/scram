/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

/**
 * This class represents an generic error when using SCRAM, which is a SASL method.
 */
public class ScramException extends Exception {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@code ScramException} with the specified detail message.
   *
   * @param message A String containing details about the exception
   */
  public ScramException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code ScramException} with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
   * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method).
   *        (A null value is permitted,and indicates that the cause is nonexistent or unknown.)
   */
  public ScramException(String message, Throwable cause) {
    super(message, cause);
  }
}
