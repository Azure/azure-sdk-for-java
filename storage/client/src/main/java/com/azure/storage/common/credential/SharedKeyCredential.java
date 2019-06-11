// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.credential;

import com.azure.core.implementation.util.ImplUtils;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SharedKey credential policy that is put into a header to authorize requests.
 */
public final class SharedKeyCredential {
    private static final String AUTHORIZATION_HEADER_FORMAT = "SharedKey %s:%s";

    // Pieces of the connection string that are needed.
    private static final String ACCOUNT_NAME = "AccountName".toLowerCase();
    private static final String ACCOUNT_KEY = "AccountKey".toLowerCase();

    private final String accountName;
    private final byte[] accountKey;

    /**
     * Initializes a new instance of SharedKeyCredentials contains an account's name and its primary or secondary
     * accountKey.
     *
     * @param accountName The account name associated with the request.
     * @param accountKey The account access key used to authenticate the request.
     */
    public SharedKeyCredential(String accountName, String accountKey) {
        this.accountName = accountName;
        this.accountKey = Base64.getDecoder().decode(accountKey);
    }

    public String accountName() {
        return accountName;
    }

    /**
     * Creates a SharedKey credential from the passed connection string.
     * @param connectionString Connection string used to build the SharedKey credential.
     * @return a SharedKey credential if the connection string contains AccountName and AccountKey
     * @throws IllegalArgumentException If {@code connectionString} doesn't have AccountName or AccountKey.
     */
    public static SharedKeyCredential fromConnectionString(String connectionString) {
        HashMap<String, String> connectionStringPieces = new HashMap<>();
        for (String connectionStringPiece : connectionString.split(";")) {
            String[] kvp = connectionStringPiece.split("=", 2);
            connectionStringPieces.put(kvp[0].toLowerCase(), kvp[1]);
        }

        String accountName = connectionStringPieces.get(ACCOUNT_NAME);
        String accountKey = connectionStringPieces.get(ACCOUNT_KEY);

        if (ImplUtils.isNullOrEmpty(accountName) || ImplUtils.isNullOrEmpty(accountKey)) {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'.");
        }

        return new SharedKeyCredential(accountName, accountKey);
    }

    /**
     * Generates the SharedKey Authorization value from information in the request.
     * @param requestURL URL of the request
     * @param httpMethod HTTP method being used
     * @param headers Headers on the request
     * @return the SharedKey authorization value
     */
    public String generateAuthorizationHeader(URL requestURL, String httpMethod, Map<String, String> headers) {
        return computeHMACSHA256(buildStringToSign(requestURL, httpMethod, headers));
    }

    private String buildStringToSign(URL requestURL, String httpMethod, Map<String, String> headers) {
        String contentLength = headers.get("Content-Length");
        contentLength = contentLength.equals("0") ? "" : contentLength;

        return String.join("\n",
            httpMethod,
            headers.getOrDefault("Content-Encoding", ""),
            headers.getOrDefault("Content-Language", ""),
            contentLength,
            headers.getOrDefault("Content-MD5", ""),
            headers.getOrDefault("Content-Type", ""),
            headers.getOrDefault("Date", ""),
            headers.getOrDefault("If-Modified-Since", ""),
            headers.getOrDefault("If-Match", ""),
            headers.getOrDefault("If-None-Match", ""),
            headers.getOrDefault("If-Unmodified-Since", ""),
            headers.getOrDefault("Range", ""),
            getAdditionalXmsHeaders(headers),
            getCanonicalizedResource(requestURL));
    }

    private String getAdditionalXmsHeaders(Map<String, String> headers) {
        // Add only headers that begin with 'x-ms-'
        final List<String> xmsHeaderNameArray = headers.entrySet().stream()
            .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).startsWith("x-ms-"))
            .filter(entry -> entry.getValue() != null)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (xmsHeaderNameArray.isEmpty()) {
            return "";
        }

        Collections.sort(xmsHeaderNameArray);

        final StringBuilder canonicalizedHeaders = new StringBuilder();
        for (final String key : xmsHeaderNameArray) {
            if (canonicalizedHeaders.length() > 0) {
                canonicalizedHeaders.append('\n');
            }

            canonicalizedHeaders.append(key)
                .append(':')
                .append(headers.get(key));
        }

        return canonicalizedHeaders.toString();
    }

    private String getCanonicalizedResource(URL requestURL) {

        // Resource path
        final StringBuilder canonicalizedResource = new StringBuilder("/");
        canonicalizedResource.append(accountName);

        // Note that AbsolutePath starts with a '/'.
        if (requestURL.getPath().length() > 0) {
            canonicalizedResource.append(requestURL.getPath());
        } else {
            canonicalizedResource.append('/');
        }

        // check for no query params and return
        if (requestURL.getQuery() == null) {
            return canonicalizedResource.toString();
        }

        // The URL object's query field doesn't include the '?'. The QueryStringDecoder expects it.
        QueryStringDecoder queryDecoder = new QueryStringDecoder("?" + requestURL.getQuery());
        Map<String, List<String>> queryParams = queryDecoder.parameters();

        ArrayList<String> queryParamNames = new ArrayList<>(queryParams.keySet());
        Collections.sort(queryParamNames);

        for (String queryParamName : queryParamNames) {
            final List<String> queryParamValues = queryParams.get(queryParamName);
            Collections.sort(queryParamValues);
            String queryParamValuesStr = String.join(",", queryParamValues);
            canonicalizedResource.append("\n")
                .append(queryParamName.toLowerCase(Locale.ROOT))
                .append(":")
                .append(queryParamValuesStr);
        }

        // append to main string builder the join of completed params with new line
        return canonicalizedResource.toString();
    }

    private String computeHMACSHA256(String stringToSign) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            hmacSha256.init(new SecretKeySpec(accountKey, "HmacSHA256"));
            byte[] utf8Bytes = stringToSign.getBytes(StandardCharsets.UTF_8);
            String signature = Base64.getEncoder().encodeToString(hmacSha256.doFinal(utf8Bytes));
            return String.format(AUTHORIZATION_HEADER_FORMAT, accountName, signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new Error(ex);
        }
    }
}
