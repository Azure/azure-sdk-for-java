// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations.source;

import com.azure.cosmos.implementation.routing.Range;

import java.util.HashMap;
import java.util.Map;

public class FeedRangeContinuationTopicPartition {
    private static final String DATABASE_NAME_KEY = "cosmos.source.database.name";
    public static final String CONTAINER_RESOURCE_ID_KEY = "cosmos.source.container.resourceId";
    private static final String CONTAINER_FEED_RANGE_KEY = "cosmos.source.feedRange";

    private final String databaseName;
    private final String containerRid;
    private final Range<String> feedRange;

    public FeedRangeContinuationTopicPartition(
        String databaseName,
        String containerRid,
        Range<String> feedRange) {

        this.databaseName = databaseName;
        this.containerRid = containerRid;
        this.feedRange = feedRange;
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

    public static Map<String, Object> toMap(FeedRangeContinuationTopicPartition partition) {
        Map<String, Object> map = new HashMap<>();
        map.put(DATABASE_NAME_KEY, partition.getDatabaseName());
        map.put(CONTAINER_RESOURCE_ID_KEY, partition.getContainerRid());
        map.put(CONTAINER_FEED_RANGE_KEY, partition.getFeedRange().toJson());

        return map;
    }

    @SuppressWarnings("unchecked")
    public static FeedRangeContinuationTopicPartition fromMap(Map<String, Object> partitionMap) {
        String databaseName = partitionMap.get(DATABASE_NAME_KEY).toString();
        String containerRid = partitionMap.get(CONTAINER_RESOURCE_ID_KEY).toString();
        Range<String> feedRange = new Range<String>(partitionMap.get(CONTAINER_FEED_RANGE_KEY).toString());

        return new FeedRangeContinuationTopicPartition(databaseName, containerRid, feedRange);
    }
}
