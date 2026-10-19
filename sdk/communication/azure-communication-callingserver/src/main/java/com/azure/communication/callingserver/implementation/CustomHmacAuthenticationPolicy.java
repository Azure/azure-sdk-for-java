// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
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
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;

/**
 * HttpPipelinePolicy to append CommunicationClient required headers
 */
public final class CustomHmacAuthenticationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(CustomHmacAuthenticationPolicy.class);
    private static final HttpHeaderName X_FORWARDED_HOST = HttpHeaderName.fromString("X-FORWARDED-HOST");
    private static final HttpHeaderName X_MS_DATE_HEADER = HttpHeaderName.fromString("x-ms-date");
    private static final HttpHeaderName X_MS_STRING_TO_SIGN_HEADER
        = HttpHeaderName.fromString("x-ms-hmac-string-to-sign-base64");
    private static final HttpHeaderName CONTENT_HASH_HEADER = HttpHeaderName.fromString("x-ms-content-sha256");

    // Previously DateTimeFormatter.RFC_1123_DATE_TIME was being used. There
    // was an issue with the day of month part. RFC_1123_DATE_TIME does not
    // append a leading '0' on days that are less than 10. It is important
    // that the locale remain US. In other locals the values that are generated
    // for the day and month strings may be different. (e.g. Canada day strings
    // have a '.' at the end)
    static final DateTimeFormatter HMAC_DATETIMEFORMATTER_PATTERN
        = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    private final AzureKeyCredential credential;
    private final String acsResource;

    /**
     * Created with a non-null client credential
     * @param clientCredential client credential with valid access key
     * @param acsResource the acs resource endpoint
     */
    public CustomHmacAuthenticationPolicy(AzureKeyCredential clientCredential, String acsResource) {
        Objects.requireNonNull(clientCredential, "'clientCredential' cannot be a null value.");
        this.credential = clientCredential;
        this.acsResource = acsResource;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final Flux<ByteBuffer> contents = context.getHttpRequest().getBody() == null
            ? Flux.just(ByteBuffer.allocate(0))
            : context.getHttpRequest().getBody();

        if (!"https".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono
                .error(new RuntimeException("AzureKeyCredential requires a URL using the HTTPS protocol scheme"));
        }

        try {
            URL hostnameToSignWith = context.getData("hmacSignatureURL")
                .filter(alternativeUrl -> alternativeUrl instanceof URL)
                .map(alternativeUrl -> (URL) alternativeUrl)
                .orElse(context.getHttpRequest().getUrl());

            return contents.collect(() -> {
                try {
                    return MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                }
            }, MessageDigest::update).flatMap(messageDigest -> {
                addAuthenticationHeaders(acsResource, hostnameToSignWith,
                    context.getHttpRequest().getHttpMethod().toString(), messageDigest,
                    context.getHttpRequest().getHeaders());

                return next.process();
            });
        } catch (RuntimeException r) {
            return Mono.error(r);
        }
    }

    private void addAuthenticationHeaders(String acsResource, URL url, String httpMethod, MessageDigest messageDigest,
        HttpHeaders headers) {
        final String contentHash = Base64.getEncoder().encodeToString(messageDigest.digest());
        headers.set(X_FORWARDED_HOST, acsResource);
        headers.set(HttpHeaderName.HOST, acsResource);
        headers.set(CONTENT_HASH_HEADER, contentHash);
        String xMsDate = OffsetDateTime.now(ZoneOffset.UTC).format(HMAC_DATETIMEFORMATTER_PATTERN);
        headers.set(X_MS_DATE_HEADER, xMsDate);
        addSignatureHeader(url, httpMethod, headers, xMsDate, acsResource, contentHash);
    }

    private void addSignatureHeader(URL url, String httpMethod, HttpHeaders headers, String xMsDate, String host,
        String xMsContentSha256) {
        String signedHeaderValues = xMsDate + ";" + host + ";" + xMsContentSha256;

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
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }

        final String signature
            = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
        String authorization = "HMAC-SHA256 SignedHeaders=x-ms-date;host;x-ms-content-sha256&Signature=" + signature;
        headers.set(HttpHeaderName.AUTHORIZATION, authorization);
        headers.set(X_MS_STRING_TO_SIGN_HEADER,
            Base64.getEncoder().encodeToString(stringToSign.getBytes(StandardCharsets.UTF_8)));
    }

}
