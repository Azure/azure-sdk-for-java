// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpRequest;
import com.azure.core.util.Context;

/**
 * Options class containing information available during HTTP request logging.
 */
public final class HttpRequestLoggingContext {
    private final HttpRequest httpRequest;
    private final Context context;
    private final Integer tryCount;

    HttpRequestLoggingContext(HttpRequest httpRequest, Context context, Integer tryCount) {
        this.httpRequest = httpRequest;
        this.context = context;
        this.tryCount = tryCount;
    }

    /**
     * Gets the HTTP request being sent.
     *
     * @return The HTTP request.
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * Gets the contextual information about the HTTP request.
     *
     * @return The contextual information.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets the try count for the HTTP request.
     *
     * @return The HTTP request try count.
     */
    public Integer getTryCount() {
        return tryCount;
    }
}
