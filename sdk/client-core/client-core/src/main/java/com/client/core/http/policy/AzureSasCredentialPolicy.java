// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

import com.client.core.credential.ClientSasCredential;
import com.client.core.http.HttpPipelineCallContext;
import com.client.core.http.HttpPipelineNextPolicy;
import com.client.core.http.HttpPipelineNextSyncPolicy;
import com.client.core.http.HttpRequest;
import com.client.core.http.HttpResponse;
import com.client.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Pipeline policy that uses an {@link ClientSasCredential} to set the shared access signature for a request.
 */
public final class ClientSasCredentialPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ClientSasCredentialPolicy.class);
    private final ClientSasCredential credential;
    private final boolean requireHttps;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            HttpRequest httpRequest = context.getHttpRequest();
            if (requireHttps && "http".equals(httpRequest.getUrl().getProtocol())) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "Shared access signature credentials require HTTPS to prevent leaking"
                        + " the shared access signature."));
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
        }
    };

    /**
     * Creates a policy that uses the passed {@link ClientSasCredential} to append sas to query string.
     * <p>
     * Requests sent with this pipeline policy are required to use {@code HTTPS}.
     * If the request isn't using {@code HTTPS}
     * an exception will be thrown to prevent leaking the shared access signature.
     *
     * @param credential The {@link ClientSasCredential} containing the shared access signature to use.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ClientSasCredentialPolicy(ClientSasCredential credential) {
        this(credential, true);
    }

    /**
     * Creates a policy that uses the passed {@link ClientSasCredential} to append sas to query string.
     *
     * @param credential The {@link ClientSasCredential} containing the shared access signature to use.
     * @param requireHttps A flag indicating whether {@code HTTPS} is required.
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ClientSasCredentialPolicy(ClientSasCredential credential, boolean requireHttps) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credential = credential;
        this.requireHttps = requireHttps;
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
