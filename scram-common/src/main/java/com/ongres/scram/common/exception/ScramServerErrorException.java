/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

import com.ongres.scram.common.message.ServerFinalMessage;

/**
 * This class represents an error when parsing SCRAM messages.
 */
public class ScramServerErrorException extends ScramException {

  private static final long serialVersionUID = 1L;

  private final ServerFinalMessage.Error error;

  /**
   * Constructs a new instance of ScramServerErrorException with a detailed message.
   *
   * @param error The SCRAM error in the message
   */
  public ScramServerErrorException(ServerFinalMessage.Error error) {
    super(toString(error));
    this.error = error;
  }

  /**
   * Constructs a new instance of ScramServerErrorException with a detailed message and a root
   * cause.
   *
   * @param error The SCRAM error in the message
   * @param ex The root exception
   */
  public ScramServerErrorException(ServerFinalMessage.Error error, Throwable ex) {
    super(toString(error), ex);
    this.error = error;
  }

  private static String toString(ServerFinalMessage.Error error) {
    return "Server-final-message is an error message. Error: " + error.getErrorMessage();
  }

  public ServerFinalMessage.Error getError() {
    return error;
  }
}
