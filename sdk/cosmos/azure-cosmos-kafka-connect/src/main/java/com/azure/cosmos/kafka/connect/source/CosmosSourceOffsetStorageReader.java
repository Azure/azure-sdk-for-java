// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source;

import com.azure.cosmos.implementation.routing.Range;
import org.apache.kafka.connect.storage.OffsetStorageReader;

import java.util.Map;

public class CosmosSourceOffsetStorageReader {
    private final OffsetStorageReader offsetStorageReader;

    public CosmosSourceOffsetStorageReader(OffsetStorageReader offsetStorageReader) {
        this.offsetStorageReader = offsetStorageReader;
    }

    public FeedRangesMetadataTopicOffset getFeedRangesMetadataOffset(String databaseName, String containerRid) {
        Map<String, Object> topicOffsetMap = this.offsetStorageReader.offset(
            new FeedRangesMetadataTopicPartition(databaseName, containerRid).getTopicPartitionMap());

        return topicOffsetMap == null ? null : new FeedRangesMetadataTopicOffset(topicOffsetMap);
    }

    public ContainersMetadataTopicOffset getContainersMetadataOffset(String databaseName) {
        Map<String, Object> topicOffsetMap = this.offsetStorageReader.offset(
            new ContainersMetadataTopicPartition(databaseName).getTopicPartitionMap());

        return topicOffsetMap == null ? null : new ContainersMetadataTopicOffset(topicOffsetMap);
    }

    public FeedRangeContinuationTopicOffset getFeedRangeContinuationOffset(
        String databaseName,
        String collectionRid,
        Range<String> feedRange) {

        Map<String, Object> topicOffset =
            this.offsetStorageReader.offset(
                new FeedRangeContinuationTopicPartition(databaseName, collectionRid, feedRange).getTopicPartitionMap());

        return topicOffset == null ? null : new FeedRangeContinuationTopicOffset(topicOffset);
    }
}
