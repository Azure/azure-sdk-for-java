// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;

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
     *
     * @return A publisher that initiates the request upon subscription and emits a response on completion.
     */
    Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next);

    /**
     * Gets the position in the {@link HttpPipelineBuilder} the policy will be placed when added.
     * <p>
     * Policy order does not need to be unique. When multiple polices with the same {@link HttpPipelineOrder} are added
     * they will be handled based on the documentation of {@link HttpPipelineOrder}.
     * <p>
     * By default, this method returns {@link HttpPipelineOrder#BETWEEN_RETRY_AND_AUTHENTICATION}.
     * <p>
     * If this method returns null, an exception will be thrown when it is added to the {@link HttpPipelineBuilder}.
     *
     * @return The order of this policy.
     */
    default HttpPipelineOrder getOrder() {
        return HttpPipelineOrder.BETWEEN_RETRY_AND_AUTHENTICATION;
    }
}
