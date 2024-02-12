// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.CosmosConstants;
import com.azure.cosmos.kafka.connect.implementation.CosmosExceptionsHelper;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
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
import java.util.stream.Collectors;

public class CosmosSourceTask extends SourceTask {
    private static final Logger logger = LoggerFactory.getLogger(CosmosSourceTask.class);
    private static final String LSN_ATTRIBUTE_NAME = "_lsn";

    private CosmosSourceTaskConfig taskConfig;
    private CosmosAsyncClient cosmosClient;
    private Queue<ITaskUnit> taskUnitsQueue = new LinkedList<>();

    @Override
    public String version() {
        return CosmosConstants.currentVersion;
    }

    @Override
    public void start(Map<String, String> map) {
        logger.info("Starting the kafka cosmos source task...");

        this.taskConfig = new CosmosSourceTaskConfig(map);
        if (this.taskConfig.getMetadataTaskUnit() != null) {
            // adding metadata task units into the head of the queue
            this.taskUnitsQueue.add(this.taskConfig.getMetadataTaskUnit());
        }

        this.taskUnitsQueue.addAll(this.taskConfig.getFeedRangeTaskUnits());
        logger.info("Creating the cosmos client");
        this.cosmosClient = CosmosClientStore.getCosmosClient(this.taskConfig.getAccountConfig());
    }

    @Override
    public List<SourceRecord> poll() {
        // do not poll it from the queue yet
        // we need to make sure not losing tasks for failure cases
        logger.info("polling task");
        ITaskUnit taskUnit = this.taskUnitsQueue.poll();
        try {
            if (taskUnit == null) {
                // there is no task to do
                return new ArrayList<>();
            }

            List<SourceRecord> results = new ArrayList<>();
            if (taskUnit instanceof MetadataTaskUnit) {
                results.addAll(executeMetadataTask((MetadataTaskUnit) taskUnit));
            } else {
                logger.trace("Polling for task {}", taskUnit);
                Pair<List<SourceRecord>, Boolean> feedRangeTaskResults = executeFeedRangeTask((FeedRangeTaskUnit) taskUnit);
                results.addAll(feedRangeTaskResults.getLeft());

                // for split, new feedRangeTaskUnit will be created, so we do not need to add the original taskUnit back to the queue
                if (!feedRangeTaskResults.getRight()) {
                    logger.trace("Adding task {} back to queue", taskUnit);
                    this.taskUnitsQueue.add(taskUnit);
                }
            }

            logger.info("Return {} records", results.size());
            return results;
        } catch (Exception e) {
            // for error cases, we should always the task back to the queue
            this.taskUnitsQueue.add(taskUnit);

            // TODO: add checking for max retries checking
            if (CosmosExceptionsHelper.isTransientFailure(e)) {
                throw new RetriableException("PollTask failed with transient failure.", e);
            }

            throw new ConnectException("PollTask failed with non-transient failure. ", e);
        }
    }

    private List<SourceRecord> executeMetadataTask(MetadataTaskUnit taskUnit) {
        List<SourceRecord> sourceRecords = new ArrayList<>();

        // add the containers metadata record - it track the databaseName -> List[containerRid] mapping
        ContainersMetadataTopicPartition metadataTopicPartition =
            new ContainersMetadataTopicPartition(taskUnit.getDatabaseName());
        ContainersMetadataTopicOffset metadataTopicOffset =
            new ContainersMetadataTopicOffset(taskUnit.getContainerRids());

        sourceRecords.add(
            new SourceRecord(
                ContainersMetadataTopicPartition.toMap(metadataTopicPartition),
                ContainersMetadataTopicOffset.toMap(metadataTopicOffset),
                taskUnit.getTopic(),
                SchemaAndValue.NULL.schema(),
                SchemaAndValue.NULL.value()));

        // add the container feedRanges metadata record - it tracks the containerRid -> List[FeedRange] mapping
        for (String containerRid : taskUnit.getContainersEffectiveRangesMap().keySet()) {
            FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
                new FeedRangesMetadataTopicPartition(taskUnit.getDatabaseName(), containerRid);
            FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
                new FeedRangesMetadataTopicOffset(taskUnit.getContainersEffectiveRangesMap().get(containerRid));

            sourceRecords.add(
                new SourceRecord(
                    FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition),
                    FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset),
                    taskUnit.getTopic(),
                    SchemaAndValue.NULL.schema(),
                    SchemaAndValue.NULL.value()));
        }

        logger.info("There are {} metadata records being created/updated", sourceRecords.size());
        return sourceRecords;
    }

    private Pair<List<SourceRecord>, Boolean> executeFeedRangeTask(FeedRangeTaskUnit feedRangeTaskUnit) {
        // each time we will only pull one page
        CosmosChangeFeedRequestOptions changeFeedRequestOptions =
            this.getChangeFeedRequestOptions(feedRangeTaskUnit);

        // split/merge will be handled in source task
        ModelBridgeInternal.getChangeFeedIsSplitHandlingDisabled(changeFeedRequestOptions);

        CosmosAsyncContainer container =
            this.cosmosClient
                .getDatabase(feedRangeTaskUnit.getDatabaseName())
                .getContainer(feedRangeTaskUnit.getContainerName());

        return container.queryChangeFeed(changeFeedRequestOptions, JsonNode.class)
            .byPage()
            .next()
            .map(feedResponse -> {
                List<SourceRecord> records = handleSuccessfulResponse(feedResponse, feedRangeTaskUnit);
                return Pair.of(records, false);
            })
            .onErrorResume(throwable -> {
                if (CosmosExceptionsHelper.isFeedRangeGoneException(throwable)) {
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
                    getItemLsn(item));

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
        feedRangeTaskUnit.setContinuationState(feedResponse.getContinuationToken());
        return sourceRecords;
    }

    private Mono<Boolean> handleFeedRangeGone(FeedRangeTaskUnit feedRangeTaskUnit) {
        // need to find out whether it is split or merge
        AsyncDocumentClient asyncDocumentClient = CosmosBridgeInternal.getAsyncDocumentClient(this.cosmosClient);
        CosmosAsyncContainer container =
            this.cosmosClient
                .getDatabase(feedRangeTaskUnit.getDatabaseName())
                .getContainer(feedRangeTaskUnit.getContainerName());
        return asyncDocumentClient
            .getCollectionCache()
            .resolveByNameAsync(null, BridgeInternal.extractContainerSelfLink(container), null)
            .flatMap(collection -> {
                return asyncDocumentClient.getPartitionKeyRangeCache().tryGetOverlappingRangesAsync(
                    null,
                    collection.getResourceId(),
                    feedRangeTaskUnit.getFeedRange(),
                    true,
                    null);
            })
            .flatMap(pkRangesValueHolder -> {
                if (pkRangesValueHolder == null || pkRangesValueHolder.v == null) {
                    return Mono.error(new IllegalStateException("There are no overlapping ranges for the range"));
                }

                List<PartitionKeyRange> partitionKeyRanges = pkRangesValueHolder.v;
                if (partitionKeyRanges.size() == 1) {
                    // merge happens
                    logger.info(
                        "FeedRange {} is merged into {}, but we will continue polling data from feedRange {}",
                        feedRangeTaskUnit.getFeedRange(),
                        partitionKeyRanges.get(0).toRange(),
                        feedRangeTaskUnit.getFeedRange());

                    // Continue using polling data from the current task unit feedRange
                    return Mono.just(false);
                } else {
                    logger.info(
                        "FeedRange {} is split into {}. Will create new task units. ",
                        feedRangeTaskUnit.getFeedRange(),
                        partitionKeyRanges.stream().map(PartitionKeyRange::toRange).collect(Collectors.toList())
                    );

                    for (PartitionKeyRange pkRange : partitionKeyRanges) {
                        FeedRangeTaskUnit childTaskUnit =
                            new FeedRangeTaskUnit(
                                feedRangeTaskUnit.getDatabaseName(),
                                feedRangeTaskUnit.getContainerName(),
                                feedRangeTaskUnit.getContainerRid(),
                                pkRange.toRange(),
                                feedRangeTaskUnit.getContinuationState(),
                                feedRangeTaskUnit.getTopic());
                        this.taskUnitsQueue.add(childTaskUnit);
                    }

                    // remove the current task unit from the queue
                    return Mono.just(true);
                }
            });
    }

    private String getItemLsn(JsonNode item) {
        return item.get(LSN_ATTRIBUTE_NAME).asText();
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

    private CosmosChangeFeedRequestOptions getChangeFeedRequestOptions(FeedRangeTaskUnit feedRangeTaskUnit) {
        CosmosChangeFeedRequestOptions changeFeedRequestOptions = null;
        FeedRange changeFeedRange = new FeedRangeEpkImpl(feedRangeTaskUnit.getFeedRange());
        if (StringUtils.isEmpty(feedRangeTaskUnit.getContinuationState())) {
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

            changeFeedRequestOptions.setMaxItemCount(this.taskConfig.getChangeFeedConfig().getMaxItemCount());
            if (this.taskConfig.getChangeFeedConfig().getChangeFeedModes() == CosmosChangeFeedModes.ALL_VERSION_AND_DELETES) {
                changeFeedRequestOptions.allVersionsAndDeletes();
            }
        } else {
            changeFeedRequestOptions =
                CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(feedRangeTaskUnit.getContinuationState());
        }

        return changeFeedRequestOptions;
    }

    @Override
    public void stop() {
        if (this.cosmosClient != null) {
            this.cosmosClient.close();
        }
    }
}
