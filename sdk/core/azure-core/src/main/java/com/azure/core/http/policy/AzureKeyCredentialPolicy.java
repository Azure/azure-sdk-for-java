// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link AzureKeyCredential} to set the authorization key for a request.
 * <p>
 * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.
 */
public final class AzureKeyCredentialPolicy implements HttpPipelinePolicy {
    // AzureKeyCredentialPolicy can be a commonly used policy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(AzureKeyCredentialPolicy.class);

    private final String name;
    private final AzureKeyCredential credential;
    private final HttpPipelineSynchronousPolicy inner = new HttpPipelineSynchronousPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
            }

            context.getHttpRequest().setHeader(name, credential.getKey());
        }
    };

    /**
     * Creates a policy that uses the passed {@link AzureKeyCredential} to set the specified header name.
     *
     * @param name The name of the key header that will be set to {@link AzureKeyCredential#getKey()}.
     * @param credential The {@link AzureKeyCredential} containing the authorization key to use.
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public AzureKeyCredentialPolicy(String name, AzureKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        Objects.requireNonNull(name, "'name' cannot be null.");
        if (name.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty."));
        }

        this.name = name;
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
