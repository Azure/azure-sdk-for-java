// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import java.time.Instant;

public class CosmosSourceChangeFeedConfig {
    private final CosmosChangeFeedMode changeFeedModes;
    private final CosmosChangeFeedStartFromMode changeFeedStartFromModes;
    private final Instant startFrom;
    private final int maxItemCountHint;

    public CosmosSourceChangeFeedConfig(
        CosmosChangeFeedMode changeFeedModes,
        CosmosChangeFeedStartFromMode changeFeedStartFromModes,
        Instant startFrom,
        int maxItemCountHint) {
        this.changeFeedModes = changeFeedModes;
        this.changeFeedStartFromModes = changeFeedStartFromModes;
        this.startFrom = startFrom;
        this.maxItemCountHint = maxItemCountHint;
    }

    public CosmosChangeFeedMode getChangeFeedModes() {
        return changeFeedModes;
    }

    public CosmosChangeFeedStartFromMode getChangeFeedStartFromModes() {
        return changeFeedStartFromModes;
    }

    public Instant getStartFrom() {
        return startFrom;
    }

    public int getMaxItemCountHint() {
        return maxItemCountHint;
    }
}
