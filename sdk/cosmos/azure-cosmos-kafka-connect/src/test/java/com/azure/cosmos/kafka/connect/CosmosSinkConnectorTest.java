// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.kafka.connect.implementation.CosmosAadAuthConfig;
import com.azure.cosmos.kafka.connect.implementation.CosmosAuthType;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosUtils;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTask;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTaskConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.IdStrategyType;
import com.azure.cosmos.kafka.connect.implementation.sink.ItemWriteStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.patch.KafkaCosmosPatchOperationType;
import com.azure.cosmos.models.CosmosContainerProperties;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.common.config.types.Password;
import org.testcontainers.shaded.org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.Assert.assertEquals;

public class CosmosSinkConnectorTest extends KafkaCosmosTestSuiteBase {

    @DataProvider(name = "patchPropertyConfigParameterProvider")
    public static Object[][] patchPropertyConfigParameterProvider() {
        return new Object[][]{
            // patch property config, isValid
            { "property", false },
            { "property(name)", false },
            { "property(name).op(remove)", true },
            { "property(name).path(cosmospath).op(add)", true },
            { "Property(name).pAth(cosmospath).Op(add)", true },
            { "Property(name).pAth(cosmospath).Op(add), property(name2).op(set)", true },
        };
    }

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
        List<KafkaCosmosConfigEntry<?>> allValidConfigs = SinkConfigs.ALL_VALID_CONFIGS;

        for (KafkaCosmosConfigEntry<?> sinkConfigEntry : allValidConfigs) {
            System.out.println(sinkConfigEntry.getName());
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
        assertThat(errorMessages.get("azure.cosmos.account.endpoint").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.sink.database.name").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.sink.containers.topicMap").size()).isGreaterThan(0);
    }

    @Test(groups = "kafka-emulator")
    public void taskConfigs() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();
        String connectorName = "test";
        KafkaCosmosReflectionUtils.setConnectorName(sinkConnector, connectorName);

        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);
        sinkConnector.start(sinkConfigMap);

        int maxTask = 2;
        List<Map<String, String>> taskConfigs = sinkConnector.taskConfigs(maxTask);
        assertThat(taskConfigs.size()).isEqualTo(maxTask);
        validateTaskConfigsTaskId(taskConfigs, connectorName);

        for (Map<String, String> taskConfig : taskConfigs) {
            assertThat(taskConfig.get("azure.cosmos.account.endpoint")).isEqualTo(KafkaCosmosTestConfigurations.HOST);
            assertThat(taskConfig.get("azure.cosmos.account.key")).isEqualTo(KafkaCosmosTestConfigurations.MASTER_KEY);
            assertThat(taskConfig.get("azure.cosmos.sink.database.name")).isEqualTo(databaseName);
            assertThat(taskConfig.get("azure.cosmos.sink.containers.topicMap"))
                .isEqualTo(singlePartitionContainerName + "#" + singlePartitionContainerName);
        }
    }

    @Test(groups = "kafka-emulator")
    public void taskConfigsForClientMetadataCachesSnapshot() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();
        String connectorName = "test";
        KafkaCosmosReflectionUtils.setConnectorName(sinkConnector, connectorName);

        String throughputControlContainerName = "throughputControl-" + UUID.randomUUID();
        // create throughput control container
        CosmosAsyncClient client = null;
        try {
            client = new CosmosClientBuilder()
                .key(KafkaCosmosTestConfigurations.MASTER_KEY)
                .endpoint(KafkaCosmosTestConfigurations.HOST)
                .buildAsyncClient();
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(throughputControlContainerName, "/groupId");
            client.getDatabase(databaseName).createContainerIfNotExists(containerProperties).block();

            Map<String, String> sinkConfigMap = new HashMap<>();
            sinkConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
            sinkConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
            sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
            String topicMap =
                String.join(
                    ",",
                    Arrays.asList(
                        singlePartitionContainerName + "#" + singlePartitionContainerName,
                        multiPartitionContainerName + "#" + multiPartitionContainerName));
            sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", topicMap);
            sinkConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
            sinkConfigMap.put("azure.cosmos.throughputControl.account.endpoint", KafkaCosmosTestConfigurations.HOST);
            sinkConfigMap.put("azure.cosmos.throughputControl.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
            sinkConfigMap.put("azure.cosmos.throughputControl.group.name", "throughputControl-metadataCachesSnapshot");
            sinkConfigMap.put("azure.cosmos.throughputControl.targetThroughput", String.valueOf(400));
            sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", databaseName);
            sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", throughputControlContainerName);
            sinkConnector.start(sinkConfigMap);

            int maxTask = 2;
            List<Map<String, String>> taskConfigs = sinkConnector.taskConfigs(maxTask);
            assertThat(taskConfigs.size()).isEqualTo(maxTask);
            validateTaskConfigsTaskId(taskConfigs, connectorName);

            for (Map<String, String> taskConfig : taskConfigs) {
                assertThat(taskConfig.get("azure.cosmos.account.endpoint")).isEqualTo(KafkaCosmosTestConfigurations.HOST);
                assertThat(taskConfig.get("azure.cosmos.account.key")).isEqualTo(KafkaCosmosTestConfigurations.MASTER_KEY);
                assertThat(taskConfig.get("azure.cosmos.sink.database.name")).isEqualTo(databaseName);
                assertThat(taskConfig.get("azure.cosmos.sink.containers.topicMap")).isEqualTo(topicMap);

                // validate the client metadata cache snapshot is also populated in the task configs
                List<Pair<String, String>> metadataCachesPairList = new ArrayList<>();
                metadataCachesPairList.add(
                    Pair.of("azure.cosmos.client.metadata.caches.snapshot", singlePartitionContainerName));
                metadataCachesPairList.add(
                    Pair.of("azure.cosmos.client.metadata.caches.snapshot", multiPartitionContainerName));
                metadataCachesPairList.add(
                    Pair.of("azure.cosmos.throughputControl.client.metadata.caches.snapshot", throughputControlContainerName));

                for (Pair<String, String> metadataCachesPair : metadataCachesPairList) {
                    CosmosClientMetadataCachesSnapshot clientMetadataCachesSnapshot =
                        KafkaCosmosUtils.getCosmosClientMetadataFromString(taskConfig.get(metadataCachesPair.getLeft()));
                    assertThat(clientMetadataCachesSnapshot).isNotNull();
                    AsyncCache<String, DocumentCollection> collectionInfoCache = clientMetadataCachesSnapshot.getCollectionInfoByNameCache();

                    AtomicInteger invokedCount = new AtomicInteger(0);
                    String cacheKey = String.format("dbs/%s/colls/%s", databaseName, metadataCachesPair.getRight());
                    collectionInfoCache.getAsync(cacheKey, null, () -> {
                        invokedCount.incrementAndGet();
                        return Mono.just(new DocumentCollection());
                    }).block();

                    assertThat(invokedCount.get()).isEqualTo(0);
                }
            }
        } finally {
            if (client != null) {
                client.getDatabase(databaseName)
                    .getContainer(throughputControlContainerName)
                    .delete()
                    .onErrorResume(throwable -> {
                        logger.warn("Failed to delete container {}", throughputControlContainerName, throwable);
                        return Mono.empty();
                    })
                    .block();

                client.close();
            }
        }
    }

    @Test(groups = "unit")
    public void evilDeserializationIsBlocked() throws Exception {
        AtomicReference<String> payload = new AtomicReference<>("Test RCE payload");
        Evil evil = new Evil(payload);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(evil);
        }
        String evilBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

        // Through KafkaCosmosUtils: should be blocked and return null
        CosmosClientMetadataCachesSnapshot snapshot =
            KafkaCosmosUtils.getCosmosClientMetadataFromString(evilBase64);
        assertThat(snapshot).isNull();
        assertThat(payload.get()).isEqualTo("Test RCE payload");
    }

    @Test(groups = "unit")
    public void misFormattedConfig() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();
        Map<String, String> sinkConfigMap = this.getValidSinkConfig();

        String topicMapConfigName = "azure.cosmos.sink.containers.topicMap";
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
    public void sinkConfigWithAuthEndpointOverride() {
        String tenantId = "tenantId";
        String clientId = "clientId";
        String clientSecret = "clientSecret";
        String authEndpoint = "https://login.example.com/";

        Map<String, String> sinkConfigMap = this.getValidSinkConfig();
        sinkConfigMap.put("azure.cosmos.auth.type", CosmosAuthType.SERVICE_PRINCIPAL.getName());
        sinkConfigMap.put("azure.cosmos.account.tenantId", tenantId);
        sinkConfigMap.put("azure.cosmos.auth.aad.clientId", clientId);
        sinkConfigMap.put("azure.cosmos.auth.aad.clientSecret", clientSecret);
        sinkConfigMap.put("azure.cosmos.auth.aad.authEndpointOverride", authEndpoint);

        CosmosSinkConfig sinkConfig = new CosmosSinkConfig(sinkConfigMap);
        assertThat(sinkConfig.getAccountConfig()).isNotNull();
        assertThat(sinkConfig.getAccountConfig().getCosmosAuthConfig()).isInstanceOf(CosmosAadAuthConfig.class);
        CosmosAadAuthConfig cosmosAadAuthConfig = (CosmosAadAuthConfig) sinkConfig.getAccountConfig()
                .getCosmosAuthConfig();
        assertThat(cosmosAadAuthConfig.getTenantId()).isEqualTo(tenantId);
        assertThat(cosmosAadAuthConfig.getClientId()).isEqualTo(clientId);
        assertThat(cosmosAadAuthConfig.getClientSecret()).isEqualTo(clientSecret);
        assertThat(cosmosAadAuthConfig.getAuthEndpoint()).isEqualTo(authEndpoint);
    }

    @Test(groups = { "unit" })
    public void sinkConfigWithThroughputControl() {
        String throughputControlGroupName = "test";
        int targetThroughput= 6;
        double targetThroughputThreshold = 0.1;
        String throughputControlDatabaseName = "throughputControlDatabase";
        String throughputControlContainerName = "throughputControlContainer";

        Map<String, String> sinkConfigMap = this.getValidSinkConfig();
        sinkConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sinkConfigMap.put("azure.cosmos.throughputControl.group.name", throughputControlGroupName);
        sinkConfigMap.put("azure.cosmos.throughputControl.targetThroughput", String.valueOf(targetThroughput));
        sinkConfigMap.put("azure.cosmos.throughputControl.targetThroughputThreshold", String.valueOf(targetThroughputThreshold));
        sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", throughputControlDatabaseName);
        sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", throughputControlContainerName);

        CosmosSinkConfig sinkConfig = new CosmosSinkConfig(sinkConfigMap);
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
        sinkConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sinkConfigMap.put("azure.cosmos.throughputControl.targetThroughput", "-1");
        sinkConfigMap.put("azure.cosmos.throughputControl.targetThroughputThreshold", "-1");
        sinkConfigMap.put("azure.cosmos.throughputControl.priorityLevel", "None");

        Config config = sinkConnector.validate(sinkConfigMap);
        Map<String, List<String>> errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("azure.cosmos.throughputControl.group.name").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.targetThroughput").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.targetThroughputThreshold").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.priorityLevel").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.globalControl.database.name").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.globalControl.container.name").size()).isGreaterThan(0);

        // invalid throughput control account config with masterKey auth
        sinkConfigMap = this.getValidSinkConfig();
        sinkConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sinkConfigMap.put("azure.cosmos.throughputControl.targetThroughput", "1");
        sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", "ThroughputControlDatabase");
        sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", "ThroughputControlContainer");
        sinkConfigMap.put("azure.cosmos.throughputControl.group.name", "groupName");
        sinkConfigMap.put("azure.cosmos.throughputControl.account.endpoint", TestConfigurations.HOST);

        config = sinkConnector.validate(sinkConfigMap);
        errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("azure.cosmos.throughputControl.account.key").size()).isGreaterThan(0);

        // targetThroughputThreshold is not supported when using add auth for throughput control
        sinkConfigMap = this.getValidSinkConfig();
        sinkConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sinkConfigMap.put("azure.cosmos.throughputControl.targetThroughputThreshold", "0.9");
        sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", "ThroughputControlDatabase");
        sinkConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", "ThroughputControlContainer");
        sinkConfigMap.put("azure.cosmos.throughputControl.group.name", "groupName");
        sinkConfigMap.put("azure.cosmos.throughputControl.account.endpoint", TestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.throughputControl.auth.type", CosmosAuthType.SERVICE_PRINCIPAL.getName());

        config = sinkConnector.validate(sinkConfigMap);
        errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("azure.cosmos.throughputControl.auth.aad.clientId").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.auth.aad.clientSecret").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.account.tenantId").size()).isGreaterThan(0);
    }

    @Test(groups = { "unit" })
    public void sinkConfigWithDifferentWriteStrategy() {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();

        // validate default item write strategy configs
        List<String> writeStrategyTestList = new ArrayList<>();
        writeStrategyTestList
            .addAll(Arrays.stream(ItemWriteStrategy.values()).map(ItemWriteStrategy::getName).collect(Collectors.toList()));

        writeStrategyTestList.add("WrongStrategy");

        for (String writeStrategy : writeStrategyTestList) {
            Map<String, String> sinkConfigMap = this.getValidSinkConfig();
            sinkConfigMap.put("azure.cosmos.sink.write.strategy", writeStrategy);

            Config config = sinkConnector.validate(sinkConfigMap);
            Map<String, List<String>> errorMessages = config.configValues().stream()
                .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));

            ItemWriteStrategy itemWriteStrategy = ItemWriteStrategy.fromName(writeStrategy);
            if (itemWriteStrategy == null) {
                assertThat(errorMessages.get("azure.cosmos.sink.write.strategy").size()).isGreaterThan(0);
            } else {
                assertThat(errorMessages.get("azure.cosmos.sink.write.strategy").size()).isEqualTo(0);
            }
        }
    }

    @Test(groups = { "unit" }, dataProvider = "patchPropertyConfigParameterProvider")
    public void sinkConfigWithPatch(String patchPropertyConfig, boolean isValid) {
        CosmosSinkConnector sinkConnector = new CosmosSinkConnector();
        Map<String, String> sinkConfigMap = this.getValidSinkConfig();
        sinkConfigMap.put("azure.cosmos.sink.write.strategy", ItemWriteStrategy.ITEM_PATCH.getName());
        sinkConfigMap.put("azure.cosmos.sink.write.patch.property.configs", patchPropertyConfig);
        Config config = sinkConnector.validate(sinkConfigMap);
        Map<String, List<String>> errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        if (isValid) {
            assertThat(errorMessages.get("azure.cosmos.sink.write.patch.property.configs").size()).isEqualTo(0);
        } else {
            assertThat(errorMessages.get("azure.cosmos.sink.write.patch.property.configs").size()).isGreaterThan(0);
        }
    }

    private void validateTaskConfigsTaskId(List<Map<String, String>> taskConfigs, String connectorName) {
        for (Map<String, String> configs : taskConfigs) {
            assertThat(configs.containsKey(CosmosSinkTaskConfig.SINK_TASK_ID));
            assertThat(configs.get(CosmosSinkTaskConfig.SINK_TASK_ID).startsWith("sink-" + connectorName));
        }
    }

    private Map<String, String> getValidSinkConfig() {
        Map<String, String> sinkConfigMap = new HashMap<>();
        sinkConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sinkConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sinkConfigMap.put("azure.cosmos.sink.database.name", databaseName);
        sinkConfigMap.put("azure.cosmos.sink.containers.topicMap", singlePartitionContainerName + "#" + singlePartitionContainerName);

        return sinkConfigMap;
    }

    public static class SinkConfigs {
        public static final List<KafkaCosmosConfigEntry<?>> ALL_VALID_CONFIGS = Arrays.asList(
            new KafkaCosmosConfigEntry<String>("azure.cosmos.account.endpoint", null, false),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.account.tenantId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.auth.type", CosmosAuthType.MASTER_KEY.getName(), true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.account.key", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.auth.aad.clientId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.auth.aad.clientSecret", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.auth.aad.authEndpointOverride", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<Boolean>("azure.cosmos.mode.gateway", false, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.preferredRegionList", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.application.name", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.enabled", false, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.account.endpoint", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.sink.write.patch.operationType.default",
                KafkaCosmosPatchOperationType.SET.getName(),
                true),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.sink.write.patch.property.configs",
                Strings.Emtpy,
                true),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.sink.write.patch.filter",
                Strings.Emtpy,
                true),
            new KafkaCosmosConfigEntry<Integer>("azure.cosmos.sink.maxRetryCount", 10, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.throughputControl.account.tenantId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.throughputControl.auth.type", CosmosAuthType.MASTER_KEY.getName(), true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.account.key", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.throughputControl.auth.aad.clientId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.throughputControl.auth.aad.clientSecret", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.preferredRegionList", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.mode.gateway", false, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.group.name", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.targetThroughput", -1, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.targetThroughputThreshold", -1d, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.priorityLevel", "None", true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.globalControl.database.name", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.globalControl.container.name", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.globalControl.renewIntervalInMS", -1, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.globalControl.expireIntervalInMS", -1, true),

            new KafkaCosmosConfigEntry<String>("azure.cosmos.sink.errors.tolerance.level", "None", true),
            new KafkaCosmosConfigEntry<Boolean>("azure.cosmos.sink.bulk.enabled", true, true),
            new KafkaCosmosConfigEntry<Integer>("azure.cosmos.sink.bulk.maxConcurrentCosmosPartitions", -1, true),
            new KafkaCosmosConfigEntry<Integer>("azure.cosmos.sink.bulk.initialBatchSize", 1, true),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.sink.write.strategy",
                ItemWriteStrategy.ITEM_OVERWRITE.getName(),
                true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.sink.database.name", null, false),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.sink.containers.topicMap", null, false),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.sink.id.strategy",
                IdStrategyType.PROVIDED_IN_VALUE_STRATEGY.getName(),
                true)
        );
    }

    public static class Evil implements Serializable {
        private static final long serialVersionUID = 1L;

        private final AtomicReference<String> payload;

        public Evil(AtomicReference<String> payload) {
            this.payload = payload;
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            System.out.println("Payload executed");
            payload.set("Payload executed");
        }

        @Override
        public String toString() {
            return "Evil{payload='" + payload.get() + "'}";
        }
    }

}
