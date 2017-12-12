/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

/**
 * The optional properties that can be set on an HttpPipeline.
 */
public class HttpPipelineOptions {
    private HttpClient httpClient;
    private HttpPipelineLogger logger;

    /**
     * Configure the HttpClient that will be used for the created HttpPipeline. If no HttpClient
     * is set (or if null is set), then a default HttpClient will be created for the
     * HttpPipeline.
     * @param httpClient the HttpClient to use for the created HttpPipeline.
     * @return This HttpPipeline options object.
     */
    public HttpPipelineOptions withHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Get the HttpClient that was set.
     * @return The HttpClient that was set.
     */
    HttpClient httpClient() {
        return httpClient;
    }

    /**
     * Configure the Logger that will be used for each RequestPolicy within the created
     * HttpPipeline.
     * @param logger The Logger to provide to each RequestPolicy.
     * @return This HttpPipeline options object.
     */
    public HttpPipelineOptions withLogger(HttpPipelineLogger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Get the Logger that was set.
     * @return The Logger that was set.
     */
    HttpPipelineLogger logger() {
        return logger;
    }
}