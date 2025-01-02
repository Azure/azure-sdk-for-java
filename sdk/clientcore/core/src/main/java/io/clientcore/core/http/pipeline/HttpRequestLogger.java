// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.instrumentation.logging.ClientLogger;

/**
 * Manages logging HTTP requests in {@link HttpInstrumentationPolicy}.
 */
@FunctionalInterface
public interface HttpRequestLogger {
    /**
     * Gets the {@link ClientLogger.LogLevel} used to log the HTTP request.
     * <p>
     * By default, this will return {@link ClientLogger.LogLevel#INFORMATIONAL}.
     *
     * @param request The request being logged.
     * @return The {@link ClientLogger.LogLevel} used to log the HTTP request.
     */
    default ClientLogger.LogLevel getLogLevel(HttpRequest request) {
        return ClientLogger.LogLevel.VERBOSE;
    }

    /**
     * Logs the HTTP request.
     * <p>
     * To get the {@link ClientLogger.LogLevel} used to log the HTTP request use {@link #getLogLevel(HttpRequest)}.
     *
     * @param logger The {@link ClientLogger} used to log the HTTP request.
     * @param request The request being logged.
     * @param redactedUrl The redacted URL of the request.
     * @param tryCount The number of times the request has been attempted.
     */
    void logRequest(ClientLogger logger, HttpRequest request, String redactedUrl, int tryCount);
}
