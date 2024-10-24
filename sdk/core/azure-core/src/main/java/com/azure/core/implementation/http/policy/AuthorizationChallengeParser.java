// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.CoreUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Authorization challenges from the {@link HttpResponse}.
 */
public class AuthorizationChallengeParser {

    private static final Pattern CHALLENGE_PATTERN = Pattern.compile("(\\w+) ((?:\\w+=\"[^\"]*\",?\\s*)+)");
    private static final Pattern CHALLENGE_PARAMS_PATTERN = Pattern.compile("(\\w+)=\"([^\"]*)\"");

    /**
     * Examines a {@link HttpResponse} to see if it is a CAE challenge.
     * @param response The {@link HttpResponse} to examine.
     * @return True if the response is a CAE challenge, false otherwise.
     */
    public static boolean isCaeClaimsChallenge(HttpResponse response) {
        String error = getChallengeParameterFromResponse(response, "Bearer", "error");
        return "insufficient_claims".equals(error);
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
        return getChallengeParameter(challenge, challengeScheme, parameter);
    }

    /**
     * Gets the specified challenge parameter from the challenge.
     * @param challenge The challenge string to parse.
     * @param challengeScheme The requested scheme (e.g. "Bearer" or "PoP")
     * @param parameter The parameter to extract.
     * @return The extracted value of the challenge parameter.
     */
    private static String getChallengeParameter(String challenge, String challengeScheme, String parameter) {
        if (CoreUtils.isNullOrEmpty(challenge)) {
            return null;
        }

        Matcher challengeMatch = CHALLENGE_PATTERN.matcher(challenge);
        while (challengeMatch.find()) {
            if (challengeMatch.group(1).equals(challengeScheme)) {
                Matcher paramsMatch = CHALLENGE_PARAMS_PATTERN.matcher(challengeMatch.group(2));
                while (paramsMatch.find()) {
                    if (parameter.equals(paramsMatch.group(1))) {
                        return paramsMatch.group(2);
                    }
                }
            }
        }
        return null;
    }
}
