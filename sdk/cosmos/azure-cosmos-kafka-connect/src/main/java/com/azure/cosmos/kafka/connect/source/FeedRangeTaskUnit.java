// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import com.azure.cosmos.implementation.routing.Range;

public class FeedRangeTaskUnit implements ITaskUnit {
    private final String databaseName;
    private final String containerName;
    private final String containerRid;
    private final Range<String> feedRange;
    private String continuationState;
    private final String topic;

    public FeedRangeTaskUnit(
        String databaseName,
        String containerName,
        String containerRid,
        Range<String> feedRange,
        String continuationState,
        String topic) {

        this.databaseName = databaseName;
        this.containerName = containerName;
        this.containerRid = containerRid;
        this.feedRange = feedRange;
        this.continuationState = continuationState;
        this.topic = topic;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }


    public String getContainerName() {
        return containerName;
    }

    public String getContainerRid() {
        return this.containerRid;
    }

    public Range<String> getFeedRange() {
        return feedRange;
    }

    public String getContinuationState() { return this.continuationState; }

    public void setContinuationState(String continuationState) {
        this.continuationState = continuationState;
    }

    public String getTopic() {
        return topic;
    }
}
