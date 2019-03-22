// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.common.credentials.AsyncServiceClientCredentials;
import com.azure.common.credentials.ServiceClientCredentials;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.policy.HttpPipelinePolicy;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Creates a addPolicy that adds the required requests with Azure Application Configuration service.
 */
final class ConfigurationCredentialsPolicy implements HttpPipelinePolicy {
    private static final String KEY_VALUE_APPLICATION_HEADER = "application/vnd.microsoft.azconfig.kv+json";

    static final String HOST_HEADER = "Host";
    static final String DATE_HEADER = "x-ms-date";
    static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String ACCEPT_HEADER = "Accept";

    private final Logger logger = LoggerFactory.getLogger(ConfigurationCredentialsPolicy.class);

    /**
     * Sign the request.
     *
     * @param context The request context
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
                        return null;
                    }
                }, (messageDigest, byteBuffer) -> {
                        if (messageDigest != null) {
                            messageDigest.update(byteBuffer.nioBuffer());
                        }
                    })
                .flatMap(messageDigest -> {
                    if (messageDigest == null) {
                        return Mono.error(new NoSuchAlgorithmException("Unable to locate SHA-256 algorithm."));
                    }

                    final Map<String, String> mapped = getDefaultHeaders(context.httpRequest().url(), context.httpRequest().headers());
                    final String contentHash = Base64.getEncoder().encodeToString(messageDigest.digest());

                    mapped.put(CONTENT_HASH_HEADER, contentHash);
                    mapped.forEach((key, value) -> context.httpRequest().headers().set(key, value));

                    return next.process().doOnSuccess(this::logResponseDelegate);
                });
    }

    private Map<String, String> getDefaultHeaders(URL url, HttpHeaders currentHeaders) {
        final Map<String, String> mapped = new HashMap<>();

        mapped.put(HOST_HEADER, url.getHost());
        mapped.put(CONTENT_TYPE_HEADER, KEY_VALUE_APPLICATION_HEADER);
        mapped.put(ACCEPT_HEADER, KEY_VALUE_APPLICATION_HEADER);

        if (currentHeaders.value(DATE_HEADER) == null) {
            String utcNow = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            mapped.put(DATE_HEADER, utcNow);
        }

        return mapped;
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

