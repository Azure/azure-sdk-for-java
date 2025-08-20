// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.UUIDs;
import com.azure.cosmos.implementation.apachecommons.lang.RandomUtils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCache;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCacheItem;
import com.azure.cosmos.kafka.connect.implementation.CosmosMasterKeyAuthConfig;
import com.azure.cosmos.kafka.connect.implementation.CosmosThroughputControlConfig;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConstants;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosUtils;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosMetadataStorageType;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceContainersConfig;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceTask;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceTaskConfig;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeContinuationTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeTaskUnit;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangesMetadataTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.IMetadataReader;
import com.azure.cosmos.kafka.connect.implementation.source.KafkaCosmosChangeFeedState;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataCosmosStorageManager;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataKafkaStorageManager;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataMonitorThread;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataTaskUnit;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.ExactlyOnceSupport;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.cosmos.kafka.connect.implementation.CosmosContainerUtils.validateDatabaseAndContainers;
import static com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig.validateCosmosAccountAuthConfig;
import static com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig.validateThroughputControlConfig;

/***
 * The CosmosDb source connector.
 */
public final class CosmosSourceConnector extends SourceConnector implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSourceConnector.class);
    private static final String CONNECTOR_NAME = "name";
    private static final int METADATA_CONTAINER_DEFAULT_RU_CONFIG = 4000;

    private CosmosSourceConfig config;
    private CosmosClientCacheItem cosmosClientItem;
    private MetadataMonitorThread monitorThread;
    private MetadataKafkaStorageManager kafkaOffsetStorageReader;
    private IMetadataReader metadataReader;
    private String connectorName;

    static {
        //initialize all accessors from different threads can cause deadlock issues, so here we force loading ahead of time
        ImplementationBridgeHelpers.initializeAllAccessors();
    }

    @Override
    public ExactlyOnceSupport exactlyOnceSupport(Map<String, String> connectorConfig) {
        return ExactlyOnceSupport.SUPPORTED;
    }

    @Override
    public void start(Map<String, String> props) {
        LOGGER.info("Starting the kafka cosmos source connector");
        try {
            this.config = new CosmosSourceConfig(props);
            this.connectorName = props.containsKey(CONNECTOR_NAME) ? props.get(CONNECTOR_NAME).toString() : "EMPTY";
            this.cosmosClientItem = CosmosClientCache.getCosmosClient(this.config.getAccountConfig(), connectorName);
            CosmosSourceContainersConfig containersConfig = this.config.getContainersConfig();

            List<String> containersExistedInDatabase = validateDatabaseAndContainers(
                containersConfig.isIncludeAllContainers() ? new ArrayList<>() : containersConfig.getIncludedContainers(),
                this.cosmosClientItem.getClient(),
                containersConfig.getDatabaseName());

            validateContainersInTopicMap(containersExistedInDatabase, new ArrayList<>(containersConfig.getContainerToTopicMap().keySet()));

            // IMPORTANT: sequence matters
            this.kafkaOffsetStorageReader = new MetadataKafkaStorageManager(this.context().offsetStorageReader());
            this.metadataReader = this.getMetadataReader();
            this.monitorThread = new MetadataMonitorThread(
                connectorName,
                this.config.getContainersConfig(),
                this.config.getMetadataConfig(),
                this.context(),
                this.metadataReader,
                this.cosmosClientItem.getClient()
            );

            this.monitorThread.start();
        } catch (Exception e) {
            // if the connector failed to start, release initialized resources here
            LOGGER.warn("Error starting the kafka cosmos sink connector", e);
            this.cleanup();
            // re-throw the exception back to kafka
            throw e;
        }

    }

    private void validateContainersInTopicMap(
        List<String> containersExistedInDatabase,
        List<String> containersInTopicMap) {
        if (containersInTopicMap != null
            && !containersInTopicMap.isEmpty()
            && !containersExistedInDatabase.containsAll(containersInTopicMap)) {
            throw new IllegalStateException("Containers specified in the topic map do not exist in the CosmosDB account.");
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return CosmosSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        // For now, we start with copying data by feed range
        // but in the future, we can have more optimization based on the data size etc.
        Pair<MetadataTaskUnit, List<FeedRangeTaskUnit>> taskUnits = this.getAllTaskUnits();
        List<Map<String, String>> taskConfigs = this.getFeedRangeTaskConfigs(taskUnits.getRight(), maxTasks);

        // Depending on where the metadata storage type is, we have different handling here.
        switch (taskUnits.getLeft().getStorageType()) {
            // If CosmosDB container is being used as the storage type, then we are going to create the metadata records in connector
            case COSMOS:
                updateMetadataRecordsInCosmos(taskUnits.getLeft());
                break;
            case KAFKA:
                // Else if using kafka topic as the storage type, then we are going to allocate the metadata records creation to one of the task. - Two issues/limitations exists:
                //   - a. The metadata topic can only be created on the same cluster as the other topics
                //   - b. As the metadata records are not created before all the feedRange tasks started, there is a rare edge cases that data from CosmosDB can be read twice (split/merge happens when writing the metadata records failed so the connector restarted)
                //
                // NOTE: we choose the current approach to avoid maintaining a producer by ourselves and also #b only happen for very rare cases.
                //
                // The metadataTaskUnit is a one time only task when the connector starts/restarts,
                // so there is no need to assign a dedicated task thread for it,
                // we are just going to assign it to the last of the task config as it has the least number of feedRange task units
                taskConfigs
                    .get(taskConfigs.size() - 1)
                    .putAll(CosmosSourceTaskConfig.getMetadataTaskUnitConfigMap(taskUnits.getLeft()));
                break;
            default:
                throw new IllegalArgumentException("StorageType " + taskUnits.getLeft().getStorageType() + " is not supported");
        }

        return taskConfigs;
    }

    private void cleanup() {
        LOGGER.info("Cleaning up CosmosSourceConnector");

        // Close monitor thread first since it uses the cosmos client
        if (this.monitorThread != null) {
            LOGGER.debug("Closing monitoring thread");
            this.monitorThread.close();
        }

        if (this.cosmosClientItem != null) {
            LOGGER.debug("Releasing cosmos client");
            CosmosClientCache.releaseCosmosClient(this.cosmosClientItem.getClientConfig());
            this.cosmosClientItem = null;
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Kafka CosmosDB source connector");
        cleanup();
    }

    @Override
    public ConfigDef config() {
        return CosmosSourceConfig.getConfigDef();
    }

    @Override
    public String version() {
        return KafkaCosmosConstants.CURRENT_VERSION;
    }

    private IMetadataReader getMetadataReader() {
        switch (this.config.getMetadataConfig().getStorageType()) {
            case KAFKA:
                return this.kafkaOffsetStorageReader;
            case COSMOS:
                CosmosAsyncContainer metadataContainer =
                    this.cosmosClientItem
                        .getClient()
                        .getDatabase(this.config.getContainersConfig().getDatabaseName())
                        .getContainer(this.config.getMetadataConfig().getStorageName());
                // validate the metadata container config
                metadataContainer.read()
                    .doOnNext(containerResponse -> {
                        PartitionKeyDefinition partitionKeyDefinition = containerResponse.getProperties().getPartitionKeyDefinition();
                        if (partitionKeyDefinition.getPaths().size() != 1 || !partitionKeyDefinition.getPaths().get(0).equals("/id")) {
                            throw new IllegalStateException("Cosmos Metadata container need to be partitioned by /id");
                        }
                    })
                    .onErrorResume(throwable -> {
                        if (KafkaCosmosExceptionsHelper.isNotFoundException(throwable)
                            && shouldCreateMetadataContainerIfNotExists()) {
                            return createMetadataContainer();
                        }

                        return Mono.error(new ConnectException(throwable));
                    })
                    .block();
                return new MetadataCosmosStorageManager(metadataContainer);
            default:
                throw new IllegalArgumentException("Metadata storage type " + this.config.getMetadataConfig().getStorageType() + " is not supported");
        }
    }

    private boolean shouldCreateMetadataContainerIfNotExists() {
        // If customer does not create the metadata container ahead of time,
        // then SDK will create one with default autoScale config only if using masterKey auth.
        return this.config.getMetadataConfig().getStorageType() == CosmosMetadataStorageType.COSMOS
            && (this.config.getAccountConfig().getCosmosAuthConfig() instanceof CosmosMasterKeyAuthConfig);
    }

    private Mono<CosmosContainerResponse> createMetadataContainer() {
        return this.createMetadataContainer(METADATA_CONTAINER_DEFAULT_RU_CONFIG)
            .onErrorResume(throwable -> {
                // for serverless database account, creating container with throughput configured will get 400/0 exceptions
                if (KafkaCosmosExceptionsHelper.isBadRequestException(throwable)) {
                    LOGGER.info(
                        "Getting exception '{}' when creating metadata container with throughput, retrying creating without throughput.",
                        throwable.getMessage());
                    return this.createMetadataContainer(null);
                }

                return Mono.error(throwable);
            });
    }

    private Mono<CosmosContainerResponse> createMetadataContainer(Integer throughput) {
        return this.cosmosClientItem
            .getClient()
            .getDatabase(this.config.getContainersConfig().getDatabaseName())
            .createContainer(
                this.config.getMetadataConfig().getStorageName(),
                "/id",
                throughput == null
                    ? null : ThroughputProperties.createAutoscaledThroughput(throughput));
    }

    private void updateMetadataRecordsInCosmos(MetadataTaskUnit metadataTaskUnit) {
        if (metadataTaskUnit.getStorageType() != CosmosMetadataStorageType.COSMOS) {
            throw new IllegalStateException("updateMetadataRecordsInCosmos should not be called when metadata storage type is not cosmos");
        }

        MetadataCosmosStorageManager cosmosProducer = (MetadataCosmosStorageManager) this.metadataReader;
        cosmosProducer.createMetadataItems(metadataTaskUnit);
    }

    private List<Map<String, String>> getFeedRangeTaskConfigs(List<FeedRangeTaskUnit> taskUnits, int maxTasks) {

        List<List<FeedRangeTaskUnit>> partitionedTaskUnits = new ArrayList<>();
        if (taskUnits.size() <= maxTasks) {
            partitionedTaskUnits.addAll(
                taskUnits.stream().map(taskUnit -> Arrays.asList(taskUnit)).collect(Collectors.toList()));
        } else {
            // using round-robin fashion to assign tasks to each buckets
            for (int i = 0; i < maxTasks; i++) {
                partitionedTaskUnits.add(new ArrayList<>());
            }

            for (int i = 0; i < taskUnits.size(); i++) {
                partitionedTaskUnits.get(i % maxTasks).add(taskUnits.get(i));
            }
        }

        List<Map<String, String>> feedRangeTaskConfigs = new ArrayList<>();
        String clientMetadataCachesString = getClientMetadataCachesSnapshotString();
        String throughputControlClientMetadataCachesString = getThroughputControlClientMetadataCachesSnapshotString();

        partitionedTaskUnits.forEach(feedRangeTaskUnits -> {
            Map<String, String> taskConfigs = this.config.originalsStrings();
            taskConfigs.putAll(
                CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(feedRangeTaskUnits));
            taskConfigs.put(CosmosSourceTaskConfig.SOURCE_TASK_ID,
                String.format("%s-%s-%d",
                    "source",
                    this.connectorName,
                    RandomUtils.nextInt(1, 9999999)));
            if (StringUtils.isNotEmpty(clientMetadataCachesString)) {
                taskConfigs.put(
                    CosmosSourceTaskConfig.COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT,
                    clientMetadataCachesString);
            }

            if (StringUtils.isNotEmpty(throughputControlClientMetadataCachesString)) {
                taskConfigs.put(
                    CosmosSourceTaskConfig.THROUGHPUT_CONTROL_COSMOS_CLIENT_METADATA_CACHES_SNAPSHOT,
                    throughputControlClientMetadataCachesString);
            }

            feedRangeTaskConfigs.add(taskConfigs);
        });

        return feedRangeTaskConfigs;
    }

    private Pair<MetadataTaskUnit, List<FeedRangeTaskUnit>> getAllTaskUnits() {
        List<CosmosContainerProperties> allContainers = this.monitorThread.getAllContainers().block();
        if (allContainers.isEmpty()) {
            throw new ConnectException("Some of the containers specified in the config were not found in the database.");
        }
        Map<String, String> containerTopicMap = this.getContainersTopicMap(allContainers);
        List<FeedRangeTaskUnit> allFeedRangeTaskUnits = new ArrayList<>();
        Map<String, List<FeedRange>> updatedContainerToFeedRangesMap = new ConcurrentHashMap<>();

        for (CosmosContainerProperties containerProperties : allContainers) {
            Map<FeedRange, KafkaCosmosChangeFeedState> effectiveFeedRangesContinuationMap =
                this.getEffectiveFeedRangesContinuationMap(
                    this.config.getContainersConfig().getDatabaseName(),
                    containerProperties);

            updatedContainerToFeedRangesMap.put(
                containerProperties.getResourceId(),
                effectiveFeedRangesContinuationMap.keySet().stream().collect(Collectors.toList())
            );

            // add feedRange task unit
            for (FeedRange effectiveFeedRange : effectiveFeedRangesContinuationMap.keySet()) {
                allFeedRangeTaskUnits.add(
                    new FeedRangeTaskUnit(
                        this.config.getContainersConfig().getDatabaseName(),
                        containerProperties.getId(),
                        containerProperties.getResourceId(),
                        effectiveFeedRange,
                        effectiveFeedRangesContinuationMap.get(effectiveFeedRange),
                        containerTopicMap.get(containerProperties.getId())
                    )
                );
            }
        }

        MetadataTaskUnit metadataTaskUnit =
            new MetadataTaskUnit(
                this.connectorName,
                this.config.getContainersConfig().getDatabaseName(),
                allContainers.stream().map(CosmosContainerProperties::getResourceId).collect(Collectors.toList()),
                updatedContainerToFeedRangesMap,
                this.config.getMetadataConfig().getStorageName(),
                this.config.getMetadataConfig().getStorageType());

        return Pair.of(metadataTaskUnit, allFeedRangeTaskUnits);
    }

    private Map<FeedRange, KafkaCosmosChangeFeedState> getEffectiveFeedRangesContinuationMap(
        String databaseName,
        CosmosContainerProperties containerProperties) {
        // Return effective feed ranges to be used for copying data from container
        // - If there is no existing offset, then use the result from container.getFeedRanges
        // - If there is existing offset, then deciding the final range sets based on:
        // -----If we can find offset by matching the feedRange, then use the feedRange
        // -----If we can not find offset by matching the exact feedRange,
        // then it means the feedRanges of the containers have changed either due to split or merge.
        // If a merge is detected, we will use the matched feedRanges from the offsets,
        // otherwise use the current feedRange, but constructing the continuationState based on the previous feedRange

        List<FeedRange> containerFeedRanges = this.getFeedRanges(containerProperties);

        FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
            this.metadataReader
                .getFeedRangesMetadataOffset(databaseName, containerProperties.getResourceId(), this.connectorName)
                .block().v;

        Map<FeedRange, KafkaCosmosChangeFeedState> effectiveFeedRangesContinuationMap = new LinkedHashMap<>();
        CosmosAsyncContainer container =
            this.cosmosClientItem
                .getClient()
                .getDatabase(databaseName).getContainer(containerProperties.getId());

        Flux.fromIterable(containerFeedRanges)
            .flatMap(containerFeedRange -> {
                if (feedRangesMetadataTopicOffset == null) {
                    return Mono.just(
                        Collections.singletonMap(containerFeedRange, (KafkaCosmosChangeFeedState) null));
                } else {
                    // there is existing offsets, need to find out effective feedRanges based on the offset
                    return this.getEffectiveContinuationMapForSingleFeedRange(
                        databaseName,
                        containerProperties.getResourceId(),
                        containerFeedRange,
                        container,
                        feedRangesMetadataTopicOffset.getFeedRanges());
                }
            })
            .doOnNext(map -> {
                effectiveFeedRangesContinuationMap.putAll(map);
            })
            .blockLast();

        return effectiveFeedRangesContinuationMap;
    }

    private Mono<Map<FeedRange, KafkaCosmosChangeFeedState>> getEffectiveContinuationMapForSingleFeedRange(
        String databaseName,
        String containerRid,
        FeedRange containerFeedRange,
        CosmosAsyncContainer cosmosAsyncContainer,
        List<FeedRange> rangesFromMetadataTopicOffset) {

        //first try to find out whether there is exact feedRange matching
        FeedRangeContinuationTopicOffset feedRangeContinuationTopicOffset =
            this.kafkaOffsetStorageReader.getFeedRangeContinuationOffset(databaseName, containerRid, containerFeedRange);

        Map<FeedRange, KafkaCosmosChangeFeedState> effectiveContinuationMap = new LinkedHashMap<>();
        if (feedRangeContinuationTopicOffset != null) {
            // we can find the continuation offset based on exact feedRange matching
            effectiveContinuationMap.put(
                containerFeedRange,
                this.getContinuationStateFromOffset(
                    feedRangeContinuationTopicOffset,
                    containerFeedRange));

            return Mono.just(effectiveContinuationMap);
        }

        // we can not find the continuation offset based on the exact feed range matching
        // it means the previous Partition key range could have gone due to container split/merge or there is no continuation state yet
        // need to find out overlapped feedRanges from offset
        return  Flux.fromIterable(rangesFromMetadataTopicOffset)
                    .flatMap(rangeFromOffset -> {
                        return ImplementationBridgeHelpers
                                .CosmosAsyncContainerHelper
                                .getCosmosAsyncContainerAccessor()
                                .checkFeedRangeOverlapping(cosmosAsyncContainer, rangeFromOffset, containerFeedRange)
                            .flatMap(overlapped -> {
                                if (overlapped) {
                                    return Mono.just(rangeFromOffset);
                                } else {
                                    return Mono.empty();
                                }
                            });
                    })
                    .collectList()
                    .flatMap(overlappedFeedRangesFromOffset -> {
                        if (overlappedFeedRangesFromOffset.size() == 1) {
                            // a. split - use the current containerFeedRange, but construct the continuationState based on the feedRange from offset
                            // b. there is no existing feed range continuationToken state yet
                            FeedRangeContinuationTopicOffset continuationTopicOffset = this.kafkaOffsetStorageReader.getFeedRangeContinuationOffset(
                                databaseName,
                                containerRid,
                                overlappedFeedRangesFromOffset.get(0)
                            );

                            if (continuationTopicOffset == null) {
                                effectiveContinuationMap.put(overlappedFeedRangesFromOffset.get(0), null);
                            } else {
                                effectiveContinuationMap.put(
                                    containerFeedRange,
                                    this.getContinuationStateFromOffset(continuationTopicOffset, containerFeedRange));
                            }

                            return Mono.just(effectiveContinuationMap);
                        }

                        if (overlappedFeedRangesFromOffset.size() > 1) {
                            // merge - use the feed ranges from the offset
                            for (FeedRange overlappedRangeFromOffset : overlappedFeedRangesFromOffset) {
                                FeedRangeContinuationTopicOffset continuationTopicOffset =
                                    this.kafkaOffsetStorageReader
                                        .getFeedRangeContinuationOffset(
                                            databaseName,
                                            containerRid,
                                            overlappedRangeFromOffset);
                                if (continuationTopicOffset == null) {
                                    effectiveContinuationMap.put(overlappedRangeFromOffset, null);
                                } else {
                                    effectiveContinuationMap.put(
                                        overlappedRangeFromOffset,
                                        this.getContinuationStateFromOffset(
                                            this.kafkaOffsetStorageReader.getFeedRangeContinuationOffset(databaseName, containerRid, overlappedRangeFromOffset),
                                            overlappedRangeFromOffset));
                                }
                            }

                            return Mono.just(effectiveContinuationMap);
                        }

                        // Can not find overlapped ranges from offset, this should never happen, fail
                        LOGGER.error("Can not find overlapped ranges for feedRange {}", containerFeedRange);
                        return Mono.error(new IllegalStateException("Can not find overlapped ranges for feedRange " + containerFeedRange));
                    });
    }

    private KafkaCosmosChangeFeedState getContinuationStateFromOffset(
        FeedRangeContinuationTopicOffset feedRangeContinuationTopicOffset,
        FeedRange feedRange) {

        KafkaCosmosChangeFeedState changeFeedState =
            new KafkaCosmosChangeFeedState(
                feedRangeContinuationTopicOffset.getResponseContinuation(),
                feedRange,
                feedRangeContinuationTopicOffset.getItemLsn());

        return changeFeedState;
    }

    private List<FeedRange> getFeedRanges(CosmosContainerProperties containerProperties) {
        return this.cosmosClientItem
            .getClient()
            .getDatabase(this.config.getContainersConfig().getDatabaseName())
            .getContainer(containerProperties.getId())
            .getFeedRanges()
            .onErrorMap(throwable ->
                KafkaCosmosExceptionsHelper.convertToConnectException(
                    throwable,
                    "GetFeedRanges failed for container " + containerProperties.getId()))
            .block();
    }

    private Map<String, String> getContainersTopicMap(List<CosmosContainerProperties> allContainers) {
        Map<String, String> topicMapFromConfig =
            this.config.getContainersConfig().getContainerToTopicMap();

        Map<String, String> effectiveContainersTopicMap = new HashMap<>();
        allContainers.forEach(containerProperties -> {
            // by default, we are using container id as the topic name as well unless customer override through containers.topicMap
            if (topicMapFromConfig.containsKey(containerProperties.getId())) {
                effectiveContainersTopicMap.put(
                    containerProperties.getId(),
                    topicMapFromConfig.get(containerProperties.getId()));
            } else {
                effectiveContainersTopicMap.put(
                    containerProperties.getId(),
                    containerProperties.getId());
            }
        });

        return effectiveContainersTopicMap;
    }

    private String getClientMetadataCachesSnapshotString() {

        CosmosSourceContainersConfig containersConfig = this.config.getContainersConfig();
        List<String> containerNames =
            this.monitorThread
                .getAllContainers()
                .map(cosmosContainerProperties ->
                    cosmosContainerProperties
                        .stream()
                        .map(CosmosContainerProperties::getId)
                        .collect(Collectors.toList()))
                .block();
        CosmosAsyncDatabase database =
            this.cosmosClientItem
                .getClient()
                .getDatabase(containersConfig.getDatabaseName());

        // read a random item from each container to populate the collection cache
        for (String containerName : containerNames) {
            CosmosAsyncContainer container = database.getContainer(containerName);
            this.readRandomItemFromContainer(container);
        }

        // read a random item from throughput control container if it is enabled and use the same account config as the cosmos client
        CosmosThroughputControlConfig cosmosThroughputControlConfig = this.config.getThroughputControlConfig();
        if (cosmosThroughputControlConfig.isThroughputControlEnabled()) {
            if (cosmosThroughputControlConfig.getThroughputControlAccountConfig() == null) {
                CosmosAsyncContainer throughputControlContainer =
                    this.cosmosClientItem
                        .getClient()
                        .getDatabase(cosmosThroughputControlConfig.getGlobalThroughputControlDatabaseName())
                        .getContainer(cosmosThroughputControlConfig.getGlobalThroughputControlContainerName());
                readRandomItemFromContainer(throughputControlContainer);
            }
        }

        // read a random item from metadata container if COSMOS storage type is used
        if (this.config.getMetadataConfig().getStorageType() == CosmosMetadataStorageType.COSMOS) {
            readRandomItemFromContainer(database.getContainer(this.config.getMetadataConfig().getStorageName()));
        }

        return KafkaCosmosUtils.convertClientMetadataCacheSnapshotToString(this.cosmosClientItem.getClient());
    }

    private String getThroughputControlClientMetadataCachesSnapshotString() {
        CosmosClientCacheItem throughputControlClientItem = null;
        try {
            CosmosThroughputControlConfig throughputControlConfig = this.config.getThroughputControlConfig();
            if (throughputControlConfig.isThroughputControlEnabled()
                && throughputControlConfig.getThroughputControlAccountConfig() != null) {
                throughputControlClientItem = CosmosClientCache.getCosmosClient(
                    this.config.getThroughputControlConfig().getThroughputControlAccountConfig(),
                    this.connectorName
                );
            }

            if (throughputControlClientItem != null) {
                this.readRandomItemFromContainer(
                    throughputControlClientItem
                        .getClient()
                        .getDatabase(throughputControlConfig.getGlobalThroughputControlDatabaseName())
                        .getContainer(throughputControlConfig.getGlobalThroughputControlContainerName())
                );
                return KafkaCosmosUtils.convertClientMetadataCacheSnapshotToString(throughputControlClientItem.getClient());
            }

            return null;

        } finally {
            if (throughputControlClientItem != null) {
                CosmosClientCache.releaseCosmosClient(throughputControlClientItem.getClientConfig());
            }
        }
    }

    private void readRandomItemFromContainer(CosmosAsyncContainer container) {
        if (container != null) {
            container.readItem(UUIDs.nonBlockingRandomUUID().toString(), new PartitionKey(UUIDs.nonBlockingRandomUUID().toString()), JsonNode.class)
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
        return config;
    }

    @Override
    public void close() {
        this.stop();
    }
}
