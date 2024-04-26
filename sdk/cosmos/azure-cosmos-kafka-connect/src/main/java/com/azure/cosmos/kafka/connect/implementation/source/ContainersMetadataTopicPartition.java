// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class ContainersMetadataTopicPartition {
    public static final String DATABASE_NAME_KEY = "database";
    public static final String CONNECTOR_NAME_KEY = "connectorName";

    private final String databaseName;
    private final String connectorName;

    public ContainersMetadataTopicPartition(String databaseName, String connectorName) {
        checkArgument(StringUtils.isNotEmpty(databaseName), "Argument 'databaseName' can not be null");

        this.databaseName = databaseName;
        this.connectorName = StringUtils.isEmpty(connectorName) ? "EMPTY" : connectorName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getConnectorName() {
        return this.connectorName;
    }

    public static Map<String, Object> toMap(ContainersMetadataTopicPartition topicPartition) {
        Map<String, Object> map = new HashMap<>();
        map.put(DATABASE_NAME_KEY, topicPartition.getDatabaseName());
        map.put(CONNECTOR_NAME_KEY, topicPartition.getConnectorName());
        return map;
    }
}
