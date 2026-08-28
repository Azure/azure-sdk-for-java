// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.instrumentation.logging.ClientLogger;

/**
 * Logs HTTP requests handled by {@link HttpInstrumentationPolicy}.
 */
@FunctionalInterface
public interface HttpRequestLogger {
    /**
     * Logs an HTTP request.
     *
     * @param logger The client logger associated with the request.
     * @param context Information available while logging the request.
     */
    void logRequest(ClientLogger logger, HttpRequestLoggingContext context);
}
