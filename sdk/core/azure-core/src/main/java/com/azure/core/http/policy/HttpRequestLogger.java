// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import reactor.core.publisher.Mono;

/**
 * Manages logging HTTP requests in {@link HttpLoggingPolicy}.
 */
public interface HttpRequestLogger {
    /**
     * Retrieves the {@link LogLevel} used to log the current request.
     * <p>
     * By default this will return {@link LogLevel#INFORMATIONAL}.
     *
     * @param callContext The contextual information about the request, including headers, body, and metadata.
     * @return The {@link LogLevel} used to log the current request.
     */
    default LogLevel getLogLevel(HttpPipelineCallContext callContext) {
        return LogLevel.INFORMATIONAL;
    }

    /**
     * Logs the request.
     *
     * @param logger The {@link ClientLogger} used to log the request.
     * @param callContext The contextual information about the request, including headers, body, and metadata.
     * @return A reactive response that will indicate that the request has been logged.
     */
    Mono<Void> logRequest(ClientLogger logger, HttpPipelineCallContext callContext);
}
