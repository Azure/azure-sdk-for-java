// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.kafka.connect.implementation.CosmosAuthType;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosChangeFeedMode;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosChangeFeedStartFromMode;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosMetadataStorageType;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceTaskConfig;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataCosmosStorageManager;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataKafkaStorageManager;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceTask;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeContinuationTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeContinuationTopicPartition;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeTaskUnit;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangesMetadataTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangesMetadataTopicPartition;
import com.azure.cosmos.kafka.connect.implementation.source.KafkaCosmosChangeFeedState;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataMonitorThread;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataTaskUnit;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.common.config.types.Password;
import org.apache.kafka.connect.source.SourceConnectorContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.Assert.assertEquals;

@Test
public class CosmosSourceConnectorTest extends KafkaCosmosTestSuiteBase {
    @Test(groups = "unit")
    public void taskClass() {
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();
        assertEquals(sourceConnector.taskClass(), CosmosSourceTask.class);
    }

    @Test(groups = "unit")
    public void config() {
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();
        ConfigDef configDef = sourceConnector.config();
        Map<String, ConfigDef.ConfigKey> configs = configDef.configKeys();
        List<KafkaCosmosConfigEntry<?>> allValidConfigs = CosmosSourceConnectorTest.SourceConfigs.ALL_VALID_CONFIGS;

        for (KafkaCosmosConfigEntry<?> sourceConfigEntry : allValidConfigs) {
            System.out.println(sourceConfigEntry.getName());
            assertThat(configs.containsKey(sourceConfigEntry.getName())).isTrue();

            configs.containsKey(sourceConfigEntry.getName());
            if (sourceConfigEntry.isOptional()) {
                if (sourceConfigEntry.isPassword()) {
                    assertThat(((Password)configs.get(sourceConfigEntry.getName()).defaultValue).value())
                        .isEqualTo(sourceConfigEntry.getDefaultValue());
                } else {
                    assertThat(configs.get(sourceConfigEntry.getName()).defaultValue).isEqualTo(sourceConfigEntry.getDefaultValue());
                }
            } else {
                assertThat(configs.get(sourceConfigEntry.getName()).defaultValue).isEqualTo(ConfigDef.NO_DEFAULT_VALUE);
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void getTaskConfigsWithoutPersistedOffset() throws JsonProcessingException {
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();
        try {
            String connectorName = "kafka-test-getTaskConfig";
            Map<String, Object> sourceConfigMap = new HashMap<>();
            sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
            sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
            sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
            List<String> containersIncludedList = Arrays.asList(
                singlePartitionContainerName,
                multiPartitionContainerName
            );
            sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());

            String singlePartitionContainerTopicName = singlePartitionContainerName + "topic";
            List<String> containerTopicMapList = Arrays.asList(singlePartitionContainerTopicName + "#" + singlePartitionContainerName);
            sourceConfigMap.put("azure.cosmos.source.containers.topicMap", containerTopicMapList.toString());

            // setup the internal state
            this.setupDefaultConnectorInternalStatesWithMetadataKafkaReader(sourceConnector, sourceConfigMap, connectorName);
            CosmosAsyncClient cosmosAsyncClient = KafkaCosmosReflectionUtils.getCosmosClient(sourceConnector);

            int maxTask = 2;
            List<Map<String, String>> taskConfigs = sourceConnector.taskConfigs(maxTask);
            assertThat(taskConfigs.size()).isEqualTo(maxTask);
            validateTaskConfigsTaskId(taskConfigs, connectorName);

            // construct expected feed range task units
            CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(cosmosAsyncClient);
            List<FeedRangeTaskUnit> singlePartitionContainerFeedRangeTasks =
                getFeedRangeTaskUnits(
                    cosmosAsyncClient,
                    databaseName,
                    singlePartitionContainer,
                    null,
                    singlePartitionContainerTopicName);
            assertThat(singlePartitionContainerFeedRangeTasks.size()).isEqualTo(1);

            CosmosContainerProperties multiPartitionContainer = getMultiPartitionContainer(cosmosAsyncClient);
            List<FeedRangeTaskUnit> multiPartitionContainerFeedRangeTasks =
                getFeedRangeTaskUnits(
                    cosmosAsyncClient,
                    databaseName,
                    multiPartitionContainer,
                    null,
                    multiPartitionContainer.getId());
            assertThat(multiPartitionContainerFeedRangeTasks.size()).isGreaterThan(1);

            List<List<FeedRangeTaskUnit>> expectedTaskUnits = new ArrayList<>();
            for (int i = 0; i < maxTask; i++) {
                expectedTaskUnits.add(new ArrayList<>());
            }

            expectedTaskUnits.get(0).add(singlePartitionContainerFeedRangeTasks.get(0));
            for (int i = 0; i < multiPartitionContainerFeedRangeTasks.size(); i++) {
                int index = ( i + 1) % 2;
                expectedTaskUnits.get(index).add(multiPartitionContainerFeedRangeTasks.get(i));
            }

            validateFeedRangeTasks(expectedTaskUnits, taskConfigs);

            MetadataTaskUnit expectedMetadataTaskUnit =
                getMetadataTaskUnit(
                    connectorName,
                    cosmosAsyncClient,
                    databaseName,
                    Arrays.asList(singlePartitionContainer, multiPartitionContainer));
            validateMetadataTask(expectedMetadataTaskUnit, taskConfigs.get(1));
        } finally {
            sourceConnector.stop();
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void getTaskConfigs_withMetadataCosmosStorageManager() throws JsonProcessingException {
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();
        String metadataStorageName = "_cosmos.metadata.topic-" + UUID.randomUUID();
        String connectorName = "kafka-test-getTaskConfigs-withMetadataCosmosStorageManager";
        CosmosAsyncClient cosmosAsyncClient = null;
        try {
            Map<String, Object> sourceConfigMap = new HashMap<>();
            sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
            sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
            sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
            List<String> containersIncludedList = Arrays.asList(
                singlePartitionContainerName,
                multiPartitionContainerName
            );
            sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());

            String singlePartitionContainerTopicName = singlePartitionContainerName + "topic";
            List<String> containerTopicMapList = Arrays.asList(singlePartitionContainerTopicName + "#" + singlePartitionContainerName);
            sourceConfigMap.put("azure.cosmos.source.containers.topicMap", containerTopicMapList.toString());
            sourceConfigMap.put("azure.cosmos.source.metadata.storage.name", metadataStorageName);
            sourceConfigMap.put("azure.cosmos.source.metadata.storage.type", CosmosMetadataStorageType.COSMOS.getName());

            // setup the internal state
            this.setupDefaultConnectorInternalStatesWithMetadataCosmosReader(
                sourceConnector,
                sourceConfigMap,
                databaseName,
                metadataStorageName,
                connectorName);
            cosmosAsyncClient = KafkaCosmosReflectionUtils.getCosmosClient(sourceConnector);

            int maxTask = 2;
            List<Map<String, String>> taskConfigs = sourceConnector.taskConfigs(maxTask);
            assertThat(taskConfigs.size()).isEqualTo(maxTask);
            validateTaskConfigsTaskId(taskConfigs, connectorName);

            // construct expected feed range task units
            CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(cosmosAsyncClient);
            List<FeedRangeTaskUnit> singlePartitionContainerFeedRangeTasks =
                getFeedRangeTaskUnits(
                    cosmosAsyncClient,
                    databaseName,
                    singlePartitionContainer,
                    null,
                    singlePartitionContainerTopicName);
            assertThat(singlePartitionContainerFeedRangeTasks.size()).isEqualTo(1);

            CosmosContainerProperties multiPartitionContainer = getMultiPartitionContainer(cosmosAsyncClient);
            List<FeedRangeTaskUnit> multiPartitionContainerFeedRangeTasks =
                getFeedRangeTaskUnits(
                    cosmosAsyncClient,
                    databaseName,
                    multiPartitionContainer,
                    null,
                    multiPartitionContainer.getId());
            assertThat(multiPartitionContainerFeedRangeTasks.size()).isGreaterThan(1);

            List<List<FeedRangeTaskUnit>> expectedTaskUnits = new ArrayList<>();
            for (int i = 0; i < maxTask; i++) {
                expectedTaskUnits.add(new ArrayList<>());
            }

            expectedTaskUnits.get(0).add(singlePartitionContainerFeedRangeTasks.get(0));
            for (int i = 0; i < multiPartitionContainerFeedRangeTasks.size(); i++) {
                int index = ( i + 1) % 2;
                expectedTaskUnits.get(index).add(multiPartitionContainerFeedRangeTasks.get(i));
            }

            validateFeedRangeTasks(expectedTaskUnits, taskConfigs);

            MetadataTaskUnit expectedMetadataTaskUnit =
                getMetadataTaskUnit(
                    connectorName,
                    cosmosAsyncClient,
                    databaseName,
                    Arrays.asList(singlePartitionContainer, multiPartitionContainer));
            CosmosAsyncContainer metadataContainer = cosmosAsyncClient.getDatabase(databaseName).getContainer(metadataStorageName);
            validateMetadataItems(expectedMetadataTaskUnit, metadataContainer, connectorName);
        } finally {
            if (cosmosAsyncClient != null) {
                cosmosAsyncClient.getDatabase(databaseName).getContainer(metadataStorageName).delete().block();
            }
            sourceConnector.stop();
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void getTaskConfigsAfterSplit() throws JsonProcessingException {
        // This test is to simulate after a split happen, the task resume with persisted offset
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();

        try {
            String connectorName = "kafka-test-getTaskConfigsAfterSplit";
            Map<String, Object> sourceConfigMap = new HashMap<>();
            sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
            sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
            sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
            List<String> containersIncludedList = Arrays.asList(multiPartitionContainerName);
            sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());

            // setup the internal state
            this.setupDefaultConnectorInternalStatesWithMetadataKafkaReader(sourceConnector, sourceConfigMap, connectorName);

            // override the storage reader with initial offset
            CosmosAsyncClient cosmosAsyncClient = KafkaCosmosReflectionUtils.getCosmosClient(sourceConnector);
            MetadataKafkaStorageManager sourceOffsetStorageReader = KafkaCosmosReflectionUtils.getKafkaOffsetStorageReader(sourceConnector);
            InMemoryStorageReader inMemoryStorageReader =
                (InMemoryStorageReader) KafkaCosmosReflectionUtils.getOffsetStorageReader(sourceOffsetStorageReader);

            CosmosContainerProperties multiPartitionContainer = getMultiPartitionContainer(cosmosAsyncClient);

            // constructing feed range continuation offset
            FeedRangeContinuationTopicPartition feedRangeContinuationTopicPartition =
                new FeedRangeContinuationTopicPartition(
                    databaseName,
                    multiPartitionContainer.getResourceId(),
                    FeedRange.forFullRange());

            String initialContinuationState = new ChangeFeedStateV1(
                multiPartitionContainer.getResourceId(),
                FeedRangeEpkImpl.forFullRange(),
                ChangeFeedMode.INCREMENTAL,
                ChangeFeedStartFromInternal.createFromBeginning(),
                FeedRangeContinuation.create(
                    multiPartitionContainer.getResourceId(),
                    FeedRangeEpkImpl.forFullRange(),
                    Arrays.asList(new CompositeContinuationToken("1", FeedRangeEpkImpl.forFullRange().getRange())))).toString();

            FeedRangeContinuationTopicOffset feedRangeContinuationTopicOffset =
                new FeedRangeContinuationTopicOffset(initialContinuationState, "1"); // using the same itemLsn as in the continuationToken
            Map<Map<String, Object>, Map<String, Object>> initialOffsetMap = new HashMap<>();
            initialOffsetMap.put(
                FeedRangeContinuationTopicPartition.toMap(feedRangeContinuationTopicPartition),
                FeedRangeContinuationTopicOffset.toMap(feedRangeContinuationTopicOffset));

            // constructing feedRange metadata offset
            FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
                new FeedRangesMetadataTopicPartition(databaseName, multiPartitionContainer.getResourceId(), connectorName);
            FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
                new FeedRangesMetadataTopicOffset(Arrays.asList(FeedRange.forFullRange()));
            initialOffsetMap.put(
                FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition),
                FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset));

            inMemoryStorageReader.populateOffset(initialOffsetMap);

            int maxTask = 2;
            List<Map<String, String>> taskConfigs = sourceConnector.taskConfigs(maxTask);
            assertThat(taskConfigs.size()).isEqualTo(maxTask);
            validateTaskConfigsTaskId(taskConfigs, connectorName);

            // construct expected feed range task units
            List<FeedRangeTaskUnit> multiPartitionContainerFeedRangeTasks =
                getFeedRangeTaskUnits(
                    cosmosAsyncClient,
                    databaseName,
                    multiPartitionContainer,
                    initialContinuationState,
                    multiPartitionContainer.getId());
            assertThat(multiPartitionContainerFeedRangeTasks.size()).isGreaterThan(1);

            List<List<FeedRangeTaskUnit>> expectedTaskUnits = new ArrayList<>();
            for (int i = 0; i < maxTask; i++) {
                expectedTaskUnits.add(new ArrayList<>());
            }

            for (int i = 0; i < multiPartitionContainerFeedRangeTasks.size(); i++) {
                expectedTaskUnits.get( i % 2).add(multiPartitionContainerFeedRangeTasks.get(i));
            }

            validateFeedRangeTasks(expectedTaskUnits, taskConfigs);

            MetadataTaskUnit expectedMetadataTaskUnit =
                getMetadataTaskUnit(
                    connectorName,
                    cosmosAsyncClient,
                    databaseName,
                    Arrays.asList(multiPartitionContainer));
            validateMetadataTask(expectedMetadataTaskUnit, taskConfigs.get(1));
        } finally {
            sourceConnector.stop();
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void getTaskConfigsAfterMerge() throws JsonProcessingException {
        // This test is to simulate after a merge happen, the task resume with previous feedRanges
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();

        try {
            Map<String, Object> sourceConfigMap = new HashMap<>();
            String connectorName = "kafka-test";
            sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
            sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
            sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
            List<String> containersIncludedList = Arrays.asList(singlePartitionContainerName);
            sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());

            // setup the internal state
            this.setupDefaultConnectorInternalStatesWithMetadataKafkaReader(sourceConnector, sourceConfigMap, connectorName);

            // override the storage reader with initial offset
            CosmosAsyncClient cosmosAsyncClient = KafkaCosmosReflectionUtils.getCosmosClient(sourceConnector);
            MetadataKafkaStorageManager sourceOffsetStorageReader = KafkaCosmosReflectionUtils.getKafkaOffsetStorageReader(sourceConnector);
            InMemoryStorageReader inMemoryStorageReader =
                (InMemoryStorageReader) KafkaCosmosReflectionUtils.getOffsetStorageReader(sourceOffsetStorageReader);

            CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(cosmosAsyncClient);

            // constructing feed range continuation offset
            List<FeedRange> childRanges =
                ImplementationBridgeHelpers
                    .CosmosAsyncContainerHelper
                    .getCosmosAsyncContainerAccessor()
                    .trySplitFeedRange(
                        cosmosAsyncClient.getDatabase(databaseName).getContainer(singlePartitionContainer.getId()),
                        FeedRange.forFullRange(),
                        2)
                    .block();

            Map<Map<String, Object>, Map<String, Object>> initialOffsetMap = new HashMap<>();
            List<FeedRangeTaskUnit> singlePartitionFeedRangeTaskUnits = new ArrayList<>();

            for (FeedRange childRange : childRanges) {
                FeedRangeContinuationTopicPartition feedRangeContinuationTopicPartition =
                    new FeedRangeContinuationTopicPartition(
                        databaseName,
                        singlePartitionContainer.getResourceId(),
                        childRange);

                ChangeFeedStateV1 childRangeContinuationState = new ChangeFeedStateV1(
                    singlePartitionContainer.getResourceId(),
                    (FeedRangeEpkImpl)childRange,
                    ChangeFeedMode.INCREMENTAL,
                    ChangeFeedStartFromInternal.createFromBeginning(),
                    FeedRangeContinuation.create(
                        singlePartitionContainer.getResourceId(),
                        (FeedRangeEpkImpl)childRange,
                        Arrays.asList(new CompositeContinuationToken("1", ((FeedRangeEpkImpl)childRange).getRange()))));

                FeedRangeContinuationTopicOffset feedRangeContinuationTopicOffset =
                    new FeedRangeContinuationTopicOffset(childRangeContinuationState.toString(), "1");

                initialOffsetMap.put(
                    FeedRangeContinuationTopicPartition.toMap(feedRangeContinuationTopicPartition),
                    FeedRangeContinuationTopicOffset.toMap(feedRangeContinuationTopicOffset));

                KafkaCosmosChangeFeedState taskUnitContinuationState =
                    new KafkaCosmosChangeFeedState(childRangeContinuationState.toString(), childRange, "1");
                singlePartitionFeedRangeTaskUnits.add(
                    new FeedRangeTaskUnit(
                        databaseName,
                        singlePartitionContainer.getId(),
                        singlePartitionContainer.getResourceId(),
                        childRange,
                        taskUnitContinuationState,
                        singlePartitionContainer.getId()));
            }

            // constructing feedRange metadata offset
            FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
                new FeedRangesMetadataTopicPartition(databaseName, singlePartitionContainer.getResourceId(), connectorName);
            FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
                new FeedRangesMetadataTopicOffset(
                    childRanges
                        .stream()
                        .collect(Collectors.toList()));

            initialOffsetMap.put(
                FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition),
                FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset));

            inMemoryStorageReader.populateOffset(initialOffsetMap);

            int maxTask = 2;
            List<Map<String, String>> taskConfigs = sourceConnector.taskConfigs(maxTask);
            assertThat(taskConfigs.size()).isEqualTo(maxTask);
            validateTaskConfigsTaskId(taskConfigs, connectorName);

            // construct expected feed range task units
            assertThat(singlePartitionFeedRangeTaskUnits.size()).isEqualTo(2);

            List<List<FeedRangeTaskUnit>> expectedTaskUnits = new ArrayList<>();
            for (int i = 0; i < maxTask; i++) {
                expectedTaskUnits.add(new ArrayList<>());
            }

            for (int i = 0; i < singlePartitionFeedRangeTaskUnits.size(); i++) {
                expectedTaskUnits.get( i % 2).add(singlePartitionFeedRangeTaskUnits.get(i));
            }

            validateFeedRangeTasks(expectedTaskUnits, taskConfigs);

            Map<String, List<FeedRange>> containersEffectiveRangesMap = new HashMap<>();
            containersEffectiveRangesMap.put(
                singlePartitionContainer.getResourceId(),
                childRanges.stream().collect(Collectors.toList()));

            MetadataTaskUnit expectedMetadataTaskUnit =
                new MetadataTaskUnit(
                    connectorName,
                    databaseName,
                    Arrays.asList(singlePartitionContainer.getResourceId()),
                    containersEffectiveRangesMap,
                    "_cosmos.metadata.topic",
                    CosmosMetadataStorageType.KAFKA);
            validateMetadataTask(expectedMetadataTaskUnit, taskConfigs.get(1));
        } finally {
            sourceConnector.stop();
        }
    }

    @Test(groups = "unit")
    public void missingRequiredConfig() {

        List<KafkaCosmosConfigEntry<?>> requiredConfigs =
            CosmosSourceConnectorTest.SourceConfigs.ALL_VALID_CONFIGS
                .stream()
                .filter(sourceConfigEntry -> !sourceConfigEntry.isOptional())
                .collect(Collectors.toList());

        assertThat(requiredConfigs.size()).isGreaterThan(1);
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();
        for (KafkaCosmosConfigEntry<?> configEntry : requiredConfigs) {

            Map<String, String> sourceConfigMap = this.getValidSourceConfig();
            sourceConfigMap.remove(configEntry.getName());
            Config validatedConfig = sourceConnector.validate(sourceConfigMap);
            ConfigValue configValue =
                validatedConfig
                    .configValues()
                    .stream()
                    .filter(config -> config.name().equalsIgnoreCase(configEntry.getName()))
                    .findFirst()
                    .get();

            assertThat(configValue.errorMessages()).isNotNull();
            assertThat(configValue.errorMessages().size()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test(groups = "unit")
    public void misFormattedConfig() {
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();
        Map<String, String> sourceConfigMap = this.getValidSourceConfig();

        String topicMapConfigName = "azure.cosmos.source.containers.topicMap";
        sourceConfigMap.put(topicMapConfigName, UUID.randomUUID().toString());

        Config validatedConfig = sourceConnector.validate(sourceConfigMap);
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
    public void sourceConfigWithThroughputControl() {
        String throughputControlGroupName = "test";
        int targetThroughput= 6;
        double targetThroughputThreshold = 0.1;
        String throughputControlDatabaseName = "throughputControlDatabase";
        String throughputControlContainerName = "throughputControlContainer";

        Map<String, String> sourceConfigMap = this.getValidSourceConfig();
        sourceConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sourceConfigMap.put("azure.cosmos.throughputControl.group.name", throughputControlGroupName);
        sourceConfigMap.put("azure.cosmos.throughputControl.targetThroughput", String.valueOf(targetThroughput));
        sourceConfigMap.put("azure.cosmos.throughputControl.targetThroughputThreshold", String.valueOf(targetThroughputThreshold));
        sourceConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", throughputControlDatabaseName);
        sourceConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", throughputControlContainerName);

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        assertThat(sourceConfig.getThroughputControlConfig()).isNotNull();
        assertThat(sourceConfig.getThroughputControlConfig().isThroughputControlEnabled()).isTrue();
        assertThat(sourceConfig.getThroughputControlConfig().getThroughputControlAccountConfig()).isNull();
        assertThat(sourceConfig.getThroughputControlConfig().getThroughputControlGroupName()).isEqualTo(throughputControlGroupName);
        assertThat(sourceConfig.getThroughputControlConfig().getTargetThroughput()).isEqualTo(targetThroughput);
        assertThat(sourceConfig.getThroughputControlConfig().getTargetThroughputThreshold()).isEqualTo(targetThroughputThreshold);
        assertThat(sourceConfig.getThroughputControlConfig().getGlobalThroughputControlDatabaseName()).isEqualTo(throughputControlDatabaseName);
        assertThat(sourceConfig.getThroughputControlConfig().getGlobalThroughputControlContainerName()).isEqualTo(throughputControlContainerName);
        assertThat(sourceConfig.getThroughputControlConfig().getGlobalThroughputControlRenewInterval()).isNull();
        assertThat(sourceConfig.getThroughputControlConfig().getGlobalThroughputControlExpireInterval()).isNull();
    }

    @Test(groups = { "unit" })
    public void invalidThroughputControlConfig() {
        CosmosSourceConnector sourceConnector = new CosmosSourceConnector();
        // invalid targetThroughput, targetThroughputThreshold, priorityLevel config and missing required config for throughput control container info

        Map<String, String> sourceConfigMap = this.getValidSourceConfig();
        sourceConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sourceConfigMap.put("azure.cosmos.throughputControl.targetThroughput", "-1");
        sourceConfigMap.put("azure.cosmos.throughputControl.targetThroughputThreshold", "-1");
        sourceConfigMap.put("azure.cosmos.throughputControl.priorityLevel", "None");

        Config config = sourceConnector.validate(sourceConfigMap);
        Map<String, List<String>> errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("azure.cosmos.throughputControl.group.name").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.targetThroughput").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.targetThroughputThreshold").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.priorityLevel").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.globalControl.database.name").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.globalControl.container.name").size()).isGreaterThan(0);

        // invalid throughput control account config with masterKey auth
        sourceConfigMap = this.getValidSourceConfig();
        sourceConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sourceConfigMap.put("azure.cosmos.throughputControl.targetThroughput", "1");
        sourceConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", "ThroughputControlDatabase");
        sourceConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", "ThroughputControlContainer");
        sourceConfigMap.put("azure.cosmos.throughputControl.group.name", "groupName");
        sourceConfigMap.put("azure.cosmos.throughputControl.account.endpoint", KafkaCosmosTestConfigurations.HOST);

        config = sourceConnector.validate(sourceConfigMap);
        errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("azure.cosmos.throughputControl.account.key").size()).isGreaterThan(0);

        // targetThroughputThreshold is not supported when using add auth for throughput control
        sourceConfigMap = this.getValidSourceConfig();
        sourceConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sourceConfigMap.put("azure.cosmos.throughputControl.targetThroughputThreshold", "0.9");
        sourceConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", "ThroughputControlDatabase");
        sourceConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", "ThroughputControlContainer");
        sourceConfigMap.put("azure.cosmos.throughputControl.group.name", "groupName");
        sourceConfigMap.put("azure.cosmos.throughputControl.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.throughputControl.auth.type", CosmosAuthType.SERVICE_PRINCIPAL.getName());

        config = sourceConnector.validate(sourceConfigMap);
        errorMessages = config.configValues().stream()
            .collect(Collectors.toMap(ConfigValue::name, ConfigValue::errorMessages));
        assertThat(errorMessages.get("azure.cosmos.throughputControl.auth.aad.clientId").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.auth.aad.clientSecret").size()).isGreaterThan(0);
        assertThat(errorMessages.get("azure.cosmos.throughputControl.account.tenantId").size()).isGreaterThan(0);
    }

    private Map<String, String> getValidSourceConfig() {
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(singlePartitionContainerName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());

        return sourceConfigMap;
    }

    private void setupDefaultConnectorInternalStatesWithMetadataKafkaReader(
        CosmosSourceConnector sourceConnector,
        Map<String, Object> sourceConfigMap,
        String connectorName) {

        KafkaCosmosReflectionUtils.setConnectorName(sourceConnector, connectorName);

        CosmosSourceConfig cosmosSourceConfig = new CosmosSourceConfig(sourceConfigMap);
        KafkaCosmosReflectionUtils.setCosmosSourceConfig(sourceConnector, cosmosSourceConfig);

        CosmosAsyncClient cosmosAsyncClient = CosmosClientStore.getCosmosClient(cosmosSourceConfig.getAccountConfig(), "testKafkaConnector");
        KafkaCosmosReflectionUtils.setCosmosClient(sourceConnector, cosmosAsyncClient);

        InMemoryStorageReader inMemoryStorageReader = new InMemoryStorageReader();
        MetadataKafkaStorageManager metadataReader = new MetadataKafkaStorageManager(inMemoryStorageReader);

        KafkaCosmosReflectionUtils.setMetadataReader(sourceConnector, metadataReader);
        KafkaCosmosReflectionUtils.setKafkaOffsetStorageReader(sourceConnector, metadataReader);

        SourceConnectorContext connectorContext = Mockito.mock(SourceConnectorContext.class);
        MetadataMonitorThread monitorThread = new MetadataMonitorThread(
            connectorName,
            cosmosSourceConfig.getContainersConfig(),
            cosmosSourceConfig.getMetadataConfig(),
            connectorContext,
            metadataReader,
            cosmosAsyncClient);

        KafkaCosmosReflectionUtils.setMetadataMonitorThread(sourceConnector, monitorThread);
    }

    private void setupDefaultConnectorInternalStatesWithMetadataCosmosReader(
        CosmosSourceConnector sourceConnector,
        Map<String, Object> sourceConfigMap,
        String databaseName,
        String containerName,
        String connectorName) {

        KafkaCosmosReflectionUtils.setConnectorName(sourceConnector, connectorName);

        CosmosSourceConfig cosmosSourceConfig = new CosmosSourceConfig(sourceConfigMap);
        KafkaCosmosReflectionUtils.setCosmosSourceConfig(sourceConnector, cosmosSourceConfig);

        CosmosAsyncClient cosmosAsyncClient = CosmosClientStore.getCosmosClient(cosmosSourceConfig.getAccountConfig(), "testKafkaConnector");
        KafkaCosmosReflectionUtils.setCosmosClient(sourceConnector, cosmosAsyncClient);

        CosmosAsyncContainer container = cosmosAsyncClient.getDatabase(databaseName).getContainer(containerName);
        MetadataCosmosStorageManager cosmosStorageManager = new MetadataCosmosStorageManager(container);
        KafkaCosmosReflectionUtils.setMetadataReader(sourceConnector, cosmosStorageManager);

        InMemoryStorageReader inMemoryStorageReader = new InMemoryStorageReader();
        MetadataKafkaStorageManager metadataReader = new MetadataKafkaStorageManager(inMemoryStorageReader);
        KafkaCosmosReflectionUtils.setKafkaOffsetStorageReader(sourceConnector, metadataReader);

        SourceConnectorContext connectorContext = Mockito.mock(SourceConnectorContext.class);
        MetadataMonitorThread monitorThread = new MetadataMonitorThread(
            connectorName,
            cosmosSourceConfig.getContainersConfig(),
            cosmosSourceConfig.getMetadataConfig(),
            connectorContext,
            cosmosStorageManager,
            cosmosAsyncClient);

        KafkaCosmosReflectionUtils.setMetadataMonitorThread(sourceConnector, monitorThread);

        // pre-create metadata container
        cosmosAsyncClient.getDatabase(databaseName)
            .createContainerIfNotExists(containerName, "/id")
            .block();
    }

    private List<FeedRangeTaskUnit> getFeedRangeTaskUnits(
        CosmosAsyncClient cosmosClient,
        String databaseName,
        CosmosContainerProperties containerProperties,
        String continuationState,
        String topicName) {

        List<FeedRange> feedRanges =
            cosmosClient
                .getDatabase(databaseName)
                .getContainer(containerProperties.getId())
                .getFeedRanges()
                .block();

        return feedRanges
            .stream()
            .map(feedRange -> {
                KafkaCosmosChangeFeedState kafkaCosmosChangeFeedState = null;
                if (StringUtils.isNotEmpty(continuationState)) {
                    ChangeFeedState changeFeedState = ChangeFeedStateV1.fromString(continuationState);
                    kafkaCosmosChangeFeedState =
                        new KafkaCosmosChangeFeedState(
                            continuationState,
                            feedRange,
                            changeFeedState.getContinuation().getCurrentContinuationToken().getToken());
                }

                return new FeedRangeTaskUnit(
                    databaseName,
                    containerProperties.getId(),
                    containerProperties.getResourceId(),
                    feedRange,
                    kafkaCosmosChangeFeedState,
                    topicName);
            })
            .collect(Collectors.toList());
    }

    private MetadataTaskUnit getMetadataTaskUnit(
        String connectorName,
        CosmosAsyncClient cosmosAsyncClient,
        String databaseName,
        List<CosmosContainerProperties> containers) {

        Map<String, List<FeedRange>> containersEffectiveRangesMap = new HashMap<>();
        for (CosmosContainerProperties containerProperties : containers) {
            List<FeedRange> feedRanges =
                cosmosAsyncClient
                    .getDatabase(databaseName)
                    .getContainer(containerProperties.getId())
                    .getFeedRanges()
                    .block();

            containersEffectiveRangesMap.put(containerProperties.getResourceId(), feedRanges);
        }

        return new MetadataTaskUnit(
            connectorName,
            databaseName,
            containers.stream().map(CosmosContainerProperties::getResourceId).collect(Collectors.toList()),
            containersEffectiveRangesMap,
            "_cosmos.metadata.topic",
            CosmosMetadataStorageType.KAFKA);
    }

    private void validateFeedRangeTasks(
        List<List<FeedRangeTaskUnit>> feedRangeTaskUnits,
        List<Map<String, String>> taskConfigs) throws JsonProcessingException {

        String taskUnitsKey = "azure.cosmos.source.task.feedRangeTaskUnits";
        List<FeedRangeTaskUnit> allTaskUnitsFromTaskConfigs = new ArrayList<>();
        for (Map<String, String> taskConfig : taskConfigs) {
            List<FeedRangeTaskUnit> taskUnitsFromTaskConfig =
                Utils
                    .getSimpleObjectMapper()
                    .readValue(taskConfig.get(taskUnitsKey), new TypeReference<List<String>>() {})
                    .stream()
                    .map(taskUnitString -> {
                        try {
                            return Utils.getSimpleObjectMapper().readValue(taskUnitString, FeedRangeTaskUnit.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            allTaskUnitsFromTaskConfigs.addAll(taskUnitsFromTaskConfig);
        }

        Map<FeedRange, FeedRangeTaskUnit> allExpectedTaskUnits = new HashMap<>();
        feedRangeTaskUnits.forEach(taskUnits -> {
            allExpectedTaskUnits.putAll(
                taskUnits.stream().collect(Collectors.toMap(taskUnit -> taskUnit.getFeedRange(), taskUnit -> taskUnit)));
        });

        assertThat(allExpectedTaskUnits.size()).isEqualTo(allTaskUnitsFromTaskConfigs.size());
        for (FeedRangeTaskUnit feedRangeTaskUnit : allTaskUnitsFromTaskConfigs) {
            FeedRangeTaskUnit expectedTaskUnit = allExpectedTaskUnits.get(feedRangeTaskUnit.getFeedRange());
            assertThat(expectedTaskUnit).isNotNull();
            assertThat(
                Utils.getSimpleObjectMapper().writeValueAsString(expectedTaskUnit)
            ).isEqualTo(
                Utils.getSimpleObjectMapper().writeValueAsString(feedRangeTaskUnit)
            );
        }
    }

    private void validateMetadataTask(
        MetadataTaskUnit expectedMetadataTaskUnit,
        Map<String, String> taskConfig) throws JsonProcessingException {

        String taskUnitKey = "azure.cosmos.source.task.metadataTaskUnit";
        assertThat(taskConfig.containsKey(taskUnitKey));
        MetadataTaskUnit metadataTaskUnitFromTaskConfig =
            Utils.getSimpleObjectMapper().readValue(taskConfig.get(taskUnitKey), MetadataTaskUnit.class);

        assertThat(expectedMetadataTaskUnit.getDatabaseName()).isEqualTo(metadataTaskUnitFromTaskConfig.getDatabaseName());
        assertThat(expectedMetadataTaskUnit.getContainerRids().size()).isEqualTo(metadataTaskUnitFromTaskConfig.getContainerRids().size());
        assertThat(expectedMetadataTaskUnit.getContainerRids().containsAll(metadataTaskUnitFromTaskConfig.getContainerRids())).isTrue();
        assertThat(expectedMetadataTaskUnit.getContainersEffectiveRangesMap().size())
            .isEqualTo(metadataTaskUnitFromTaskConfig.getContainersEffectiveRangesMap().size());

        for (String containerRid : expectedMetadataTaskUnit.getContainersEffectiveRangesMap().keySet()) {
            assertThat(metadataTaskUnitFromTaskConfig.getContainersEffectiveRangesMap().get(containerRid)).isNotNull();
            assertThat(expectedMetadataTaskUnit.getContainersEffectiveRangesMap().get(containerRid).size())
                .isEqualTo(metadataTaskUnitFromTaskConfig.getContainersEffectiveRangesMap().get(containerRid).size());
            assertThat(
                expectedMetadataTaskUnit
                    .getContainersEffectiveRangesMap()
                    .get(containerRid)
                    .containsAll(metadataTaskUnitFromTaskConfig.getContainersEffectiveRangesMap().get(containerRid)))
                .isTrue();
        }
    }

    private void validateTaskConfigsTaskId(List<Map<String, String>> taskConfigs, String connectorName) {
        for (Map<String, String> configs : taskConfigs) {
            assertThat(configs.containsKey(CosmosSourceTaskConfig.SOURCE_TASK_ID));
            assertThat(configs.get(CosmosSourceTaskConfig.SOURCE_TASK_ID).startsWith("source-" + connectorName));
        }
    }

    private void validateMetadataItems(
        MetadataTaskUnit expectedMetadataTaskUnit,
        CosmosAsyncContainer metadataContainer,
        String connectorName) throws JsonProcessingException {

        // validate containers metadata exists
        String itemId = expectedMetadataTaskUnit.getDatabaseName() + "_" + connectorName;
        JsonNode containersMetadata =
            metadataContainer
                .readItem(itemId, new PartitionKey(itemId), JsonNode.class)
                .block()
                .getItem();
        Map<String, Object> metadataMap =
            Utils
                .getSimpleObjectMapper()
                .convertValue(containersMetadata.get("metadata"), new TypeReference<Map<String, Object>>(){});

        assertThat(metadataMap.containsKey("containerRids")).isTrue();
        List<String> persistedContainerRids =
            Utils
                .getSimpleObjectMapper()
                .readValue(metadataMap.get("containerRids").toString(), new TypeReference<List<String>>() {
                });
        assertThat(persistedContainerRids.size()).isEqualTo(expectedMetadataTaskUnit.getContainerRids().size());
        assertThat(persistedContainerRids.containsAll(expectedMetadataTaskUnit.getContainerRids())).isTrue();

        // validate feedRanges metadata exists
        for (String containerRid : expectedMetadataTaskUnit.getContainersEffectiveRangesMap().keySet()) {
            List<String> expectedFeedRanges =
                expectedMetadataTaskUnit
                    .getContainersEffectiveRangesMap()
                    .get(containerRid)
                    .stream()
                    .map(FeedRange::toString)
                    .collect(Collectors.toList());

            String cosmosItemId = expectedMetadataTaskUnit.getDatabaseName() + "_" + containerRid + "_" + connectorName;
            JsonNode persistedFeedRangesMetadata =
                metadataContainer
                    .readItem(cosmosItemId, new PartitionKey(cosmosItemId), JsonNode.class)
                    .block()
                    .getItem();
            Map<String, Object> feedRangesMetadataMap =
                Utils
                    .getSimpleObjectMapper()
                    .convertValue(persistedFeedRangesMetadata.get("metadata"), new TypeReference<Map<String, Object>>() {});
            assertThat(feedRangesMetadataMap.containsKey("feedRanges")).isTrue();
            List<String> persistedFeedRanges =
                Utils
                    .getSimpleObjectMapper()
                    .readValue(feedRangesMetadataMap.get("feedRanges").toString(), new TypeReference<List<String>>() {
                    });
            assertThat(expectedFeedRanges.size()).isEqualTo(persistedFeedRanges.size());
            assertThat(expectedFeedRanges.containsAll(persistedFeedRanges)).isTrue();
        }
    }

    public static class SourceConfigs {
        public static final List<KafkaCosmosConfigEntry<?>> ALL_VALID_CONFIGS = Arrays.asList(
            new KafkaCosmosConfigEntry<String>("azure.cosmos.account.endpoint", null, false),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.account.tenantId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.auth.type", CosmosAuthType.MASTER_KEY.getName(), true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.account.key", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.auth.aad.clientId", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.auth.aad.clientSecret", Strings.Emtpy, true, true),
            new KafkaCosmosConfigEntry<Boolean>("azure.cosmos.mode.gateway", false, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.preferredRegionList", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.application.name", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.enabled", false, true),
            new KafkaCosmosConfigEntry<>("azure.cosmos.throughputControl.account.endpoint", Strings.Emtpy, true),
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

            new KafkaCosmosConfigEntry<String>("azure.cosmos.source.database.name", null, false),
            new KafkaCosmosConfigEntry<Boolean>("azure.cosmos.source.containers.includeAll", false, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.source.containers.includedList", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.source.containers.topicMap", Strings.Emtpy, true),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.source.changeFeed.startFrom",
                CosmosChangeFeedStartFromMode.BEGINNING.getName(),
                true),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.source.changeFeed.mode",
                CosmosChangeFeedMode.LATEST_VERSION.getName(),
                true),
            new KafkaCosmosConfigEntry<Integer>("azure.cosmos.source.changeFeed.maxItemCountHint", 1000, true),
            new KafkaCosmosConfigEntry<Integer>("azure.cosmos.source.metadata.poll.delay.ms", 5 * 60 * 1000, true),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.source.metadata.storage.name",
                "_cosmos.metadata.topic",
                true),
            new KafkaCosmosConfigEntry<String>(
                "azure.cosmos.source.metadata.storage.type",
                CosmosMetadataStorageType.KAFKA.getName(),
                true),
            new KafkaCosmosConfigEntry<Boolean>("azure.cosmos.source.messageKey.enabled", true, true),
            new KafkaCosmosConfigEntry<String>("azure.cosmos.source.messageKey.field", "id", true)
        );
    }
}
