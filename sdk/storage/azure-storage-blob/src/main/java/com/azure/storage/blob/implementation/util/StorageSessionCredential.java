// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;

import java.net.URL;
import java.text.Collator;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Holds session credentials and signs requests using the Shared Key string-to-sign with the
 * Session scheme prefix.
 */
final class StorageSessionCredential {

    private static final HttpHeaderName X_MS_DATE = HttpHeaderName.fromString("x-ms-date");
    private static final String SESSION_PREFIX = "Session ";

    private final String sessionToken;
    private final String sessionKey;
    private final OffsetDateTime expiration;
    private final String accountName;
    private final StorageSharedKeyCredential sharedKey;

    StorageSessionCredential(String sessionToken, String sessionKey, OffsetDateTime expiration, String accountName) {
        this.sessionToken = Objects.requireNonNull(sessionToken, "'sessionToken' cannot be null.");
        this.sessionKey = Objects.requireNonNull(sessionKey, "'sessionKey' cannot be null.");
        this.expiration = expiration != null ? expiration : OffsetDateTime.now().plusMinutes(5L);
        this.accountName = Objects.requireNonNull(accountName, "'accountName' cannot be null.");
        this.sharedKey = new StorageSharedKeyCredential(accountName, sessionKey);
    }

    void signRequest(HttpRequest request) {
        // Pin x-ms-date so the value we sign matches what is on the wire (AddDatePolicy only sets Date).
        // Honor any pre-set x-ms-date so callers (e.g., tests, retries) can pin a deterministic value.
        if (request.getHeaders().getValue(X_MS_DATE) == null) {
            request.setHeader(X_MS_DATE, DateTimeRfc1123.toRfc1123String(OffsetDateTime.now()));
        }

        String stringToSign = buildStringToSign(request);
        String signature = sharedKey.computeHmac256(stringToSign);
        request.setHeader(HttpHeaderName.AUTHORIZATION, SESSION_PREFIX + sessionToken + ":" + signature);
    }

    // Mirrors StorageSharedKeyCredential.buildStringToSign. The server canonicalizes
    // Content-Length: 0 to "" before computing its HMAC (matching the documented Shared Key
    // canonicalization), so we must do the same here to produce a matching signature.
    //
    // TODO (azure-core, RFC hygiene only — does NOT affect Storage signing correctness):
    //   azure-core's RestProxyBase.configRequest (sdk/core/azure-core/.../RestProxyBase.java)
    //   unconditionally sets Content-Length: 0 on body-less requests, including GETs. Per
    //   RFC 7230 §3.3.2 a user agent SHOULD NOT send a Content-Length header when the request
    //   has no body and the method does not anticipate one (.NET's transports skip it). This
    //   does NOT cause a signing mismatch here — the server normalizes "0" -> "" and our local
    //   normalization above matches — so it is purely an RFC-hygiene issue. The Content-Length
    //   normalization in this method should remain in place even if azure-core is fixed: it
    //   reflects the documented Shared Key canonicalization rule, not a workaround for
    //   azure-core behavior. Track the azure-core fix separately if pursued.

    private String buildStringToSign(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        Collator collator = Collator.getInstance(Locale.ROOT);

        String contentLength = getHeaderOrEmpty(headers, HttpHeaderName.CONTENT_LENGTH);
        // Normalize "0" to "" to match the server's canonicalization (matches
        // StorageSharedKeyCredential.buildStringToSign).
        if ("0".equals(contentLength)) {
            contentLength = "";
        }
        // If x-ms-date is present, the Date slot is empty.
        String dateHeader = headers.getValue(X_MS_DATE) != null ? "" : getHeaderOrEmpty(headers, HttpHeaderName.DATE);

        return String.join("\n", request.getHttpMethod().toString(),
            getHeaderOrEmpty(headers, HttpHeaderName.CONTENT_ENCODING),
            getHeaderOrEmpty(headers, HttpHeaderName.CONTENT_LANGUAGE), contentLength,
            getHeaderOrEmpty(headers, HttpHeaderName.CONTENT_MD5),
            getHeaderOrEmpty(headers, HttpHeaderName.CONTENT_TYPE), dateHeader,
            getHeaderOrEmpty(headers, HttpHeaderName.IF_MODIFIED_SINCE),
            getHeaderOrEmpty(headers, HttpHeaderName.IF_MATCH), getHeaderOrEmpty(headers, HttpHeaderName.IF_NONE_MATCH),
            getHeaderOrEmpty(headers, HttpHeaderName.IF_UNMODIFIED_SINCE),
            getHeaderOrEmpty(headers, HttpHeaderName.RANGE), canonicalizedXmsHeaders(headers, collator),
            canonicalizedResource(request.getUrl(), collator));
    }

    private static String getHeaderOrEmpty(HttpHeaders headers, HttpHeaderName name) {
        String value = headers.getValue(name);
        return value == null ? "" : value;
    }

    private static String canonicalizedXmsHeaders(HttpHeaders headers, Collator collator) {
        List<HttpHeader> xmsHeaders = new ArrayList<>();
        for (HttpHeader header : headers) {
            if ("x-ms-".regionMatches(true, 0, header.getName(), 0, 5)) {
                xmsHeaders.add(header);
            }
        }
        if (xmsHeaders.isEmpty()) {
            return "";
        }
        xmsHeaders.sort((a, b) -> collator.compare(a.getName(), b.getName()));
        StringBuilder sb = new StringBuilder();
        for (HttpHeader h : xmsHeaders) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(h.getName().toLowerCase(Locale.ROOT)).append(':').append(h.getValue());
        }
        return sb.toString();
    }

    private String canonicalizedResource(URL url, Collator collator) {
        String path = url.getPath();
        if (CoreUtils.isNullOrEmpty(path)) {
            path = "/";
        }
        String query = url.getQuery();
        if (CoreUtils.isNullOrEmpty(query)) {
            return "/" + accountName + path;
        }

        // Sort query parameters with locale-insensitive collation, lower-cased keys.
        // Values must be URL-decoded (and split on commas) to match the canonicalization that the
        // service performs; otherwise percent-encoded characters (e.g., %3A in a snapshot timestamp)
        // would produce a different HMAC than Shared Key.
        TreeMap<String, List<String>> params = new TreeMap<>(collator);
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            String key = Utility.urlDecode(eq < 0 ? pair : pair.substring(0, eq)).toLowerCase(Locale.ROOT);
            String rawValue = eq < 0 ? "" : pair.substring(eq + 1);
            List<String> decoded = params.computeIfAbsent(key, k -> new ArrayList<>());
            for (String v : rawValue.split(",")) {
                decoded.add(Utility.urlDecode(v));
            }
        }

        StringBuilder sb = new StringBuilder("/").append(accountName).append(path);
        for (java.util.Map.Entry<String, List<String>> entry : params.entrySet()) {
            List<String> values = entry.getValue();
            java.util.Collections.sort(values);
            sb.append('\n').append(entry.getKey()).append(':').append(String.join(",", values));
        }
        return sb.toString();
    }

    String getSessionToken() {
        return sessionToken;
    }

    String getSessionKey() {
        return sessionKey;
    }

    OffsetDateTime getExpiration() {
        return expiration;
    }

    String getAccountName() {
        return accountName;
    }

    boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiration);
    }
}
