// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link AzureSasCredential} to set the shared access signature for a request.
 */
public final class AzureSasCredentialPolicy implements HttpPipelinePolicy {
    private final AzureSasCredential credential;
    private final boolean requireHttps;

    /**
     * Creates a policy that uses the passed {@link AzureSasCredential} to append sas to query string.
     * <p>
     * Requests sent with this pipeline policy are required to use {@code HTTPS}.
     * If the request isn't using {@code HTTPS}
     * an exception will be thrown to prevent leaking the shared access signature.
     *
     * @param credential The {@link AzureSasCredential} containing the shared access signature to use.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public AzureSasCredentialPolicy(AzureSasCredential credential) {
        this(credential, true);
    }

    /**
     * Creates a policy that uses the passed {@link AzureSasCredential} to append sas to query string.
     *
     * @param credential The {@link AzureSasCredential} containing the shared access signature to use.
     * @param requireHttps A flag indicating whether {@code HTTPS} is required.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public AzureSasCredentialPolicy(AzureSasCredential credential, boolean requireHttps) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credential = credential;
        this.requireHttps = requireHttps;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest httpRequest = context.getHttpRequest();
        if (requireHttps && "http".equals(httpRequest.getUrl().getProtocol())) {
            return Mono.error(new IllegalStateException(
                "Shared access signature credentials require HTTPS to prevent leaking the shared access signature."));
        }

        String signature = credential.getSignature();
        if (signature.startsWith("?")) {
            signature = signature.substring(1);
        }

        String query = httpRequest.getUrl().getQuery();
        String url = httpRequest.getUrl().toString();
        if (query == null || query.isEmpty()) {
            if (url.endsWith("?")) {
                url = url + signature;
            } else {
                url = url + "?" + signature;
            }
        } else {
            url = url + "&" + signature;
        }
        httpRequest.setUrl(url);

        return next.process();
    }
}
