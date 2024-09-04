// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;

import java.util.Map;

public class AuthorizationChallengeParser {

    /**
     * Parses the specified parameter from a challenge header found in the specified response.
     *
     * @param response          The Response to parse.
     * @param challengeScheme   The challenge scheme containing the challengeParameter. For example: "Bearer"
     * @param challengeParameter The parameter key name containing the value to return.
     * @return The value of the parameter name specified in challengeParameter if it is found in the specified challengeScheme, or null if not found.
     */
    public static String getChallengeParameterFromResponse(Response response, String challengeScheme, String challengeParameter) {
        String headerValue = response.getHeaders().get("WWW-Authenticate");

        if (headerValue == null || headerValue.isEmpty()) {
            return null;
        }

        String headerSpan = headerValue.trim();
        String scheme = challengeScheme;
        String parameter = challengeParameter;

        // Iterate through each challenge value.
        while (true) {
            String challengeKey = getNextChallenge(headerSpan);
            if (challengeKey == null) {
                break;
            }

            headerSpan = headerSpan.substring(challengeKey.length()).trim();

            // Enumerate each key-value parameter until we find the parameter key on the specified scheme challenge.
            while (true) {
                ParameterResult paramResult = getNextParameter(headerSpan);
                if (paramResult == null) {
                    break;
                }

                headerSpan = paramResult.remainingHeader;

                if (challengeKey.equalsIgnoreCase(scheme) && paramResult.paramKey.equalsIgnoreCase(parameter)) {
                    return paramResult.paramValue;
                }
            }
        }

        return null;
    }

    /**
     * Parses the next challenge scheme from a challenge header value.
     *
     * @param headerValue The header value containing the challenges.
     * @return The parsed challenge scheme or null if no more schemes are found.
     */
    private static String getNextChallenge(String headerValue) {
        headerValue = headerValue.trim();
        int endOfChallengeKey = headerValue.indexOf(' ');

        if (endOfChallengeKey < 0) {
            return null;
        }

        return headerValue.substring(0, endOfChallengeKey);
    }

    /**
     * Parses the next parameter key-value pair from a challenge header value.
     *
     * @param headerValue The header value after being parsed by getNextChallenge.
     * @return A ParameterResult containing the key, value, and the remaining header value, or null if no more parameters are found.
     */
    private static ParameterResult getNextParameter(String headerValue) {
        headerValue = headerValue.trim();
        int nextSpace = headerValue.indexOf(' ');
        int nextSeparator = headerValue.indexOf('=');

        if (nextSpace != -1 && nextSpace < nextSeparator) {
            // We encountered another challenge value.
            return null;
        }

        if (nextSeparator < 0) {
            return null;
        }

        String paramKey = headerValue.substring(0, nextSeparator).trim();
        headerValue = headerValue.substring(nextSeparator + 1).trim();

        String paramValue;
        int quoteIndex = headerValue.indexOf('\"');
        if (quoteIndex >= 0) {
            int endQuoteIndex = headerValue.indexOf('\"', quoteIndex + 1);
            paramValue = headerValue.substring(quoteIndex + 1, endQuoteIndex);
            headerValue = headerValue.substring(endQuoteIndex + 1).trim();
        } else {
            int trailingDelimiterIndex = Math.min(
                headerValue.indexOf(' '),
                headerValue.indexOf(',')
            );
            if (trailingDelimiterIndex >= 0) {
                paramValue = headerValue.substring(0, trailingDelimiterIndex).trim();
                headerValue = headerValue.substring(trailingDelimiterIndex).trim();
            } else {
                paramValue = headerValue;
                headerValue = "";
            }
        }

        return new ParameterResult(paramKey, paramValue, headerValue);
    }

    /**
     * Helper class to store the result of a parameter parsing operation.
     */
    private static class ParameterResult {
        String paramKey;
        String paramValue;
        String remainingHeader;

        ParameterResult(String paramKey, String paramValue, String remainingHeader) {
            this.paramKey = paramKey;
            this.paramValue = paramValue;
            this.remainingHeader = remainingHeader;
        }
    }

    // Dummy Response class for demonstration purposes.
    static class Response {
        private final Map<String, String> headers;

        public Response(Map<String, String> headers) {
            this.headers = headers;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }
    }
}
