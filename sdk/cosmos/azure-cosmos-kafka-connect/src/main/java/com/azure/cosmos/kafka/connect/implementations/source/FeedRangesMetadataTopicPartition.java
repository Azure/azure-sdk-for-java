// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations.source;

import java.util.HashMap;
import java.util.Map;

public class FeedRangesMetadataTopicPartition {
    public static final String DATABASE_NAME_KEY = "cosmos.source.metadata.database.name";
    public static final String CONTAINER_RESOURCE_ID_KEY = "cosmos.source.metadata.container.resourceId";
    private final String databaseName;
    private final String containerRid;

    public FeedRangesMetadataTopicPartition(String databaseName, String containerRid) {
        this.databaseName = databaseName;
        this.containerRid = containerRid;

    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getContainerRid() {
        return containerRid;
    }

    public static Map<String, Object> toMap(FeedRangesMetadataTopicPartition partition) {
        Map<String, Object> map = new HashMap<>();
        map.put(DATABASE_NAME_KEY, partition.getDatabaseName());
        map.put(CONTAINER_RESOURCE_ID_KEY, partition.getContainerRid());
        return map;
    }

    public static FeedRangesMetadataTopicPartition fromMap(Map<String, Object> partitionMap) {
        if (partitionMap == null) {
            return null;
        }

        String databaseName = partitionMap.get(DATABASE_NAME_KEY).toString();
        String containerRid = partitionMap.get(CONTAINER_RESOURCE_ID_KEY).toString();

        return new FeedRangesMetadataTopicPartition(databaseName, containerRid);
    }
}
