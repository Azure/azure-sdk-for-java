// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.Constants;

/**
 * handle ConditionalAccess from obo flow.
 */
public final class ConditionalAccessException extends RuntimeException {
    private final String claims;

    protected ConditionalAccessException(String claims) {
        this.claims = claims;
    }

    public String getClaims() {
        return claims;
    }

    public static ConditionalAccessException fromHttpBody(String httpBody) {
        return new ConditionalAccessException(httpBodyToClaims(httpBody));
    }

    public static String httpBodyToClaims(String httpBody) {
        return httpBody.split(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS)[1];
    }

    public static String claimsToHttpBody(String claims) {
        return Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS + claims + Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS;
    }

    public static boolean isConditionAccessException(String httpBody) {
        return httpBody.startsWith(Constants.CONDITIONAL_ACCESS_POLICY_CLAIMS);
    }
}
