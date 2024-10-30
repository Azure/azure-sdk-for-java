// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;

/**
 * Parses Authorization challenges from the {@link HttpResponse}.
 */
public class AuthorizationChallengeParser {

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
        if (challenge == null || challengeScheme == null) {
            return null;
        }

        String[] challengeParts = challenge.split(",", -1);
        for (String part : challengeParts) {
            // Extract the challenge scheme and the parameters.
            int spaceIndex = part.indexOf(' ');
            if (spaceIndex != -1) {
                String scheme = part.substring(0, spaceIndex).trim();
                String parameters = part.substring(spaceIndex + 1).trim();

                if (scheme.equals(challengeScheme)) {
                    return parameters;
                }
            }
        }
        return null;
    }

    /**
     * Gets the specified challenge parameter from the challenge.
     * @param parameters The challenge parameters to parse.
     * @param parameter The parameter to extract.
     * @return The extracted value of the challenge parameter.
     */
    private static String getChallengeParameterValue(String parameters, String parameter) {
        if (parameters == null || parameter == null || parameters.isEmpty()) {
            return null;
        }

        String[] paramPairs = parameters.split(",", -1);
        for (String pair : paramPairs) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex != -1) {
                String key = pair.substring(0, equalsIndex).trim();
                String value = pair.substring(equalsIndex + 1).replace("\"", "").trim();

                if (key.equals(parameter)) {
                    return value;
                }
            }
        }
        return null;
    }
}
