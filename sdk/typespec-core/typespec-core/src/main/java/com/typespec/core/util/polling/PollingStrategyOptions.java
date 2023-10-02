// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling;

import com.typespec.core.annotation.Fluent;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.util.Context;
import com.typespec.core.util.serializer.ObjectSerializer;

import java.util.Objects;

/**
 * Options to configure polling strategy.
 */
@Fluent
public final class PollingStrategyOptions {

    private final HttpPipeline httpPipeline;
    private String endpoint;
    private ObjectSerializer serializer;
    private Context context;
    private String serviceVersion;

    /**
     * The {@link HttpPipeline} to use for polling and getting the final result of the long-running operation.
     *
     * @param httpPipeline {@link HttpPipeline} to use for polling and getting the final result of the long-running operation.
     * @throws NullPointerException if {@code httpPipeline} is null.
     */
    public PollingStrategyOptions(HttpPipeline httpPipeline) {
        this.httpPipeline = Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null");
    }

    /**
     * Returns {@link HttpPipeline} to use for polling and getting the final result of the long-running operation.
     *
     * @return {@link HttpPipeline} to use for polling and getting the final result of the long-running operation.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Returns the endpoint that will be used as prefix if the service response returns a relative path for getting the
     * long-running operation status and final result.
     *
     * @return the endpoint that will be used as prefix if the service response returns a relative path for getting the
     * long-running operation status and final result.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpoint that will be used as prefix if the service response returns a relative path for getting the
     * long-running operation status and final result.
     *
     * @param endpoint the endpoint that will be used as prefix if the service response returns a relative path for getting the
     * long-running operation status and final result.
     * @return the updated {@link PollingStrategyOptions} instance.
     */
    public PollingStrategyOptions setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Returns the serializer to use for serializing and deserializing the request and response.
     *
     * @return the serializer to use for serializing and deserializing the request and response.
     */
    public ObjectSerializer getSerializer() {
        return serializer;
    }

    /**
     * Set the serializer to use for serializing and deserializing the request and response.
     *
     * @param serializer the serializer to use for serializing and deserializing the request and response.
     * @return the updated {@link PollingStrategyOptions} instance.
     */
    public PollingStrategyOptions setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
        return this;
    }

    /**
     * Returns the context to use for sending the request using the {@link #getHttpPipeline()}.
     *
     * @return the context to use for sending the request using the {@link #getHttpPipeline()}.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Sets the context to use for sending the request using the {@link #getHttpPipeline()}.
     *
     * @param context the context to use for sending the request using the {@link #getHttpPipeline()}.
     * @return the updated {@link PollingStrategyOptions} instance.
     */
    public PollingStrategyOptions setContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     * Returns the service version that will be added as query param to each polling
     * request and final result request URL. If the request URL already contains a service version, it will be replaced
     * by the service version set in this constructor.
     *
     * @return the service version to use for polling and getting the final result.
     */
    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Sets the service version that will be added as query param to each polling
     * request and final result request URL. If the request URL already contains a service version, it will be replaced
     * by the service version set in this constructor.
     *
     * @param serviceVersion the service version to use for polling and getting the final result.
     * @return the updated {@link PollingStrategyOptions} instance.
     */
    public PollingStrategyOptions setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }
}
