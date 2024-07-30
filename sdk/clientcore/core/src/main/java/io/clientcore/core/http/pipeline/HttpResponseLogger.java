// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.ClientLogger;

import java.time.Duration;

/**
 * Manages logging HTTP responses in {@link HttpLoggingPolicy}.
 */
@FunctionalInterface
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
     * @param duration The duration of the HTTP call.
     * @return The HTTP response that was logged.
     */
    Response<?> logResponse(ClientLogger logger, Response<?> response, Duration duration);
}
