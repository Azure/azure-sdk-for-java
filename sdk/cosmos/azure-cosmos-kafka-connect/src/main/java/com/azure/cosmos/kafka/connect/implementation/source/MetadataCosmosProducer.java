package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class MetadataCosmosProducer {
    private final CosmosAsyncClient client;

    public MetadataCosmosProducer(CosmosAsyncClient client) {
        this.client = client;
    }

    public CosmosAsyncClient getClient() {
        return client;
    }

    public void executeMetadataTask(MetadataTaskUnit metadataTaskUnit) {
        CosmosAsyncContainer container =
            this.client
                .getDatabase(metadataTaskUnit.getDatabaseName())
                .getContainer(metadataTaskUnit.getStorageName());

        // To be as consistent as what will be persisted to kafka topic if kafka being the storage type
        // we created the cosmos metadata item from the kafka topic records

        // add the containers metadata record - it tracks the databaseName -> List[containerRid] mapping
        this.createContainersMetadataItem(
            container,
            metadataTaskUnit.getContainersMetadata().getLeft(),
            metadataTaskUnit.getContainersMetadata().getRight());

        // add the container feedRanges metadata record - it tracks the containerRid -> List[FeedRange] mapping
        for (Pair<FeedRangesMetadataTopicPartition, FeedRangesMetadataTopicOffset> feedRangesMetadata : metadataTaskUnit.getFeedRangesMetadataList()) {
            this.createFeedRangesMetadataItem(container, feedRangesMetadata.getLeft(), feedRangesMetadata.getRight());
        }
    }

    private void createContainersMetadataItem(
        CosmosAsyncContainer container,
        ContainersMetadataTopicPartition topicPartition,
        ContainersMetadataTopicOffset topicOffset) {

        ContainersMetadataItem containersMetadataItem =
            new ContainersMetadataItem(
                topicPartition.getDatabaseName(),
                ContainersMetadataTopicOffset.toMap(topicOffset));

        container.upsertItem(containersMetadataItem).block();
    }

    private void createFeedRangesMetadataItem(
        CosmosAsyncContainer container,
        FeedRangesMetadataTopicPartition topicPartition,
        FeedRangesMetadataTopicOffset topicOffset) {

        String recordId = topicPartition.getDatabaseName() + "_" + topicPartition.getContainerRid();
        FeedRangesMetadataItem feedRangesMetadataItem =
            new FeedRangesMetadataItem(recordId, FeedRangesMetadataTopicOffset.toMap(topicOffset));

        container.upsertItem(feedRangesMetadataItem).block();
    }

    private static class ContainersMetadataItem {
        @JsonProperty("id")
        private String databaseName;
        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        public ContainersMetadataItem() {}

        public ContainersMetadataItem(String databaseName, Map<String, Object> metadata) {
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

        private FeedRangesMetadataItem() {}

        public FeedRangesMetadataItem(String databaseAndContainerRid, Map<String, Object> metadata) {
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
