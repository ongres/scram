/*
 * Copyright (c) 2017 OnGres, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.ongres.scram.common.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This attribute specifies an error that occurred during authentication exchange. It is sent by the
 * server in its final message and can help diagnose the reason for the authentication exchange
 * failure.
 */
public final class ServerErrorValue {

  private static final Map<String, String> ERROR_MESSAGE = initServerErrorValue();

  private ServerErrorValue() {
    throw new IllegalStateException();
  }

  private static Map<String, String> initServerErrorValue() {
    Map<String, String> map = new HashMap<>();
    map.put("invalid-encoding", "The message format or encoding is incorrect");
    map.put("extensions-not-supported", "Requested extensions are not recognized by the server");
    map.put("invalid-proof", "The client-provided proof is invalid");
    map.put("channel-bindings-dont-match",
        "Channel bindings sent by the client don't match those expected by the server.");
    map.put("server-does-support-channel-binding",
        "Server doesn't support channel binding at all.");
    map.put("channel-binding-not-supported", "Channel binding is not supported for this user");
    map.put("unsupported-channel-binding-type",
        "The requested channel binding type is not supported.");
    map.put("unknown-user", "The specified username is not recognized");
    map.put("invalid-username-encoding",
        "The username encoding is invalid (either invalid UTF-8 or SASLprep failure)");
    map.put("no-resources", "The server lacks resources to process the request");
    map.put("other-error", "A generic error occurred that doesn't fit into other categories");
    return Collections.unmodifiableMap(map);
  }

  /**
   * This get the error message used in a {@link ScramServerErrorException}.
   *
   * @param errorValue the {@code server-error-value} send by the server
   * @return String with a user friendly message about the error
   */
  public static String getErrorMessage(String errorValue) {
    return ERROR_MESSAGE.get(errorValue);
  }
}
