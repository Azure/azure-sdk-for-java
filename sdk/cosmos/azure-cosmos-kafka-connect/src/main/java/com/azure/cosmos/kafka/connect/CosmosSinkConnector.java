// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConstants;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTask;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig.validateCosmosAccountAuthConfig;
import static com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig.validateThroughputControlConfig;
import static com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig.validateWriteConfig;

/**
 * A Sink connector that publishes topic messages to CosmosDB.
 */
public class CosmosSinkConnector extends SinkConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSinkConnector.class);

    private CosmosSinkConfig sinkConfig;

    @Override
    public void start(Map<String, String> props) {
        LOGGER.info("Starting the kafka cosmos sink connector");
        this.sinkConfig = new CosmosSinkConfig(props);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return CosmosSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        LOGGER.info("Setting task configurations with maxTasks {}", maxTasks);
        List<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) {
            configs.add(this.sinkConfig.originalsStrings());
        }

        return configs;
    }

    @Override
    public void stop() {
        LOGGER.info("Kafka Cosmos sink connector {} is stopped.");
    }

    @Override
    public ConfigDef config() {
        return CosmosSinkConfig.getConfigDef();
    }

    @Override
    public String version() {
        return KafkaCosmosConstants.CURRENT_VERSION;
    }

    @Override
    public Config validate(Map<String, String> connectorConfigs) {
        Config config = super.validate(connectorConfigs);
        //there are errors based on the config def
        if (config.configValues().stream().anyMatch(cv -> !cv.errorMessages().isEmpty())) {
            return config;
        }

        Map<String, ConfigValue> configValues =
            config
                .configValues()
                .stream()
                .collect(Collectors.toMap(ConfigValue::name, Function.identity()));

        validateCosmosAccountAuthConfig(configValues);
        validateThroughputControlConfig(configValues);
        validateWriteConfig(configValues);
        return config;
    }
}
