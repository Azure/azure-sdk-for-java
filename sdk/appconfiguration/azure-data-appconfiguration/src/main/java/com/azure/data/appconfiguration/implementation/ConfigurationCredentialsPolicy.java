// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A policy that authenticates requests with Azure App Configuration service. The content added by this policy
 * is leveraged in {@link ConfigurationClientCredentials} to generate the correct "Authorization" header value.
 *
 * @see ConfigurationClientCredentials
 * @see ConfigurationAsyncClient
 * @see ConfigurationClientBuilder
 */
public final class ConfigurationCredentialsPolicy implements HttpPipelinePolicy {
    // "Host", "Date", and "x-ms-content-sha256" are required to generate "Authorization" value in
    // ConfigurationClientCredentials.

    private final ConfigurationClientCredentials credentials;

    /**
     * Creates an instance that is able to apply a {@link ConfigurationClientCredentials} credential to a request in the
     * pipeline.
     *
     * @param credentials the credential information to authenticate to Azure App Configuration service
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public ConfigurationCredentialsPolicy(ConfigurationClientCredentials credentials) {
        Objects.requireNonNull(credentials, "'credential' can not be a null value.");
        this.credentials = credentials;
    }

    /**
     * Adds the required headers to authenticate a request to Azure App Configuration service.
     *
     * @param context The request context
     * @param next The next HTTP pipeline policy to process the {@code context's} request after this policy
     *     completes.
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final Flux<ByteBuffer> contents = context.getHttpRequest().getBody() == null
            ? Flux.just(getEmptyBuffer())
            : context.getHttpRequest().getBody();

        return credentials
            .getAuthorizationHeadersAsync(
                context.getHttpRequest().getUrl(),
                context.getHttpRequest().getHttpMethod().toString(),
                contents.defaultIfEmpty(getEmptyBuffer()))
            .flatMapMany(headers -> Flux.fromIterable(headers.entrySet()))
            .map(header -> context.getHttpRequest().setHeader(header.getKey(), header.getValue()))
            .last()
            .flatMap(request -> next.process());
    }

    private ByteBuffer getEmptyBuffer() {
        return ByteBuffer.allocate(0);
    }
}

