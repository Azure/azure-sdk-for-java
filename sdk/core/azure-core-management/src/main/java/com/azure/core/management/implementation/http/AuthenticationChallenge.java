// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.http;

import com.azure.core.annotation.Immutable;

/**
 * Represents an Authentication Challenge received in a 401 HTTP response.
 */
@Immutable
public class AuthenticationChallenge {
    private final String scheme;
    private final String challengeParameters;

    /**
     * Constructs an instance of the {@link AuthenticationChallenge}
     * @param scheme the scheme of the challenge response
     * @param challengeParameters the parameters requested in the challenge.
     */
    public AuthenticationChallenge(String scheme, String challengeParameters) {
        this.scheme = scheme;
        this.challengeParameters = challengeParameters;
    }

    /**
     * Get the challenge parameters.
     *
     * @return the challenge parameters.
     */
    public String getChallengeParameters() {
        return challengeParameters;
    }

    /**
     * Get the scheme of the challenge.
     *
     * @return the scheme of the challenge.
     */
    public String getScheme() {
        return scheme;
    }
}

