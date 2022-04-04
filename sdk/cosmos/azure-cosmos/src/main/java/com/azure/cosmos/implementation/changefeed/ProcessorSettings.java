// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for the partition processor properties.
 */
public class ProcessorSettings {
    private Integer maxItemCount;
    private Duration feedPollDelay;
    private final ChangeFeedState startState;
    private final CosmosAsyncContainer feedContainer;

    public ProcessorSettings(
        ChangeFeedState startState,
        CosmosAsyncContainer feedContainer) {

        checkNotNull(startState, "Argument 'startState' must not be null");
        checkNotNull(feedContainer, "Argument 'feedContainer' must not be null");

        this.feedContainer = feedContainer;
        this.startState = startState;
    }

    public int getMaxItemCount() {
        return this.maxItemCount;
    }

    public ProcessorSettings withMaxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    public Duration getFeedPollDelay() {
        return this.feedPollDelay;
    }

    public ProcessorSettings withFeedPollDelay(Duration feedPollDelay) {
        this.feedPollDelay = feedPollDelay;
        return this;
    }

    public ChangeFeedState getStartState() {
        return this.startState;
    }

    public CosmosAsyncContainer getFeedContainer() {
        return this.feedContainer;
    }
}
