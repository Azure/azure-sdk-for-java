// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpRedirectOptions;

import java.util.List;

/**
 * Represents Http Pipeline Options used when constructing the Http Pipeline.
 */
public class HttpPipelineOptions implements Cloneable {

    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpInstrumentationOptions httpInstrumentationOptions;
    private HttpRetryOptions httpRetryOptions;
    private HttpRedirectOptions httpRedirectOptions;
    private List<HttpPipelinePolicy> httpPipelinePolicy;

    /**
     * Creates an instance of HttpOptions with default settings.
     */
    public HttpPipelineOptions() { }

    /**
     * @return the HttpPipeline to send all requests
     */
    public HttpPipeline getHttpPipeline() {
        return httpPipeline;
    }

    /**
     * @return the HttpClient to use for requests
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Specifies the HttpPipeline to send all requests. This setting overrides the others.
     * @param httpPipeline the HttpPipeline to send all requests
     * @return The HttpOptions object itself
     */
    public HttpPipelineOptions setHttpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        return this;
    }


    /**
     * Specifies the HttpClient to send use for requests.
     * @param httpClient the http client to use for requests
     * @return The HttpOptions object itself
     */
    public HttpPipelineOptions setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
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
     */
    public HttpPipelineOptions setHttpRedirectOptions(HttpRedirectOptions httpRedirectOptions) {
        this.httpRedirectOptions = httpRedirectOptions;
        return this;
    }

    /**
     * Gets the Http Pipeline Policy list.
     *
     * @return The Http Pipeline Policy List.
     */
    public List<HttpPipelinePolicy> getHttpPipelinePolicy() {
        return httpPipelinePolicy;
    }

    /**
     * Adds the Http Pipeline Policy.
     *
     * @param httpPipelinePolicy The Http Pipeline Policy.
     * @return The HttpPipelineOptions itself.
     */
    public HttpPipelineOptions addHttpPipelinePolicy(HttpPipelinePolicy httpPipelinePolicy) {
        this.httpPipelinePolicy.add(httpPipelinePolicy);
        return this;
    }

    public HttpPipelineOptions clone() {
        HttpPipelineOptions clone = new HttpPipelineOptions()
            .setHttpClient(this.httpClient)
            .setHttpPipeline(this.httpPipeline)
            .setHttpInstrumentationOptions(this.httpInstrumentationOptions)
            .setHttpRetryOptions(this.httpRetryOptions)
            .setHttpRedirectOptions(this.httpRedirectOptions);
        return clone;
    }
}
