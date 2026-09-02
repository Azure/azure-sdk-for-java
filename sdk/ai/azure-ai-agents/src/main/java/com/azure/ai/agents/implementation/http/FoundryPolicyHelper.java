// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.http;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for adding AI Foundry-specific policies to Azure Core {@link HttpPipeline HttpPipelines}.
 */
public final class FoundryPolicyHelper {

    private static final HttpHeaderName FOUNDRY_FEATURES = HttpHeaderName.fromString("Foundry-Features");
    private static final String CLIENT_SDK_QUERY_PARAMETER = "x-ms-client-sdk";

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
     * Creates a policy that adds the SDK identifier to the request URL when it isn't already present.
     *
     * @param userAgent The SDK user agent to add as the {@code x-ms-client-sdk} query parameter.
     * @return A policy that adds the SDK identifier, or {@code null} if {@code userAgent} is empty.
     */
    public static HttpPipelinePolicy createClientSdkQueryPolicy(String userAgent) {
        return CoreUtils.isNullOrEmpty(userAgent) ? null : new ClientSdkQueryPolicy(userAgent);
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

    private static final class ClientSdkQueryPolicy implements HttpPipelinePolicy {

        private final String encodedUserAgent;

        private ClientSdkQueryPolicy(String userAgent) {
            try {
                this.encodedUserAgent = URLEncoder.encode(userAgent, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("UTF-8 encoding is not supported.", e);
            }
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            addClientSdkQueryParameter(context);
            return next.process();
        }

        @Override
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            addClientSdkQueryParameter(context);
            return next.processSync();
        }

        private void addClientSdkQueryParameter(HttpPipelineCallContext context) {
            UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            if (urlBuilder.getQuery().containsKey(CLIENT_SDK_QUERY_PARAMETER)) {
                return;
            }

            try {
                context.getHttpRequest()
                    .setUrl(urlBuilder.setQueryParameter(CLIENT_SDK_QUERY_PARAMETER, encodedUserAgent).toUrl());
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Failed to add the SDK identifier to the request URL.", e);
            }
        }
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
