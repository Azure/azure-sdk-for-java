// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.FeedRange;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import reactor.core.publisher.Mono;

import java.util.Map;

public class MetadataKafkaStorageManager implements IMetadataReader {
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

        return Mono.just(new Utils.ValueHolder<>(FeedRangesMetadataTopicOffset.fromMap(topicOffsetMap)));
    }

    public Mono<Utils.ValueHolder<ContainersMetadataTopicOffset>> getContainersMetadataOffset(String databaseName, String connectorName) {
        Map<String, Object> topicOffsetMap =
            this.offsetStorageReader
                .offset(
                    ContainersMetadataTopicPartition.toMap(
                        new ContainersMetadataTopicPartition(databaseName, connectorName)));

        return Mono.just(new Utils.ValueHolder<>(ContainersMetadataTopicOffset.fromMap(topicOffsetMap)));
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
        return offsetStorageReader;
    }
}
