// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosExceptionsHelper;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class MetadataCosmosStorageManager implements IMetadataReader {
    private final CosmosAsyncContainer metadataContainer;

    public MetadataCosmosStorageManager(CosmosAsyncContainer metadataContainer) {
        checkNotNull(metadataContainer, "Argument 'metadataContainer' can not be null");
        this.metadataContainer = metadataContainer;
    }

    public void createMetadataItems(MetadataTaskUnit metadataTaskUnit) {
        // To be as consistent as what will be persisted to kafka topic if kafka being the storage type
        // we created the cosmos metadata item from the kafka topic records

        // add the containers metadata items - it tracks the databaseName -> List[containerRid] mapping
        this.createContainersMetadataItem(
            metadataTaskUnit.getContainersMetadata().getLeft(),
            metadataTaskUnit.getContainersMetadata().getRight());

        // add the container feedRanges metadata item - it tracks the containerRid -> List[FeedRange] mapping
        for (Pair<FeedRangesMetadataTopicPartition, FeedRangesMetadataTopicOffset> feedRangesMetadata : metadataTaskUnit.getFeedRangesMetadataList()) {
            this.createFeedRangesMetadataItem(feedRangesMetadata.getLeft(), feedRangesMetadata.getRight());
        }
    }

    public void createContainersMetadataItem(
        ContainersMetadataTopicPartition topicPartition,
        ContainersMetadataTopicOffset topicOffset) {

        ContainersMetadataItem containersMetadataItem =
            new ContainersMetadataItem(
                this.getContainersMetadataItemId(topicPartition.getDatabaseName(), topicPartition.getConnectorName()),
                ContainersMetadataTopicOffset.toMap(topicOffset));

        this.metadataContainer
            .upsertItem(containersMetadataItem)
            .onErrorMap(throwable ->
                KafkaCosmosExceptionsHelper.convertToConnectException(
                    throwable,
                    "createContainersMetadataItem failed for database " + topicPartition.getDatabaseName()))
            .block();
    }

    public void createFeedRangesMetadataItem(
        FeedRangesMetadataTopicPartition topicPartition,
        FeedRangesMetadataTopicOffset topicOffset) {

        String itemId = getFeedRangesMetadataItemId(
            topicPartition.getDatabaseName(),
            topicPartition.getContainerRid(),
            topicPartition.getConnectorName());
        FeedRangesMetadataItem feedRangesMetadataItem =
            new FeedRangesMetadataItem(itemId, FeedRangesMetadataTopicOffset.toMap(topicOffset));

        this.metadataContainer
            .upsertItem(feedRangesMetadataItem)
            .onErrorMap(throwable ->
                KafkaCosmosExceptionsHelper.convertToConnectException(
                    throwable,
                    String.format(
                        "createFeedRangesMetadataItem failed for database %s, containerRid %s, connector %s",
                        topicPartition.getDatabaseName(),
                        topicPartition.getContainerRid(),
                        topicPartition.getConnectorName())
                    ))
            .block();
    }

    private String getContainersMetadataItemId(String databaseName, String connectorName) {
        return databaseName + "_" + connectorName;
    }
    private String getFeedRangesMetadataItemId(String databaseName, String collectionRid, String connectorName) {
        return databaseName + "_" + collectionRid + "_" + connectorName;
    }

    @Override
    public Mono<Utils.ValueHolder<ContainersMetadataTopicOffset>> getContainersMetadataOffset(String databaseName, String connectorName) {
        String itemId = this.getContainersMetadataItemId(databaseName, connectorName);
        return this.metadataContainer
            .readItem(itemId, new PartitionKey(itemId), ContainersMetadataItem.class)
            .map(itemResponse -> new Utils.ValueHolder<>(ContainersMetadataTopicOffset.fromMap(itemResponse.getItem().getMetadata())))
            .onErrorResume(throwable -> {
                if (KafkaCosmosExceptionsHelper.isNotFoundException(throwable)) {
                    return Mono.just(new Utils.ValueHolder<>(null));
                }

                return Mono.error(
                    KafkaCosmosExceptionsHelper.convertToConnectException(
                        throwable,
                        "getContainersMetadataOffset failed for database " + databaseName));
            });
    }

    @Override
    public Mono<Utils.ValueHolder<FeedRangesMetadataTopicOffset>> getFeedRangesMetadataOffset(
        String databaseName,
        String collectionRid,
        String connectorName) {
        String itemId = this.getFeedRangesMetadataItemId(databaseName, collectionRid, connectorName);
        return this.metadataContainer
            .readItem(itemId, new PartitionKey(itemId), FeedRangesMetadataItem.class)
            .map(itemResponse -> new Utils.ValueHolder<>(FeedRangesMetadataTopicOffset.fromMap(itemResponse.getItem().getMetadata())))
            .onErrorResume(throwable -> {
                if (KafkaCosmosExceptionsHelper.isNotFoundException(throwable)) {
                    return Mono.just(new Utils.ValueHolder<>(null));
                }

                return Mono.error(
                    KafkaCosmosExceptionsHelper.convertToConnectException(
                        throwable,
                        "getFeedRangesMetadataOffset failed for database " + databaseName + ", containerRid: " + collectionRid)
                );
            });
    }

    private static class ContainersMetadataItem {
        @JsonProperty("id")
        private String databaseName;
        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        ContainersMetadataItem() {}

        ContainersMetadataItem(String databaseName, Map<String, Object> metadata) {
            this.databaseName = databaseName;
            this.metadata = metadata;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    private static class FeedRangesMetadataItem {
        @JsonProperty("id")
        private String databaseAndContainerRid;
        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        FeedRangesMetadataItem() {}

        FeedRangesMetadataItem(String databaseAndContainerRid, Map<String, Object> metadata) {
            this.databaseAndContainerRid = databaseAndContainerRid;
            this.metadata = metadata;
        }

        public String getDatabaseAndContainerRid() {
            return databaseAndContainerRid;
        }

        public void setDatabaseAndContainerRid(String databaseAndContainerRid) {
            this.databaseAndContainerRid = databaseAndContainerRid;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }
}
