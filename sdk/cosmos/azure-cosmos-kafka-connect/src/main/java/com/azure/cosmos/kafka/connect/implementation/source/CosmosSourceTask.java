// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.guava25.base.Stopwatch;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCache;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCacheItem;
import com.azure.cosmos.kafka.connect.implementation.CosmosThroughputControlHelper;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConstants;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class CosmosSourceTask extends SourceTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSourceTask.class);
    private static final String LSN_ATTRIBUTE_NAME = "_lsn";
    private static final String METADATA_ATTRIBUTE_NAME = "metadata";
    private static final String METADATA_LSN_ATTRIBUTE_NAME = "lsn";

    private CosmosSourceTaskConfig taskConfig;
    private CosmosClientCacheItem cosmosClientItem;
    private CosmosClientCacheItem throughputControlCosmosClientItem;
    private final Queue<ITaskUnit> taskUnitsQueue = new LinkedList<>();

    private long lastLogTimeMs = System.currentTimeMillis();
    private final Map<String, FeedRangeLoggingContext> feedRangeCounts = new ConcurrentHashMap<>();

    @Override
    public String version() {
        return KafkaCosmosConstants.CURRENT_VERSION;
    }

    @Override
    public void start(Map<String, String> map) {
        LOGGER.info("Starting the kafka cosmos source task...");
        try {
            LOGGER.info("Resetting task queue");
            this.taskUnitsQueue.clear();

            this.taskConfig = new CosmosSourceTaskConfig(map);
            if (this.taskConfig.getMetadataTaskUnit() != null) {
                // adding metadata task units into the head of the queue
                LOGGER.info("Adding metadata task to task {}", this.taskConfig.getTaskId());
                this.taskUnitsQueue.add(this.taskConfig.getMetadataTaskUnit());
            }

            LOGGER.info(
                "Adding {} feed range tasks to task {}",
                this.taskConfig.getFeedRangeTaskUnits().size(),
                this.taskConfig.getTaskId());

            this.taskUnitsQueue.addAll(this.taskConfig.getFeedRangeTaskUnits());
            LOGGER.info("Creating the cosmos client");

            this.cosmosClientItem =
                CosmosClientCache.getCosmosClient(
                    this.taskConfig.getAccountConfig(),
                    this.taskConfig.getTaskId(),
                    this.taskConfig.getCosmosClientMetadataCachesSnapshot());
            this.throughputControlCosmosClientItem = this.getThroughputControlCosmosClientItem();
        } catch (Throwable ex) {
            LOGGER.warn("Failed to start the cosmos source task", ex);
            this.cleanup();
            throw ex;
        }
    }

    private CosmosClientCacheItem getThroughputControlCosmosClientItem() {
        if (this.taskConfig.getThroughputControlConfig().isThroughputControlEnabled()
            && this.taskConfig.getThroughputControlConfig().getThroughputControlAccountConfig() != null) {
            // throughput control is using a different database account config
            return CosmosClientCache.getCosmosClient(
                this.taskConfig.getThroughputControlConfig().getThroughputControlAccountConfig(),
                this.taskConfig.getTaskId(),
                this.taskConfig.getThroughputControlCosmosClientMetadataCachesSnapshot());
        } else {
            return this.cosmosClientItem;
        }
    }

    @Override
    public List<SourceRecord> poll() {
        // do not poll it from the queue yet
        // we need to make sure not losing tasks for failure cases
        ITaskUnit taskUnit = this.taskUnitsQueue.poll();
        try {
            if (taskUnit == null) {
                // there is no task to do
                return new ArrayList<>();
            }

            List<SourceRecord> results = new ArrayList<>();
            if (taskUnit instanceof MetadataTaskUnit) {
                results.addAll(executeMetadataTask((MetadataTaskUnit) taskUnit));
                LOGGER.info(
                    "Return {} metadata records, databaseName {}", results.size(), ((MetadataTaskUnit) taskUnit).getDatabaseName());

            } else {
                Stopwatch stopwatch = Stopwatch.createStarted();

                LOGGER.trace("Polling for task {}", taskUnit);
                Pair<List<SourceRecord>, Boolean> feedRangeTaskResults = executeFeedRangeTask((FeedRangeTaskUnit) taskUnit);
                results.addAll(feedRangeTaskResults.getLeft());

                // for split, new feedRangeTaskUnit will be created, so we do not need to add the original taskUnit back to the queue
                if (!feedRangeTaskResults.getRight()) {
                    LOGGER.trace("Adding task {} back to queue", taskUnit);
                    this.taskUnitsQueue.add(taskUnit);
                }

                stopwatch.stop();
                
                // Update count for this feed range
                String feedRangeKey = ((FeedRangeTaskUnit) taskUnit).getFeedRange().toString();
                feedRangeCounts.compute(feedRangeKey, (key, loggingContext) -> {
                    if (loggingContext == null) {
                        loggingContext = new FeedRangeLoggingContext((FeedRangeTaskUnit) taskUnit);
                    }
                    loggingContext.increaseCount((long) results.size());
                    return loggingContext;
                });

                logFeedRangeCounts();
            }

            return results;
        } catch (Exception e) {
            // for error cases, we should always put the task back to the queue
            this.taskUnitsQueue.add(taskUnit);
            LOGGER.warn("Polling task failed", e);

            throw KafkaCosmosExceptionsHelper.convertToConnectException(e, "PollTask failed");
        }
    }

    private void logFeedRangeCounts() {
        long currentTimeInMs = System.currentTimeMillis();
        long durationInMs = currentTimeInMs - lastLogTimeMs;
        if (durationInMs >= CosmosSourceTaskConfig.LOG_INTERVAL_MS) {
            // Log accumulated counts for all feed ranges
            for (Map.Entry<String, FeedRangeLoggingContext> entry : feedRangeCounts.entrySet()) {
                LOGGER.info(
                    "Return total {} records, databaseName {}, containerName {}, containerRid {}, feedRange {}, durationInMs {}, taskId {}",
                    entry.getValue().count,
                    entry.getValue().feedRangeTaskUnit.getDatabaseName(),
                    entry.getValue().feedRangeTaskUnit.getContainerName(),
                    entry.getValue().feedRangeTaskUnit.getContainerRid(),
                    entry.getKey(),
                    durationInMs,
                    this.taskConfig.getTaskId()
                );
            }

            // Reset counts and update last log time
            feedRangeCounts.clear();
            lastLogTimeMs = currentTimeInMs;
        }
    }

    private List<SourceRecord> executeMetadataTask(MetadataTaskUnit taskUnit) {
        List<SourceRecord> sourceRecords = new ArrayList<>();

        // add the containers metadata record - it tracks the databaseName -> List[containerRid] mapping
        Pair<ContainersMetadataTopicPartition, ContainersMetadataTopicOffset> containersMetadata =
            taskUnit.getContainersMetadata();

        // Convert JSON to Kafka Connect struct and JSON schema
        SchemaAndValue containersMetadataSchemaAndValue = null;
        try {
            containersMetadataSchemaAndValue = JsonToStruct.recordToUnifiedSchema(
                MetadataEntityTypes.CONTAINERS_METADATA_V1,
                Utils.getSimpleObjectMapper().writeValueAsString(
                    ContainersMetadataTopicOffset.toMap(containersMetadata.getRight())));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        sourceRecords.add(
            new SourceRecord(
                ContainersMetadataTopicPartition.toMap(containersMetadata.getLeft()),
                ContainersMetadataTopicOffset.toMap(containersMetadata.getRight()),
                taskUnit.getStorageName(),
                Schema.STRING_SCHEMA,
                getContainersMetadataItemId(containersMetadata.getLeft().getDatabaseName(), containersMetadata.getLeft().getConnectorName()),
                containersMetadataSchemaAndValue.schema(),
                containersMetadataSchemaAndValue.value()));

        // add the container feedRanges metadata record - it tracks the containerRid -> List[FeedRange] mapping
        for (Pair<FeedRangesMetadataTopicPartition, FeedRangesMetadataTopicOffset> feedRangesMetadata : taskUnit.getFeedRangesMetadataList()) {
            SchemaAndValue feedRangeMetadataSchemaAndValue = null;
            try {
                feedRangeMetadataSchemaAndValue = JsonToStruct.recordToUnifiedSchema(
                    MetadataEntityTypes.FEED_RANGES_METADATA_V1,
                    Utils.getSimpleObjectMapper().writeValueAsString(
                        FeedRangesMetadataTopicOffset.toMap(feedRangesMetadata.getRight())));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            sourceRecords.add(
                new SourceRecord(
                    FeedRangesMetadataTopicPartition.toMap(feedRangesMetadata.getLeft()),
                    FeedRangesMetadataTopicOffset.toMap(feedRangesMetadata.getRight()),
                    taskUnit.getStorageName(),
                    Schema.STRING_SCHEMA,
                    this.getFeedRangesMetadataItemId(
                        feedRangesMetadata.getLeft().getDatabaseName(),
                        feedRangesMetadata.getLeft().getContainerRid(),
                        feedRangesMetadata.getLeft().getConnectorName()
                    ),
                    feedRangeMetadataSchemaAndValue.schema(),
                    feedRangeMetadataSchemaAndValue.value()));
        }

        LOGGER.info("There are {} metadata records being created/updated", sourceRecords.size());
        return sourceRecords;
    }

    private String getContainersMetadataItemId(String databaseName, String connectorName) {
        return databaseName + "_" + connectorName;
    }
    private String getFeedRangesMetadataItemId(String databaseName, String collectionRid, String connectorName) {
        return databaseName + "_" + collectionRid + "_" + connectorName;
    }

    private Pair<List<SourceRecord>, Boolean> executeFeedRangeTask(FeedRangeTaskUnit feedRangeTaskUnit) {
        CosmosAsyncContainer container =
            this.cosmosClientItem
                .getClient()
                .getDatabase(feedRangeTaskUnit.getDatabaseName())
                .getContainer(feedRangeTaskUnit.getContainerName());
        CosmosThroughputControlHelper.tryEnableThroughputControl(
            container,
            this.throughputControlCosmosClientItem.getClient(),
            this.taskConfig.getThroughputControlConfig());

        // each time we will only pull one page
        CosmosChangeFeedRequestOptions changeFeedRequestOptions =
            this.getChangeFeedRequestOptions(feedRangeTaskUnit);

        // split/merge will be handled in source task
        ModelBridgeInternal.disableSplitHandling(changeFeedRequestOptions);
        CosmosThroughputControlHelper
            .tryPopulateThroughputControlGroupName(
                changeFeedRequestOptions,
                this.taskConfig.getThroughputControlConfig());

        return container.queryChangeFeed(changeFeedRequestOptions, JsonNode.class)
            .byPage(this.taskConfig.getChangeFeedConfig().getMaxItemCountHint())
            .next()
            .map(feedResponse -> {
                List<SourceRecord> records = handleSuccessfulResponse(feedResponse, feedRangeTaskUnit);
                return Pair.of(records, false);
            })
            .onErrorResume(throwable -> {
                if (KafkaCosmosExceptionsHelper.isFeedRangeGoneException(throwable)) {
                    return this.handleFeedRangeGone(feedRangeTaskUnit)
                        .map(shouldRemoveOriginalTaskUnit -> Pair.of(new ArrayList<>(), shouldRemoveOriginalTaskUnit));
                }

                return Mono.error(throwable);
            })
            .block();
    }

    private List<SourceRecord> handleSuccessfulResponse(
        FeedResponse<JsonNode> feedResponse,
        FeedRangeTaskUnit feedRangeTaskUnit) {

        List<SourceRecord> sourceRecords = new ArrayList<>();
        for (JsonNode item : feedResponse.getResults()) {
            FeedRangeContinuationTopicPartition feedRangeContinuationTopicPartition =
                new FeedRangeContinuationTopicPartition(
                    feedRangeTaskUnit.getDatabaseName(),
                    feedRangeTaskUnit.getContainerRid(),
                    feedRangeTaskUnit.getFeedRange());

            FeedRangeContinuationTopicOffset feedRangeContinuationTopicOffset =
                new FeedRangeContinuationTopicOffset(
                    feedResponse.getContinuationToken(),
                    getItemLsn(item, this.taskConfig.getChangeFeedConfig().getChangeFeedModes()));

            // Set the Kafka message key if option is enabled and field is configured in document
            String messageKey = this.getMessageKey(item);

            // Convert JSON to Kafka Connect struct and JSON schema
            SchemaAndValue schemaAndValue = JsonToStruct.recordToSchemaAndValue(item);

            sourceRecords.add(
                new SourceRecord(
                    FeedRangeContinuationTopicPartition.toMap(feedRangeContinuationTopicPartition),
                    FeedRangeContinuationTopicOffset.toMap(feedRangeContinuationTopicOffset),
                    feedRangeTaskUnit.getTopic(),
                    Schema.STRING_SCHEMA,
                    messageKey,
                    schemaAndValue.schema(),
                    schemaAndValue.value()));
        }

        // Important: track the continuationToken
        feedRangeTaskUnit.setContinuationState(
            new KafkaCosmosChangeFeedState(feedResponse.getContinuationToken(), feedRangeTaskUnit.getFeedRange()));
        return sourceRecords;
    }

    private Mono<Boolean> handleFeedRangeGone(FeedRangeTaskUnit feedRangeTaskUnit) {
        // need to find out whether it is split or merge
        CosmosAsyncContainer container =
            this.cosmosClientItem
                .getClient()
                .getDatabase(feedRangeTaskUnit.getDatabaseName())
                .getContainer(feedRangeTaskUnit.getContainerName());

        return ImplementationBridgeHelpers
            .CosmosAsyncContainerHelper
            .getCosmosAsyncContainerAccessor()
            .getOverlappingFeedRanges(container, feedRangeTaskUnit.getFeedRange(), true)
            .flatMap(overlappedRanges -> {

                if (overlappedRanges.size() == 1) {
                    // merge happens
                    LOGGER.info(
                        "FeedRange {} is merged into {}, but we will continue polling data from feedRange {}",
                        feedRangeTaskUnit.getFeedRange(),
                        overlappedRanges.get(0).toString(),
                        feedRangeTaskUnit.getFeedRange());

                    // Continue using polling data from the current task unit feedRange
                    return Mono.just(false);
                } else {
                    LOGGER.info(
                        "FeedRange {} is split into {}. Will create new task units. ",
                        feedRangeTaskUnit.getFeedRange(),
                        overlappedRanges.stream().map(FeedRange::toString).collect(Collectors.toList())
                    );

                    for (FeedRange pkRange : overlappedRanges) {
                        FeedRangeTaskUnit childTaskUnit =
                            new FeedRangeTaskUnit(
                                feedRangeTaskUnit.getDatabaseName(),
                                feedRangeTaskUnit.getContainerName(),
                                feedRangeTaskUnit.getContainerRid(),
                                pkRange,
                                getChildRangeChangeFeedState(feedRangeTaskUnit.getContinuationState(), pkRange),
                                feedRangeTaskUnit.getTopic());
                        this.taskUnitsQueue.add(childTaskUnit);
                    }

                    // remove the current task unit from the queue
                    return Mono.just(true);
                }
            });
    }

    private String getItemLsn(JsonNode item, CosmosChangeFeedMode changeFeedMode) {
        switch (changeFeedMode) {
            case LATEST_VERSION:
                JsonNode lsnNode = item.get(LSN_ATTRIBUTE_NAME);
                return lsnNode != null ? lsnNode.asText() : null;
            case ALL_VERSION_AND_DELETES:
                JsonNode metadataNode = item.get(METADATA_ATTRIBUTE_NAME);
                if (metadataNode != null) {
                    JsonNode lsnNodeInMetadata = metadataNode.get(METADATA_LSN_ATTRIBUTE_NAME);
                    return lsnNodeInMetadata != null ? lsnNodeInMetadata.asText() : null;
                } else {
                    return null;
                }
            default:
                throw new IllegalArgumentException("Invalid change mode " + changeFeedMode);
        }
    }

    private String getMessageKey(JsonNode item) {
        String messageKey = "";
        if (this.taskConfig.getMessageKeyConfig().isMessageKeyEnabled()) {
            JsonNode messageKeyFieldNode = item.get(this.taskConfig.getMessageKeyConfig().getMessageKeyField());
            if (messageKeyFieldNode != null) {
                messageKey = messageKeyFieldNode.asText();
            }
        }

        return messageKey;
    }

    private KafkaCosmosChangeFeedState getChildRangeChangeFeedState(
        KafkaCosmosChangeFeedState parent,
        FeedRange feedRange) {
        return parent == null
            ? null : new KafkaCosmosChangeFeedState(parent.getResponseContinuation(), feedRange);
    }

    private CosmosChangeFeedRequestOptions getChangeFeedRequestOptions(FeedRangeTaskUnit feedRangeTaskUnit) {
        CosmosChangeFeedRequestOptions changeFeedRequestOptions = null;
        FeedRange changeFeedRange = feedRangeTaskUnit.getFeedRange();
        if (feedRangeTaskUnit.getContinuationState() == null) {
            switch (this.taskConfig.getChangeFeedConfig().getChangeFeedStartFromModes()) {
                case BEGINNING:
                    changeFeedRequestOptions =
                        CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(changeFeedRange);
                    break;
                case NOW:
                    changeFeedRequestOptions =
                        CosmosChangeFeedRequestOptions.createForProcessingFromNow(changeFeedRange);
                    break;
                case POINT_IN_TIME:
                    changeFeedRequestOptions =
                        CosmosChangeFeedRequestOptions
                            .createForProcessingFromPointInTime(
                                this.taskConfig.getChangeFeedConfig().getStartFrom(),
                                changeFeedRange);
                    break;
                default:
                    throw new IllegalArgumentException(feedRangeTaskUnit.getContinuationState() + " is not supported");
            }

            if (this.taskConfig.getChangeFeedConfig().getChangeFeedModes() == CosmosChangeFeedMode.ALL_VERSION_AND_DELETES) {
                changeFeedRequestOptions.allVersionsAndDeletes();
            }
        } else {
            KafkaCosmosChangeFeedState kafkaCosmosChangeFeedState = feedRangeTaskUnit.getContinuationState();
            changeFeedRequestOptions =
                ImplementationBridgeHelpers.CosmosChangeFeedRequestOptionsHelper
                    .getCosmosChangeFeedRequestOptionsAccessor()
                    .createForProcessingFromContinuation(
                        kafkaCosmosChangeFeedState.getResponseContinuation(),
                        kafkaCosmosChangeFeedState.getTargetRange(),
                        kafkaCosmosChangeFeedState.getItemLsn());
        }

        return changeFeedRequestOptions;
    }

    private void cleanup() {
        LOGGER.info("Cleaning up CosmosSourceTask");

        if (this.throughputControlCosmosClientItem != null && this.throughputControlCosmosClientItem != this.cosmosClientItem) {
            LOGGER.debug("Releasing throughput control cosmos client");
            CosmosClientCache.releaseCosmosClient(this.throughputControlCosmosClientItem.getClientConfig());
            this.throughputControlCosmosClientItem = null;
        }

        if (this.cosmosClientItem != null) {
            LOGGER.debug("Releasing cosmos client");
            CosmosClientCache.releaseCosmosClient(this.cosmosClientItem.getClientConfig());
            this.cosmosClientItem = null;
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping CosmosSourceTask");
        this.cleanup();
    }

    private static class FeedRangeLoggingContext {
        private final FeedRangeTaskUnit feedRangeTaskUnit;
        private final AtomicLong count;

        FeedRangeLoggingContext(FeedRangeTaskUnit feedRangeTaskUnit) {
            checkNotNull(feedRangeTaskUnit, "Argument feedRangeTaskUnit must not be null");
            this.feedRangeTaskUnit = feedRangeTaskUnit;
            this.count = new AtomicLong(0);
        }

        public void increaseCount(Long increments) {
            this.count.accumulateAndGet(increments, Long::sum);
        }
    }
}
