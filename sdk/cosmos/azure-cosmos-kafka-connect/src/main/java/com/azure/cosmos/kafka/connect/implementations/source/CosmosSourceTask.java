// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.kafka.connect.implementations.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementations.CosmosConstants;
import com.azure.cosmos.kafka.connect.implementations.CosmosExceptionsHelper;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
        logger.debug("polling task");
        try {
            ITaskUnit taskUnit = this.taskUnitsQueue.peek();
            if (taskUnit == null) {
                // there is no task to do
                return new ArrayList<>();
            }

            List<SourceRecord> results = new ArrayList<>();
            if (taskUnit instanceof MetadataTaskUnit) {
                results.addAll(executeMetadataTask((MetadataTaskUnit) taskUnit));
                // for metadata task, after successfully adding it, we are going to remove it from the queue
                this.taskUnitsQueue.poll();

            } else {
                results.addAll(executeFeedRangeTask((FeedRangeTaskUnit) taskUnit));
                // for feed range task, we will put the task to the end of the queue
                this.taskUnitsQueue.poll();
                this.taskUnitsQueue.add(taskUnit);
            }

            logger.debug("Return {} records", results.size());
            return results;
        } catch (Exception e) {
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

    private List<SourceRecord> executeFeedRangeTask(FeedRangeTaskUnit feedRangeTaskUnit) {
        List<SourceRecord> sourceRecords = new ArrayList<>();

        // each time we will only pull one page
        CosmosChangeFeedRequestOptions changeFeedRequestOptions =
            this.getChangeFeedRequestOptions(feedRangeTaskUnit);
        CosmosAsyncContainer container =
            this.cosmosClient
                .getDatabase(feedRangeTaskUnit.getDatabaseName())
                .getContainer(feedRangeTaskUnit.getContainerName());
       FeedResponse<JsonNode> feedResponse = container
            .queryChangeFeed(changeFeedRequestOptions, JsonNode.class)
            .byPage()
            .next()
            .block();

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
