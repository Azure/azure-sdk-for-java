// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;

import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.Map;


/**
 * A helper class for parsing Authorization challenge headers.
 */
public final class ChallengeParser {

    private ChallengeParser() {
        // Prevent instantiation.
    }

    /**
     * Parses the specified parameter from a challenge header found in the specified {@link HttpResponse}.
     *
     * @param response The {@link HttpResponse} to parse.
     * @param challengeScheme The challenge scheme containing the {@code challengeParameter}. For example: "Bearer"
     * @param challengeParameter The parameter key name containing the value to return.
     * @return The value of the parameter name specified in {@code challengeParameter} if it is found in the
     * specified {@code challengeScheme}.
     */
    public static String getChallengeParameterFromResponse(HttpResponse response, String challengeScheme, String challengeParameter) {
        if (response.getStatusCode() != HttpURLConnection.HTTP_UNAUTHORIZED
            || CoreUtils.isNullOrEmpty(response.getHeaders().getValue("WWW-Authenticate"))) {
            return null;
        }

        String headerValue = response.getHeaders().getValue("WWW-Authenticate");
        CharSequence bearer = challengeScheme;
        CharSequence claims = challengeParameter;
        CharSequence scheme = challengeScheme;
        CharSequence parameter = challengeParameter;
        CharSequence headerSpan = headerValue;

        // Iterate through each challenge value.
        Map.Entry<String, String> nextChallenge = tryGetNextChallenge(headerSpan);
        while (nextChallenge != null) {
            // Enumerate each key=value parameter until we find the 'claims' key on the 'Bearer' challenge.
            Map.Entry<String, String> pair = tryGetNextParameter(headerSpan);
            while (pair != null) {
                if (nextChallenge.getKey().equalsIgnoreCase(bearer.toString())
                    && pair.getKey().equalsIgnoreCase(claims.toString())) {
                    return pair.getKey();
                }
                // Continue to the next parameter.
                pair = tryGetNextParameter(headerSpan);
            }
            // Continue to the next challenge.
            nextChallenge = tryGetNextChallenge(nextChallenge.getValue());
        }
        return null;
    }

    /**
     * Iterates through the challenge schemes present in a challenge header.
     *
     * @param headerValue The header value which will be sliced to remove the first parsed {@code challengeKey}.
     * @return {@code true} if a challenge scheme was successfully parsed. The value of {@code headerValue} should be passed to
     * {@link #tryGetNextParameter(CharSequence)} to parse the challenge parameters if {@code true}.
     */
    private static Map.Entry<String, String> tryGetNextChallenge(CharSequence headerValue) {
        headerValue = trimStart(headerValue, " ");
        int endOfChallengeKey = indexOf(headerValue, ' ');
        if (endOfChallengeKey < 0) {
            return null;
        }
        CharSequence challengeKey = headerValue.subSequence(0, endOfChallengeKey);
        // Slice the challenge key from the headerValue.
        headerValue = headerValue.subSequence(endOfChallengeKey + 1, headerValue.length());
        return new AbstractMap.SimpleEntry<>(challengeKey.toString(), headerValue.toString());
    }

    /**
     * Iterates through a challenge header value after being parsed by {@link #tryGetNextChallenge(CharSequence)}.
     *
     * @param headerValue The header value after being parsed by {@link #tryGetNextChallenge(CharSequence)}.
     * @return {@code true} if the next available challenge parameter was successfully parsed. {@code false} if there
     * are no more parameters for the current challenge scheme or an additional challenge scheme was encountered in the
     * {@code headerValue}.
     */
    private static Map.Entry<String, String> tryGetNextParameter(CharSequence headerValue) {
        CharSequence spaceOrComma = " ,";
        headerValue = trimStart(headerValue, spaceOrComma);
        int nextSpace = indexOf(headerValue, ' ');
        int nextSeparator = indexOf(headerValue, '=');
        if (nextSpace < nextSeparator && nextSpace != -1) {
            // We encountered another challenge value.
            return null;
        }
        if (nextSeparator < 0) {
            return null;
        }
        // Get the paramKey.
        CharSequence paramKey = headerValue.subSequence(0, nextSeparator).toString().trim();
        // Slice to remove the 'paramKey=' from the parameters.
        headerValue = headerValue.subSequence(nextSeparator + 1, headerValue.length());
        // The start of paramValue will usually be a quoted string. Find the first quote.
        int quoteIndex = indexOf(headerValue, '\"');
        CharSequence paramValue;
        if (quoteIndex >= 0) {
            // The values are quote wrapped.
            paramValue = headerValue.subSequence(quoteIndex + 1, indexOf(headerValue, '\"'));
        } else {
            // The values are not quote wrapped (storage is one example of this).
            int trailingDelimiterIndex = indexOfAny(headerValue, spaceOrComma);
            if (trailingDelimiterIndex >= 0) {
                paramValue = headerValue.subSequence(0, trailingDelimiterIndex);
            } else {
                paramValue = headerValue;
            }
        }
        return new AbstractMap.SimpleEntry<>(paramKey.toString(), paramValue.toString());
    }

    /**
     * Helper method to trim characters from the start of a {@link CharSequence}.
     *
     * @param source The source {@link CharSequence}.
     * @param chars The characters to trim.
     * @return The trimmed {@link CharSequence}.
     */
    private static CharSequence trimStart(CharSequence source, CharSequence chars) {
        int start = 0;
        while (start < source.length() && chars.toString().indexOf(source.charAt(start)) >= 0) {
            start++;
        }
        return source.subSequence(start, source.length());
    }

    /**
     * Helper method to find the index of a character in a {@link CharSequence}.
     *
     * @param source The source {@link CharSequence}.
     * @param ch The character to find.
     * @return The index of the character, or -1 if not found.
     */
    private static int indexOf(CharSequence source, char ch) {
        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Helper method to find the index of any character in a {@link CharSequence}.
     *
     * @param source The source {@link CharSequence}.
     * @param chars The characters to find.
     * @return The index of the first character found, or -1 if none are found.
     */
    private static int indexOfAny(CharSequence source, CharSequence chars) {
        for (int i = 0; i < source.length(); i++) {
            if (chars.toString().indexOf(source.charAt(i)) >= 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * A functional interface for handling key-value pairs.
     */
    @FunctionalInterface
    private interface KeyValueCallback<T> {
        void call(T value);
    }
}
