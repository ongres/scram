/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

/**
 * This class specifies an error that occurred during authentication exchange in a
 * {@code server-final-message}.
 *
 * <p>It is sent by the server in its {@code server-final-message} and can help diagnose the reason
 * for the authentication exchange failure.
 */
public class ScramServerErrorException extends ScramException {

  private static final long serialVersionUID = 1L;

  /**
   * {@code server-error-value}.
   */
  private final String serverError;

  /**
   * Constructs a new instance of ScramServerErrorException with a detailed message.
   *
   * @param serverError The SCRAM error in the message
   */
  public ScramServerErrorException(String serverError) {
    super(ServerErrorValue.getErrorMessage(serverError));
    this.serverError = serverError;
  }

  /**
   * Constructs a new instance of ScramServerErrorException with a detailed message and a root
   * cause.
   *
   * @param serverError The SCRAM error in the message
   * @param ex The root exception
   */
  public ScramServerErrorException(String serverError, Throwable ex) {
    super(ServerErrorValue.getErrorMessage(serverError), ex);
    this.serverError = serverError;
  }

  /**
   * Return the "e=" server-error-value from the server-final-message.
   *
   * @return the error type returned in the server-final-message
   */
  public String getServerError() {
    return serverError;
  }
}
