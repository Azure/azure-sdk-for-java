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
}
