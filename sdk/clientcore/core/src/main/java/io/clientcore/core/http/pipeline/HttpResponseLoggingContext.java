// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

import java.time.Duration;

/**
 * Information available to a custom HTTP response logger.
 */
public final class HttpResponseLoggingContext {
    private final Response<BinaryData> response;
    private final Duration responseDuration;
    private final RequestContext requestContext;
    private final int tryCount;

    HttpResponseLoggingContext(Response<BinaryData> response, Duration responseDuration, RequestContext requestContext,
        int tryCount) {
        this.response = response;
        this.responseDuration = responseDuration;
        this.requestContext = requestContext;
        this.tryCount = tryCount;
    }

    /**
     * Gets the response being logged.
     *
     * @return The HTTP response.
     */
    public Response<BinaryData> getHttpResponse() {
        return response;
    }

    /**
     * Gets the elapsed request duration when the response was received.
     *
     * @return The response duration.
     */
    public Duration getResponseDuration() {
        return responseDuration;
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
