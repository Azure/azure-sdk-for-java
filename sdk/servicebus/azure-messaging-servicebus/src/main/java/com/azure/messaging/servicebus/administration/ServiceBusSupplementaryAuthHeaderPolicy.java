// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusSharedKeyCredential;
import reactor.core.publisher.Mono;

import java.net.URL;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME;

/**
 * Authentication policy to add necessary supplementary auth headers when forwarding is set.
 */
public final class ServiceBusSupplementaryAuthHeaderPolicy implements HttpPipelinePolicy {
    private final TokenCredential tokenCredential;

    /**
     * Creates an instance that adds necessary supplementary authentication headers.
     *
     * @param tokenCredential Credential to get access token.
     */
    public ServiceBusSupplementaryAuthHeaderPolicy(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    /**
     * Add the additional authentication token needed for various types of forwarding options.
     *
     * @param context HTTP request context.
     * @param next The next HTTP policy in the pipeline.
     *
     * @return A mono that completes with the HTTP response.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final HttpHeaders headers = context.getHttpRequest().getHeaders();
        if (headers.get(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME) != null
            || headers.get(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME) != null) {
            final String scope;
            if (this.tokenCredential instanceof ServiceBusSharedKeyCredential) {
                final URL url = context.getHttpRequest().getUrl();
                scope = String.format("%s://%s", url.getProtocol(), url.getHost());
            } else {
                scope = ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE;
            }
            return tokenCredential.getToken(new TokenRequestContext().addScopes(scope)).flatMap(token -> {
                if (headers.get(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME) != null) {
                    headers.set(SERVICE_BUS_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME, token.getToken());
                }
                if (headers.get(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME) != null) {
                    headers.set(SERVICE_BUS_DLQ_SUPPLEMENTARY_AUTHORIZATION_HEADER_NAME, token.getToken());
                }
                return next.process();
            });
        }
        return next.process();
    }
}
