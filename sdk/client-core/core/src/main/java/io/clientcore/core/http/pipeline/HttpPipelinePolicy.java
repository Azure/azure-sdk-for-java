// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A policy within the {@link HttpPipeline}.
 *
 * @see HttpPipeline
 */
public abstract class HttpPipelinePolicy implements Cloneable {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelinePolicy.class);
    private HttpPipelinePolicy nextPolicy;

    /**
     * Processes an HTTP request and invokes the next policy in the chain synchronously.
     *
     * @param httpRequest The HTTP request.
     * @param httpPipeline The HTTP pipeline the request will be sent through.
     *
     * @return A response produced from sending the HTTP request.
     */
    public Response<?> process(HttpRequest httpRequest, HttpPipeline httpPipeline) {
        if (nextPolicy == null) {
            try {
                return httpPipeline.getHttpClient().send(httpRequest);
            } catch (IOException e) {
                // TODO (alzimmer): Having 'process' throw an IOException forces HttpPipelinePolicy and HttpPipeline to
                //  also throw IOException. Is this something we'd want?
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }
        } else {
            return nextPolicy.process(httpRequest, httpPipeline);
        }
    }

    /**
     * Gets the next policy in the chain.
     *
     * @return The next policy in the chain.
     */
    public HttpPipelinePolicy getNextPolicy() {
        return this.nextPolicy;
    }

    void setNextPolicy(HttpPipelinePolicy nextPolicy) {
        this.nextPolicy = nextPolicy;
    }

    /**
     * Creates a new instance that's a copy of this policy.
     *
     * @return A new instance that's a copy of this policy.
     */
    @Override
    public abstract HttpPipelinePolicy clone();
}
