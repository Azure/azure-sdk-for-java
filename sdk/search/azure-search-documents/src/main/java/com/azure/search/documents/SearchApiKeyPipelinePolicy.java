// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link AzureKeyCredential} to set the {@code api-key} header which authorizes requests
 * sent to the Azure Search service.
 *
 * <p>
 * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.
 */
public final class SearchApiKeyPipelinePolicy implements HttpPipelinePolicy {
    private static final String API_KEY = "api-key";

    private final AzureKeyCredential credential;

    /**
     * Creates a pipeline policy that uses the passed {@link AzureKeyCredential} used to authorize requests sent to the
     * Azure Search service.
     *
     * @param credential An {@link AzureKeyCredential} containing the key used to authorize Search service requests.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public SearchApiKeyPipelinePolicy(AzureKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
        }

        context.getHttpRequest().setHeader(API_KEY, credential.getKey());
        return next.process();
    }
}
