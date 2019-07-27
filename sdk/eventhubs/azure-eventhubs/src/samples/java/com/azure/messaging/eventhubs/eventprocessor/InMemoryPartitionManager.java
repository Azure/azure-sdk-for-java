// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor;

import com.azure.messaging.eventhubs.PartitionManager;
import com.azure.messaging.eventhubs.eventprocessor.models.Checkpoint;
import com.azure.messaging.eventhubs.eventprocessor.models.PartitionOwnership;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A simple in-memory implementation of a {@link PartitionManager}.
 */
public class InMemoryPartitionManager implements PartitionManager {

    private final Map<String, PartitionOwnership> partitionOwnershipMap = new ConcurrentHashMap<>();

    @Override
    public Flux<PartitionOwnership> listOwnership(String eventHubName, String consumerGroupName) {
        return Flux.fromIterable(partitionOwnershipMap.values());
    }

    @Override
    public Flux<PartitionOwnership> claimOwnership(
        List<PartitionOwnership> requestedPartitionOwnerships) {
        return Flux.fromIterable(requestedPartitionOwnerships)
            .filter(partitionOwnership -> !partitionOwnershipMap
                .containsKey(partitionOwnership.partitionId()))
            .map(partitionOwnership -> {
                partitionOwnership.eTag(UUID.randomUUID().toString()); // set new etag
                partitionOwnershipMap.put(partitionOwnership.partitionId(), partitionOwnership);
                return partitionOwnership;
            });
    }

    @Override
    public Mono<String> updateCheckpoint(Checkpoint checkpoint) {
        partitionOwnershipMap.get(checkpoint.partitionId())
            .sequenceNumber(checkpoint.sequenceNumber());
        partitionOwnershipMap.get(checkpoint.partitionId())
            .offset(checkpoint.offset());
        String updatedeTag = UUID.randomUUID().toString();
        partitionOwnershipMap.get(checkpoint.partitionId()).eTag(updatedeTag);
        return Mono.just(updatedeTag);
    }
}
