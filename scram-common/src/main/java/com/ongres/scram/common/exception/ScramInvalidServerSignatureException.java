/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

/**
 * This class represents an error when parsing SCRAM messages.
 */
public class ScramInvalidServerSignatureException extends ScramException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new instance of ScramInvalidServerSignatureException with a detailed message.
   *
   * @param detail A String containing details about the exception
   */
  public ScramInvalidServerSignatureException(String detail) {
    super(detail);
  }

  /**
   * Constructs a new instance of ScramInvalidServerSignatureException with a detailed message and a
   * root cause.
   *
   * @param detail A String containing details about the exception
   * @param ex The root exception
   */
  public ScramInvalidServerSignatureException(String detail, Throwable ex) {
    super(detail, ex);
  }
}
