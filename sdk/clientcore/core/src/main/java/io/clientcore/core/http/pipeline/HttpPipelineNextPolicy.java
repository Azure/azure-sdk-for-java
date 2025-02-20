// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpPipelineCallState;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

/**
 * A type that invokes next policy in the pipeline.
 */
public class HttpPipelineNextPolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineNextPolicy.class);

    private final HttpPipelineCallState state;

    /**
     * Package-private constructor. Creates an HttpPipelineNextPolicy instance.
     *
     * @param state The pipeline call state.
     */
    HttpPipelineNextPolicy(HttpPipelineCallState state) {
        this.state = state;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return The response.
     * @throws UncheckedIOException If an error occurs when sending the request or receiving the response.
     */
    public Response<?> process() {
        HttpPipelinePolicy nextPolicy = state.getNextPolicy();

        if (nextPolicy == null) {
            try {
                return this.state.getPipeline().getHttpClient().send(this.state.getHttpRequest());
            } catch (IOException e) {
                // TODO (alzimmer): Having 'process' throw an IOException forces HttpPipelinePolicy and HttpPipeline to
                //  also throw IOException. Is this something we'd want?
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }
        } else {
            return nextPolicy.process(this.state.getHttpRequest(), this);
        }
    }

    public CompletableFuture<Response<?>> processAsync() {
        // TODO (alzimmer): Do we need a different design for async where this is doing something like
        //  CompletableFuture.thenCompose or CompletableFuture.thenApply, etc?
        //  I would imagine in most cases we don't want any thread switching happening while executing the pipeline,
        //  except maybe in the case of TokenCredentials where they might be making a network call to get a token.
        HttpPipelinePolicy nextPolicy = state.getNextPolicy();

        if (nextPolicy == null) {
            return this.state.getPipeline().getHttpClient().sendAsync(this.state.getHttpRequest());
        } else {
            return nextPolicy.processAsync(this.state.getHttpRequest(), this);
        }
    }

    /**
     * Copies the current state of the {@link HttpPipelineNextPolicy}.
     * <p>
     * This method must be used when a re-request is made in the pipeline.
     *
     * @return A new instance of this next pipeline policy.
     */
    public HttpPipelineNextPolicy copy() {
        return new HttpPipelineNextPolicy(this.state.copy());
    }
}
