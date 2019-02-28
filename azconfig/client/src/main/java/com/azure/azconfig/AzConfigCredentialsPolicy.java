// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.azconfig;

import com.microsoft.rest.v3.http.HttpHeaders;
import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpPipelineLogLevel;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.NextPolicy;
import com.microsoft.rest.v3.http.policy.HostPolicy;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

/**
 * Creates a policy that authenticates request with AzConfig service.
 */
public final class AzConfigCredentialsPolicy extends HostPolicy {
    private static final String SIGNED_HEADERS = "host;x-ms-date;x-ms-content-sha256";
    private static final String KEY_VALUE_APPLICATION_HEADER = "application/vnd.microsoft.azconfig.kv+json";

    private static final String HOST_HEADER = "Host";
    private static final String DATE_HEADER = "x-ms-date";
    private static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String ACCEPT_HEADER = "Accept";

    private final AzConfigClient.AzConfigCredentials credentials;
    private final HttpPipelineOptions options;

    /**
     * Initializes a new instance of AzConfigCredentialsPolicy based on credentials.
     *
     * @param credentials for the Configuration Store in Azure
     */
    AzConfigCredentialsPolicy(AzConfigClient.AzConfigCredentials credentials) {
        this(credentials, new HttpPipelineOptions(null));
    }

    /**
     * Initializes a new instance of AzConfigCredentialsPolicy based on credentials.
     *
     * @param credentials for the Configuration Store in Azure
     * @param options the request options
     */
    AzConfigCredentialsPolicy(AzConfigClient.AzConfigCredentials credentials, HttpPipelineOptions options) {
        super(credentials.baseUri().getHost(), options);
        this.credentials = credentials;
        this.options = options;
    }

    /**
     * Sign the request.
     *
     * @param context The request context
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, NextPolicy next) {
        context.httpRequest().headers().set(HOST_HEADER, credentials.baseUri().getHost());
        if (context.httpRequest().headers().value(DATE_HEADER) == null) {
            OffsetDateTime now = Instant.now().atOffset(ZoneOffset.UTC);
            String utcNowString = DateTimeFormatter.RFC_1123_DATE_TIME.toFormat().format(now);
            context.httpRequest().headers().set(DATE_HEADER, utcNowString);
        }
        String contentHash = getContentHash(context.httpRequest().body() == null ? new byte[0] : context.httpRequest().body().blockFirst().array());
        context.httpRequest().headers().set(CONTENT_HASH_HEADER, contentHash);
        context.httpRequest().headers().set(CONTENT_TYPE_HEADER, KEY_VALUE_APPLICATION_HEADER);
        context.httpRequest().headers().set(ACCEPT_HEADER, KEY_VALUE_APPLICATION_HEADER);
        final String stringToSign = buildStringToSign(context.httpRequest());
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(credentials.secret(), "HmacSHA256");
            sha256HMAC.init(secretKey);

            String signature = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            String signedString = String.format("HMAC-SHA256 Credential=%s, SignedHeaders=%s, Signature=%s", credentials.credential(), SIGNED_HEADERS, signature);
            context.httpRequest().headers().set(AUTHORIZATION_HEADER, signedString);
        } catch (Exception e) {
            return Mono.error(e);
        }

        Mono<HttpResponse> response = next.process();
        return response.doOnSuccess(httpResponse -> {
            if (httpResponse.statusCode() == HttpResponseStatus.UNAUTHORIZED.code()) {
                if (options.shouldLog(HttpPipelineLogLevel.ERROR)) {
                    options.log(HttpPipelineLogLevel.ERROR,
                            "===== HTTP Unauthorized status, String-to-Sign:%n'%s'%n==================%n",
                            stringToSign);
                }
            }
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

    private static String getContentHash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(content);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

