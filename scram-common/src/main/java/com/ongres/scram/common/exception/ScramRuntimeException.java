/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

/**
 * This class represents an error when using SCRAM, which is a SASL method.
 */
public class ScramRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new instance of ScramRuntimeException with a detailed message.
   *
   * @param detail A String containing details about the exception
   */
  public ScramRuntimeException(String detail) {
    super(detail);
  }

  /**
   * Constructs a new instance of ScramRuntimeException with a detailed message and a root cause.
   *
   * @param detail A String containing details about the exception
   * @param ex The root exception
   */
  public ScramRuntimeException(String detail, Throwable ex) {
    super(detail, ex);
  }
}
