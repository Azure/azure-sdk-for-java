// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.FormRecognizerApiKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * Policy that adds the {@link FormRecognizerApiKeyCredential} into the request's `Ocp-Apim-Subscription-Key`
 * header.
 */
public final class ApiKeyCredentialPolicy implements HttpPipelinePolicy {
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private final FormRecognizerApiKeyCredential credential;

    /**
     * Creates a {@link ApiKeyCredentialPolicy} pipeline policy that adds the
     * {@link FormRecognizerApiKeyCredential} into the request's `Ocp-Apim-Subscription-Key` header.
     *
     * @param credential the {@link FormRecognizerApiKeyCredential} credential used to create the policy.
     */
    public ApiKeyCredentialPolicy(FormRecognizerApiKeyCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.getHttpRequest().setHeader(OCP_APIM_SUBSCRIPTION_KEY, credential.getApiKey());
        return next.process();
    }
}
