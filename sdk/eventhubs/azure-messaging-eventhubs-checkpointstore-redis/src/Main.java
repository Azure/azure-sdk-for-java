package com.azure.messaging.eventhubs.checkpointstore.redis;

import com.azure.messaging.eventhubs.CheckpointStore;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
Added this file so that an execuatable jar is produced for the build.
 */

/**
 * Implementation of CheckpointStore that uses Azure Redis Cache.
 */
public class RedisCheckpointStore implements  CheckpointStore{
    public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships){
        return null;
    }

    public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName, String consumerGroup){
        return null;
    }

    public Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName, String consumerGroup){
        return null;
    }

    public Mono<Void> updateCheckpoint(Checkpoint checkpoint){
        return null;
    }
}
