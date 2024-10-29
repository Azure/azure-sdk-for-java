// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Authorization challenges from the {@link HttpResponse}.
 */
public class AuthorizationChallengeParser {

    // These patterns are use to parse a challenge string from the WWw-Authenticate header.
    // A WWW-Authenticate may have more than one challenge, grouped by scheme. The first pattern groups schemes and their parameters.
    // The second pattern parses the parameters out of a given challenge scheme.
    private static final Pattern CHALLENGE_PATTERN = Pattern.compile("(\\w+) ((?:\\w+=\"[^\"]*\",?\\s*)+)");
    private static final Pattern CHALLENGE_PARAMS_PATTERN = Pattern.compile("(\\w+)=\"([^\"]*)\"");

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
        Matcher challengeMatch = CHALLENGE_PATTERN.matcher(challenge);
        while (challengeMatch.find()) {
            if (challengeMatch.group(1).equals(challengeScheme)) {
                return challengeMatch.group(2);
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
        if (CoreUtils.isNullOrEmpty(parameters)) {
            return null;
        }

        Matcher paramsMatch = CHALLENGE_PARAMS_PATTERN.matcher(parameters);
        while (paramsMatch.find()) {
            if (parameter.equals(paramsMatch.group(1))) {
                return paramsMatch.group(2);
            }
        }

        return null;
    }
}
