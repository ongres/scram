/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

import java.io.IOException;

/**
 * This class represents an error when using SCRAM, which is a SASL method.
 */
public class ScramException extends IOException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new instance of ScramException with a detailed message.
   *
   * @param detail A String containing details about the exception
   */
  public ScramException(String detail) {
    super(detail);
  }

  /**
   * Constructs a new instance of ScramException with a detailed message and a root cause.
   *
   * @param detail A String containing details about the exception
   * @param ex The root exception
   */
  public ScramException(String detail, Throwable ex) {
    super(detail, ex);
  }
}
