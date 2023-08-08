// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data.entity;

import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.benchmark.linkedin.data.KeyGenerator;
import java.util.Random;


/**
 * KeyGenerator for the Invitations data, which models invitations sent to users
 * using (inviter, invitee) pairs
 */
public class InvitationsKeyGenerator implements KeyGenerator {
    private static final double MEMBER_TO_INVITATION_RATIO = 0.36;

    private final long _modeledUserCount;

    /**
     * The Random Number generator is initialized using the modeled user count as the seed,
     * resulting in the same sequence of numbers generated on each iteration
     *
     * @see java.util.Random javadocs for additional information
     */
    private final Random _randomNumberGenerator;

    public InvitationsKeyGenerator(int documentCount) {
        _modeledUserCount = (long) (documentCount * MEMBER_TO_INVITATION_RATIO);
        _randomNumberGenerator = new Random(_modeledUserCount);
    }

    @Override
    public Key key() {
        final long inviterId = (long) (_randomNumberGenerator.nextFloat() * (_modeledUserCount));
        final long inviteeId = (long) (_randomNumberGenerator.nextFloat() * (_modeledUserCount));
        return new Key(String.valueOf(inviterId), String.valueOf(inviteeId));
    }
}
