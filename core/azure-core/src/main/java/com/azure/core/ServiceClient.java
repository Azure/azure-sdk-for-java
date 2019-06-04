// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.http.HttpPipeline;

/**
 * The base class for REST service clients.
 */
public abstract class ServiceClient {
    /**
     * The HTTP pipeline to send requests through.
     */
    private HttpPipeline httpPipeline;

    /**
     * Creates ServiceClient.
     *
     * @param httpPipeline The HTTP pipeline to send requests through
     */
    protected ServiceClient(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
    }

    /**
     * @return the HTTP pipeline to send requests through.
     */
    public HttpPipeline httpPipeline() {
        return this.httpPipeline;
    }
}
