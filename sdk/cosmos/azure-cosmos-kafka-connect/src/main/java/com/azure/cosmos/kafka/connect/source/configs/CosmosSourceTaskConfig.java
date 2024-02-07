// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source.configs;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.kafka.connect.common.CosmosAccountConfig;
import com.azure.cosmos.kafka.connect.common.CosmosDiagnosticsConfig;
import com.azure.cosmos.kafka.connect.source.FeedRangeTaskUnit;
import com.azure.cosmos.kafka.connect.source.MetadataTaskUnit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.config.ConfigException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmosSourceTaskConfig {
    private static final ObjectMapper objectMapper = Utils.getSimpleObjectMapper();
    public static final String SOURCE_TASK_ACCOUNT_CONFIG = "kafka.connect.cosmos.source.task.accountConfig";
    public static final String SOURCE_TASK_DIAGNOSTIC_CONFIG = "kafka.connect.cosmos.source.task.diagnosticsConfig";
    public static final String SOURCE_TASK_CHANGE_FEED_CONFIG = "kafka.connect.cosmos.source.task.changeFeedConfig";
    public static final String SOURCE_TASK_MESSAGE_KEY_CONFIG = "kafka.connect.cosmos.source.task.messageKeyConfig";
    public static final String SOURCE_METADATA_TASK_UNIT= "kafka.connect.cosmos.source.task.metadataTaskUnit";
    public static final String SOURCE_FEED_RANGE_TASK_UNITS = "kafka.connect.cosmos.source.task.feedRangeTaskUnits";

    private final CosmosAccountConfig accountConfig;
    private final CosmosDiagnosticsConfig diagnosticsConfig;
    private final CosmosSourceChangeFeedConfig changeFeedConfig;
    private final CosmosSourceMessageKeyConfig messageKeyConfig;
    private final List<FeedRangeTaskUnit> feedRangeTaskUnits;
    private MetadataTaskUnit metadataTaskUnit;

    public CosmosSourceTaskConfig(
        CosmosAccountConfig accountConfig,
        CosmosDiagnosticsConfig diagnosticsConfig,
        CosmosSourceChangeFeedConfig changeFeedConfig,
        CosmosSourceMessageKeyConfig messageKeyConfig,
        List<FeedRangeTaskUnit> feedRangeTaskUnits) {

        this.accountConfig = accountConfig;
        this.diagnosticsConfig = diagnosticsConfig;
        this.changeFeedConfig = changeFeedConfig;
        this.messageKeyConfig = messageKeyConfig;
        this.feedRangeTaskUnits = feedRangeTaskUnits;
    }

    public Map<String, String> getTaskConfigMap() {
        try {
            Map<String, String> taskConfigMap = new HashMap<>();

            taskConfigMap.put(SOURCE_TASK_ACCOUNT_CONFIG, objectMapper.writeValueAsString(this.accountConfig));
            taskConfigMap.put(SOURCE_TASK_DIAGNOSTIC_CONFIG, objectMapper.writeValueAsString(this.diagnosticsConfig));
            taskConfigMap.put(SOURCE_TASK_CHANGE_FEED_CONFIG, objectMapper.writeValueAsString(this.changeFeedConfig));
            taskConfigMap.put(SOURCE_TASK_MESSAGE_KEY_CONFIG, objectMapper.writeValueAsString(this.messageKeyConfig));
            taskConfigMap.put(SOURCE_FEED_RANGE_TASK_UNITS, objectMapper.writeValueAsString(this.feedRangeTaskUnits));

            if (this.metadataTaskUnit != null) {
                taskConfigMap.put(SOURCE_METADATA_TASK_UNIT, objectMapper.writeValueAsString(this.metadataTaskUnit));
            }

            return taskConfigMap;
        } catch (JsonProcessingException e) {
            throw new ConfigException("Failed to create task config ", e);
        }
    }

    public static CosmosSourceTaskConfig parseTaskConfig(Map<String, String> taskConfigMap) {
        CosmosAccountConfig accountConfig =
            objectMapper.convertValue(
                taskConfigMap.get(SOURCE_TASK_ACCOUNT_CONFIG),
                CosmosAccountConfig.class);

        CosmosDiagnosticsConfig diagnosticsConfig =
            objectMapper.convertValue(
                taskConfigMap.get(SOURCE_TASK_DIAGNOSTIC_CONFIG),
                CosmosDiagnosticsConfig.class);

        CosmosSourceChangeFeedConfig changeFeedConfig =
            objectMapper.convertValue(
                taskConfigMap.get(SOURCE_TASK_CHANGE_FEED_CONFIG),
                CosmosSourceChangeFeedConfig.class);

        CosmosSourceMessageKeyConfig messageKeyConfig =
            objectMapper.convertValue(
                taskConfigMap.get(SOURCE_TASK_MESSAGE_KEY_CONFIG),
                CosmosSourceMessageKeyConfig.class);

        List<FeedRangeTaskUnit> feedRangeTaskUnits =
            objectMapper.convertValue(taskConfigMap.get(SOURCE_FEED_RANGE_TASK_UNITS), List.class);

        CosmosSourceTaskConfig taskConfig = new CosmosSourceTaskConfig(
            accountConfig,
            diagnosticsConfig,
            changeFeedConfig,
            messageKeyConfig,
            feedRangeTaskUnits);

        if (taskConfigMap.containsKey(SOURCE_METADATA_TASK_UNIT)) {
            MetadataTaskUnit metadataTaskUnit =
                objectMapper.convertValue(taskConfigMap.get(SOURCE_METADATA_TASK_UNIT), MetadataTaskUnit.class);
            taskConfig.assignMetadataTask(metadataTaskUnit);
        }

        return taskConfig;
    }

    public void assignMetadataTask(MetadataTaskUnit metadataTaskUnit) {
        this.metadataTaskUnit = metadataTaskUnit;
    }

    public CosmosAccountConfig getAccountConfig() {
        return accountConfig;
    }

    public CosmosDiagnosticsConfig getDiagnosticsConfig() {
        return diagnosticsConfig;
    }

    public CosmosSourceChangeFeedConfig getChangeFeedConfig() {
        return changeFeedConfig;
    }

    public CosmosSourceMessageKeyConfig getMessageKeyConfig() {
        return messageKeyConfig;
    }

    public List<FeedRangeTaskUnit> getFeedRangeTaskUnits() {
        return feedRangeTaskUnits;
    }

    public MetadataTaskUnit getMetadataTaskUnit() {
        return metadataTaskUnit;
    }
}
