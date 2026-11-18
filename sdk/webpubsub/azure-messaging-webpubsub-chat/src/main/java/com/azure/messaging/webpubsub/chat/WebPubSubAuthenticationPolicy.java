// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * An {@link HttpPipelinePolicy} for authenticating requests to the Azure Web PubSub Chat service.
 */
public final class WebPubSubAuthenticationPolicy implements HttpPipelinePolicy {
    private static final Duration DEFAULT_EXPIRATION = Duration.ofHours(1);

    private final AzureKeyCredential credential;

    /**
     * Creates a policy that authenticates requests using the supplied credential.
     *
     * @param credential The credential used to authenticate outgoing requests.
     */
    public WebPubSubAuthenticationPolicy(AzureKeyCredential credential) {
        this.credential = credential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return Mono.fromRunnable(() -> {
            String audience = context.getHttpRequest().getUrl().toString();
            String token = WebPubSubTokenGenerator.generateToken(audience, null, null, DEFAULT_EXPIRATION, credential);
            if (token != null) {
                context.getHttpRequest().setHeader(HttpHeaderName.AUTHORIZATION, "Bearer " + token);
            }
        }).then(next.process());
    }
}
