// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses Authorization challenges from the Http response.
 */
public class AuthorizationChallengeParser {

    /**
     * Gets the specified challenge parameter from the challenge response.
     *
     * @param response the Http response with auth challenge
     * @param challengeScheme the challenge scheme to be checked
     * @param parameter the challenge parameter value to get
     *
     * @return the extracted value of the challenge parameter
     */
    public static String getChallengeParameterFromResponse(HttpResponse response, String challengeScheme,
        String parameter) {
        String headerValue = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        return getChallengeParameter(headerValue, challengeScheme, parameter);
    }

    static String getChallengeParameter(String challengeResponse, String challengeScheme, String parameter) {
        if (challengeResponse == null || challengeResponse.isEmpty()) {
            return null;
        }

        // Check if the challenge scheme matches
        if (!challengeResponse.startsWith(challengeScheme)) {
            return null;
        }

        // Remove the scheme from the challenge string
        String challenge = challengeResponse.substring(challengeScheme.length()).trim();

        // Parse the challenge parameters into a map
        Map<String, String> challengeParameters = parseChallengeParameters(challenge);

        // Return the requested parameter
        return challengeParameters.get(parameter);
    }

    private static Map<String, String> parseChallengeParameters(String challenge) {
        Map<String, String> parameters = new HashMap<>();
        int length = challenge.length();
        int start = 0;

        while (start < length) {
            int equalsIndex = challenge.indexOf('=', start);
            if (equalsIndex == -1) {
                break;
            }

            String key = challenge.substring(start, equalsIndex).trim();

            int commaIndex = challenge.indexOf(',', equalsIndex + 1);

            if (commaIndex == -1) {
                commaIndex = length;
            }

            String value = challenge.substring(equalsIndex + 1, commaIndex).trim();

            // Remove surrounding quotes if present
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            parameters.put(key, value);
            start = commaIndex + 1; // Move to the next part after the comma
        }

        return parameters;
    }

}
