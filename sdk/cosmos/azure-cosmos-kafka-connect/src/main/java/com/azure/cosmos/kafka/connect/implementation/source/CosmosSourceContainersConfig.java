// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosSourceContainersConfig {
    public static final String CONTAINER_TOPIC_MAP_SEPARATOR = "#";

    private final String databaseName;
    private final boolean includeAllContainers;
    private final List<String> includedContainers;
    private final List<String> containersTopicMap;

    public CosmosSourceContainersConfig(
        String databaseName,
        boolean includeAllContainers,
        List<String> includedContainers,
        List<String> containersTopicMap) {

        checkArgument(StringUtils.isNotEmpty(databaseName), "Argument 'databaseName' can not be null");
        checkNotNull(includedContainers, "Argument 'includedContainers' can not be null");

        this.databaseName = databaseName;
        this.includeAllContainers = includeAllContainers;
        this.includedContainers = includedContainers;
        this.containersTopicMap = containersTopicMap;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public boolean isIncludeAllContainers() {
        return includeAllContainers;
    }

    public List<String> getIncludedContainers() {
        return includedContainers;
    }

    public List<String> getContainersTopicMap() {
        return containersTopicMap;
    }
}
