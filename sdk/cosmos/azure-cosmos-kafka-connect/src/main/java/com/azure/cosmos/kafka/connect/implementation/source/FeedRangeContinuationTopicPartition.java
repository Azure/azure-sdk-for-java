// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.FeedRange;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FeedRangeContinuationTopicPartition {
    private static final String DATABASE_NAME_KEY = "cosmos.source.database.name";
    public static final String CONTAINER_RESOURCE_ID_KEY = "cosmos.source.container.resourceId";
    private static final String CONTAINER_FEED_RANGE_KEY = "cosmos.source.feedRange";

    private final String databaseName;
    private final String containerRid;
    private final FeedRange feedRange;

    public FeedRangeContinuationTopicPartition(
        String databaseName,
        String containerRid,
        FeedRange feedRange) {
        checkArgument(StringUtils.isNotEmpty(databaseName), "Argument 'databaseName' should not be null");
        checkArgument(StringUtils.isNotEmpty(containerRid), "Argument 'containerRid' should not be null");
        checkNotNull(feedRange, "Argument 'feedRange' can not be null");

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

    public FeedRange getFeedRange() {
        return feedRange;
    }

    public static Map<String, Object> toMap(FeedRangeContinuationTopicPartition partition) {
        Map<String, Object> map = new HashMap<>();
        map.put(DATABASE_NAME_KEY, partition.getDatabaseName());
        map.put(CONTAINER_RESOURCE_ID_KEY, partition.getContainerRid());
        map.put(CONTAINER_FEED_RANGE_KEY, partition.getFeedRange().toString());

        return map;
    }
}
