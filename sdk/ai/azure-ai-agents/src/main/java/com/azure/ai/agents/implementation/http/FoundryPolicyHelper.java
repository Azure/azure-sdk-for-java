// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for adding AI Foundry-specific policies to Azure Core {@link HttpPipeline HttpPipelines}.
 */
public final class FoundryPolicyHelper {

    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");
    private static final ClientLogger LOGGER = new ClientLogger(FoundryPolicyHelper.class);

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
     * Creates a policy that adds Java preview opt-in guidance to preview-required service errors.
     *
     * @param allowPreview Whether automatic preview opt-in is enabled for the client.
     * @return The error policy, or {@code null} when preview is already enabled.
     */
    public static HttpPipelinePolicy createPreviewErrorPolicy(boolean allowPreview) {
        return allowPreview ? null : new PreviewErrorPolicy();
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
            if (context.getHttpRequest().getHeaders().get(FOUNDRY_FEATURES) == null) {
                context.getHttpRequest().getHeaders().set(FOUNDRY_FEATURES, foundryFeatures);
            }
            return next.process();
        }
    }

    private static final class PreviewErrorPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return next.process().flatMap(response -> {
                if (response.getStatusCode() != 403) {
                    return Mono.just(response);
                }
                HttpResponse bufferedResponse = response.buffer();
                return bufferedResponse.getBodyAsByteArray().flatMap(bytes -> {
                    HttpResponseException exception = previewException(bufferedResponse, bytes);
                    return exception == null
                        ? Mono.just(bufferedResponse)
                        : Mono.error(LOGGER.logExceptionAsError(exception));
                });
            });
        }

        private static HttpResponseException previewException(HttpResponse response, byte[] bytes) {
            Object value;
            try (JsonReader reader = JsonProviders.createReader(bytes)) {
                value = reader.readUntyped();
            } catch (IOException | IllegalStateException exception) {
                return null;
            }
            if (!(value instanceof Map)) {
                return null;
            }
            Object error = ((Map<?, ?>) value).get("error");
            if (!(error instanceof Map)
                || !"preview_feature_required".equals(((Map<?, ?>) error).get("code"))) {
                return null;
            }
            String message = "Status code 403, \"" + new String(bytes, StandardCharsets.UTF_8)
                + "\". To use preview features, configure AgentsClientBuilder.allowPreview(true).";
            return new HttpResponseException(message, response, value);
        }
    }
}
