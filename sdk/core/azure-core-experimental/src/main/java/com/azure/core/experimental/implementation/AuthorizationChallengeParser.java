// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;

/**
 * A parser for extracting challenge parameters from a challenge header.
 */
public class AuthorizationChallengeParser {

    /**
     * Parses the specified parameter from a challenge header found in the specified {@link HttpResponse}.
     *
     * @param response The {@link HttpResponse} to parse.
     * @param challengeScheme The challenge scheme containing the {@code challengeParameter}. For example: "Bearer".
     * @param challengeParameter The parameter key name containing the value to return.
     * @return The value of the parameter name specified in {@code challengeParameter} if it is found in the
     * specified {@code challengeScheme}, or {@code null} if not found.
     */
    public static String getChallengeParameterFromResponse(HttpResponse response, String challengeScheme,
        String challengeParameter) {
        String headerValue = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        if (headerValue == null) {
            return null;
        }
        return extractChallengeParameter(headerValue, challengeScheme, challengeParameter);
    }

    static String extractChallengeParameter(String headerValue, String challengeScheme, String challengeParameter) {
        String remainingHeaderValue = headerValue.trim();
        while (!remainingHeaderValue.isEmpty()) {
            int schemeEnd = remainingHeaderValue.indexOf(' ');
            if (schemeEnd == -1) {
                break;
            }

            String scheme = remainingHeaderValue.substring(0, schemeEnd);
            remainingHeaderValue = remainingHeaderValue.substring(schemeEnd + 1).trim();

            if (scheme.equalsIgnoreCase(challengeScheme)) {
                String parameterValue = extractParameter(remainingHeaderValue, challengeParameter);
                if (parameterValue != null) {
                    return parameterValue;
                }
            }

            int nextComma = remainingHeaderValue.indexOf(',');
            if (nextComma == -1) {
                break;
            }
            remainingHeaderValue = remainingHeaderValue.substring(nextComma + 1).trim();
        }
        return null;
    }

    static String extractParameter(String headerValue, String targetParameter) {
        String remainingHeaderValue = headerValue.trim();
        while (!remainingHeaderValue.isEmpty()) {
            int separatorIndex = remainingHeaderValue.indexOf('=');
            if (separatorIndex == -1) {
                break;
            }

            String paramKey = remainingHeaderValue.substring(0, separatorIndex).trim();
            remainingHeaderValue = remainingHeaderValue.substring(separatorIndex + 1).trim();

            if (paramKey.equalsIgnoreCase(targetParameter)) {
                if (remainingHeaderValue.startsWith("\"")) {
                    int endQuoteIndex = remainingHeaderValue.indexOf('\"', 1);
                    if (endQuoteIndex == -1) {
                        break;
                    }
                    return remainingHeaderValue.substring(1, endQuoteIndex);
                } else {
                    int nextCommaOrSpace = findFirstOf(remainingHeaderValue, ' ', ',');
                    if (nextCommaOrSpace == -1) {
                        return remainingHeaderValue;
                    } else {
                        return remainingHeaderValue.substring(0, nextCommaOrSpace);
                    }
                }
            }

            int nextComma = remainingHeaderValue.indexOf(',');
            if (nextComma == -1) {
                break;
            }
            remainingHeaderValue = remainingHeaderValue.substring(nextComma + 1).trim();
        }
        return null;
    }

    private static int findFirstOf(String input, char... chars) {
        for (int i = 0; i < input.length(); i++) {
            for (char c : chars) {
                if (input.charAt(i) == c) {
                    return i;
                }
            }
        }
        return -1;
    }
}
