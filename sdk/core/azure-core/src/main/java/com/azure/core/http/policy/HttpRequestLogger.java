// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import reactor.core.publisher.Mono;

/**
 * Manages logging HTTP requests in {@link HttpLoggingPolicy}.
 */
@FunctionalInterface
public interface HttpRequestLogger {
    /**
     * Gets the {@link LogLevel} used to log the HTTP request.
     * <p>
     * By default this will return {@link LogLevel#INFORMATIONAL}.
     *
     * @param loggingOptions The information available during request logging.
     * @return The {@link LogLevel} used to log the HTTP request.
     */
    default LogLevel getLogLevel(HttpRequestLoggingContext loggingOptions) {
        return LogLevel.INFORMATIONAL;
    }

    /**
     * Logs the HTTP request.
     * <p>
     * To get the {@link LogLevel} used to log the HTTP request use {@link #getLogLevel(HttpRequestLoggingContext)}.
     *
     * @param logger The {@link ClientLogger} used to log the HTTP request.
     * @param loggingOptions The information available during request logging.
     * @return A reactive response that indicates that the HTTP request has been logged.
     */
    Mono<Void> logRequest(ClientLogger logger, HttpRequestLoggingContext loggingOptions);
}
