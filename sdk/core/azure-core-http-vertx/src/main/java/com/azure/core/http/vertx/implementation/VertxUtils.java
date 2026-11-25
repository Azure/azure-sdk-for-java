// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.vertx.implementation;

import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.implementation.ReflectiveInvoker;
import com.azure.core.util.logging.ClientLogger;
import io.vertx.core.Future;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpClientRequest;

import java.io.IOException;

/**
 * Utility class containing helper methods when working with Vert.x HTTP.
 */
public final class VertxUtils {
    private static final ClientLogger LOGGER = new ClientLogger(VertxUtils.class);
    private static final ReflectiveInvoker REQUEST_RESET;

    static {
        try {
            // reset returns boolean in Vert.x 4.5 and Future<Void> in 5.x.
            REQUEST_RESET = ReflectionUtils.getMethodInvoker(HttpClientRequest.class,
                HttpClientRequest.class.getMethod("reset", long.class, Throwable.class), false);
        } catch (Exception ex) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(ex));
        }
    }

    /**
     * Resets an HTTP exchange without replacing its original failure if resetting also fails.
     *
     * @param request The request to reset.
     * @param code The HTTP/2 reset code.
     * @param cause The reason for resetting the exchange.
     */
    public static void resetRequest(HttpClientRequest request, long code, Throwable cause) {
        try {
            Object result = REQUEST_RESET.invokeWithArguments(request, code, cause);
            if (result instanceof Future<?>) {
                ((Future<?>) result).onFailure(error -> logResetFailure(cause, error));
            }
        } catch (Exception ex) {
            logResetFailure(cause, ex);
        }
    }

    private static void logResetFailure(Throwable cause, Throwable error) {
        if (cause != error) {
            cause.addSuppressed(error);
        }
        LOGGER.atWarning().log("Failed to reset the Vert.x HttpClientRequest.", error);
    }

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
