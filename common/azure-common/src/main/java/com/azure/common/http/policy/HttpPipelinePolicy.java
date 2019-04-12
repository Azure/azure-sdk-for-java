// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http.policy;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * Pipeline policy.
 */
@FunctionalInterface
public interface HttpPipelinePolicy {
    /**
     * Process provided request context and invokes the next policy.
     *
     * @param context request context
     * @param next the next policy to invoke
     * @return publisher that initiate the request upon subscription and emits response on completion.
     */
    Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next);
}
