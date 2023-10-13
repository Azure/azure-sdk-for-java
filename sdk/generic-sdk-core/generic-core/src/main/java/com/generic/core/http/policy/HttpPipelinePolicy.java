// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.HttpPipeline;
import com.generic.core.http.HttpPipelineCallContext;
import com.generic.core.http.HttpPipelineNextSyncPolicy;
import com.generic.core.http.HttpResponse;

/**
 * A policy within the {@link HttpPipeline}.
 *
 * @see HttpPipeline
 */
@FunctionalInterface
public interface HttpPipelinePolicy {

    /**
     * Processes provided request context and invokes the next policy synchronously.
     *
     * @param context The request context.
     * @param next The next policy to invoke.
     * @return A publisher that initiates the request upon subscription and emits a response on completion.
     */
    HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next);
}
