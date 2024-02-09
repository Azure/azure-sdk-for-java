// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations.source;

import com.azure.cosmos.implementation.routing.Range;
import org.apache.kafka.connect.storage.OffsetStorageReader;

import java.util.Map;

public class CosmosSourceOffsetStorageReader {
    private final OffsetStorageReader offsetStorageReader;

    public CosmosSourceOffsetStorageReader(OffsetStorageReader offsetStorageReader) {
        this.offsetStorageReader = offsetStorageReader;
    }

    public FeedRangesMetadataTopicOffset getFeedRangesMetadataOffset(String databaseName, String containerRid) {
        Map<String, Object> topicOffsetMap =
            this.offsetStorageReader
                .offset(
                    FeedRangesMetadataTopicPartition.toMap(
                            new FeedRangesMetadataTopicPartition(databaseName, containerRid)));

        return FeedRangesMetadataTopicOffset.fromMap(topicOffsetMap);
    }

    public ContainersMetadataTopicOffset getContainersMetadataOffset(String databaseName) {
        Map<String, Object> topicOffsetMap =
            this.offsetStorageReader
                .offset(
                    ContainersMetadataTopicPartition.toMap(
                        new ContainersMetadataTopicPartition(databaseName)));

        return ContainersMetadataTopicOffset.fromMap(topicOffsetMap);
    }

    public FeedRangeContinuationTopicOffset getFeedRangeContinuationOffset(
        String databaseName,
        String collectionRid,
        Range<String> feedRange) {

        Map<String, Object> topicOffsetMap =
            this.offsetStorageReader
                .offset(
                    FeedRangeContinuationTopicPartition.toMap(
                        new FeedRangeContinuationTopicPartition(databaseName, collectionRid, feedRange)));

        return FeedRangeContinuationTopicOffset.fromMap(topicOffsetMap);
    }
}
