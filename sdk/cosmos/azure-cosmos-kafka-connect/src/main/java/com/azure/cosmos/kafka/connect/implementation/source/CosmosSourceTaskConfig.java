// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.config.ConfigDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CosmosSourceTaskConfig extends CosmosSourceConfig {
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    public static final String SOURCE_METADATA_TASK_UNIT = "azure.cosmos.source.task.metadataTaskUnit";
    public static final String SOURCE_FEED_RANGE_TASK_UNITS = "azure.cosmos.source.task.feedRangeTaskUnits";
    public static final String SOURCE_TASK_ID = "azure.cosmos.source.task.id";

    private final List<FeedRangeTaskUnit> feedRangeTaskUnits;
    private final MetadataTaskUnit metadataTaskUnit;
    private final String taskId;

    public CosmosSourceTaskConfig(Map<String, String> parsedConfigs) {
        super(getConfigDef(), parsedConfigs);

        this.feedRangeTaskUnits = this.parseFeedRangeTaskUnits();
        this.metadataTaskUnit = this.parseMetadataTaskUnit();
        this.taskId = this.getString(SOURCE_TASK_ID);
    }

    public static ConfigDef getConfigDef() {
        ConfigDef configDef = CosmosSourceConfig.getConfigDef();
        defineTaskUnitsConfig(configDef);

        return configDef;
    }

    private static void defineTaskUnitsConfig(ConfigDef result) {
        result
            .defineInternal(
                SOURCE_FEED_RANGE_TASK_UNITS,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH
            )
            .defineInternal(
                SOURCE_METADATA_TASK_UNIT,
                ConfigDef.Type.STRING,
                null,
                ConfigDef.Importance.HIGH
            )
            .defineInternal(
                SOURCE_TASK_ID,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.MEDIUM
            );
    }

    private List<FeedRangeTaskUnit> parseFeedRangeTaskUnits() {
        String feedRangesTaskUnitsConfig = this.getString(SOURCE_FEED_RANGE_TASK_UNITS);

        try {
            if (!StringUtils.isEmpty(feedRangesTaskUnitsConfig)) {
                return OBJECT_MAPPER
                    .readValue(feedRangesTaskUnitsConfig, new TypeReference<List<String>>() {})
                    .stream()
                    .map(taskUnitConfigJson -> {
                        try {
                            return OBJECT_MAPPER.readValue(taskUnitConfigJson, FeedRangeTaskUnit.class);
                        } catch (JsonProcessingException e) {
                            throw new IllegalArgumentException("Failed to parseFeedRangeTaskUnit[" + taskUnitConfigJson + "]", e);
                        }
                    })
                    .collect(Collectors.toList());
            }

            return new ArrayList<>();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parseFeedRangeTaskUnits[" + feedRangesTaskUnitsConfig + "]", e);
        }

    }

    private MetadataTaskUnit parseMetadataTaskUnit() {
        String metadataTaskUnitConfig = this.getString(SOURCE_METADATA_TASK_UNIT);
        if (!StringUtils.isEmpty(metadataTaskUnitConfig)) {
            try {
                return OBJECT_MAPPER.readValue(metadataTaskUnitConfig, MetadataTaskUnit.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to parseMetadataTaskUnit[" + metadataTaskUnitConfig + "]", e);
            }
        }

        return null;
    }

    public static Map<String, String> getFeedRangeTaskUnitsConfigMap(List<FeedRangeTaskUnit> feedRangeTaskUnits) {
        try {
            Map<String, String> taskConfigMap = new HashMap<>();
            taskConfigMap.put(
                SOURCE_FEED_RANGE_TASK_UNITS,
                OBJECT_MAPPER.writeValueAsString(
                    feedRangeTaskUnits
                        .stream()
                        .map(taskUnit -> {
                            try {
                                return OBJECT_MAPPER.writeValueAsString(taskUnit);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList())
                ));
            return taskConfigMap;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getMetadataTaskUnitConfigMap(MetadataTaskUnit metadataTaskUnit) {
        try {
            Map<String, String> taskConfigMap = new HashMap<>();
            if (metadataTaskUnit != null) {
                taskConfigMap.put(SOURCE_METADATA_TASK_UNIT, OBJECT_MAPPER.writeValueAsString(metadataTaskUnit));
            }
            return taskConfigMap;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FeedRangeTaskUnit> getFeedRangeTaskUnits() {
        return feedRangeTaskUnits;
    }

    public MetadataTaskUnit getMetadataTaskUnit() {
        return metadataTaskUnit;
    }

    public String getTaskId() {
        return taskId;
    }
}
