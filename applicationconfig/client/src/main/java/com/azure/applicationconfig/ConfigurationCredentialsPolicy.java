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
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

/**
 * A policy that authenticates requests with Azure Application Configuration service.
 *
 * @see ConfigurationAsyncClient
 * @see ConfigurationClientBuilder
 */
public final class ConfigurationCredentialsPolicy implements HttpPipelinePolicy {
    private static final String KEY_VALUE_APPLICATION_HEADER = "application/vnd.microsoft.azconfig.kv+json";

    static final String HOST_HEADER = "Host";
    static final String DATE_HEADER = "x-ms-date";
    static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String ACCEPT_HEADER = "Accept";

    private final Logger logger = LoggerFactory.getLogger(ConfigurationCredentialsPolicy.class);

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

                    headers.set(CONTENT_HASH_HEADER, contentHash);
                    headers.set(HOST_HEADER, context.httpRequest().url().getHost());
                    headers.set(CONTENT_TYPE_HEADER, KEY_VALUE_APPLICATION_HEADER);
                    headers.set(ACCEPT_HEADER, KEY_VALUE_APPLICATION_HEADER);

                    if (headers.value(DATE_HEADER) == null) {
                        String utcNow = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
                        headers.set(DATE_HEADER, utcNow);
                    }

                    return next.process().doOnSuccess(this::logResponseDelegate);
                });
    }

    private ByteBuf getEmptyBuffer() {
        return new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT);
    }

    private void logResponseDelegate(HttpResponse response) {
        Objects.requireNonNull(response, "HttpResponse is required.");

        if (response.statusCode() == HttpResponseStatus.UNAUTHORIZED.code()) {
            logger.error("HTTP Unauthorized status, String-to-Sign:'{}'",
                    response.headers().value(AUTHORIZATION_HEADER));
        }
    }
}

