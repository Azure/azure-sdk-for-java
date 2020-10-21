// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Manages logging HTTP responses in {@link HttpLoggingPolicy}.
 */
public interface HttpResponseLogger {
    /**
     * Retrieves the {@link LogLevel} used to log the current response.
     * <p>
     * By default this will return {@link LogLevel#INFORMATIONAL}.
     *
     * @param response The HTTP response.
     * @param responseDuration The duration between sending the request and receiving the response.
     * @return The {@link LogLevel} used to log the current response.
     */
    default LogLevel getLogLevel(HttpResponse response, Duration responseDuration) {
        return LogLevel.INFORMATIONAL;
    }

    /**
     * Logs the response.
     *
     * @param logger The {@link ClientLogger} used to log the response.
     * @param response The HTTP response.
     * @param responseDuration The duration between sending the request and receiving the response.
     * @return A reactive response that returns the response that was logged.
     */
    Mono<HttpResponse> logResponse(ClientLogger logger, HttpResponse response, Duration responseDuration);
}
