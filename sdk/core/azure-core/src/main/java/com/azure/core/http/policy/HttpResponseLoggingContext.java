// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;

import java.time.Duration;

/**
 * Options class containing information available during HTTP response logging.
 */
public final class HttpResponseLoggingContext {
    private final HttpResponse httpResponse;
    private final Duration responseDuration;
    private final Context context;
    private final Integer tryCount;

    HttpResponseLoggingContext(HttpResponse httpResponse, Duration responseDuration, Context context,
        Integer tryCount) {
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
    public HttpResponse getHttpResponse() {
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
