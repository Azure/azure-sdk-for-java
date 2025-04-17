// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.FeedRange;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import reactor.core.publisher.Mono;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.connect.errors.ConnectException;

import java.util.Map;
import java.util.HashMap;

public class MetadataKafkaStorageManager implements IMetadataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataKafkaStorageManager.class);
    private final OffsetStorageReader offsetStorageReader;

    public MetadataKafkaStorageManager(OffsetStorageReader offsetStorageReader) {
        this.offsetStorageReader = offsetStorageReader;
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
            // The data is stored in a Struct with our unified schema
            Object value = topicOffsetMap.get("value");
            if (value instanceof Struct) {
                Struct unifiedStruct = (Struct) value;
                String entityType = unifiedStruct.getString(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME);
                if (MetadataEntityTypes.FEED_RANGES_METADATA_V1.equals(entityType)) {
                    String feedRangesJson = unifiedStruct.getString(UnifiedMetadataSchemaConstants.JSON_VALUE_NAME);
                    if (feedRangesJson != null) {
                        Map<String, Object> feedRangesMap = new HashMap<>();
                        feedRangesMap.put(FeedRangesMetadataTopicOffset.CONTAINER_FEED_RANGES_KEY, feedRangesJson);
                        return Mono.just(new Utils.ValueHolder<>(FeedRangesMetadataTopicOffset.fromMap(feedRangesMap)));
                    }

                    LOGGER.warn(
                        "No feed ranges found in unified schema for database: {}, containerRid: {}",
                        databaseName,
                        containerRid);
                    return Mono.just(new Utils.ValueHolder<>(null));
                }

                throw new IllegalStateException("Unknown EntityType '" + entityType + "'");
            }

            return Mono.just(new Utils.ValueHolder<>(FeedRangesMetadataTopicOffset.fromMap(topicOffsetMap)));
        } catch (Exception e) {
            LOGGER.error("Error processing feed ranges metadata from unified schema", e);
            return Mono.error(new ConnectException("Failed to process feed ranges metadata", e));
        }
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
            Object value = topicOffsetMap.get("value");
            if (value instanceof Struct) {
                Struct unifiedStruct = (Struct) value;
                String entityType = unifiedStruct.getString(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME);
                if (MetadataEntityTypes.CONTAINERS_METADATA_V1.equals(entityType)) {
                    String containerRidsJson = unifiedStruct.getString(UnifiedMetadataSchemaConstants.JSON_VALUE_NAME);
                    if (containerRidsJson != null) {
                        Map<String, Object> containerRidsMap = new HashMap<>();
                        containerRidsMap.put(
                            ContainersMetadataTopicOffset.CONTAINERS_RESOURCE_IDS_NAME_KEY, containerRidsJson);
                        return Mono.just(
                            new Utils.ValueHolder<>(ContainersMetadataTopicOffset.fromMap(containerRidsMap)));
                    }

                    LOGGER.warn("No container RIDs found in unified schema for database: {}", databaseName);
                    return Mono.just(new Utils.ValueHolder<>(null));
                }

                throw new IllegalStateException("Unknown EntityType '" + entityType + "'");
            }

            return Mono.just(new Utils.ValueHolder<>(ContainersMetadataTopicOffset.fromMap(topicOffsetMap)));
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
