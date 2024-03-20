// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTask;
import com.azure.cosmos.kafka.connect.implementation.sink.IdStrategies;
import com.azure.cosmos.kafka.connect.implementation.sink.ItemWriteStrategy;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.cosmos.kafka.connect.CosmosDBSinkConnectorTest.SinkConfigs.ALL_VALID_CONFIGS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.Assert.assertEquals;

public class CosmosDBSinkConnectorTest extends KafkaCosmosTestSuiteBase {
    @Test(groups = "unit")
    public void taskClass() {
        CosmosDBSinkConnector sinkConnector = new CosmosDBSinkConnector();
        assertEquals(sinkConnector.taskClass(), CosmosSinkTask.class);
    }

    @Test(groups = "unit")
    public void config() {
        CosmosDBSinkConnector sinkConnector = new CosmosDBSinkConnector();
        ConfigDef configDef = sinkConnector.config();
        Map<String, ConfigDef.ConfigKey> configs = configDef.configKeys();
        List<KafkaCosmosConfigEntry<?>> allValidConfigs = ALL_VALID_CONFIGS;

        for (KafkaCosmosConfigEntry<?> sinkConfigEntry : allValidConfigs) {
            assertThat(configs.containsKey(sinkConfigEntry.getName())).isTrue();

            configs.containsKey(sinkConfigEntry.getName());
            if (sinkConfigEntry.isOptional()) {
                assertThat(configs.get(sinkConfigEntry.getName()).defaultValue).isEqualTo(sinkConfigEntry.getDefaultValue());
            } else {
                assertThat(configs.get(sinkConfigEntry.getName()).defaultValue).isEqualTo(ConfigDef.NO_DEFAULT_VALUE);
            }
        }
    }

    @Test(groups = "unit")
    public void requiredConfig() {
        Config config = new CosmosDBSinkConnector().validate(Collections.emptyMap());
        Map<String, List<String>> errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("kafka.connect.cosmos.accountEndpoint").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.accountKey").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.sink.database.name").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.sink.containers.topicMap").size()).isGreaterThan(0);
    }

    @Test(groups = "unit")
    public void taskConfigs() {
        CosmosDBSinkConnector sinkConnector = new CosmosDBSinkConnector();

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("kafka.connect.cosmos.accountEndpoint", TestConfigurations.HOST);
        sinkConfigMap.put("kafka.connect.cosmos.accountKey", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("kafka.connect.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);
        sinkConnector.start(sinkConfigMap);

        int maxTask = 2;
        List<Map<String, String>> taskConfigs = sinkConnector.taskConfigs(maxTask);
        assertThat(taskConfigs.size()).isEqualTo(maxTask);

        for (Map<String, String> taskConfig : taskConfigs) {
            assertThat(taskConfig.get("kafka.connect.cosmos.accountEndpoint")).isEqualTo(TestConfigurations.HOST);
            assertThat(taskConfig.get("kafka.connect.cosmos.accountKey")).isEqualTo(TestConfigurations.MASTER_KEY);
            assertThat(taskConfig.get("kafka.connect.cosmos.sink.database.name")).isEqualTo(databaseName);
            assertThat(taskConfig.get("kafka.connect.cosmos.sink.containers.topicMap"))
                .isEqualTo(singlePartitionContainerName + "#" + singlePartitionContainerName);
        }
    }

    @Test(groups = "unit")
    public void misFormattedConfig() {
        CosmosDBSinkConnector sinkConnector = new CosmosDBSinkConnector();
        Map<String, String> sinkConfigMap = this.getValidSinkConfig();

        String topicMapConfigName = "kafka.connect.cosmos.sink.containers.topicMap";
        sinkConfigMap.put(topicMapConfigName, UUID.randomUUID().toString());

        Config validatedConfig = sinkConnector.validate(sinkConfigMap);
        ConfigValue configValue =
            validatedConfig
                .configValues()
                .stream()
                .filter(config -> config.name().equalsIgnoreCase(topicMapConfigName))
                .findFirst()
                .get();

        assertThat(configValue.errorMessages()).isNotNull();
        assertThat(
            configValue
                .errorMessages()
                .get(0)
                .contains(
                    "The topic-container map should be a comma-delimited list of Kafka topic to Cosmos containers." +
                        " Each mapping should be a pair of Kafka topic and Cosmos container separated by '#'." +
                        " For example: topic1#con1,topic2#con2."))
            .isTrue();

        // TODO[Public Preview]: add other config validations
    }

    private Map<String, String> getValidSinkConfig() {
        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("kafka.connect.cosmos.accountEndpoint", TestConfigurations.HOST);
        sinkConfigMap.put("kafka.connect.cosmos.accountKey", TestConfigurations.MASTER_KEY);
        sinkConfigMap.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("kafka.connect.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);

        return sinkConfigMap;
    }

    public static class SinkConfigs {
        public static final List<KafkaCosmosConfigEntry<?>> ALL_VALID_CONFIGS = Arrays.asList(
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.accountEndpoint", null, false),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.accountKey", null, false),
            new KafkaCosmosConfigEntry<Boolean>("kafka.connect.cosmos.useGatewayMode", false, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.preferredRegionsList", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.applicationName", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.sink.errors.tolerance", "None", true),
            new KafkaCosmosConfigEntry<Boolean>("kafka.connect.cosmos.sink.bulk.enabled", true, true),
            new KafkaCosmosConfigEntry<Integer>("kafka.connect.cosmos.sink.bulk.maxConcurrentCosmosPartitions", -1, true),
            new KafkaCosmosConfigEntry<Integer>("kafka.connect.cosmos.sink.bulk.initialBatchSize", 1, true),
            new KafkaCosmosConfigEntry<String>(
                "kafka.connect.cosmos.sink.write.strategy",
                ItemWriteStrategy.ITEM_OVERWRITE.getName(),
                true),
            new KafkaCosmosConfigEntry<Integer>("kafka.connect.cosmos.sink.maxRetryCount", 10, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.sink.database.name", null, false),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.sink.containers.topicMap", null, false),
            new KafkaCosmosConfigEntry<String>(
                "kafka.connect.cosmos.sink.id.strategy",
                IdStrategies.PROVIDED_IN_VALUE_STRATEGY.getName(),
                true)
        );
    }
}
