// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPipelineSyncPolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link NonAzureOpenAIKeyCredential} to set the authorization key for a request to
 * an HTTP request with "Bearer" scheme.
 * <p>
 * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.
 */
public class NonAzureOpenAIKeyCredentialPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(NonAzureOpenAIKeyCredentialPolicy.class);
    private static final String BEARER = "Bearer";
    private final HttpHeaderName name;
    private final NonAzureOpenAIKeyCredential credential;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
            }

            context.getHttpRequest().setHeader(name, BEARER + " " + credential.getKey());
        }
    };

    /**
     * Creates a policy that uses the passed {@link NonAzureOpenAIKeyCredential} to set the specified header name.
     *
     * @param credential The {@link AzureKeyCredential} containing the authorization key to use.
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public NonAzureOpenAIKeyCredentialPolicy(NonAzureOpenAIKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.name = HttpHeaderName.AUTHORIZATION;
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return inner.process(context, next);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return inner.processSync(context, next);
    }
}
