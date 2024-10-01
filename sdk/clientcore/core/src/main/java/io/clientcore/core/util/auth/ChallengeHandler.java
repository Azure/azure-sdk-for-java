// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

import java.util.Arrays;

/**
 * Abstract class representing a challenge handler for authentication.
 */
public abstract class ChallengeHandler {

    /**
     * Handles the authentication challenge.
     *
     * @return The authorization header or token.
     */
    public abstract String handle();

    // Factory method for creating composite handlers
    public static ChallengeHandler of(ChallengeHandler... handlers) {
        return new CompositeChallengeHandler(Arrays.asList(handlers));
    }
}
