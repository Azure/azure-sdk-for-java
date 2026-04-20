// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Header;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.util.Objects;

import java.net.URL;
import java.text.Collator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.azure.storage.common.Utility.urlDecode;

/**
 * Holds session credentials (token, key, expiration) and signs requests using the Shared Key protocol.
 * The Authorization header format is: {@code Session <sessionToken>:<signature>}
 */
public final class StorageSessionCredential {

    private static final HttpHeaderName X_MS_DATE = HttpHeaderName.fromString("x-ms-date");

    private final String sessionToken;
    private final String sessionKey;
    private final OffsetDateTime expiration;
    private final String accountName;

    /**
     * Creates a StorageSessionCredential with the given session token, key, expiration, and storage account name.
     *
     * @param sessionToken The opaque session token from Create Session response.
     * @param sessionKey The Base64-encoded symmetric key for HMAC signing.
     * @param expiration The time when this session expires.
     * @param accountName The storage account name associated with the request.
     */
    public StorageSessionCredential(String sessionToken, String sessionKey, OffsetDateTime expiration,
        String accountName) {
        this.sessionToken = Objects.requireNonNull(sessionToken, "'sessionToken' cannot be null.");
        this.sessionKey = Objects.requireNonNull(sessionKey, "'sessionKey' cannot be null.");
        this.expiration = expiration != null ? expiration : OffsetDateTime.now().plusMinutes(5L);
        this.accountName = Objects.requireNonNull(accountName, "'accountName' cannot be null.");
    }

    /**
     * Computes an HMAC-SHA256 signature for the given string-to-sign using the session key.
     *
     * @param stringToSign The string to sign.
     * @return The Base64-encoded HMAC-SHA256 signature.
     */
    public String computeHmac256(String stringToSign) {
        return StorageImplUtils.computeHMac256(sessionKey, stringToSign);
    }

    /**
     * Generates the Session Authorization header value for a request.
     * Format: {@code Session <sessionToken>:<signature>}
     *
     * @param requestURL The request URL.
     * @param httpMethod The HTTP method (GET, PUT, etc.).
     * @param headers The request headers.
     * @return The Authorization header value.
     */
    public String generateAuthorizationHeader(URL requestURL, String httpMethod, HttpHeaders headers) {
        String stringToSign = buildStringToSign(requestURL, httpMethod, headers);
        String signature = computeHmac256(stringToSign);
        return "Session " + sessionToken + ":" + signature;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public OffsetDateTime getExpiration() {
        return expiration;
    }

    public Boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiration);
    }

    // ---- String-to-sign logic (Shared Key protocol) ----
    // Ported from StorageSharedKeyCredential.buildStringToSign(). The signing format is identical.

    private String buildStringToSign(URL requestURL, String httpMethod, HttpHeaders headers) {
        String contentLength = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        contentLength = "0".equals(contentLength) ? "" : contentLength;

        String dateHeader
            = (headers.getValue(X_MS_DATE) != null) ? "" : getStandardHeaderValue(headers, HttpHeaderName.DATE);

        Collator collator = Collator.getInstance(Locale.ROOT);
        return String.join("\n", httpMethod, getStandardHeaderValue(headers, HttpHeaderName.CONTENT_ENCODING),
            getStandardHeaderValue(headers, HttpHeaderName.CONTENT_LANGUAGE), contentLength,
            getStandardHeaderValue(headers, HttpHeaderName.CONTENT_MD5),
            getStandardHeaderValue(headers, HttpHeaderName.CONTENT_TYPE), dateHeader,
            getStandardHeaderValue(headers, HttpHeaderName.IF_MODIFIED_SINCE),
            getStandardHeaderValue(headers, HttpHeaderName.IF_MATCH),
            getStandardHeaderValue(headers, HttpHeaderName.IF_NONE_MATCH),
            getStandardHeaderValue(headers, HttpHeaderName.IF_UNMODIFIED_SINCE),
            getStandardHeaderValue(headers, HttpHeaderName.RANGE), getAdditionalXmsHeaders(headers, collator),
            getCanonicalizedResource(requestURL, collator));
    }

    private static String getStandardHeaderValue(HttpHeaders headers, HttpHeaderName headerName) {
        final Header header = headers.get(headerName);
        return header == null ? "" : header.getValue();
    }

    private static String getAdditionalXmsHeaders(HttpHeaders headers, Collator collator) {
        List<Header> xmsHeaders = new ArrayList<>();

        int stringBuilderSize = 0;
        for (HttpHeader header : headers) {
            String headerName = header.getName();
            if (!"x-ms-".regionMatches(true, 0, headerName, 0, 5)) {
                continue;
            }

            String headerValue = header.getValue();
            stringBuilderSize += headerName.length() + headerValue.length();

            xmsHeaders.add(header);
        }

        if (xmsHeaders.isEmpty()) {
            return "";
        }

        final StringBuilder canonicalizedHeaders = new StringBuilder(stringBuilderSize + (2 * xmsHeaders.size()) - 1);

        xmsHeaders.sort((o1, o2) -> collator.compare(o1.getName(), o2.getName()));

        for (Header xmsHeader : xmsHeaders) {
            if (canonicalizedHeaders.length() > 0) {
                canonicalizedHeaders.append('\n');
            }
            canonicalizedHeaders.append(xmsHeader.getName().toLowerCase(Locale.ROOT))
                .append(':')
                .append(xmsHeader.getValue());
        }

        return canonicalizedHeaders.toString();
    }

    private String getCanonicalizedResource(URL requestURL, Collator collator) {
        String absolutePath = requestURL.getPath();
        if (CoreUtils.isNullOrEmpty(absolutePath)) {
            absolutePath = "/";
        }

        String query = requestURL.getQuery();
        if (CoreUtils.isNullOrEmpty(query)) {
            return "/" + accountName + absolutePath;
        }

        int stringBuilderSize = 1 + accountName.length() + absolutePath.length() + query.length();

        TreeMap<String, List<String>> pieces = new TreeMap<>(collator);

        StorageImplUtils.parseQueryParameters(query).forEachRemaining(kvp -> {
            String key = urlDecode(kvp.getKey()).toLowerCase(Locale.ROOT);

            pieces.compute(key, (k, values) -> {
                if (values == null) {
                    values = new ArrayList<>();
                }

                for (String value : kvp.getValue().split(",")) {
                    values.add(urlDecode(value));
                }

                return values;
            });
        });

        stringBuilderSize += pieces.size();

        StringBuilder canonicalizedResource
            = new StringBuilder(stringBuilderSize).append('/').append(accountName).append(absolutePath);

        for (Map.Entry<String, List<String>> queryParam : pieces.entrySet()) {
            List<String> queryParamValues = queryParam.getValue();
            queryParamValues.sort(collator);
            canonicalizedResource.append('\n').append(queryParam.getKey()).append(':');

            int size = queryParamValues.size();
            for (int i = 0; i < size; i++) {
                String queryParamValue = queryParamValues.get(i);
                if (i > 0) {
                    canonicalizedResource.append(',');
                }

                canonicalizedResource.append(queryParamValue);
            }
        }

        return canonicalizedResource.toString();
    }
}
