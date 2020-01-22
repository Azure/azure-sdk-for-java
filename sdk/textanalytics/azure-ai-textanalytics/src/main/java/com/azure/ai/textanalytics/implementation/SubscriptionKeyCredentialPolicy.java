// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.ai.textanalytics.models.TextAnalyticsSubscriptionKeyCredential;
import reactor.core.publisher.Mono;

/**
 * Policy that adds the {@link TextAnalyticsSubscriptionKeyCredential} into the request's `Ocp-Apim-Subscription-Key`
 * header.
 */
public final class SubscriptionKeyCredentialPolicy implements HttpPipelinePolicy {
    private final TextAnalyticsSubscriptionKeyCredential credential;

    /**
     * Creates a {@link SubscriptionKeyCredentialPolicy} pipeline policy that adds the SharedKeyCredential into the
     * request's `Ocp-Apim-Subscription-Key` header.
     *
     * @param credential the SharedKeyCredential credential used to create the policy.
     */
    public SubscriptionKeyCredentialPolicy(TextAnalyticsSubscriptionKeyCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getHttpRequest().setHeader("Ocp-Apim-Subscription-Key", credential.getSubscriptionKey());
        return next.process();
    }
}
