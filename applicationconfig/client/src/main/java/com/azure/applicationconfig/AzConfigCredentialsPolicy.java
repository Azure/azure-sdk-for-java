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
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Creates a policy that authenticates request with AzConfig service.
 */
public final class AzConfigCredentialsPolicy implements HttpPipelinePolicy {
    private static final String KEY_VALUE_APPLICATION_HEADER = "application/vnd.microsoft.azconfig.kv+json";

    private static final String HOST_HEADER = "Host";
    private static final String DATE_HEADER = "x-ms-date";
    private static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String ACCEPT_HEADER = "Accept";

    private final AzConfigClient.AzConfigCredentials credentials;
    private final AuthorizationHeaderProvider provider = new AuthorizationHeaderProvider();
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
            String utcNow = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            mapped.put(DATE_HEADER, utcNow);
        }

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
                    mapped.put(CONTENT_HASH_HEADER, contentHash);
                    mapped.forEach((key, value) -> context.httpRequest().headers().set(key, value));

                    try {
                        String signedString = provider.getAuthenticationHeaderValue(context.httpRequest());
                        context.httpRequest().headers().set(AUTHORIZATION_HEADER, signedString);
                    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                        return Mono.error(e);
                    }

                    return next.process().doOnSuccess(httpResponse -> {
                        Objects.requireNonNull(httpResponse, "HttpResponse is required.");

                        if (httpResponse.statusCode() == HttpResponseStatus.UNAUTHORIZED.code()) {
                            logger.error("HTTP Unauthorized status, String-to-Sign:'{}'",
                                    httpResponse.headers().value(AUTHORIZATION_HEADER));
                        }
                    });
                });
    }

    private class AuthorizationHeaderProvider {
        private final String[] signedHeaders = new String[]{HOST_HEADER, DATE_HEADER, CONTENT_HASH_HEADER};
        private final String signedHeadersValue = String.join(";", signedHeaders);

        private String getAuthenticationHeaderValue(final HttpRequest request) throws NoSuchAlgorithmException, InvalidKeyException {
            final String stringToSign = provider.getStringToSign(request);

            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(credentials.secret(), "HmacSHA256");
            sha256HMAC.init(secretKey);

            String signature = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            return String.format("HMAC-SHA256 Credential=%s, SignedHeaders=%s, Signature=%s",
                    credentials.credential(),
                    signedHeadersValue,
                    signature);
        }

        private String getStringToSign(final HttpRequest request) {
            String pathAndQuery = request.url().getPath();
            if (request.url().getQuery() != null) {
                pathAndQuery += '?' + request.url().getQuery();
            }

            HttpHeaders httpHeaders = request.headers();
            String signed = Arrays.stream(signedHeaders)
                    .map(httpHeaders::value)
                    .collect(Collectors.joining(";"));

            // String-To-Sign=HTTP_METHOD + '\n' + path_and_query + '\n' + signed_headers_values
            // Signed headers: "host;x-ms-date;x-ms-content-sha256"
            return String.format("%s\n%s\n%s",
                    request.httpMethod().toString().toUpperCase(),
                    pathAndQuery,
                    signed);
        }
    }
}

