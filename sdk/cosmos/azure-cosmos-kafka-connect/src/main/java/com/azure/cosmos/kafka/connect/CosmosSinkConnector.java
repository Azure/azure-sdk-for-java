// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.apachecommons.lang.RandomUtils;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConstants;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTask;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTaskConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
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
public final class CosmosSinkConnector extends SinkConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSinkConnector.class);
    private static final String CONNECTOR_NAME = "name";

    private CosmosSinkConfig sinkConfig;
    private String connectorName;

    @Override
    public void start(Map<String, String> props) {
        LOGGER.info("Starting the kafka cosmos sink connector");
        this.sinkConfig = new CosmosSinkConfig(props);
        this.connectorName = props.containsKey(CONNECTOR_NAME) ? props.get(CONNECTOR_NAME).toString() : "EMPTY";
        validateContainers();
    }

    private void validateContainers() {
        List<String> containerNames = new ArrayList<>(this.sinkConfig.getContainersConfig().getTopicToContainerMap().values());
        StringBuilder queryStringBuilder = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>();

        queryStringBuilder.append("SELECT * FROM c WHERE c.id IN ( ");
        for (int i = 0; i < containerNames.size(); i++) {
            String idValue = containerNames.get(i);
            String idParamName = "@param" + i;

            parameters.add(new SqlParameter(idParamName, idValue));
            queryStringBuilder.append(idParamName);

            if (i < containerNames.size() - 1) {
                queryStringBuilder.append(", ");
            }
        }
        queryStringBuilder.append(" )");
        CosmosAsyncClient cosmosAsyncClient = CosmosClientStore.getCosmosClient(this.sinkConfig.getAccountConfig(), connectorName);
        List<CosmosContainerProperties> cosmosContainerProperties = cosmosAsyncClient.getDatabase(this.sinkConfig.getContainersConfig().getDatabaseName())
            .queryContainers(new SqlQuerySpec(queryStringBuilder.toString(), parameters))
            .byPage()
            .flatMapIterable(response -> response.getResults())
            .collectList()
            .onErrorMap(throwable -> KafkaCosmosExceptionsHelper.convertToConnectException(throwable, "getAllContainers failed.")).block();
        if (cosmosContainerProperties.isEmpty() || cosmosContainerProperties.size() != containerNames.size()) {
            throw new IllegalStateException("Containers specified in the topic to container map do not exist in the CosmosDB account.");
        }
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
            Map<String, String> taskConfigs = this.sinkConfig.originalsStrings();
            taskConfigs.put(CosmosSinkTaskConfig.SINK_TASK_ID,
                String.format("%s-%s-%d",
                    "sink",
                    this.connectorName,
                    RandomUtils.nextInt(1, 9999999)));
            configs.add(taskConfigs);
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
