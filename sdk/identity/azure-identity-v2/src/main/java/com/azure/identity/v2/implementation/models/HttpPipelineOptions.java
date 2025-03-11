// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpRedirectOptions;
import io.clientcore.core.http.pipeline.HttpRetryOptions;

/**
 * Represents Http Pipeline Options used when constructing the Http Pipeline.
 */
public class HttpPipelineOptions {

    private HttpPipeline httpPipeline;
    private HttpInstrumentationOptions httpInstrumentationOptions;
    private HttpRetryOptions httpRetryOptions;
    private HttpRedirectOptions httpRedirectOptions;
    private HttpPipelineBuilder httpPipelineBuilder = new HttpPipelineBuilder();

    /**
     * Creates an instance of HttpPipelineOptions with default settings.
     */
    public HttpPipelineOptions() {
    }

    /**
     * @return the HttpPipeline to send all requests
     */
    public HttpPipeline getHttpPipeline() {
        return httpPipeline;
    }

    /**
     * Gets the {@link HttpPipelineBuilder} that will create the {@link HttpPipeline} to send all request.
     *
     * @return The {@link HttpPipelineBuilder} that will create the {@link HttpPipeline} to send all request.
     */
    public HttpPipelineBuilder getHttpPipelineBuilder() {
        return httpPipelineBuilder;
    }

    /**
     * Specifies the HttpPipeline to send all requests. This setting overrides the others.
     * @param httpPipeline the HttpPipeline to send all requests
     * @return The HttpPipelineOptions object itself
     */
    public HttpPipelineOptions setHttpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }

    /**
     * Sets the {@link HttpPipelineBuilder} that will create the {@link HttpPipeline} to send all request.
     * @param httpPipelineBuilder The {@link HttpPipelineBuilder} that will create the {@link HttpPipeline} to send all
     * request.
     * @return The HttpPipelineOptions object itself
     */
    public HttpPipelineOptions setHttpPipelineBuilder(HttpPipelineBuilder httpPipelineBuilder) {
        this.httpPipelineBuilder = httpPipelineBuilder;
        return this;
    }

    /**
     * Gets the Http Instrumentation options.
     *
     * @return the Http Instrumentation options.
     */
    public HttpInstrumentationOptions getHttpInstrumentationOptions() {
        return httpInstrumentationOptions;
    }

    /**
     * Sets the Http Instrumentation Options.
     *
     * @param httpInstrumentationOptions the Http instrumentation options to set.
     * @return The HttpPipelineOptions object itself
     */
    public HttpPipelineOptions setHttpInstrumentationOptions(HttpInstrumentationOptions httpInstrumentationOptions) {
        this.httpInstrumentationOptions = httpInstrumentationOptions;
        return this;
    }

    /**
     * Gets the Http Retry Options.
     *
     * @return The Http Retry Options.
     */
    public HttpRetryOptions getHttpRetryOptions() {
        return httpRetryOptions;
    }

    /**
     * Sets the Http Retry Options.
     *
     * @param httpRetryOptions the Http Retry Options.
     * @return The HttpPipelineOptions object itself
     */
    public HttpPipelineOptions setHttpRetryOptions(HttpRetryOptions httpRetryOptions) {
        this.httpRetryOptions = httpRetryOptions;
        return this;
    }

    /**
     * Gets the Http Redirect Options.
     *
     * @return The Http Redirect Options.
     */
    public HttpRedirectOptions getHttpRedirectOptions() {
        return httpRedirectOptions;
    }

    /**
     * Sets the Http Redirect Options.
     *
     * @param httpRedirectOptions the Http Redirect Options.
     * @return The HttpPipelineOptions object itself
     */
    public HttpPipelineOptions setHttpRedirectOptions(HttpRedirectOptions httpRedirectOptions) {
        this.httpRedirectOptions = httpRedirectOptions;
        return this;
    }

    public HttpPipelineOptions copy() {
        return new HttpPipelineOptions().setHttpPipelineBuilder(httpPipelineBuilder)
            .setHttpPipeline(this.httpPipeline)
            .setHttpInstrumentationOptions(this.httpInstrumentationOptions)
            .setHttpRetryOptions(this.httpRetryOptions)
            .setHttpRedirectOptions(this.httpRedirectOptions);
    }
}
