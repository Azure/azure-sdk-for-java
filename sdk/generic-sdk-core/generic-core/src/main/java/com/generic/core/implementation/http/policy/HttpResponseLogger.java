// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy;

import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.policy.HttpLoggingPolicy;
import com.generic.core.util.ClientLogger;

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
    default ClientLogger.LogLevel getLogLevel(HttpResponse<?> response) {
        return ClientLogger.LogLevel.INFORMATIONAL;
    }

    /**
     * Logs the HTTP response.
     * <p>
     * To get the {@link ClientLogger.LogLevel} used to log the HTTP response use {@link #getLogLevel(HttpResponse)} .
     *
     * @param logger The {@link ClientLogger} used to log the response.
     * @param response The response being logged.
     * @param duration The duration of the HTTP call.
     * @return The HTTP response that was logged.
     */
    HttpResponse<?> logResponse(ClientLogger logger, HttpResponse<?> response, Duration duration);
}
