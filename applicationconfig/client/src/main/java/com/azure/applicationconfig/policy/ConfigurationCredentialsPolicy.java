// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.policy;

import com.azure.applicationconfig.ConfigurationAsyncClient;
import com.azure.applicationconfig.ConfigurationAsyncClientBuilder;
import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * A policy that authenticates requests with Azure Application Configuration service. The content added by this policy
 * is leveraged in {@link ConfigurationClientCredentials} to generate the correct "Authorization" header value.
 *
 * @see ConfigurationClientCredentials
 * @see ConfigurationAsyncClient
 * @see ConfigurationAsyncClientBuilder
 */
public final class ConfigurationCredentialsPolicy implements HttpPipelinePolicy {
    // "Host", "Date", and "x-ms-content-sha256" are required to generate "Authorization" value in
    // ConfigurationClientCredentials.
    private static final String HOST_HEADER = "Host";
    private static final String DATE_HEADER = "Date";
    private static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    private static final String[] SIGNED_HEADERS = new String[]{HOST_HEADER, DATE_HEADER, CONTENT_HASH_HEADER };
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final ConfigurationClientCredentials credentials;
    private final AuthorizationHeaderProvider headerProvider;

    /**
     * Creates an instance that is able to apply a {@link ConfigurationClientCredentials} credential to a request in the pipeline.
     *
     * @param credentials the credential information to authenticate to Azure App Configuration service
     * @throws NoSuchAlgorithmException When the HMAC-SHA256 MAC algorithm cannot be instantiated.
     * @throws InvalidKeyException When the {@code connectionString} secret is invalid and cannot instantiate the HMAC-SHA256 algorithm.
     */
    public ConfigurationCredentialsPolicy(ConfigurationClientCredentials credentials)  {
        this.credentials = credentials;
        this.headerProvider = new AuthorizationHeaderProvider(credentials.id(), credentials.secret());
    }

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
                    headerProvider.addAuthenticationHeaders(context.httpRequest(), messageDigest);
                    return next.process();
                });
    }

    private static class AuthorizationHeaderProvider {
        private final String signedHeadersValue = String.join(";", SIGNED_HEADERS);
        private final Mac sha256HMAC;
        private final String credentialId;

        AuthorizationHeaderProvider(String id, byte[] secret) {
            credentialId = id;
            try {
                sha256HMAC = Mac.getInstance("HmacSHA256");
                sha256HMAC.init(new SecretKeySpec(secret, "HmacSHA256"));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }

        private void addAuthenticationHeaders(final HttpRequest request, final MessageDigest messageDigest) {
            final HttpHeaders headers = request.headers();
            final String contentHash = Base64.getEncoder().encodeToString(messageDigest.digest());

            // All three of these headers are used by ConfigurationClientCredentials to generate the
            // Authentication header value. So, we need to ensure that they exist.
            headers.put(HOST_HEADER, request.url().getHost())
                .put(CONTENT_HASH_HEADER, contentHash);

            if (headers.value(DATE_HEADER) == null) {
                String utcNow = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
                headers.put(DATE_HEADER, utcNow);
            }

            addSignatureHeader(request, headers);
        }

        private void addSignatureHeader(final HttpRequest request, final HttpHeaders headers) {
            String pathAndQuery = request.url().getPath();
            if (request.url().getQuery() != null) {
                pathAndQuery += '?' + request.url().getQuery();
            }

            final HttpHeaders httpHeaders = request.headers();
            final String signed = Arrays.stream(SIGNED_HEADERS)
                .map(httpHeaders::value)
                .collect(Collectors.joining(";"));

            // String-To-Sign=HTTP_METHOD + '\n' + path_and_query + '\n' + signed_headers_values
            // Signed headers: "host;x-ms-date;x-ms-content-sha256"
            // The line separator has to be \n. Using %n with String.format will result in a 401 from the service.
            String stringToSign = request.httpMethod().toString().toUpperCase(Locale.US) + "\n" + pathAndQuery + "\n" + signed;

            final String signature = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            headers.put(AUTHORIZATION_HEADER, String.format("HMAC-SHA256 Credential=%s, SignedHeaders=%s, Signature=%s",
                credentialId,
                signedHeadersValue,
                signature));
        }
    }

    private ByteBuf getEmptyBuffer() {
        return new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT);
    }
}

