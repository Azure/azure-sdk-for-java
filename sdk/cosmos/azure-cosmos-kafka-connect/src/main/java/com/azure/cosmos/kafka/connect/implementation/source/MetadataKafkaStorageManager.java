// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.FeedRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.connect.errors.ConnectException;

import java.util.Map;

public class MetadataKafkaStorageManager implements IMetadataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataKafkaStorageManager.class);
    private final OffsetStorageReader offsetStorageReader;

    public MetadataKafkaStorageManager(OffsetStorageReader offsetStorageReader) {
        this.offsetStorageReader = offsetStorageReader;
    }

    public static Utils.ValueHolder<FeedRangesMetadataTopicOffset> parseFeedRangesMetadata(
        String databaseName,
        String containerRid,
        Map<String, Object> topicOffsetMap) {

        // The data is stored in a Struct with our unified schema
        if (topicOffsetMap.containsKey(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME)) {
            String entityType = (String) topicOffsetMap.get(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME);
            if (MetadataEntityTypes.FEED_RANGES_METADATA_V1.equals(entityType)) {
                String jsonValue = (String) topicOffsetMap.get(UnifiedMetadataSchemaConstants.JSON_VALUE_NAME);

                if (jsonValue != null) {
                    Map<String, Object> feedRangesMap;
                    try {
                        feedRangesMap = Utils
                            .getSimpleObjectMapper()
                            .readValue(jsonValue, JsonToStruct.JACKSON_MAP_TYPE);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return new Utils.ValueHolder<>(FeedRangesMetadataTopicOffset.fromMap(feedRangesMap));
                }

                LOGGER.warn(
                    "No feed ranges found in unified schema for database: {}, containerRid: {}",
                    databaseName,
                    containerRid);
                return new Utils.ValueHolder<>(null);
            }

            throw new IllegalStateException("Unknown EntityType '" + entityType + "'");
        }

        return new Utils.ValueHolder<>(FeedRangesMetadataTopicOffset.fromMap(topicOffsetMap));
    }

    public Mono<Utils.ValueHolder<FeedRangesMetadataTopicOffset>> getFeedRangesMetadataOffset(
        String databaseName,
        String containerRid,
        String connectorName) {
        Map<String, Object> topicOffsetMap =
            this.offsetStorageReader
                .offset(
                    FeedRangesMetadataTopicPartition.toMap(
                            new FeedRangesMetadataTopicPartition(databaseName, containerRid, connectorName)));

        if (topicOffsetMap == null) {
            return Mono.just(new Utils.ValueHolder<>(null));
        }

        try {
            return Mono.just(parseFeedRangesMetadata(databaseName, containerRid, topicOffsetMap));
        } catch (Exception e) {
            LOGGER.error("Error processing feed ranges metadata from unified schema", e);
            return Mono.error(new ConnectException("Failed to process feed ranges metadata", e));
        }
    }

    public static Utils.ValueHolder<ContainersMetadataTopicOffset> parseContainersMetadata(
        String databaseName,
        Map<String, Object> topicOffsetMap) {

        // The data is stored in a Struct with our unified schema
        if (topicOffsetMap.containsKey(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME)) {
            String entityType = (String) topicOffsetMap.get(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME);
            if (MetadataEntityTypes.CONTAINERS_METADATA_V1.equals(entityType)) {
                String jsonValue = (String) topicOffsetMap.get(UnifiedMetadataSchemaConstants.JSON_VALUE_NAME);

                if (jsonValue != null) {
                    Map<String, Object> containerRidsMap;
                    try {
                        containerRidsMap = Utils
                            .getSimpleObjectMapper()
                            .readValue(jsonValue, JsonToStruct.JACKSON_MAP_TYPE);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return new Utils.ValueHolder<>(ContainersMetadataTopicOffset.fromMap(containerRidsMap));
                }

                LOGGER.warn("No container RIDs found in unified schema for database: {}", databaseName);
                return new Utils.ValueHolder<>(null);
            }

            throw new IllegalStateException("Unknown EntityType '" + entityType + "'");
        }

        return new Utils.ValueHolder<>(ContainersMetadataTopicOffset.fromMap(topicOffsetMap));
    }

    public Mono<Utils.ValueHolder<ContainersMetadataTopicOffset>> getContainersMetadataOffset(
        String databaseName,
        String connectorName) {

        Map<String, Object> topicOffsetMap =
            this.offsetStorageReader
                .offset(
                    ContainersMetadataTopicPartition.toMap(
                        new ContainersMetadataTopicPartition(databaseName, connectorName)));

        if (topicOffsetMap == null) {
            return Mono.just(new Utils.ValueHolder<>(null));
        }

        try {
            // The data is stored in a Struct with our unified schema
            return Mono.just(parseContainersMetadata(databaseName, topicOffsetMap));
        } catch (Exception e) {
            LOGGER.error("Error processing containers metadata from unified schema", e);
            return Mono.error(new ConnectException("Failed to process containers metadata", e));
        }
    }

    public FeedRangeContinuationTopicOffset getFeedRangeContinuationOffset(
        String databaseName,
        String collectionRid,
        FeedRange feedRange) {

        Map<String, Object> topicOffsetMap =
            this.offsetStorageReader
                .offset(
                    FeedRangeContinuationTopicPartition.toMap(
                        new FeedRangeContinuationTopicPartition(databaseName, collectionRid, feedRange)));

        return FeedRangeContinuationTopicOffset.fromMap(topicOffsetMap);
    }

    public OffsetStorageReader getOffsetStorageReader() {
        return this.offsetStorageReader;
    }
}
