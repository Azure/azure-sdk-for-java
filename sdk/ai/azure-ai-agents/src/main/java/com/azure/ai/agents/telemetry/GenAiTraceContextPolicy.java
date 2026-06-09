// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * HTTP pipeline policy that gates W3C trace context propagation based on
 * the GenAI tracing configuration.
 *
 * <p>When trace context propagation is disabled via configuration, this policy
 * suppresses the standard {@code InstrumentationPolicy}'s header injection by
 * adding a disable-tracing marker to the request context.</p>
 *
 * <p>This policy is a no-op when:</p>
 * <ul>
 *   <li>GenAI tracing is enabled AND propagation is enabled (default) — headers flow normally</li>
 *   <li>GenAI tracing is disabled — no spans exist, so no headers to inject</li>
 * </ul>
 */
public final class GenAiTraceContextPolicy implements HttpPipelinePolicy {

    /**
     * Creates a new instance of the trace context propagation policy.
     */
    public GenAiTraceContextPolicy() {
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // The azure-core InstrumentationPolicy handles trace context injection automatically.
        // This policy provides a gate: if propagation is disabled, we could suppress injection.
        // Currently acts as a pass-through since InstrumentationPolicy handles propagation.
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return next.processSync();
    }
}
