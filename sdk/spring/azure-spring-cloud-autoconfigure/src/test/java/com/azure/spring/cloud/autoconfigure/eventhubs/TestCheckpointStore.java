// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

class TestCheckpointStore implements CheckpointStore {

    @Override
    public Flux<PartitionOwnership> listOwnership(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> list) {
        return null;
    }

    @Override
    public Flux<Checkpoint> listCheckpoints(String s, String s1, String s2) {
        return null;
    }

    @Override
    public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
        return null;
    }
}
