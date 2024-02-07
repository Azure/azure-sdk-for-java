// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import java.util.HashMap;
import java.util.Map;

public class ContainersMetadataTopicPartition {
    public static final String DATABASE_NAME_KEY = "cosmos.source.database.name";

    private final String databaseName;
    private final Map<String, String> topicPartitionMap;

    public ContainersMetadataTopicPartition(String databaseName) {
        this.databaseName = databaseName;
        this.topicPartitionMap = new HashMap<>();
        this.topicPartitionMap.put(DATABASE_NAME_KEY, databaseName);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Map<String, String> getTopicPartitionMap() {
        return topicPartitionMap;
    }
}
