package com.azure.cosmos.kafka.connect.implementation.sink;

import java.util.Map;

public class CosmosSinkContainersConfig {
    private final String databaseName;
    private final Map<String, String> topicToContainerMap;

    public CosmosSinkContainersConfig(String databaseName, Map<String, String> topicToContainerMap) {
        this.databaseName = databaseName;
        this.topicToContainerMap = topicToContainerMap;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Map<String, String> getTopicToContainerMap() {
        return topicToContainerMap;
    }
}
