// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.credentials;

import com.azure.core.implementation.util.ImplUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Holds a SAS token used for authenticating requests.
 */
public final class SASTokenCredential {
    private static final String SIGNATURE = "sig";
    private static final String UTF8_CHARSET = "UTF-8";

    private final String sasToken;

    /**
     * Creates a SAS token credential from the passed SAS token.
     *
     * @param sasToken SAS token used to authenticate requests with the service.
     */
    private SASTokenCredential(String sasToken) {
        this.sasToken = sasToken;
    }

    /**
     * @return the SAS token
     */
    public String sasToken() {
        return sasToken;
    }

    /**
     * Creates a SAS token credential from the passed SAS token.
     *
     * @param sasToken SAS token
     * @return a SAS token credential if {@code sasToken} is not {@code null} or empty, otherwise null.
     */
    public static SASTokenCredential fromSASTokenString(String sasToken) {
        if (ImplUtils.isNullOrEmpty(sasToken)) {
            return null;
        }

        return new SASTokenCredential(sasToken);
    }

    /**
     * Creates a SAS token credential from the passed query string parameters.
     *
     * @param queryParameterString URL query parameters
     * @return a SAS token credential if {@code queryParameters} is not {@code null} and has
     * the signature ("sig") query parameter, otherwise returns {@code null}.
     */
    public static SASTokenCredential fromQueryParameters(String queryParameterString) {
        Map<String, String> queryParameters = parseQueryString(queryParameterString);
        if (ImplUtils.isNullOrEmpty(queryParameters) || !queryParameters.containsKey(SIGNATURE)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> kvp : queryParameters.entrySet()) {
            if (sb.length() != 0) {
                sb.append("&");

            }

            sb.append(kvp.getKey()).append("=").append(kvp.getValue());
        }

        return new SASTokenCredential(sb.toString());
    }

    /**
     * Parses the query string into a key-value pair map that maintains key, query parameter key, order.
     *
     * @param queryString Query string to parse
     * @return a mapping of query string pieces as key-value pairs.
     */
    private static TreeMap<String, String> parseQueryString(final String queryString) {
        TreeMap<String, String> pieces = new TreeMap<>(String::compareTo);

        if (ImplUtils.isNullOrEmpty(queryString)) {
            return pieces;
        }

        for (String kvp : queryString.split("&")) {
            int equalIndex = kvp.indexOf("=");
            String key = urlDecode(kvp.substring(0, equalIndex)).toLowerCase(Locale.ROOT);
            String value = urlDecode(kvp.substring(equalIndex + 1));

            pieces.putIfAbsent(key, value);
        }

        return pieces;
    }

    /**
     * Performs a safe decoding of the passed string, taking care to preserve each {@code +} character rather than
     * replacing it with a space character.
     *
     * @param stringToDecode String value to decode
     * @return the decoded string value
     * @throws RuntimeException If the UTF-8 charset isn't supported
     */
    static String urlDecode(final String stringToDecode) {
        if (ImplUtils.isNullOrEmpty(stringToDecode)) {
            return "";
        }

        if (stringToDecode.contains("+")) {
            StringBuilder outBuilder = new StringBuilder();

            int startDex = 0;
            for (int m = 0; m < stringToDecode.length(); m++) {
                if (stringToDecode.charAt(m) == '+') {
                    if (m > startDex) {
                        outBuilder.append(decode(stringToDecode.substring(startDex, m)));
                    }

                    outBuilder.append("+");
                    startDex = m + 1;
                }
            }

            if (startDex != stringToDecode.length()) {
                outBuilder.append(decode(stringToDecode.substring(startDex)));
            }

            return outBuilder.toString();
        } else {
            return decode(stringToDecode);
        }
    }

    /*
     * Helper method to reduce duplicate calls of URLDecoder.decode
     */
    private static String decode(final String stringToDecode) {
        try {
            return URLDecoder.decode(stringToDecode, UTF8_CHARSET);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
