// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.redis;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * Implementation of {@link CheckpointStore} that uses Azure Redis Cache, specifically Jedis.
 */
public class JedisRedisCheckpointStore implements CheckpointStore {

    JedisPool jedisPool = new JedisPool(); // Look into other constructors that need config data to initalize JedisPool

    /**
     * This method returns the list of partitions that were owned successfully.
     *
     * @param requestedPartitionOwnerships List of partition ownerships from the current instance
     * @return Null
     */
    @Override
    public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships) {
        return null;
    }

    /**
     * This method returns the list of checkpoints from the underlying data store, and if no checkpoints are available, then it returns empty results.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the current instance of Event Hub
     * @param eventHubName The Event Hub name from which checkpoint information is acquired
     * @param consumerGroup The consumer group name associated with the checkpoint
     * @return Null
     */
    @Override
    public Flux<Checkpoint> listCheckpoints(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return null;
    }

    /**
     * @param fullyQualifiedNamespace The fully qualified namespace of the current instance of Event Hub
     * @param eventHubName The Event Hub name from which checkpoint information is acquired
     * @param consumerGroup The consumer group name associated with the checkpoint
     * @return Null
     */
    @Override
    public Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return null;
    }

    /**
     * This method updates the checkpoint in the Jedis resource for a given partition.
     *
     * @param checkpoint Checkpoint information for this partition
     * @return The new eTag of the checkpoint that has been successfully added.
     */
    @Override
    public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
        //Check if the checkpoint is valid - checkpoint is invalid if it is null or if sequence number is null and offset is null
        if (!isCheckpointValid(checkpoint)) {
            //TO DO 1: Throw exception
            System.out.println("Checkpoint is not valid.");
        }

        //TO DO 2: Get access to the Jedis resource

        //Getting access to the Partition ID for the current checkpoint
        String partitionID = checkpoint.getPartitionId();
        String eventHubName = checkpoint.getEventHubName();
        String fullyQualifiedNamespace = checkpoint.getFullyQualifiedNamespace();
        String consumerGroup = checkpoint.getConsumerGroup();

        //TO DO 3: Use the above information to create a format to store each checkpoint and its associated metadata

        //TO DO 4: Add the above metadata to the Jedis Resource

        return null;
    }

    private Boolean isCheckpointValid(Checkpoint checkpoint) {
        return !(checkpoint == null || (checkpoint.getOffset() == null && checkpoint.getSequenceNumber() == null));
    }
}
