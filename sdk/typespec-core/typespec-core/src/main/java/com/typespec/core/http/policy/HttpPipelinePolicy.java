// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineCallContext;
import com.typespec.core.http.HttpPipelineNextPolicy;
import com.typespec.core.http.HttpPipelineNextSyncPolicy;
import com.typespec.core.http.HttpPipelinePosition;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.implementation.http.HttpPipelineNextSyncPolicyHelper;
import reactor.core.publisher.Mono;

/**
 * A policy within the {@link HttpPipeline}.
 *
 * @see HttpPipeline
 */
@FunctionalInterface
public interface HttpPipelinePolicy {
    /**
     * Processes provided request context and invokes the next policy.
     *
     * @param context The request context.
     * @param next The next policy to invoke.
     * @return A publisher that initiates the request upon subscription and emits a response on completion.
     */
    Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next);

    /**
     * Processes provided request context and invokes the next policy synchronously.
     *
     * @param context The request context.
     * @param next The next policy to invoke.
     * @return A publisher that initiates the request upon subscription and emits a response on completion.
     */
    default HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return process(context, HttpPipelineNextSyncPolicyHelper.toAsyncPolicy(next)).block();
    }

    /**
     * Gets the position to place the policy.
     * <p>
     * By default pipeline policies are positioned {@link HttpPipelinePosition#PER_RETRY}.
     *
     * @return The position to place the policy.
     */
    default HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_RETRY;
    }
}
