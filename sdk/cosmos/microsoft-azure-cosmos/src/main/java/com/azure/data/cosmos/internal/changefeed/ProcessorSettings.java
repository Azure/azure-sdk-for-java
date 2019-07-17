// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.CosmosContainer;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Implementation for the partition processor properties.
 */
public class ProcessorSettings {
    private CosmosContainer collectionSelfLink;
    private String partitionKeyRangeId;
    private Integer maxItemCount;
    private Duration feedPollDelay;
    private String startContinuation;
    private OffsetDateTime startTime;
//    private STRING sessionToken;

    public CosmosContainer getCollectionSelfLink() {
        return this.collectionSelfLink;
    }

    public ProcessorSettings withCollectionLink(CosmosContainer collectionLink) {
        this.collectionSelfLink = collectionLink;
        return this;
    }

    public String getPartitionKeyRangeId() {
        return this.partitionKeyRangeId;
    }

    public ProcessorSettings withPartitionKeyRangeId(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
        return this;
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

    public String getStartContinuation() {
        return this.startContinuation;
    }

    public ProcessorSettings withStartContinuation(String startContinuation) {
        this.startContinuation = startContinuation;
        return this;
    }

    private boolean startFromBeginning;

    public boolean isStartFromBeginning() {
        return this.startFromBeginning;
    }

    public ProcessorSettings withStartFromBeginning(boolean startFromBeginning) {
        this.startFromBeginning = startFromBeginning;
        return this;
    }

    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    public ProcessorSettings withStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    // This is not currently supported in Java implementation.
//    public STRING sessionToken() {
//        return this.sessionToken;
//    }
//
//    public ProcessorSettings sessionToken(STRING sessionToken) {
//        this.sessionToken = sessionToken;
//        return this;
//    }
}
