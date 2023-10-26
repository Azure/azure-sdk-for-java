// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.policy.logging;

import com.generic.core.http.models.HttpResponse;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.logging.LogLevel;

/**
 * Manages logging HTTP responses in {@link HttpLoggingPolicy}.
 */
@FunctionalInterface
public interface HttpResponseLogger {
    /**
     * Gets the {@link LogLevel} used to log the HTTP response.
     * <p>
     * By default, this will return {@link LogLevel#INFORMATIONAL}.
     *
     * @param loggingOptions The information available during response logging.
     * @return The {@link LogLevel} used to log the HTTP response.
     */
    default LogLevel getLogLevel(HttpResponseLoggingContext loggingOptions) {
        return LogLevel.INFORMATIONAL;
    }

    /**
     * Logs the HTTP response.
     * To get the {@link LogLevel} used to log the HTTP response use {@link #getLogLevel(HttpResponseLoggingContext)} .
     *
     * @param logger The {@link ClientLogger} used to log the response.
     * @param loggingOptions The information available during response logging.
     * @return A response that returns the HTTP response that was logged.
     */
    HttpResponse logResponseSync(ClientLogger logger, HttpResponseLoggingContext loggingOptions);
}
