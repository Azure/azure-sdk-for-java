// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import java.net.URL;
import java.nio.ByteBuffer;
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

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HttpPipelinePolicy to append CommunicationClient required headers
 */
public final class HmacAuthenticationPolicy implements HttpPipelinePolicy {
    private static final String X_MS_DATE_HEADER = "x-ms-date";
    private static final String X_MS_STRING_TO_SIGN_HEADER = "x-ms-hmac-string-to-sign-base64";
    private static final String HOST_HEADER = "host";
    private static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    // Order of the headers are important here for generating correct signature
    private static final String[] SIGNED_HEADERS = new String[]{X_MS_DATE_HEADER, HOST_HEADER, CONTENT_HASH_HEADER};

    private static final String AUTHORIZATIONHEADERNAME = "Authorization";
    private static final String HMACSHA256FORMAT = "HMAC-SHA256 SignedHeaders=%s&Signature=%s";

    // Previously DateTimeFormatter.RFC_1123_DATE_TIME was being used. There
    // was an issue with the day of month part. RFC_1123_DATE_TIME does not
    // append a leading '0' on days that are less than 10. It is important
    // that the locale remain US. In other locals the values that are generated
    // for the day and month strings may be different. (e.g. Canada day strings
    // have a '.' at the end)
    static final DateTimeFormatter HMAC_DATETIMEFORMATTER_PATTERN =
        DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    private final AzureKeyCredential credential;
    private final ClientLogger logger = new ClientLogger(HmacAuthenticationPolicy.class);

    /**
     * Created with a non-null client credential
     * @param clientCredential client credential with valid access key
     */
    public HmacAuthenticationPolicy(AzureKeyCredential clientCredential) {
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
                new RuntimeException("AzureKeyCredential requires a URL using the HTTPS protocol scheme"));
        }

        try {
            URL hostnameToSignWith = context.getData("hmacSignatureURL")
                .filter(alternativeUrl -> alternativeUrl instanceof URL)
                .map(alternativeUrl -> (URL) alternativeUrl)
                .orElse(context.getHttpRequest().getUrl());

            return appendAuthorizationHeaders(
                    hostnameToSignWith,
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

    private Mono<Map<String, String>> appendAuthorizationHeaders(URL url, String httpMethod, Flux<ByteBuffer> contents) {
        return contents.collect(() -> {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw logger.logExceptionAsError(Exceptions.propagate(e));
            }
        }, MessageDigest::update)
            .map(messageDigest -> addAuthenticationHeaders(url, httpMethod, messageDigest));
    }

    private Map<String, String> addAuthenticationHeaders(final URL url,
                                                         final String httpMethod,
                                                         final MessageDigest messageDigest) {
        final Map<String, String> headers = new HashMap<>();

        final String contentHash = Base64.getEncoder().encodeToString(messageDigest.digest());
        headers.put(CONTENT_HASH_HEADER, contentHash);
        String utcNow = OffsetDateTime.now(ZoneOffset.UTC)
            .format(HMAC_DATETIMEFORMATTER_PATTERN);
        headers.put(X_MS_DATE_HEADER, utcNow);
        headers.put(HOST_HEADER, url.getHost());
        addSignatureHeader(url, httpMethod, headers);
        return headers;
    }

    private void addSignatureHeader(final URL url, final String httpMethod, final Map<String, String> httpHeaders) {
        final String signedHeaderNames = String.join(";", SIGNED_HEADERS);
        final String signedHeaderValues = Arrays.stream(SIGNED_HEADERS)
            .map(httpHeaders::get)
            .collect(Collectors.joining(";"));

        String pathAndQuery = url.getPath();
        if (url.getQuery() != null) {
            pathAndQuery += '?' + url.getQuery();
        }

        // String-To-Sign=HTTP_METHOD + '\n' + path_and_query + '\n' + signed_headers_values
        // The line separator has to be \n. Using %n with String.format will result in a 401 from the service.
        String stringToSign = httpMethod.toUpperCase(Locale.US) + "\n" + pathAndQuery + "\n" + signedHeaderValues;

        String accessKey = credential.getKey();
        byte[] key = Base64.getDecoder().decode(accessKey);
        Mac sha256HMAC;
        try {
            sha256HMAC = Mac.getInstance("HmacSHA256");
            sha256HMAC.init(new SecretKeySpec(key, "HmacSHA256"));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }

        final String signature =
            Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
        httpHeaders.put(AUTHORIZATIONHEADERNAME, String.format(HMACSHA256FORMAT, signedHeaderNames, signature));
        httpHeaders.put(X_MS_STRING_TO_SIGN_HEADER, Base64.getEncoder().encodeToString(stringToSign.getBytes(StandardCharsets.UTF_8)));
    }

}
