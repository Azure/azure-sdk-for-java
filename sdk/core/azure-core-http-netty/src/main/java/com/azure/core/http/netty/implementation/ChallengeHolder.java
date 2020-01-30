// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.annotation.Immutable;

import java.util.List;
import java.util.Map;

/**
 * Model class that contains the authentication challenges returned by a server.
 */
@Immutable
public final class ChallengeHolder {
    private final boolean hasBasicChallenge;
    private final List<Map<String, String>> digestChallenges;

    /**
     * Creates a {@link ChallengeHolder} which contains the parsed authentication digest challenges and a flag
     * indicating if basic authorization is accepted.
     *
     * @param hasBasicChallenge Flag indicating if basic authorization is accepted.
     * @param digestChallenges Parsed digest challenges.
     */
    public ChallengeHolder(boolean hasBasicChallenge, List<Map<String, String>> digestChallenges) {
        this.hasBasicChallenge = hasBasicChallenge;
        this.digestChallenges = digestChallenges;
    }

    /**
     * @return Flag indicating if basic authorization is accepted.
     */
    public boolean hasBasicChallenge() {
        return hasBasicChallenge;
    }

    /**
     * @return The parsed digest challenges.
     */
    public List<Map<String, String>> getDigestChallenges() {
        return digestChallenges;
    }
}
