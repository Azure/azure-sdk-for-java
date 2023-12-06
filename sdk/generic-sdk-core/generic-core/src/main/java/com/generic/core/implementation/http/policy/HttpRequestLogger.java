// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.logging.LogLevel;

/**
 * Manages logging HTTP requests in {@link HttpLoggingPolicy}.
 */
@FunctionalInterface
public interface HttpRequestLogger {
    /**
     * Gets the {@link LogLevel} used to log the HTTP request.
     * <p>
     * By default, this will return {@link LogLevel#INFORMATIONAL}.
     *
     * @param request The request being logged.
     * @return The {@link LogLevel} used to log the HTTP request.
     */
    default LogLevel getLogLevel(HttpRequest request) {
        return LogLevel.INFORMATIONAL;
    }

    /**
     * Logs the HTTP request.
     * <p>
     * To get the {@link LogLevel} used to log the HTTP request use {@link #getLogLevel(HttpRequest)}.
     *
     * @param logger The {@link ClientLogger} used to log the HTTP request.
     * @param request The request being logged.
     */
    void logRequest(ClientLogger logger, HttpRequest request);
}
