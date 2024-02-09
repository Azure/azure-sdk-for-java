// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations.source.configs;

import java.time.Instant;

public class CosmosSourceChangeFeedConfig {
    private final CosmosChangeFeedModes changeFeedModes;
    private final CosmosChangeFeedStartFromModes changeFeedStartFromModes;
    private final Instant startFrom;
    private final int maxItemCount;

    public CosmosSourceChangeFeedConfig(
        CosmosChangeFeedModes changeFeedModes,
        CosmosChangeFeedStartFromModes changeFeedStartFromModes,
        Instant startFrom,
        int maxItemCount) {
        this.changeFeedModes = changeFeedModes;
        this.changeFeedStartFromModes = changeFeedStartFromModes;
        this.startFrom = startFrom;
        this.maxItemCount = maxItemCount;
    }

    public CosmosChangeFeedModes getChangeFeedModes() {
        return changeFeedModes;
    }

    public CosmosChangeFeedStartFromModes getChangeFeedStartFromModes() {
        return changeFeedStartFromModes;
    }

    public Instant getStartFrom() {
        return startFrom;
    }

    public int getMaxItemCount() {
        return maxItemCount;
    }
}
