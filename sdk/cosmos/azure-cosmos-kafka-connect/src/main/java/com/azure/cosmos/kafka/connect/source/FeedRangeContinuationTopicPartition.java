// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import com.azure.cosmos.implementation.routing.Range;

import java.util.HashMap;
import java.util.Map;

public class FeedRangeContinuationTopicPartition {
    private static final String DATABASE_NAME_KEY = "cosmos.source.database.name";
    public static final String CONTAINER_RESOURCE_ID_KEY = "cosmos.source.container.resourceId";
    private static final String CONTAINER_FEED_RANGE_KEY = "cosmos.source.container.feedRange";

    private final String databaseName;
    private final String containerRid;
    private final Range<String> feedRange;
    private final Map<String, Object> topicPartitionMap;

    public FeedRangeContinuationTopicPartition(
        String databaseName,
        String containerRid,
        Range<String> feedRange) {

        this.databaseName = databaseName;
        this.containerRid = containerRid;
        this.feedRange = feedRange;

        this.topicPartitionMap = new HashMap<>();
        this.topicPartitionMap.put(DATABASE_NAME_KEY, databaseName);
        this.topicPartitionMap.put(CONTAINER_RESOURCE_ID_KEY, containerRid);
        this.topicPartitionMap.put(CONTAINER_FEED_RANGE_KEY, feedRange);
    }

    public FeedRangeContinuationTopicPartition(Map<String, Object> topicPartitionMap) {
        this.topicPartitionMap = topicPartitionMap;
        this.databaseName = this.topicPartitionMap.get(DATABASE_NAME_KEY).toString();
        this.containerRid = this.topicPartitionMap.get(CONTAINER_RESOURCE_ID_KEY).toString();
        this.feedRange = (Range<String>) this.topicPartitionMap.get(CONTAINER_FEED_RANGE_KEY);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getContainerRid() {
        return containerRid;
    }

    public Range<String> getFeedRange() {
        return feedRange;
    }

    public Map<String, Object> getTopicPartitionMap() {
        return topicPartitionMap;
    }
}
