// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;

/**
 * Information available to a custom HTTP request logger.
 */
public final class HttpRequestLoggingContext {
    private final HttpRequest request;
    private final RequestContext requestContext;
    private final int tryCount;

    HttpRequestLoggingContext(HttpRequest request, RequestContext requestContext, int tryCount) {
        this.request = request;
        this.requestContext = requestContext;
        this.tryCount = tryCount;
    }

    /**
     * Gets the request being logged.
     *
     * @return The HTTP request.
     */
    public HttpRequest getHttpRequest() {
        return request;
    }

    /**
     * Gets the request context.
     *
     * @return The request context.
     */
    public RequestContext getRequestContext() {
        return requestContext;
    }

    /**
     * Gets the zero-based request attempt count.
     *
     * @return The request attempt count.
     */
    public int getTryCount() {
        return tryCount;
    }
}
