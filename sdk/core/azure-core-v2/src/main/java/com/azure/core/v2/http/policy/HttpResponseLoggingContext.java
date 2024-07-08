// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http.policy;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.Context;

import java.time.Duration;

/**
 * The {@code HttpResponseLoggingContext} class provides contextual information available during HTTP response logging.
 *
 * <p>This class is useful when you need to access information about an HTTP response during logging. It provides
 * access to the HTTP response being received, the duration between the HTTP request being sent and the HTTP response
 * being received, the contextual information about the response, and the try count for the request.</p>
 *
 * @see Response
 * @see java.time.Duration
 * @see com.azure.core.util.Context
 * @see HttpPipelinePolicy
 */
public final class HttpResponseLoggingContext {
    private final Response<?> httpResponse;
    private final Duration responseDuration;
    private final Context context;
    private final Integer tryCount;

    HttpResponseLoggingContext(Response<?> httpResponse, Duration responseDuration, Context context, Integer tryCount) {
        this.httpResponse = httpResponse;
        this.responseDuration = responseDuration;
        this.context = context;
        this.tryCount = tryCount;
    }

    /**
     * Gets the HTTP response being received.
     *
     * @return The HTTP response being received.
     */
    public Response<?> getHttpResponse() {
        return httpResponse;
    }

    /**
     * Gets the duration between the HTTP request being sent and the HTTP response being received.
     *
     * @return The duration between the HTTP request being sent and the HTTP response being received.
     */
    public Duration getResponseDuration() {
        return responseDuration;
    }

    /**
     * Gets the contextual information about the HTTP response.
     *
     * @return The contextual information.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets the try count for the HTTP request associated to the HTTP response.
     *
     * @return The HTTP request try count.
     */
    public Integer getTryCount() {
        return tryCount;
    }
}
