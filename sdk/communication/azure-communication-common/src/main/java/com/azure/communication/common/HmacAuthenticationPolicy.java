// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import java.nio.ByteBuffer;
import java.util.Objects;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * HttpPipelinePolicy to append CommunicationClient required headers
 */
public final class HmacAuthenticationPolicy implements HttpPipelinePolicy {
    private final CommunicationClientCredential credential;

    /**
     * Created with a non-null client credential
     * @param clientCredential client credential with valid access key
     */
    public HmacAuthenticationPolicy(CommunicationClientCredential clientCredential) {
        Objects.requireNonNull(clientCredential, "'clientCredential' cannot be a null value.");
        credential = clientCredential;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final Flux<ByteBuffer> contents = context.getHttpRequest().getBody() == null
            ? Flux.just(ByteBuffer.allocate(0))
            : context.getHttpRequest().getBody();

        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(
                new RuntimeException("CommunicationClientCredential requires a URL using the HTTPS protocol scheme"));
        }

        try {            
            return credential
                .appendAuthorizationHeaders(
                    context.getHttpRequest().getUrl(),
                    context.getHttpRequest().getHttpMethod().toString(),
                    contents)
                .flatMap(headers -> {
                    headers.entrySet().forEach(
                        header -> context.getHttpRequest().setHeader(header.getKey(), header.getValue()));
                
                    return next.process();
                });
        } catch (RuntimeException r) {
            return Mono.error(r);
        }
    }
    
}
