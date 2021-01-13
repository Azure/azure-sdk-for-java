// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.common.CommunicationUserCredential;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * HttpPipelinePolicy policy that uses a token to set the credential key for a request.
 * <p>
 * Requests sent with this pipeline policy are required to use {@code HTTPS}. If the request isn't using {@code HTTPS}
 * an exception will be thrown to prevent leaking the key.
 */
public final class ChatUserCredentialPolicy implements HttpPipelinePolicy {
    private final CommunicationUserCredential credential;

    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE_FORMAT = "Bearer %s";

    /**
     * Creates a policy that uses the passed token to set the specified header name.
     *
     * @param credential The token containing the authorization key to use.
     * @throws NullPointerException If {@code name} or {@code credential} is {@code null}.
     * @throws IllegalArgumentException If {@code name} is empty.
     */
    public ChatUserCredentialPolicy(CommunicationUserCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");

        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new IllegalStateException("Key credentials require HTTPS to prevent leaking the key."));
        }

        try {
            AccessToken token = credential.getToken().get();
            context.getHttpRequest().setHeader(
                AUTHORIZATION_HEADER_KEY,
                String.format(AUTHORIZATION_HEADER_VALUE_FORMAT, token.getToken()));
        } catch (InterruptedException ex) {
            return Mono.error(ex);
        } catch (ExecutionException ex) {
            return Mono.error(ex);
        }

        return next.process();
    }
}
