// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.models.Context;
import com.generic.core.util.ClientLogger;

/**
 * Contains metadata associated with a given {@link HttpRequest}.
 * <p>
 * {@link HttpRequest} metadata is anything that is not part of the request body, such as the request's URL, headers,
 * and HTTP method.
 */
public final class HttpRequestMetadata {
    private Context context = Context.NONE;
    private int retryCount;
    private ClientLogger requestLogger;
    private boolean eagerlyConvertHeaders;
    private boolean eagerlyReadResponse;
    private boolean ignoreResponseBody;

    /**
     * Creates an instance of {@link HttpRequestMetadata}.
     */
    public HttpRequestMetadata() {
    }

    /**
     * Gets the {@link Context} associated with the request.
     *
     * @return The {@link Context} associated with the request.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Sets the {@link Context} associated with the request.
     *
     * @param context The {@link Context} associated with the request.
     * @return The updated {@link HttpRequestMetadata} object.
     */
    public HttpRequestMetadata setContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     * Gets the number of times the request has been retried.
     *
     * @return The number of times the request has been retried.
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Sets the number of times the request has been retried.
     *
     * @param retryCount The number of times the request has been retried.
     * @return The updated {@link HttpRequestMetadata} object.
     */
    public HttpRequestMetadata setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /**
     * Whether the response headers should be eagerly converted.
     *
     * @return Whether the response headers should be eagerly converted.
     */
    public boolean isEagerlyConvertHeaders() {
        return eagerlyConvertHeaders;
    }

    /**
     * Sets whether the response headers should be eagerly converted.
     *
     * @param eagerlyConvertHeaders Whether the response headers should be eagerly converted.
     * @return The updated {@link HttpRequestMetadata} object.
     */
    public HttpRequestMetadata setEagerlyConvertHeaders(boolean eagerlyConvertHeaders) {
        this.eagerlyConvertHeaders = eagerlyConvertHeaders;
        return this;
    }

    /**
     * Whether the response body should be eagerly read.
     *
     * @return Whether the response body should be eagerly read.
     */
    public boolean isEagerlyReadResponse() {
        return eagerlyReadResponse;
    }

    /**
     * Sets whether the response body should be eagerly read.
     *
     * @param eagerlyReadResponse Whether the response body should be eagerly read.
     * @return The updated {@link HttpRequestMetadata} object.
     */
    public HttpRequestMetadata setEagerlyReadResponse(boolean eagerlyReadResponse) {
        this.eagerlyReadResponse = eagerlyReadResponse;
        return this;
    }

    /**
     * Whether the response body should be ignored.
     *
     * @return Whether the response body should be ignored.
     */
    public boolean isIgnoreResponseBody() {
        return ignoreResponseBody;
    }

    /**
     * Sets whether the response body should be ignored.
     *
     * @param ignoreResponseBody Whether the response body should be ignored.
     * @return The updated {@link HttpRequestMetadata} object.
     */
    public HttpRequestMetadata setIgnoreResponseBody(boolean ignoreResponseBody) {
        this.ignoreResponseBody = ignoreResponseBody;
        return this;
    }

    /**
     * Creates a copy of the request metadata.
     *
     * @return A new {@link HttpRequestMetadata} instance with the same values as the current instance.
     */
    public HttpRequestMetadata copy() {
        HttpRequestMetadata copy = new HttpRequestMetadata();
        copy.context = context;
        copy.retryCount = retryCount;
        copy.requestLogger = requestLogger;
        copy.eagerlyConvertHeaders = eagerlyConvertHeaders;
        copy.eagerlyReadResponse = eagerlyReadResponse;
        copy.ignoreResponseBody = ignoreResponseBody;

        return copy;
    }
}
