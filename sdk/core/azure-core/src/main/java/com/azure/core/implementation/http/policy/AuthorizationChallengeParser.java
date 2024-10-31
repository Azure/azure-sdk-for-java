// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;

/**
 * Parses Authorization challenges from the {@link HttpResponse}.
 */
public final class AuthorizationChallengeParser {

    /**
     * Creates an instance of the AuthorizationChallengeParser.
     */
    private AuthorizationChallengeParser() {
    }

    /**
     * Examines a {@link HttpResponse} to see if it is a CAE challenge.
     * @param response The {@link HttpResponse} to examine.
     * @return True if the response is a CAE challenge, false otherwise.
     */
    public static boolean isCaeClaimsChallenge(HttpResponse response) {
        String challenge = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);

        String parameters = getChallengeParametersForScheme(challenge, "Bearer");
        String error = getChallengeParameterValue(parameters, "error");
        String claims = getChallengeParameterValue(parameters, "claims");
        return !CoreUtils.isNullOrEmpty(claims) && "insufficient_claims".equals(error);
    }

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
        String challenge = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        String parameters = getChallengeParametersForScheme(challenge, challengeScheme);
        return getChallengeParameterValue(parameters, parameter);
    }

    /**
     * Gets the set of challenge parameters for the specified challenge scheme.
     * @param challenge The challenge to parse.
     * @param challengeScheme The challenge scheme to extract parameters for.
     * @return The extracted challenge parameters for the specified challenge scheme.
     */
    private static String getChallengeParametersForScheme(String challenge, String challengeScheme) {
        if (CoreUtils.isNullOrEmpty(challenge)) {
            return null;
        }

        int schemeIndex = -1;
        int length = challenge.length();
        int schemeLength = challengeScheme.length();

        for (int i = 0; i <= length - schemeLength - 1; i++) {
            // Check if the scheme matches and is followed by a space
            if (challenge.startsWith(challengeScheme, i)
                && (i + schemeLength < length)
                && challenge.charAt(i + schemeLength) == ' ') {
                schemeIndex = i;
                break;
            }
        }

        if (schemeIndex == -1) {
            return null; // Scheme not found
        }

        int startIndex = schemeIndex + challengeScheme.length();
        int endIndex = challenge.length();

        // Skip whitespace after the scheme to avoid unnecessary trim
        while (startIndex < endIndex && Character.isWhitespace(challenge.charAt(startIndex))) {
            startIndex++;
        }

        // Skip trailing whitespace
        while (endIndex > startIndex && Character.isWhitespace(challenge.charAt(endIndex - 1))) {
            endIndex--;
        }

        return startIndex < endIndex ? challenge.substring(startIndex, endIndex) : null;
    }

    /**
     * Gets the specified challenge parameter from the challenge.
     * @param parameters The challenge parameters to parse.
     * @param parameter The parameter to extract.
     * @return The extracted value of the challenge parameter.
     */
    private static String getChallengeParameterValue(String parameters, String parameter) {
        if (CoreUtils.isNullOrEmpty(parameters)) {
            return null;
        }

        String[] paramPairs = parameters.split(",", -1);
        for (String pair : paramPairs) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex != -1) {
                String key = pair.substring(0, equalsIndex).trim();

                if (key.equals(parameter)) {
                    String value = pair.substring(equalsIndex + 1).replace("\"", "").trim();
                    return value;
                }
            }
        }
        return null;
    }
}
