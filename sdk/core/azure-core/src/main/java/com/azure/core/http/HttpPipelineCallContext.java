// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.implementation.http.HttpPipelineCallContextHelper;
import com.azure.core.util.Context;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the information used to make a single HTTP request.
 */
public final class HttpPipelineCallContext {
    private HttpRequest httpRequest;
    private Context data;

    /**
     * Package private ctr.
     *
     * Creates HttpPipelineCallContext.
     *
     * @param httpRequest the request for which context needs to be created
     *
     * @throws IllegalArgumentException if there are multiple policies with same name
     */
    HttpPipelineCallContext(HttpRequest httpRequest) {
        this(httpRequest, Context.NONE);
    }

    /**
     * Package private ctr.
     *
     * Creates HttpPipelineCallContext.
     *
     * @param httpRequest the request for which context needs to be created
     * @param data the data to associate with this context
     *
     * @throws IllegalArgumentException if there are multiple policies with same name
     */
    HttpPipelineCallContext(HttpRequest httpRequest, Context data) {
        Objects.requireNonNull(httpRequest, "'httpRequest' cannot be null.");
        Objects.requireNonNull(data, "'data' cannot be null.");
        //
        this.httpRequest = httpRequest;
        this.data = data;
    }

    static {
        HttpPipelineCallContextHelper.setAccessor(HttpPipelineCallContext::getContext);
    }

    /**
     * Stores a key-value data in the context.
     *
     * @param key The key to add.
     * @param value The value to associate with that key.
     */
    public void setData(String key, Object value) {
        this.data = this.data.addData(key, value);
    }

    /**
     * Gets a value with the given key stored in the context.
     *
     * @param key The key to find in the context.
     * @return The value associated with the key.
     */
    public Optional<Object> getData(String key) {
        return this.data.getData(key);
    }

    /**
     *  Gets the context associated to the HTTP call.
     *
     * @return The context associated to the HTTP call.
     */
    Context getContext() {
        return data;
    }

    /**
     * Gets the HTTP request.
     *
     * @return The HTTP request.
     */
    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    /**
     * Sets the HTTP request object in the context.
     *
     * @param request The HTTP request.
     * @return The updated HttpPipelineCallContext object.
     */
    public HttpPipelineCallContext setHttpRequest(HttpRequest request) {
        this.httpRequest = request;
        return this;
    }
}
