// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class FeedRangesMetadataTopicPartition {
    public static final String DATABASE_NAME_KEY = "database";
    public static final String CONTAINER_RESOURCE_ID_KEY = "containerResourceId";
    public static final String CONNECTOR_NAME_KEY = "connectorName";
    private final String databaseName;
    private final String containerRid;
    private final String connectorName;

    public FeedRangesMetadataTopicPartition(String databaseName, String containerRid, String connectorName) {
        checkArgument(StringUtils.isNotEmpty(databaseName), "Argument 'databaseName' should not be null");
        checkArgument(StringUtils.isNotEmpty(containerRid), "Argument 'containerRid' should not be null");

        this.databaseName = databaseName;
        this.containerRid = containerRid;
        this.connectorName = StringUtils.isEmpty(connectorName) ? "EMPTY" : connectorName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getContainerRid() {
        return containerRid;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public static Map<String, Object> toMap(FeedRangesMetadataTopicPartition partition) {
        Map<String, Object> map = new HashMap<>();
        map.put(DATABASE_NAME_KEY, partition.getDatabaseName());
        map.put(CONTAINER_RESOURCE_ID_KEY, partition.getContainerRid());
        map.put(CONNECTOR_NAME_KEY, partition.getConnectorName());
        return map;
    }
}
