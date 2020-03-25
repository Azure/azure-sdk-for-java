// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link AzureKeyCredential} to set the specified header which authorizes requests.
 *
 * <p>
 * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.
 */
public final class AzureKeyCredentialPolicy implements HttpPipelinePolicy {
    private final ClientLogger logger = new ClientLogger(AzureKeyCredentialPolicy.class);

    private final String header;
    private final AzureKeyCredential credential;

    /**
     * Creates a policy that uses the passed {@link AzureKeyCredential} to set the specified header.
     *
     * @param header The header that will be set to the {@link AzureKeyCredential} key value.
     * @param credential The {@link AzureKeyCredential} containing the authorization key to use.
     * @throws NullPointerException If {@code header} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code header} is empty.
     */
    public AzureKeyCredentialPolicy(String header, AzureKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        Objects.requireNonNull(header, "'header' cannot be null.");
        if (header.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'header' cannot be empty."));
        }

        this.header = header;
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
        }

        context.getHttpRequest().setHeader(header, credential.getKey());
        return next.process();
    }
}
