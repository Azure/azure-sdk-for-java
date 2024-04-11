// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.kafka.connect.implementation.CosmosAuthTypes;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTask;
import com.azure.cosmos.kafka.connect.implementation.sink.IdStrategyType;
import com.azure.cosmos.kafka.connect.implementation.sink.ItemWriteStrategy;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.common.config.types.Password;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.Assert.assertEquals;

public class CosmosSinkConnectorTest extends KafkaCosmosTestSuiteBase {
    @Test(groups = "unit")
    public void taskClass() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();
        assertEquals(sinkConnector.taskClass(), CosmosSinkTask.class);
    }

    @Test(groups = "unit")
    public void config() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();
        ConfigDef configDef = sinkConnector.config();
        Map<String, ConfigDef.ConfigKey> configs = configDef.configKeys();
        List<KafkaCosmosConfigEntry<?>> allValidConfigs = CosmosSinkConnectorTest.SinkConfigs.ALL_VALID_CONFIGS;

        for (KafkaCosmosConfigEntry<?> sinkConfigEntry : allValidConfigs) {
            assertThat(configs.containsKey(sinkConfigEntry.getName())).isTrue();

            configs.containsKey(sinkConfigEntry.getName());
            if (sinkConfigEntry.isOptional()) {
                if (sinkConfigEntry.isPassword()) {
                    assertThat(((Password)configs.get(sinkConfigEntry.getName()).defaultValue).value())
                        .isEqualTo(sinkConfigEntry.getDefaultValue());
                } else {
                    assertThat(configs.get(sinkConfigEntry.getName()).defaultValue).isEqualTo(sinkConfigEntry.getDefaultValue());
                }
            } else {
                assertThat(configs.get(sinkConfigEntry.getName()).defaultValue).isEqualTo(ConfigDef.NO_DEFAULT_VALUE);
            }
        }
    }

    @Test(groups = "unit")
    public void requiredConfig() {
        Config config = new CosmosSinkConnector().validate(Collections.emptyMap());
        Map<String, List<String>> errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("kafka.connect.cosmos.accountEndpoint").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.sink.database.name").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.sink.containers.topicMap").size()).isGreaterThan(0);
    }

    @Test(groups = "unit")
    public void taskConfigs() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConfigMap.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConfigMap.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("kafka.connect.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);
        sinkConnector.start(sinkConfigMap);

        int maxTask = 2;
        List<Map<String, String>> taskConfigs = sinkConnector.taskConfigs(maxTask);
        assertThat(taskConfigs.size()).isEqualTo(maxTask);

        for (Map<String, String> taskConfig : taskConfigs) {
            assertThat(taskConfig.get("kafka.connect.cosmos.accountEndpoint")).isEqualTo(KafkaCosmosTestConfigurations.HOST);
            assertThat(taskConfig.get("kafka.connect.cosmos.accountKey")).isEqualTo(KafkaCosmosTestConfigurations.MASTER_KEY);
            assertThat(taskConfig.get("kafka.connect.cosmos.sink.database.name")).isEqualTo(databaseName);
            assertThat(taskConfig.get("kafka.connect.cosmos.sink.containers.topicMap"))
                .isEqualTo(singlePartitionContainerName + "#" + singlePartitionContainerName);
        }
    }

    @Test(groups = "unit")
    public void misFormattedConfig() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();
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

    @Test(groups = { "unit" })
    public void sinkConfigWithThroughputControl() {
        String throughputControlGroupName = "test";
        int targetThroughput= 6;
        double targetThroughputThreshold = 0.1;
        String throughputControlDatabaseName = "throughputControlDatabase";
        String throughputControlContainerName = "throughputControlContainer";

        Map<String, String> sourceConfigMap = this.getValidSinkConfig();
        sourceConfigMap.put("kafka.connect.cosmos.throughputControl.enabled", "true");
        sourceConfigMap.put("kafka.connect.cosmos.throughputControl.name", throughputControlGroupName);
        sourceConfigMap.put("kafka.connect.cosmos.throughputControl.targetThroughput", String.valueOf(targetThroughput));
        sourceConfigMap.put("kafka.connect.cosmos.throughputControl.targetThroughputThreshold", String.valueOf(targetThroughputThreshold));
        sourceConfigMap.put("kafka.connect.cosmos.throughputControl.globalControl.database", throughputControlDatabaseName);
        sourceConfigMap.put("kafka.connect.cosmos.throughputControl.globalControl.container", throughputControlContainerName);

        CosmosSinkConfig sinkConfig = new CosmosSinkConfig(sourceConfigMap);
        assertThat(sinkConfig.getThroughputControlConfig()).isNotNull();
        assertThat(sinkConfig.getThroughputControlConfig().isThroughputControlEnabled()).isTrue();
        assertThat(sinkConfig.getThroughputControlConfig().getThroughputControlAccountConfig()).isNull();
        assertThat(sinkConfig.getThroughputControlConfig().getThroughputControlGroupName()).isEqualTo(throughputControlGroupName);
        assertThat(sinkConfig.getThroughputControlConfig().getTargetThroughput()).isEqualTo(targetThroughput);
        assertThat(sinkConfig.getThroughputControlConfig().getTargetThroughputThreshold()).isEqualTo(targetThroughputThreshold);
        assertThat(sinkConfig.getThroughputControlConfig().getGlobalThroughputControlDatabaseName()).isEqualTo(throughputControlDatabaseName);
        assertThat(sinkConfig.getThroughputControlConfig().getGlobalThroughputControlContainerName()).isEqualTo(throughputControlContainerName);
        assertThat(sinkConfig.getThroughputControlConfig().getGlobalThroughputControlRenewInterval()).isNull();
        assertThat(sinkConfig.getThroughputControlConfig().getGlobalThroughputControlExpireInterval()).isNull();
    }

    @Test(groups = { "unit" })
    public void invalidThroughputControlConfig() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();
        // invalid targetThroughput, targetThroughputThreshold, priorityLevel config and missing required config for throughput control container info
        Map<String, String> sinkConfigMap = this.getValidSinkConfig();
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.enabled", "true");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.targetThroughput", "-1");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.targetThroughputThreshold", "-1");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.priorityLevel", "None");

        Config config = sinkConnector.validate(sinkConfigMap);
        Map<String, List<String>> errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.name").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.targetThroughput").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.targetThroughputThreshold").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.priorityLevel").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.globalControl.database").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.globalControl.container").size()).isGreaterThan(0);

        // invalid throughput control account config with masterKey auth
        sinkConfigMap = this.getValidSinkConfig();
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.enabled", "true");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.targetThroughput", "1");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.globalControl.database", "ThroughputControlDatabase");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.globalControl.container", "ThroughputControlContainer");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.name", "groupName");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.accountEndpoint", TestConfigurations.HOST);

        config = sinkConnector.validate(sinkConfigMap);
        errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.accountKey").size()).isGreaterThan(0);

        // targetThroughputThreshold is not supported when using add auth for throughput control
        sinkConfigMap = this.getValidSinkConfig();
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.enabled", "true");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.targetThroughputThreshold", "0.9");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.globalControl.database", "ThroughputControlDatabase");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.globalControl.container", "ThroughputControlContainer");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.name", "groupName");
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.accountEndpoint", TestConfigurations.HOST);
        sinkConfigMap.put("kafka.connect.cosmos.throughputControl.auth.type", CosmosAuthTypes.SERVICE_PRINCIPAL.getName());

        config = sinkConnector.validate(sinkConfigMap);
        errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.auth.aad.clientId").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.auth.aad.clientSecret").size()).isGreaterThan(0);
        assertThat(errorMessages.get("kafka.connect.cosmos.throughputControl.account.tenantId").size()).isGreaterThan(0);
    }

    private Map<String, String> getValidSinkConfig() {
        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("kafka.connect.cosmos.accountEndpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConfigMap.put("kafka.connect.cosmos.accountKey", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConfigMap.put("kafka.connect.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("kafka.connect.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);

        return sinkConfigMap;
    }

    public static class SinkConfigs {
        public static final List<KafkaCosmosConfigEntry<?>> ALL_VALID_CONFIGS = Arrays.asList(
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.accountEndpoint", null, false),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.account.tenantId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.auth.type", CosmosAuthTypes.MASTER_KEY.getName(), true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.accountKey", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.auth.aad.clientId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.auth.aad.clientSecret", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<Boolean>("kafka.connect.cosmos.useGatewayMode", false, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.preferredRegionsList", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.applicationName", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.enabled", false, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.accountEndpoint", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.throughputControl.account.tenantId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.throughputControl.auth.type", CosmosAuthTypes.MASTER_KEY.getName(), true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.accountKey", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.throughputControl.auth.aad.clientId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("kafka.connect.cosmos.throughputControl.auth.aad.clientSecret", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.preferredRegionsList", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.useGatewayMode", false, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.name", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.targetThroughput", -1, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.targetThroughputThreshold", -1d, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.priorityLevel", "None", true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.globalControl.database", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.globalControl.container", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.globalControl.renewIntervalInMS", -1, true),
            new KafkaCosmosConfigEntry<>("kafka.connect.cosmos.throughputControl.globalControl.expireIntervalInMS", -1, true),

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
                IdStrategyType.PROVIDED_IN_VALUE_STRATEGY.getName(),
                true)
        );
    }
}
