// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CosmosSourceTaskConfig extends CosmosSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(CosmosSourceTaskConfig.class);
    private static final ObjectMapper objectMapper = Utils.getSimpleObjectMapper();
    private static final String SOURCE_TASK_CONFIG_PREFIX = "kafka.connect.cosmos.source.task.";

    public static final String SOURCE_METADATA_TASK_UNIT= SOURCE_TASK_CONFIG_PREFIX + "metadataTaskUnit";
    public static final String SOURCE_FEED_RANGE_TASK_UNITS = SOURCE_TASK_CONFIG_PREFIX + "feedRangeTaskUnits";

    private final List<FeedRangeTaskUnit> feedRangeTaskUnits;
    private MetadataTaskUnit metadataTaskUnit;

    public CosmosSourceTaskConfig(Map<String, String> parsedConfigs) {
        super(getConfigDef(), parsedConfigs);

        this.feedRangeTaskUnits = this.parseFeedRangeTaskUnits();
        this.metadataTaskUnit = this.parseMetadataTaskUnit();
    }

    public static ConfigDef getConfigDef() {
        ConfigDef configDef = CosmosSourceConfig.getConfigDef();
        defineTaskUnitsConfig(configDef);

        return configDef;
    }

    private static void defineTaskUnitsConfig(ConfigDef result) {
        //TODO: seems like all the configs does not provide default value will need to be configured, re-evaluate
        result
            .defineInternal(
                SOURCE_FEED_RANGE_TASK_UNITS,
                ConfigDef.Type.STRING,
                Collections.EMPTY_LIST,
                ConfigDef.Importance.HIGH
            )
            .defineInternal(
                SOURCE_METADATA_TASK_UNIT,
                ConfigDef.Type.STRING,
                null,
                ConfigDef.Importance.HIGH
            );
    }

    private List<FeedRangeTaskUnit> parseFeedRangeTaskUnits() {
        try {
            String feedRangesTaskUnitsConfig = this.getString(SOURCE_FEED_RANGE_TASK_UNITS);
            if (!StringUtils.isEmpty(feedRangesTaskUnitsConfig)) {
                return objectMapper
                    .readValue(feedRangesTaskUnitsConfig, new TypeReference<List<String>>() {})
                    .stream()
                    .map(taskUnitConfigJson -> {
                        try {
                            return objectMapper.readValue(taskUnitConfigJson, FeedRangeTaskUnit.class);
                        } catch (JsonProcessingException e) {
                            throw new IllegalArgumentException("Failed to parseFeedRangeTaskUnit", e);
                        }
                    })
                    .collect(Collectors.toList());
            }

            return new ArrayList<>();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parseFeedRangeTaskUnits", e);
        }

    }

    private MetadataTaskUnit parseMetadataTaskUnit() {
        String metadataTaskUnitConfig = this.getString(SOURCE_METADATA_TASK_UNIT);
        if (!StringUtils.isEmpty(metadataTaskUnitConfig)) {
            try {
                return objectMapper.readValue(metadataTaskUnitConfig, MetadataTaskUnit.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to parseMetadataTaskUnit", e);
            }
        }

        return null;
    }

    public static Map<String, String> getFeedRangeTaskUnitsConfigMap(List<FeedRangeTaskUnit> feedRangeTaskUnits) {
        try {
            Map<String, String> taskConfigMap = new HashMap<>();
            taskConfigMap.put(
                SOURCE_FEED_RANGE_TASK_UNITS,
                objectMapper.writeValueAsString(
                    feedRangeTaskUnits
                        .stream()
                        .map(taskUnit -> {
                            try {
                                return objectMapper.writeValueAsString(taskUnit);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList())
                ));
            return taskConfigMap;
        } catch (JsonProcessingException e) {
            throw new ConfigException("Failed to getFeedRangTaskUnitsConfigMap ", e);
        }
    }

    public static Map<String, String> getMetadataTaskUnitConfigMap(MetadataTaskUnit metadataTaskUnit) {
        try {
            Map<String, String> taskConfigMap = new HashMap<>();
            if (metadataTaskUnit != null) {
                taskConfigMap.put(SOURCE_METADATA_TASK_UNIT, objectMapper.writeValueAsString(metadataTaskUnit));
            }
            return taskConfigMap;
        } catch (JsonProcessingException e) {
            throw new ConfigException("Failed to getFeedRangTaskUnitsConfigMap ", e);
        }
    }

    public List<FeedRangeTaskUnit> getFeedRangeTaskUnits() {
        return feedRangeTaskUnits;
    }

    public MetadataTaskUnit getMetadataTaskUnit() {
        return metadataTaskUnit;
    }
}
