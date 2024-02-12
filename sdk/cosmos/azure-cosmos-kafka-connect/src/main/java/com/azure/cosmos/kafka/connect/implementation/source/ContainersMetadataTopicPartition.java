// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import java.util.HashMap;
import java.util.Map;

public class ContainersMetadataTopicPartition {
    public static final String DATABASE_NAME_KEY = "cosmos.source.metadata.database.name";

    private final String databaseName;

    public ContainersMetadataTopicPartition(String databaseName) {
        this.databaseName = databaseName;

    }

    public String getDatabaseName() {
        return databaseName;
    }

    public static Map<String, Object> toMap(ContainersMetadataTopicPartition topicPartition) {
        Map<String, Object> map = new HashMap<>();
        map.put(DATABASE_NAME_KEY, topicPartition.getDatabaseName());
        return map;
    }
}
