// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

import java.util.concurrent.CompletableFuture;

/**
 * A policy within the {@link HttpPipeline}.
 *
 * @see HttpPipeline
 */
@FunctionalInterface
public interface HttpPipelinePolicy {
    /**
     * Processes the provided HTTP request and invokes the next policy synchronously.
     *
     * @param httpRequest The HTTP request.
     * @param next The next policy to invoke.
     * @return The response from the next policy or the HTTP client if there are no more policies.
     */
    Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next);

    /**
     * Processes the provided HTTP request and invokes the next policy asynchronously.
     *
     * @param httpRequest The HTTP request.
     * @param next The next policy to invoke.
     * @return A CompletableFuture that will complete with the response from the next policy or the HTTP client if there
     * are no more policies.
     */
    default CompletableFuture<Response<?>> processAsync(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        return CompletableFuture.completedFuture(process(httpRequest, next));
    }

    /**
     * Gets the position in the {@link HttpPipelineBuilder} the policy will be placed when added.
     * <p>
     * Policy position does not need to be unique. When multiple polices with the same {@link HttpPipelinePosition} are
     * added they will be handled based on the documentation of {@link HttpPipelinePosition}.
     * <p>
     * By default, this method returns {@link HttpPipelinePosition#AFTER_RETRY}.
     * <p>
     * If this method returns null, an exception will be thrown when it is added to the {@link HttpPipelineBuilder}.
     *
     * @return The position of this policy.
     */
    default HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.AFTER_RETRY;
    }
}
