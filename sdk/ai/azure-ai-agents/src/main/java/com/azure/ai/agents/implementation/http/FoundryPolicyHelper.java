// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for adding AI Foundry-specific policies to Azure Core {@link HttpPipeline HttpPipelines}.
 */
public final class FoundryPolicyHelper {

    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");

    private FoundryPolicyHelper() {
    }

    /**
     * Creates a policy that adds the {@code Foundry-Features} header when it isn't already present on the request.
     *
     * @param foundryFeatures The {@code Foundry-Features} header value to add.
     * @return A policy that adds the requested Foundry features, or {@code null} if {@code foundryFeatures} is empty.
     */
    public static HttpPipelinePolicy createFoundryFeaturesPolicy(String foundryFeatures) {
        return CoreUtils.isNullOrEmpty(foundryFeatures) ? null : new FoundryFeaturesPolicy(foundryFeatures);
    }

    /**
     * Creates a new pipeline with {@code policy} prepended to the existing pipeline policies.
     * <p>
     * {@link HttpPipeline} instances are immutable once built. This method doesn't mutate the supplied pipeline.
     * Instead, it creates a new pipeline that reuses the same HTTP client, tracer, and policy instances from the
     * original pipeline, with the additional policy inserted at the beginning.
     * <p>
     * Individual policy instances aren't cloned because Azure Core policies don't expose a cloning contract.
     *
     * @param pipeline The pipeline to copy.
     * @param policy The policy to prepend. If {@code null}, the original pipeline is returned unchanged.
     * @return A pipeline with {@code policy} prepended, or the original pipeline if {@code policy} is {@code null}.
     */
    public static HttpPipeline prependPolicy(HttpPipeline pipeline, HttpPipelinePolicy policy) {
        if (policy == null) {
            return pipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>(pipeline.getPolicyCount() + 1);
        policies.add(policy);
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            policies.add(pipeline.getPolicy(i));
        }

        return new HttpPipelineBuilder().httpClient(pipeline.getHttpClient())
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .tracer(pipeline.getTracer())
            .build();
    }

    private static final class FoundryFeaturesPolicy implements HttpPipelinePolicy {

        private final String foundryFeatures;

        private FoundryFeaturesPolicy(String foundryFeatures) {
            this.foundryFeatures = foundryFeatures;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            if (CoreUtils.isNullOrEmpty(context.getHttpRequest().getHeaders().getValue(FOUNDRY_FEATURES))) {
                context.getHttpRequest().getHeaders().set(FOUNDRY_FEATURES, foundryFeatures);
            }
            return next.process();
        }
    }
}
