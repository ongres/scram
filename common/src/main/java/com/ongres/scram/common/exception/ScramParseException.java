/*
 * Copyright (C) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

/**
 * This class represents an error when parsing SCRAM messages
 */
public class ScramParseException extends ScramException {
    /**
     * Constructs a new instance of ScramParseException with a detailed message.
     * @param detail A String containing details about the exception
     */
    public ScramParseException(String detail) {
        super(detail);
    }

    /**
     * Constructs a new instance of ScramParseException with a detailed message and a root cause.
     * @param detail A String containing details about the exception
     * @param ex The root exception
     */
    public ScramParseException(String detail, Throwable ex) {
        super(detail, ex);
    }
}
