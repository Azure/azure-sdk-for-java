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
     *
     * @param defaultLogLevel The default log level to use.
     * @param response The HTTP response.
     * @param responseDuration The duration between sending the request and receiving the response.
     * @return The {@link LogLevel} used to log the current request.
     */
    LogLevel getLogLevel(LogLevel defaultLogLevel, HttpResponse response, Duration responseDuration);

    /**
     * Logs the request.
     *
     * @param logger The {@link ClientLogger} used to log the request.
     * @param logLevel The {@link LogLevel} used to loge the request.
     * @param response The HTTP response.
     * @param responseDuration The duration between sending the request and receiving the response.
     * @return A reactive response that will indicate that the response has been logged.
     */
    Mono<HttpResponse> logResponse(ClientLogger logger, LogLevel logLevel, HttpResponse response,
        Duration responseDuration);
}
