// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

import java.util.concurrent.CompletableFuture;

/**
 * A policy within the {@link HttpPipeline}.
 *
 * @see HttpPipeline
 */
@FunctionalInterface
public interface HttpPipelinePolicy {
    /**
     * Processes the provided HTTP request and invokes the next policy.
     *
     * @param httpRequest The HTTP request.
     * @param next The next policy to invoke.
     * @return The {@link Response} from the next policy or the HTTP client if there are no more policies.
     */
    Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next);

    /**
     * Processes the provided HTTP request asynchronously and invokes the next policy.
     * <p>
     * This method is called when {@link HttpPipeline#sendAsync(HttpRequest)} is used.
     * <p>
     * The default implementation of this method wraps the synchronous
     * {@link #process(HttpRequest, HttpPipelineNextPolicy)} method with
     * {@link CompletableFuture#completedFuture(Object)} if the request is successful. If the request completes
     * exceptionally a {@link CompletableFuture} failed with {@link CompletableFuture#completeExceptionally(Throwable)}
     * is returned instead.
     *
     * @param httpRequest The HTTP request.
     * @param next The next policy to invoke.
     * @return A {@link CompletableFuture} that completes with the {@link Response} from the next policy or the HTTP
     * client if there are no more policies.
     */
    default CompletableFuture<Response<BinaryData>> processAsync(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        try {
            return CompletableFuture.completedFuture(process(httpRequest, next));
        } catch (Exception e) {
            CompletableFuture<Response<BinaryData>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
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
