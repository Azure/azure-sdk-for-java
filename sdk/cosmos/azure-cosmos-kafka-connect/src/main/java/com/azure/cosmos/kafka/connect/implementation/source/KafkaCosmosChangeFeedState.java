// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.models.FeedRange;

public class KafkaCosmosChangeFeedState {
    private final String continuationState;
    private final FeedRange targetRange;
    private final String continuationLsn;

    public KafkaCosmosChangeFeedState(String continuationState, FeedRange targetRange, String continuationLsn) {
        this.continuationState = continuationState;
        this.targetRange = targetRange;
        this.continuationLsn = continuationLsn;
    }

    public String getContinuationState() {
        return continuationState;
    }

    public FeedRange getTargetRange() {
        return targetRange;
    }

    public String getContinuationLsn() {
        return continuationLsn;
    }
}
