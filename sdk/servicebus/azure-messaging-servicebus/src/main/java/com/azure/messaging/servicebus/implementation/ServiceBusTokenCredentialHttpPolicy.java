// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * Token credential policy for authenticating with service bus.
 */
public class ServiceBusTokenCredentialHttpPolicy implements HttpPipelinePolicy {
    private final TokenCredential tokenCredential;

    /**
     * Creates an instance that authorizes with the tokenCredential.
     *
     * @param tokenCredential Credential to get access token.
     */
    public ServiceBusTokenCredentialHttpPolicy(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    /**
     * Adds the authorization header to a Service Bus management request.
     *
     * @param context HTTP request context.
     * @param next The next HTTP policy in the pipeline.
     *
     * @return A mono that completes with the HTTP response.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final String url = context.getHttpRequest().getUrl().toString();
        return tokenCredential.getToken(new TokenRequestContext().addScopes(url)).flatMap(token -> {
            context.getHttpRequest().getHeaders().put("Authorization", token.getToken());
            return next.process();
        });
    }
}
