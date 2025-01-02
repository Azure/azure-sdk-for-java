// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;

/**
 * Manages logging HTTP responses in {@link HttpInstrumentationPolicy}.
 */
public interface HttpResponseLogger {
    /**
     * Gets the {@link ClientLogger.LogLevel} used to log the HTTP response.
     * <p>
     * By default, this will return {@link ClientLogger.LogLevel#INFORMATIONAL}.
     *
     * @param response The response being logged.
     * @return The {@link ClientLogger.LogLevel} used to log the HTTP response.
     */
    default ClientLogger.LogLevel getLogLevel(Response<?> response) {
        return ClientLogger.LogLevel.INFORMATIONAL;
    }

    /**
     * Logs the HTTP response.
     * <p>
     * To get the {@link ClientLogger.LogLevel} used to log the HTTP response use {@link #getLogLevel(Response)} .
     *
     * @param logger The {@link ClientLogger} used to log the response.
     * @param response The response being logged.
     * @param startNanoTime The start time of the HTTP call captured via {@link System#nanoTime()}.
     * @param headersNanoTime The time when headers were received captured via {@link System#nanoTime()}. {@code null} if headers were not received.
     * @param redactedUrl The sanitized URL of the HTTP call.
     * @param tryCount The try count of the HTTP call starting from 0.
     */
    void logResponse(ClientLogger logger, Response<?> response, long startNanoTime, long headersNanoTime,
        String redactedUrl, int tryCount);

    /**
     * Logs the HTTP request exception. If logging request body is enabled, it captures
     * <p>
     * To get the {@link ClientLogger.LogLevel} used to log the HTTP response use {@link #getLogLevel(Response)} .
     *
     * @param logger The {@link ClientLogger} used to log the response.
     * @param request The request instance.
     * @param response The response being logged or {@code null} if exception was thrown before response was received.
     * @param throwable The exception that was thrown.
     * @param startNanoTime The start time of the HTTP call captured via {@link System#nanoTime()}.
     * @param headersNanoTime The time when headers were received captured via {@link System#nanoTime()}. {@code null} if headers were not received.
     * @param sanitizedUrl The sanitized URL of the HTTP call.
     * @param tryCount The try count of the HTTP call starting from 0.
     */
    void logException(ClientLogger logger, HttpRequest request, Response<?> response, Throwable throwable,
        long startNanoTime, Long headersNanoTime, String sanitizedUrl, int tryCount);
}
