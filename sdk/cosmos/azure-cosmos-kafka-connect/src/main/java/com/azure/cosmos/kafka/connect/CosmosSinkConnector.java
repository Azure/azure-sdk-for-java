// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.apachecommons.lang.RandomUtils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.CosmosThroughputControlConfig;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConstants;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosUtils;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkContainersConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTask;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTaskConfig;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.kafka.connect.implementation.CosmosContainerUtils.validateDatabaseAndContainers;
import static com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig.validateCosmosAccountAuthConfig;
import static com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig.validateThroughputControlConfig;
import static com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig.validateWriteConfig;

/**
 * A Sink connector that publishes topic messages to CosmosDB.
 */
public final class CosmosSinkConnector extends SinkConnector implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSinkConnector.class);
    private static final String CONNECTOR_NAME = "name";

    private CosmosSinkConfig sinkConfig;
    private String connectorName;
    private CosmosAsyncClient cosmosClient;

    @Override
    public void start(Map<String, String> props) {
        LOGGER.info("Starting the kafka cosmos sink connector");
        this.sinkConfig = new CosmosSinkConfig(props);
        this.connectorName = props.containsKey(CONNECTOR_NAME) ? props.get(CONNECTOR_NAME).toString() : "EMPTY";
        CosmosSinkContainersConfig containersConfig = this.sinkConfig.getContainersConfig();
        this.cosmosClient =
            CosmosClientStore.getCosmosClient(this.sinkConfig.getAccountConfig(), this.connectorName);
        validateDatabaseAndContainers(
            new ArrayList<>(containersConfig.getTopicToContainerMap().values()),
            this.cosmosClient,
            containersConfig.getDatabaseName());
    }

    @Override
    public Class<? extends Task> taskClass() {
        return CosmosSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        LOGGER.info("Setting task configurations with maxTasks {}", maxTasks);
        List<Map<String, String>> configs = new ArrayList<>();

        String clientMetadataCachesString = getClientMetadataCachesSnapshotString();
        String throughputControlClientMetadataCachesString = getThroughputControlClientMetadataCachesSnapshotString();

        for (int i = 0; i < maxTasks; i++) {
            Map<String, String> taskConfigs = this.sinkConfig.originalsStrings();
            taskConfigs.put(CosmosSinkTaskConfig.SINK_TASK_ID,
                String.format("%s-%s-%d",
                    "sink",
                    this.connectorName,
                    RandomUtils.nextInt(1, 9999999)));
            if (StringUtils.isNotEmpty(clientMetadataCachesString)) {
                taskConfigs.put(
                    CosmosSinkTaskConfig.COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT,
                    clientMetadataCachesString);
            }

            if (StringUtils.isNotEmpty(throughputControlClientMetadataCachesString)) {
                taskConfigs.put(
                    CosmosSinkTaskConfig.THROUGHPUT_CONTROL_COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT,
                    throughputControlClientMetadataCachesString);
            }
            configs.add(taskConfigs);
        }

        return configs;
    }

    private String getClientMetadataCachesSnapshotString() {
        CosmosSinkContainersConfig containersConfig = this.sinkConfig.getContainersConfig();
        List<String> containerNames = new ArrayList<>(containersConfig.getTopicToContainerMap().values());
        CosmosAsyncDatabase database = this.cosmosClient.getDatabase(containersConfig.getDatabaseName());

        // read a random item from each container to populate the collection cache
        for (String containerName : containerNames) {
            CosmosAsyncContainer container = database.getContainer(containerName);
            readRandomItemFromContainer(container);
        }

        // read a random item from throughput control container if it is enabled and use the same account config as the cosmos client
        CosmosThroughputControlConfig cosmosThroughputControlConfig = this.sinkConfig.getThroughputControlConfig();
        if (cosmosThroughputControlConfig.isThroughputControlEnabled()) {
            if (cosmosThroughputControlConfig.getThroughputControlAccountConfig() == null) {
                CosmosAsyncContainer throughputControlContainer =
                    this.cosmosClient
                        .getDatabase(cosmosThroughputControlConfig.getGlobalThroughputControlDatabaseName())
                        .getContainer(cosmosThroughputControlConfig.getGlobalThroughputControlContainerName());
                readRandomItemFromContainer(throughputControlContainer);
            }
        }

        return KafkaCosmosUtils.convertClientMetadataCacheSnapshotToString(this.cosmosClient);
    }

    private String getThroughputControlClientMetadataCachesSnapshotString() {
        CosmosAsyncClient throughputControlClient = null;
        CosmosThroughputControlConfig throughputControlConfig = this.sinkConfig.getThroughputControlConfig();

        try {
            if (throughputControlConfig.isThroughputControlEnabled()
                && throughputControlConfig.getThroughputControlAccountConfig() != null) {
                throughputControlClient = CosmosClientStore.getCosmosClient(
                    throughputControlConfig.getThroughputControlAccountConfig(),
                    this.connectorName
                );
            }

            if (throughputControlClient != null) {
                readRandomItemFromContainer(
                    throughputControlClient
                        .getDatabase(throughputControlConfig.getGlobalThroughputControlDatabaseName())
                        .getContainer(throughputControlConfig.getGlobalThroughputControlContainerName()));
            }
            return KafkaCosmosUtils.convertClientMetadataCacheSnapshotToString(throughputControlClient);

        } finally {
            if (throughputControlClient != null) {
                throughputControlClient.close();
            }
        }
    }

    private void readRandomItemFromContainer(CosmosAsyncContainer container) {
        if (container != null) {
            container.readItem(UUID.randomUUID().toString(), new PartitionKey(UUID.randomUUID().toString()), JsonNode.class)
                .onErrorResume(throwable -> {
                    if (!KafkaCosmosExceptionsHelper.isNotFoundException(throwable)) {
                        LOGGER.warn("Failed to read item from container {}", container.getId(), throwable);
                    }
                    return Mono.empty();
                })
                .block();
        }
    }

    @Override
    public void stop() {
        if (this.cosmosClient != null) {
            this.cosmosClient.close();
        }
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

    @Override
    public void close() {
        this.stop();
    }
}
