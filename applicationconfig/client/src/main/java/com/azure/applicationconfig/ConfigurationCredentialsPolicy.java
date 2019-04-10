// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * A policy that authenticates requests with Azure Application Configuration service. The content added by this policy
 * is leveraged in {@link ConfigurationClientCredentials} to generate the correct Authorization header.
 *
 * @see ConfigurationClientCredentials
 * @see ConfigurationAsyncClient
 * @see ConfigurationAsyncClientBuilder
 */
public final class ConfigurationCredentialsPolicy implements HttpPipelinePolicy {
    static final String HOST_HEADER = "Host";
    static final String DATE_HEADER = "Date";
    static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";

    /**
     * Adds the required headers to authenticate a request to Azure Application Configuration service.
     *
     * @param context The request context
     * @param next The next HTTP pipeline policy to process the {@code context's} request after this policy completes.
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final Flux<ByteBuf> contents = context.httpRequest().body() == null
                ? Flux.just(getEmptyBuffer())
                : context.httpRequest().body();

        return contents.defaultIfEmpty(getEmptyBuffer())
                .collect(() -> {
                    try {
                        return MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException e) {
                        throw Exceptions.propagate(e);
                    }
                }, (messageDigest, byteBuffer) -> {
                        if (messageDigest != null) {
                            messageDigest.update(byteBuffer.nioBuffer());
                        }
                    })
                .flatMap(messageDigest -> {
                    final HttpHeaders headers = context.httpRequest().headers();
                    final String contentHash = Base64.getEncoder().encodeToString(messageDigest.digest());

                    // All three of these headers are used by ConfigurationClientCredentials to generate the
                    // Authentication header value. So, we need to ensure that they exist.
                    headers.set(HOST_HEADER, context.httpRequest().url().getHost());
                    headers.set(CONTENT_HASH_HEADER, contentHash);

                    if (headers.value(DATE_HEADER) == null) {
                        String utcNow = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
                        headers.set(DATE_HEADER, utcNow);
                    }

                    return next.process();
                });
    }

    private ByteBuf getEmptyBuffer() {
        return new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT);
    }
}

