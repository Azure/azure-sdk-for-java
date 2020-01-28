// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.ai.textanalytics.models.TextAnalyticsApiKeyCredential;
import reactor.core.publisher.Mono;

/**
 * Policy that adds the {@link TextAnalyticsApiKeyCredential} into the request's `Ocp-Apim-Subscription-Key`
 * header.
 */
public final class SubscriptionKeyCredentialPolicy implements HttpPipelinePolicy {
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private final TextAnalyticsApiKeyCredential credential;

    /**
     * Creates a {@link SubscriptionKeyCredentialPolicy} pipeline policy that adds the
     * {@link TextAnalyticsApiKeyCredential} into the request's `Ocp-Apim-Subscription-Key` header.
     *
     * @param credential the {@link TextAnalyticsApiKeyCredential} credential used to create the policy.
     */
    public SubscriptionKeyCredentialPolicy(TextAnalyticsApiKeyCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getHttpRequest().setHeader(OCP_APIM_SUBSCRIPTION_KEY, credential.getSubscriptionKey());
        return next.process();
    }
}
