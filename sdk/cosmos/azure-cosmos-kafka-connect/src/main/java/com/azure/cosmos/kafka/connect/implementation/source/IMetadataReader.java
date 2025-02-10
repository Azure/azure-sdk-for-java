// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import reactor.core.publisher.Mono;

public interface IMetadataReader {
    Mono<Utils.ValueHolder<ContainersMetadataTopicOffset>> getContainersMetadataOffset(
        String databaseName,
        String connectorName);
    Mono<Utils.ValueHolder<FeedRangesMetadataTopicOffset>> getFeedRangesMetadataOffset(
        String databaseName,
        String collectionRid,
        String connectorName);
}
