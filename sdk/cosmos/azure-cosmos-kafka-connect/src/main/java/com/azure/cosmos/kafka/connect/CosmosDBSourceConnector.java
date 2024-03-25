// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.query.CompositeContinuationToken;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.CosmosConstants;
import com.azure.cosmos.kafka.connect.implementation.CosmosExceptionsHelper;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceOffsetStorageReader;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceTask;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceTaskConfig;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeContinuationTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangeTaskUnit;
import com.azure.cosmos.kafka.connect.implementation.source.FeedRangesMetadataTopicOffset;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataMonitorThread;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataTaskUnit;
import com.azure.cosmos.models.CosmosContainerProperties;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/***
 * The CosmosDb source connector.
 */
public class CosmosDBSourceConnector extends SourceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDBSourceConnector.class);
    private CosmosSourceConfig config;
    private CosmosAsyncClient cosmosClient;
    private MetadataMonitorThread monitorThread;
    private CosmosSourceOffsetStorageReader offsetStorageReader;

    @Override
    public void start(Map<String, String> props) {
        LOGGER.info("Starting the kafka cosmos source connector");
        this.config = new CosmosSourceConfig(props);
        this.cosmosClient = CosmosClientStore.getCosmosClient(this.config.getAccountConfig());
        this.offsetStorageReader = new CosmosSourceOffsetStorageReader(this.context().offsetStorageReader());
        this.monitorThread = new MetadataMonitorThread(
            this.config.getContainersConfig(),
            this.config.getMetadataConfig(),
            this.context(),
            this.offsetStorageReader,
            this.cosmosClient
        );

        this.monitorThread.start();
    }

    @Override
    public Class<? extends Task> taskClass() {
        return CosmosSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        // For now, we start with copying data by feed range
        // but in the future, we can have more optimization based on the data size etc.
        return this.getTaskConfigs(maxTasks);
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Kafka CosmosDB source connector");
        if (this.cosmosClient != null) {
            LOGGER.debug("Closing cosmos client");
            this.cosmosClient.close();
        }

        if (this.monitorThread != null) {
            LOGGER.debug("Closing monitoring thread");
            this.monitorThread.close();
        }
    }

    @Override
    public ConfigDef config() {
        return CosmosSourceConfig.getConfigDef();
    }

    @Override
    public String version() {
        return CosmosConstants.CURRENT_VERSION;
    } // TODO[public preview]: how this is being used

    private List<Map<String, String>> getTaskConfigs(int maxTasks) {
        Pair<MetadataTaskUnit, List<FeedRangeTaskUnit>> taskUnits = this.getAllTaskUnits();

        // The metadataTaskUnit is a one time only task when the connector starts/restarts,
        // so there is no need to assign a dedicated task thread for it
        // we are just going to assign it to one of the tasks which processing feedRanges tasks
        List<List<FeedRangeTaskUnit>> partitionedTaskUnits = new ArrayList<>();
        if (taskUnits.getRight().size() <= maxTasks) {
            partitionedTaskUnits.addAll(
                taskUnits.getRight().stream().map(taskUnit -> Arrays.asList(taskUnit)).collect(Collectors.toList()));
        } else {
            // using round-robin fashion to assign tasks to each buckets
            for (int i = 0; i < maxTasks; i++) {
                partitionedTaskUnits.add(new ArrayList<>());
            }

            for (int i = 0; i < taskUnits.getRight().size(); i++) {
                partitionedTaskUnits.get(i % maxTasks).add(taskUnits.getRight().get(i));
            }
        }

        List<Map<String, String>> allSourceTaskConfigs = new ArrayList<>();
        partitionedTaskUnits.forEach(feedRangeTaskUnits -> {
            Map<String, String> taskConfigs = this.config.originalsStrings();
            taskConfigs.putAll(
                CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(feedRangeTaskUnits));
            allSourceTaskConfigs.add(taskConfigs);
        });

        // assign the metadata task to the last of the task config as it has least number of feedRange task units
        allSourceTaskConfigs
            .get(allSourceTaskConfigs.size() - 1)
            .putAll(CosmosSourceTaskConfig.getMetadataTaskUnitConfigMap(taskUnits.getLeft()));

        return allSourceTaskConfigs;
    }

    private Pair<MetadataTaskUnit, List<FeedRangeTaskUnit>> getAllTaskUnits() {
        List<CosmosContainerProperties> allContainers = this.monitorThread.getAllContainers().block();
        Map<String, String> containerTopicMap = this.getContainersTopicMap(allContainers);
        List<FeedRangeTaskUnit> allFeedRangeTaskUnits = new ArrayList<>();
        Map<String, List<Range<String>>> updatedContainerToFeedRangesMap = new ConcurrentHashMap<>();

        for (CosmosContainerProperties containerProperties : allContainers) {
            Map<Range<String>, String> effectiveFeedRangesContinuationMap =
                this.getEffectiveFeedRangesContinuationMap(
                    this.config.getContainersConfig().getDatabaseName(),
                    containerProperties);

            updatedContainerToFeedRangesMap.put(
                containerProperties.getResourceId(),
                effectiveFeedRangesContinuationMap.keySet().stream().collect(Collectors.toList())
            );

            // add feedRange task unit
            for (Range<String> effectiveFeedRange : effectiveFeedRangesContinuationMap.keySet()) {
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
                this.config.getContainersConfig().getDatabaseName(),
                allContainers.stream().map(CosmosContainerProperties::getResourceId).collect(Collectors.toList()),
                updatedContainerToFeedRangesMap,
                this.config.getMetadataConfig().getMetadataTopicName());

        return Pair.of(metadataTaskUnit, allFeedRangeTaskUnits);
    }

    private Map<Range<String>, String> getEffectiveFeedRangesContinuationMap(
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

        List<Range<String>> containerFeedRanges = this.getFeedRanges(containerProperties);
        containerFeedRanges.sort(new Comparator<Range<String>>() {
            @Override
            public int compare(Range<String> o1, Range<String> o2) {
                return o1.getMin().compareTo(o2.getMin());
            }
        });

        FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
            this.offsetStorageReader.getFeedRangesMetadataOffset(databaseName, containerProperties.getResourceId());

        Map<Range<String>, String> effectiveFeedRangesContinuationMap = new LinkedHashMap<>();

        for (Range<String> containerFeedRange : containerFeedRanges) {
            if (feedRangesMetadataTopicOffset == null) {
                // there is no existing offset, return the current container feedRanges with continuationState null (start from refresh)
                effectiveFeedRangesContinuationMap.put(containerFeedRange, Strings.Emtpy);
            } else {
                // there is existing offsets, need to find out effective feedRanges based on the offset
                effectiveFeedRangesContinuationMap.putAll(
                    this.getEffectiveContinuationMapForSingleFeedRange(
                        databaseName,
                        containerProperties.getResourceId(),
                        containerFeedRange,
                        feedRangesMetadataTopicOffset.getFeedRanges())
                );
            }
        }

        return effectiveFeedRangesContinuationMap;
    }

    private Map<Range<String>, String> getEffectiveContinuationMapForSingleFeedRange(
        String databaseName,
        String containerRid,
        Range<String> containerFeedRange,
        List<Range<String>> rangesFromMetadataTopicOffset) {

        //first try to find out whether there is exact feedRange matching
        FeedRangeContinuationTopicOffset feedRangeContinuationTopicOffset =
            this.offsetStorageReader.getFeedRangeContinuationOffset(databaseName, containerRid, containerFeedRange);

        Map<Range<String>, String> effectiveContinuationMap = new LinkedHashMap<>();
        if (feedRangeContinuationTopicOffset != null) {
            // we can find the continuation offset based on exact feedRange matching
            effectiveContinuationMap.put(
                containerFeedRange,
                this.getContinuationStateFromOffset(
                    containerRid,
                    feedRangeContinuationTopicOffset,
                    containerFeedRange));

            return effectiveContinuationMap;
        }

        // we can not find the continuation offset based on the exact feed range matching
        // it means the previous Partition key range could have gone due to container split/merge
        // need to find out overlapped feedRanges from offset
        List<Range<String>> overlappedFeedRangesFromOffset =
            rangesFromMetadataTopicOffset
                .stream()
                .filter(rangeFromOffset -> Range.checkOverlapping(rangeFromOffset, containerFeedRange))
                .collect(Collectors.toList());

        if (overlappedFeedRangesFromOffset.size() == 1) {
            // split - use the current containerFeedRange, but construct the continuationState based on the feedRange from offset
            effectiveContinuationMap.put(
                containerFeedRange,
                this.getContinuationStateFromOffset(
                    containerRid,
                    this.offsetStorageReader.getFeedRangeContinuationOffset(databaseName, containerRid, overlappedFeedRangesFromOffset.get(0)),
                    containerFeedRange));
            return effectiveContinuationMap;
        }

        if (overlappedFeedRangesFromOffset.size() > 1) {
            // merge - use the feed ranges from the offset
            for (Range<String> overlappedRangeFromOffset : overlappedFeedRangesFromOffset) {
                effectiveContinuationMap.put(
                    overlappedRangeFromOffset,
                    this.getContinuationStateFromOffset(
                        containerRid,
                        this.offsetStorageReader.getFeedRangeContinuationOffset(databaseName, containerRid, overlappedRangeFromOffset),
                        overlappedRangeFromOffset));
            }

            return effectiveContinuationMap;
        }

        // Can not find overlapped ranges from offset, this should never happen, fail
        LOGGER.error("Can not find overlapped ranges for feedRange {}", containerFeedRange);
        throw new IllegalStateException("Can not find overlapped ranges for feedRange " + containerFeedRange);
    }

    private String getContinuationStateFromOffset(
        String containerRid,
        FeedRangeContinuationTopicOffset feedRangeContinuationTopicOffset,
        Range<String> range) {

        ChangeFeedState stateFromOffset = ChangeFeedStateV1.fromString(feedRangeContinuationTopicOffset.getContinuationState());
        String itemLsn = feedRangeContinuationTopicOffset.getItemLsn();
        return new ChangeFeedStateV1(
            containerRid,
            new FeedRangeEpkImpl(range),
            stateFromOffset.getMode(),
            stateFromOffset.getStartFromSettings(),
            FeedRangeContinuation.create(
                containerRid,
                new FeedRangeEpkImpl(range),
                Arrays.asList(new CompositeContinuationToken(itemLsn, range)))).toString();
    }

    private List<Range<String>> getFeedRanges(CosmosContainerProperties containerProperties) {
        return this.cosmosClient
            .getDatabase(this.config.getContainersConfig().getDatabaseName())
            .getContainer(containerProperties.getId())
            .getFeedRanges()
            .onErrorMap(throwable ->
                CosmosExceptionsHelper.convertToConnectException(
                    throwable,
                    "GetFeedRanges failed for container " + containerProperties.getId()))
            .block()
            .stream()
            .map(feedRange -> FeedRangeInternal.normalizeRange(((FeedRangeEpkImpl) feedRange).getRange()))
            .collect(Collectors.toList());
    }

    private Map<String, String> getContainersTopicMap(List<CosmosContainerProperties> allContainers) {
        Map<String, String> topicMapFromConfig =
            this.config.getContainersConfig().getContainersTopicMap()
                .stream()
                .map(containerTopicMapString -> containerTopicMapString.split("#"))
                .collect(
                    Collectors.toMap(
                        containerTopicMapArray -> containerTopicMapArray[1],
                        containerTopicMapArray -> containerTopicMapArray[0]));

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
}
