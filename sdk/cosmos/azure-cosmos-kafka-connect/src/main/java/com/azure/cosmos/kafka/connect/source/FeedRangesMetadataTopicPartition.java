// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import java.util.HashMap;
import java.util.Map;

public class FeedRangesMetadataTopicPartition {
    public static final String DATABASE_NAME_KEY = "cosmos.source.database.name";
    public static final String CONTAINER_RESOURCE_ID_KEY = "cosmos.source.container.resourceId";
    private final String databaseName;
    private final String containerRid;
    private final Map<String, Object> topicPartitionMap;

    public FeedRangesMetadataTopicPartition(String databaseName, String containerRid) {
        this.databaseName = databaseName;
        this.containerRid = containerRid;
        this.topicPartitionMap = new HashMap<>();
        this.topicPartitionMap.put(DATABASE_NAME_KEY, databaseName);
        this.topicPartitionMap.put(CONTAINER_RESOURCE_ID_KEY, containerRid);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getContainerRid() {
        return containerRid;
    }

    public Map<String, Object> getTopicPartitionMap() {
        return topicPartitionMap;
    }
}
