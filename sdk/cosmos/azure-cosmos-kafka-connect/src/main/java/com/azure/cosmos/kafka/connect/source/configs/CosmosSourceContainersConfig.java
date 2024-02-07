// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source.configs;

import java.util.List;

public class CosmosSourceContainersConfig {
    private final String databaseName;
    private final boolean includeAllContainers;
    private final List<String> includedContainers;
    private final List<String> containersTopicMap;

    public CosmosSourceContainersConfig(
        String databaseName,
        boolean includeAllContainers,
        List<String> includedContainers,
        List<String> containersTopicMap) {

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
