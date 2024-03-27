// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class ContainersMetadataTopicPartition {
    public static final String DATABASE_NAME_KEY = "cosmos.source.metadata.database.name";

    private final String databaseName;

    public ContainersMetadataTopicPartition(String databaseName) {
        checkArgument(StringUtils.isNotEmpty(databaseName), "Argument 'databaseName' can not be null");

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
