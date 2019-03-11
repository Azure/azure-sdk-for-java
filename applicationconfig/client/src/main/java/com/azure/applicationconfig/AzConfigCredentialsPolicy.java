// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.microsoft.rest.v3.http.HttpHeaders;
import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpPipelineLogLevel;
import com.microsoft.rest.v3.http.HttpPipelineNextPolicy;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Creates a policy that authenticates request with AzConfig service.
 */
public final class AzConfigCredentialsPolicy implements HttpPipelinePolicy {
    private static final String SIGNED_HEADERS = "host;x-ms-date;x-ms-content-sha256";
    private static final String KEY_VALUE_APPLICATION_HEADER = "application/vnd.microsoft.azconfig.kv+json";

    private static final String HOST_HEADER = "Host";
    private static final String DATE_HEADER = "x-ms-date";
    private static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String ACCEPT_HEADER = "Accept";

    private final AzConfigClient.AzConfigCredentials credentials;
    private final Logger logger = LoggerFactory.getLogger(AzConfigCredentialsPolicy.class);

    /**
     * Initializes a new instance of AzConfigCredentialsPolicy based on credentials.
     *
     * @param credentials for the Configuration Store in Azure
     */
    AzConfigCredentialsPolicy(AzConfigClient.AzConfigCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Sign the request.
     *
     * @param context The request context
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        Map<String, String> mapped = new HashMap<>();
        mapped.put(HOST_HEADER, credentials.baseUri().getHost());
        mapped.put(CONTENT_TYPE_HEADER, KEY_VALUE_APPLICATION_HEADER);
        mapped.put(ACCEPT_HEADER, KEY_VALUE_APPLICATION_HEADER);

        if (context.httpRequest().headers().value(DATE_HEADER) == null) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            String utcNowString = now.format(DateTimeFormatter.RFC_1123_DATE_TIME);
            mapped.put(DATE_HEADER, utcNowString);
        }

        mapped.forEach((key, value) -> context.httpRequest().headers().set(key, value));

        Flux<ByteBuf> contents = context.httpRequest().body() == null
                ? Flux.just(new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT))
                : context.httpRequest().body();

        return contents.defaultIfEmpty(new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT))
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
                .flatMap(messageDigest -> messageDigest == null
                        ? Mono.error(new NoSuchAlgorithmException("Unable to locate SHA-256 algorithm."))
                        : Mono.just(Base64.getEncoder().encodeToString(messageDigest.digest()))
                )
                .flatMap(contentHash -> {
                    context.httpRequest().headers().set(CONTENT_HASH_HEADER, contentHash);

                    final String stringToSign = buildStringToSign(context.httpRequest());

                    Mac sha256HMAC;
                    try {
                        sha256HMAC = Mac.getInstance("HmacSHA256");
                        SecretKeySpec secretKey = new SecretKeySpec(credentials.secret(), "HmacSHA256");
                        sha256HMAC.init(secretKey);

                        String signature = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
                        String signedString = String.format("HMAC-SHA256 Credential=%s, SignedHeaders=%s, Signature=%s", credentials.credential(), SIGNED_HEADERS, signature);
                        context.httpRequest().headers().set(AUTHORIZATION_HEADER, signedString);
                    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                        return Mono.error(e);
                    }

                    return next.process().doOnSuccess(httpResponse -> {
                        Objects.requireNonNull(httpResponse, "HttpResponse is required.");

                        if (httpResponse.statusCode() == HttpResponseStatus.UNAUTHORIZED.code()) {
                            logger.error("HTTP Unauthorized status, String-to-Sign:'{}'", stringToSign);
                        }
                    });
                });
    }

    /**
     * Constructs a string for signing a request.
     *
     * @param request The request to sign
     * @return constructed string
     */
    private String buildStringToSign(final HttpRequest request) {
        try {
            HttpHeaders httpHeaders = request.headers();
            String verb = request.httpMethod().toString().toUpperCase();
            String pathAndQuery = request.url().getPath();
            if (request.url().getQuery() != null) {
                pathAndQuery += '?' + request.url().getQuery();
            }
            String utcNowString = httpHeaders.value(DATE_HEADER);
            String contentHash = httpHeaders.value(CONTENT_HASH_HEADER);
            // String-To-Sign=HTTP_METHOD + '\n' + path_and_query + '\n' + signed_headers_values
            // Signed headers: "host;x-ms-date;x-ms-content-sha256"
            return String.format("%s\n%s\n%s;%s;%s", verb, pathAndQuery, credentials.baseUri().getHost(), utcNowString, contentHash);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}

