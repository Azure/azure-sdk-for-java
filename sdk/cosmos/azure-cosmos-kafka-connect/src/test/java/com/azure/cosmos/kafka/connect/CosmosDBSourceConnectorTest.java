// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosChangeFeedModes;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosChangeFeedStartFromModes;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceOffsetStorageReader;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceTask;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeContinuationTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeContinuationTopicPartition;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeTaskUnit;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangesMetadataTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangesMetadataTopicPartition;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataMonitorThread;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataTaskUnit;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.source.SourceConnectorContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.cosmos.kafka.connect.CosmosDBSourceConnectorTest.SourceConfigs.ALL_VALID_CONFIGS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.Assert.assertEquals;

@Test
public class CosmosDBSourceConnectorTest extends KafkaCosmosTestSuiteBase {
    @Test(groups = "unit")
    public void taskClass() {
        CosmosDBSourceConnector sourceConnector = new CosmosDBSourceConnector();
        assertEquals(sourceConnector.taskClass(), CosmosSourceTask.class);
    }

    @Test(groups = "unit")
    public void config() {
        CosmosDBSourceConnector sourceConnector = new CosmosDBSourceConnector();
        ConfigDef configDef = sourceConnector.config();
        Map<String, ConfigDef.ConfigKey> configs = configDef.configKeys();
        List<SourceConfigEntry<?>> allValidConfigs = ALL_VALID_CONFIGS;

        for (SourceConfigEntry<?> sourceConfigEntry : allValidConfigs) {
            assertThat(configs.containsKey(sourceConfigEntry.getName())).isTrue();

            configs.containsKey(sourceConfigEntry.getName());
            if (sourceConfigEntry.isOptional()) {
                assertThat(configs.get(sourceConfigEntry.getName()).defaultValue).isEqualTo(sourceConfigEntry.getDefaultValue());
            } else {
                assertThat(configs.get(sourceConfigEntry.getName()).defaultValue).isEqualTo(ConfigDef.NO_DEFAULT_VALUE);
            }
        }
    }

    @Test(groups = "{ fast }", timeOut = TIMEOUT)
    public void getTaskConfigsWithoutPersistedOffset() throws JsonProcessingException {
        CosmosDBSourceConnector sourceConnector = new CosmosDBSourceConnector();

        Map<String, Object> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("kafka.connect.cosmos.accountEndpoint", TestConfigurations.HOST);
        sourceConfigMap.put("kafka.connect.cosmos.accountKey", TestConfigurations.MASTER_KEY);
        sourceConfigMap.put("kafka.connect.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(
            singlePartitionContainerName,
            multiPartitionContainerName
        );
        sourceConfigMap.put("kafka.connect.cosmos.source.containers.includedList", containersIncludedList.toString());

        String singlePartitionContainerTopicName = singlePartitionContainerName + "topic";
        List<String> containerTopicMapList = Arrays.asList(singlePartitionContainerTopicName + "#" + singlePartitionContainerName);
        sourceConfigMap.put("kafka.connect.cosmos.source.containers.topicMap", containerTopicMapList.toString());

        // setup the internal state
        this.setupDefaultConnectorInternalStates(sourceConnector, sourceConfigMap);
        CosmosAsyncClient cosmosAsyncClient = KafkaCosmosReflectionUtils.getCosmosClient(sourceConnector);

        int maxTask = 2;
        List<Map<String, String>> taskConfigs = sourceConnector.taskConfigs(maxTask);
        assertThat(taskConfigs.size()).isEqualTo(maxTask);

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
                cosmosAsyncClient,
                databaseName,
                Arrays.asList(singlePartitionContainer, multiPartitionContainer));
        validateMetadataTask(expectedMetadataTaskUnit, taskConfigs.get(1));
    }

    @Test(groups = "{ fast }", timeOut = TIMEOUT)
    public void getTaskConfigsAfterSplit() throws JsonProcessingException {
        // This test is to simulate after a split happen, the task resume with persisted offset
        CosmosDBSourceConnector sourceConnector = new CosmosDBSourceConnector();

        Map<String, Object> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("kafka.connect.cosmos.accountEndpoint", TestConfigurations.HOST);
        sourceConfigMap.put("kafka.connect.cosmos.accountKey", TestConfigurations.MASTER_KEY);
        sourceConfigMap.put("kafka.connect.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(multiPartitionContainerName);
        sourceConfigMap.put("kafka.connect.cosmos.source.containers.includedList", containersIncludedList.toString());

        // setup the internal state
        this.setupDefaultConnectorInternalStates(sourceConnector, sourceConfigMap);

        // override the storage reader with initial offset
        CosmosAsyncClient cosmosAsyncClient = KafkaCosmosReflectionUtils.getCosmosClient(sourceConnector);
        CosmosSourceOffsetStorageReader sourceOffsetStorageReader = KafkaCosmosReflectionUtils.getSourceOffsetStorageReader(sourceConnector);
        InMemoryStorageReader inMemoryStorageReader =
            (InMemoryStorageReader) KafkaCosmosReflectionUtils.getOffsetStorageReader(sourceOffsetStorageReader);

        CosmosContainerProperties multiPartitionContainer = getMultiPartitionContainer(cosmosAsyncClient);

        // constructing feed range continuation offset
        FeedRangeContinuationTopicPartition feedRangeContinuationTopicPartition =
            new FeedRangeContinuationTopicPartition(
                databaseName,
                multiPartitionContainer.getResourceId(),
                FeedRangeEpkImpl.forFullRange().getRange());

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
            new FeedRangesMetadataTopicPartition(databaseName, multiPartitionContainer.getResourceId());
        FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
            new FeedRangesMetadataTopicOffset(Arrays.asList(FeedRangeEpkImpl.forFullRange().getRange()));
        initialOffsetMap.put(
            FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition),
            FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset));

        inMemoryStorageReader.populateOffset(initialOffsetMap);

        int maxTask = 2;
        List<Map<String, String>> taskConfigs = sourceConnector.taskConfigs(maxTask);
        assertThat(taskConfigs.size()).isEqualTo(maxTask);

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
                cosmosAsyncClient,
                databaseName,
                Arrays.asList(multiPartitionContainer));
        validateMetadataTask(expectedMetadataTaskUnit, taskConfigs.get(1));
    }

    @Test(groups = "{ fast }", timeOut = TIMEOUT)
    public void getTaskConfigsAfterMerge() throws JsonProcessingException {
        // This test is to simulate after a merge happen, the task resume with previous feedRanges
        CosmosDBSourceConnector sourceConnector = new CosmosDBSourceConnector();

        Map<String, Object> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("kafka.connect.cosmos.accountEndpoint", TestConfigurations.HOST);
        sourceConfigMap.put("kafka.connect.cosmos.accountKey", TestConfigurations.MASTER_KEY);
        sourceConfigMap.put("kafka.connect.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(singlePartitionContainerName);
        sourceConfigMap.put("kafka.connect.cosmos.source.containers.includedList", containersIncludedList.toString());

        // setup the internal state
        this.setupDefaultConnectorInternalStates(sourceConnector, sourceConfigMap);

        // override the storage reader with initial offset
        CosmosAsyncClient cosmosAsyncClient = KafkaCosmosReflectionUtils.getCosmosClient(sourceConnector);
        CosmosSourceOffsetStorageReader sourceOffsetStorageReader = KafkaCosmosReflectionUtils.getSourceOffsetStorageReader(sourceConnector);
        InMemoryStorageReader inMemoryStorageReader =
            (InMemoryStorageReader) KafkaCosmosReflectionUtils.getOffsetStorageReader(sourceOffsetStorageReader);

        CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(cosmosAsyncClient);

        // constructing feed range continuation offset
        List<FeedRangeEpkImpl> childRanges =
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

        for (FeedRangeEpkImpl childRange : childRanges) {
            FeedRangeContinuationTopicPartition feedRangeContinuationTopicPartition =
                new FeedRangeContinuationTopicPartition(
                    databaseName,
                    singlePartitionContainer.getResourceId(),
                    childRange.getRange());

            String childRangeContinuationState = new ChangeFeedStateV1(
                singlePartitionContainer.getResourceId(),
                childRange,
                ChangeFeedMode.INCREMENTAL,
                ChangeFeedStartFromInternal.createFromBeginning(),
                FeedRangeContinuation.create(
                    singlePartitionContainer.getResourceId(),
                    childRange,
                    Arrays.asList(new CompositeContinuationToken("1", childRange.getRange())))).toString();

            FeedRangeContinuationTopicOffset feedRangeContinuationTopicOffset =
                new FeedRangeContinuationTopicOffset(childRangeContinuationState, "1");

            initialOffsetMap.put(
                FeedRangeContinuationTopicPartition.toMap(feedRangeContinuationTopicPartition),
                FeedRangeContinuationTopicOffset.toMap(feedRangeContinuationTopicOffset));

            singlePartitionFeedRangeTaskUnits.add(
                new FeedRangeTaskUnit(
                    databaseName,
                    singlePartitionContainer.getId(),
                    singlePartitionContainer.getResourceId(),
                    childRange.getRange(),
                    childRangeContinuationState,
                    singlePartitionContainer.getId()));
        }

        // constructing feedRange metadata offset
        FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
            new FeedRangesMetadataTopicPartition(databaseName, singlePartitionContainer.getResourceId());
        FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
            new FeedRangesMetadataTopicOffset(
                childRanges
                    .stream()
                    .map(childRange -> childRange.getRange())
                    .collect(Collectors.toList()));

        initialOffsetMap.put(
            FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition),
            FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset));

        inMemoryStorageReader.populateOffset(initialOffsetMap);

        int maxTask = 2;
        List<Map<String, String>> taskConfigs = sourceConnector.taskConfigs(maxTask);
        assertThat(taskConfigs.size()).isEqualTo(maxTask);

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

        Map<String, List<Range<String>>> containersEffectiveRangesMap = new HashMap<>();
        containersEffectiveRangesMap.put(
            singlePartitionContainer.getResourceId(),
            childRanges.stream().map(FeedRangeEpkImpl::getRange).collect(Collectors.toList()));

        MetadataTaskUnit expectedMetadataTaskUnit =
            new MetadataTaskUnit(
                databaseName,
                Arrays.asList(singlePartitionContainer.getResourceId()),
                containersEffectiveRangesMap,
                "_cosmos.metadata.topic"
            );
        validateMetadataTask(expectedMetadataTaskUnit, taskConfigs.get(1));
    }

    @Test(groups = "unit")
    public void missingRequiredConfig() {

        List<SourceConfigEntry<?>> requiredConfigs =
            ALL_VALID_CONFIGS
                .stream()
                .filter(sourceConfigEntry -> !sourceConfigEntry.isOptional)
                .collect(Collectors.toList());

        assertThat(requiredConfigs.size()).isGreaterThan(1);
        CosmosDBSourceConnector sourceConnector = new CosmosDBSourceConnector();
        for (SourceConfigEntry configEntry : requiredConfigs) {

            Map<String, String> sourceConfigMap = this.getValidSourceConfig();
            sourceConfigMap.remove(configEntry.getName());
            Config validatedConfig = sourceConnector.validate(sourceConfigMap);
            ConfigValue configValue =
                validatedConfig
                    .configValues()
                    .stream()
                    .filter(config -> config.name().equalsIgnoreCase(configEntry.name))
                    .findFirst()
                    .get();

            assertThat(configValue.errorMessages()).isNotNull();
            assertThat(configValue.errorMessages().size()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test(groups = "unit")
    public void misFormattedConfig() {
        CosmosDBSourceConnector sourceConnector = new CosmosDBSourceConnector();
        Map<String, String> sourceConfigMap = this.getValidSourceConfig();

        String topicMapConfigName = "kafka.connect.cosmos.source.containers.topicMap";
        sourceConfigMap.put(topicMapConfigName, singlePartitionContainerName.toString());

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

        // TODO: add other config validations
    }

    private Map<String, String> getValidSourceConfig() {
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("kafka.connect.cosmos.accountEndpoint", TestConfigurations.HOST);
        sourceConfigMap.put("kafka.connect.cosmos.accountKey", TestConfigurations.MASTER_KEY);
        sourceConfigMap.put("kafka.connect.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(singlePartitionContainerName);
        sourceConfigMap.put("kafka.connect.cosmos.source.containers.includedList", containersIncludedList.toString());

        return sourceConfigMap;
    }

    private void setupDefaultConnectorInternalStates(CosmosDBSourceConnector sourceConnector, Map<String, Object> sourceConfigMap) {
        CosmosSourceConfig cosmosSourceConfig = new CosmosSourceConfig(sourceConfigMap);
        KafkaCosmosReflectionUtils.setCosmosSourceConfig(sourceConnector, cosmosSourceConfig);

        CosmosAsyncClient cosmosAsyncClient = CosmosClientStore.getCosmosClient(cosmosSourceConfig.getAccountConfig());
        KafkaCosmosReflectionUtils.setCosmosClient(sourceConnector, cosmosAsyncClient);

        InMemoryStorageReader inMemoryStorageReader = new InMemoryStorageReader();
        CosmosSourceOffsetStorageReader storageReader = new CosmosSourceOffsetStorageReader(inMemoryStorageReader);
        KafkaCosmosReflectionUtils.setOffsetStorageReader(sourceConnector, storageReader);

        SourceConnectorContext connectorContext = Mockito.mock(SourceConnectorContext.class);
        MetadataMonitorThread monitorThread = new MetadataMonitorThread(
            cosmosSourceConfig.getContainersConfig(),
            cosmosSourceConfig.getMetadataConfig(),
            connectorContext,
            storageReader,
            cosmosAsyncClient);

        KafkaCosmosReflectionUtils.setMetadataMonitorThread(sourceConnector, monitorThread);
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
                String feedRangeContinuationState = null;
                if (StringUtils.isNotEmpty(continuationState)) {
                    ChangeFeedState changeFeedState = ChangeFeedStateV1.fromString(continuationState);
                    feedRangeContinuationState =
                        new ChangeFeedStateV1(
                            changeFeedState.getContainerRid(),
                            FeedRangeEpkImpl.forFullRange(),
                            ChangeFeedMode.INCREMENTAL,
                            ChangeFeedStartFromInternal.createFromBeginning(),
                            FeedRangeContinuation.create(
                                changeFeedState.getContainerRid(),
                                (FeedRangeEpkImpl)feedRange,
                                Arrays.asList(
                                    new CompositeContinuationToken(
                                        changeFeedState.getContinuation().getCurrentContinuationToken().getToken(),
                                        ((FeedRangeEpkImpl)feedRange).getRange())))).toString();
                }

                return new FeedRangeTaskUnit(
                    databaseName,
                    containerProperties.getId(),
                    containerProperties.getResourceId(),
                    ((FeedRangeEpkImpl)feedRange).getRange(),
                    feedRangeContinuationState,
                    topicName);
            })
            .collect(Collectors.toList());
    }

    private MetadataTaskUnit getMetadataTaskUnit(
        CosmosAsyncClient cosmosAsyncClient,
        String databaseName,
        List<CosmosContainerProperties> containers) {

        Map<String, List<Range<String>>> containersEffectiveRangesMap = new HashMap<>();
        for (CosmosContainerProperties containerProperties : containers) {
            List<FeedRange> feedRanges =
                cosmosAsyncClient
                    .getDatabase(databaseName)
                    .getContainer(containerProperties.getId())
                    .getFeedRanges()
                    .block();

            containersEffectiveRangesMap.put(
                containerProperties.getResourceId(),
                feedRanges
                    .stream()
                    .map(feedRange -> ((FeedRangeEpkImpl)feedRange).getRange())
                    .collect(Collectors.toList()));
        }

        return new MetadataTaskUnit(
            databaseName,
            containers.stream().map(CosmosContainerProperties::getResourceId).collect(Collectors.toList()),
            containersEffectiveRangesMap,
            "_cosmos.metadata.topic"
        );
    }

    private void validateFeedRangeTasks(
        List<List<FeedRangeTaskUnit>> feedRangeTaskUnits,
        List<Map<String, String>> taskConfig) throws JsonProcessingException {

        String taskUnitsKey = "kafka.connect.cosmos.source.task.feedRangeTaskUnits";
        for (int i = 0; i< feedRangeTaskUnits.size(); i++) {
            List<FeedRangeTaskUnit> expectedTaskUnits = feedRangeTaskUnits.get(i);
            assertThat(taskConfig.get(i).containsKey(taskUnitsKey)).isTrue();
            List<FeedRangeTaskUnit> taskUnitsFromTaskConfig =
                Utils
                    .getSimpleObjectMapper()
                    .readValue(taskConfig.get(i).get(taskUnitsKey), new TypeReference<List<String>>() {})
                    .stream()
                    .map(taskUnitString -> {
                        try {
                            return Utils.getSimpleObjectMapper().readValue(taskUnitString, FeedRangeTaskUnit.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            assertThat(expectedTaskUnits.size()).isEqualTo(taskUnitsFromTaskConfig.size());
            assertThat(expectedTaskUnits.containsAll(taskUnitsFromTaskConfig)).isTrue();
        }
    }

    private void validateMetadataTask(
        MetadataTaskUnit expectedMetadataTaskUnit,
        Map<String, String> taskConfig) throws JsonProcessingException {

        String taskUnitKey = "kafka.connect.cosmos.source.task.metadataTaskUnit";
        assertThat(taskConfig.containsKey(taskUnitKey));
        MetadataTaskUnit metadataTaskUnitFromTaskConfig =
            Utils.getSimpleObjectMapper().readValue(taskConfig.get(taskUnitKey), MetadataTaskUnit.class);

        assertThat(expectedMetadataTaskUnit).isEqualTo(metadataTaskUnitFromTaskConfig);
    }

    public static class SourceConfigEntry<T> {
        private final String name;
        private final T defaultValue;
        private final boolean isOptional;

        public SourceConfigEntry(String name, T defaultValue, boolean isOptional) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.isOptional = isOptional;
        }

        public String getName() {
            return name;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public boolean isOptional() {
            return isOptional;
        }
    }

    public static class SourceConfigs {
        public static final List<SourceConfigEntry<?>> ALL_VALID_CONFIGS = Arrays.asList(
            new SourceConfigEntry<String>("kafka.connect.cosmos.accountEndpoint", null, false),
            new SourceConfigEntry<String>("kafka.connect.cosmos.accountKey", null, false),
            new SourceConfigEntry<Boolean>("kafka.connect.cosmos.useGatewayMode", false, true),
            new SourceConfigEntry<String>("kafka.connect.cosmos.preferredRegionsList", Strings.Emtpy, true),
            new SourceConfigEntry<String>("kafka.connect.cosmos.applicationName", Strings.Emtpy, true),
            new SourceConfigEntry<Boolean>("kafka.connect.cosmos.clientTelemetry.enabled", false, true),
            new SourceConfigEntry<String>("kafka.connect.cosmos.clientTelemetry.endpoint", Strings.Emtpy, true),
            new SourceConfigEntry<String>("kafka.connect.cosmos.source.database.name", null, false),
            new SourceConfigEntry<Boolean>("kafka.connect.cosmos.source.containers.includeAll", false, true),
            new SourceConfigEntry<String>("kafka.connect.cosmos.source.containers.includedList", Strings.Emtpy, true),
            new SourceConfigEntry<String>("kafka.connect.cosmos.source.containers.topicMap", Strings.Emtpy, true),
            new SourceConfigEntry<String>(
                "kafka.connect.cosmos.source.changeFeed.startFrom",
                CosmosChangeFeedStartFromModes.BEGINNING.getName(),
                true),
            new SourceConfigEntry<String>(
                "kafka.connect.cosmos.source.changeFeed.mode",
                CosmosChangeFeedModes.LATEST_VERSION.getName(),
                true),
            new SourceConfigEntry<Integer>("kafka.connect.cosmos.source.changeFeed.maxItemCount", 1000, true),
            new SourceConfigEntry<Integer>("kafka.connect.cosmos.source.metadata.poll.delay.ms", 5 * 60 * 1000, true),
            new SourceConfigEntry<String>(
                "kafka.connect.cosmos.source.metadata.storage.topic",
                "_cosmos.metadata.topic",
                true),
            new SourceConfigEntry<Boolean>("kafka.connect.cosmos.source.messageKey.enabled", true, true),
            new SourceConfigEntry<String>("kafka.connect.cosmos.source.messageKey.field", "id", true)
        );
    }
}
