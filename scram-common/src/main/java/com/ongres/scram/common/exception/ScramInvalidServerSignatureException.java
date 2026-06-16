/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

/**
 * This class represents an error when verifying the a base64-encoded ServerSignature in a
 * {@code server-final-message}.
 *
 * <p>Is used by the client to verify that the server has access to the user's authentication
 * information.
 */
public class ScramInvalidServerSignatureException extends ScramException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@code ScramInvalidServerSignatureException} with the specified detail message.
   *
   * @param message A String containing details about the exception
   */
  public ScramInvalidServerSignatureException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code ScramInvalidServerSignatureException} with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
   * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method).
   *        (A null value is permitted,and indicates that the cause is nonexistent or unknown.)
   */
  public ScramInvalidServerSignatureException(String message, Throwable cause) {
    super(message, cause);
  }
}
