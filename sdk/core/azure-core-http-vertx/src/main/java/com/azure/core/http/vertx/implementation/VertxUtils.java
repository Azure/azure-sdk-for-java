// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.vertx.implementation;

import io.vertx.core.VertxException;

import java.io.IOException;

/**
 * Utility class containing helper methods when working with Vert.x HTTP.
 */
public final class VertxUtils {

    /**
     * Temporary helper method that wraps a Vert.x exception in an IOException.
     * <p>
     * Vert.x uses RuntimeExceptions for a variety of HTTP error conditions, which is convenient in not needing to
     * try-catch everywhere, but for our HttpClient and HttpPipeline purposes we want these to be IOExceptions. The
     * reason is that the RetryPolicy checks for IOException to indicate that there was an error during the request or
     * response process related to the network, and not something invalid in the request or response format
     * (ex, NullPointerException due to missing information or a bug in the code).
     *
     * @param throwable The throwable to inspect for being a Vert.x exception and to wrap.
     * @return Either the Vert.x exception wrapped in an IOException or the passed exception.
     */
    public static Throwable wrapVertxException(Throwable throwable) {
        return (throwable instanceof VertxException) ? new IOException(throwable) : throwable;
    }

    private VertxUtils() {
    }
}
