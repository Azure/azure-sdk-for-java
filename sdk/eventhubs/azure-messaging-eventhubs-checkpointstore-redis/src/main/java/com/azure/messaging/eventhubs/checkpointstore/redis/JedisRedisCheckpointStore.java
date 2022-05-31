// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.redis;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * Implementation of {@link CheckpointStore} that uses Azure Redis Cache, specifically Jedis.
 */
public class JedisRedisCheckpointStore implements CheckpointStore {

    private final JedisPool jedisPool = new JedisPool(); // Look into other constructors that need config data to initalize JedisPool
    private static final ClientLogger LOGGER = new ClientLogger(JedisRedisCheckpointStore.class);
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
     * @return Null
     */
    @Override
    public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
        //Check if the checkpoint is valid - checkpoint is invalid if it is null or if sequence number is null and offset is null
        if (!isCheckpointValid(checkpoint)) {
            //TO DO 1: Throw exception
            throw LOGGER.logExceptionAsWarning(Exceptions.propagate(new IllegalStateException("Checkpoint is either null, or both the offset and the sequence number are null.")));
        }

        //TO DO 2: Get access to the Jedis resource
        Jedis jedis = jedisPool.getResource();
        String key = keyBuilder(checkpoint);
        //TO DO 3: Use the above information to create a format to store each checkpoint and its associated metadata

        //TO DO 4: Add the above metadata to the Jedis Resource
        jedisPool.returnResource(jedis);
        return null;
    }
    private String keyBuilder(Checkpoint checkpoint) {
        return checkpoint.getFullyQualifiedNamespace() + checkpoint.getEventHubName() + checkpoint.getConsumerGroup() + checkpoint.getPartitionId();
    }
    private Boolean isCheckpointValid(Checkpoint checkpoint) {
        return !(checkpoint == null || (checkpoint.getOffset() == null && checkpoint.getSequenceNumber() == null));
    }
}
