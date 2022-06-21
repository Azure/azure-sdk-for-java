// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Implementation of {@link CheckpointStore} that uses Azure Redis Cache, specifically Jedis.
 */
public class JedisRedisCheckpointStore implements CheckpointStore {
    private static final ClientLogger LOGGER = new ClientLogger(JedisRedisCheckpointStore.class);
    private final JedisPool jedisPool;
    private final JacksonAdapter jacksonAdapter = new JacksonAdapter();
    JedisRedisCheckpointStore(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
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
        String prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup);
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> members = jedis.smembers(prefix);
            ArrayList<Checkpoint> list = new ArrayList<>();
            for (String member : members) {
                //get the associated JSON representation for each for the members
                String checkpointJSON = jedis.hmget(member, "checkpoint").get(0);
                //convert JSON representation into Checkpoint
                try {
                    Checkpoint checkpoint = jacksonAdapter.deserialize(checkpointJSON, Checkpoint.class, SerializerEncoding.JSON);
                    list.add(checkpoint);
                }
                catch (IOException e) {
                    throw LOGGER.logExceptionAsError(Exceptions
                        .propagate(e)); }
            }
            jedisPool.returnResource(jedis);
            return Flux.fromStream(list.stream());
        }

    }
    /**
     * @param fullyQualifiedNamespace The fully qualified namespace of the current instance of Event Hub
     * @param eventHubName The Event Hub name from which checkpoint information is acquired
     * @param consumerGroup The consumer group name associated with the checkpoint
     * @return Null
     */
    @Override
    public Flux<PartitionOwnership> listOwnership(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        String prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup);
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> members = jedis.smembers(prefix);
            ArrayList<PartitionOwnership> list = new ArrayList<>();
            for (String member : members) {
                //get the associated JSON representation for each for the members
                String partitionOwnershipJSON = jedis.hmget(member, "partitionOwnership").get(0);
                //convert JSON representation into PartitionOwnership
                try {
                    PartitionOwnership partitionOwnership = jacksonAdapter.deserialize(partitionOwnershipJSON, PartitionOwnership.class, SerializerEncoding.JSON);
                    list.add(partitionOwnership);
                }
                catch (IOException e) {
                    throw LOGGER.logExceptionAsError(Exceptions
                        .propagate(e)); }
            }
            jedisPool.returnResource(jedis);
            return Flux.fromStream(list.stream());
        }
    }

    /**
     * This method updates the checkpoint in the Jedis resource for a given partition.
     *
     * @param checkpoint Checkpoint information for this partition
     * @return Null
     */
    @Override
    public Mono<Void> updateCheckpoint(Checkpoint checkpoint) {
        if (!isCheckpointValid(checkpoint)) {
            throw LOGGER.logExceptionAsWarning(Exceptions
                .propagate(new IllegalStateException(
                    "Checkpoint is either null, or both the offset and the sequence number are null.")));
        }
        String prefix = prefixBuilder(checkpoint.getFullyQualifiedNamespace(), checkpoint.getEventHubName(), checkpoint.getConsumerGroup());
        String key = keyBuilder(prefix, checkpoint.getPartitionId());
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            //Case 1: Checkpoint & it's prefix is not in the Redis Cache
            //Add the key to redis as a (prefix, key)
            //Case 2: Checkpoint is not in Redis Cache, but prefix is
            //Add the key as a member to the prefix group

            //Case 3: Checkpoint already exists, but had not been modified by another client
            //Case 4: Checkpoint already exists, has been modified by another client
            jedisPool.returnResource(jedis);
        }
        return null;
    }

    private String prefixBuilder(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return fullyQualifiedNamespace + "/" + eventHubName + "/" + consumerGroup;
    }
    private String keyBuilder(String prefix, String partitionId) {
        return prefix + "/" + partitionId;
    }
    private Boolean isCheckpointValid(Checkpoint checkpoint) {
        return !(checkpoint == null || (checkpoint.getOffset() == null && checkpoint.getSequenceNumber() == null));
    }
}
