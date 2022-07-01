// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Jedis;

/**
 * Implementation of {@link CheckpointStore} that uses Azure Redis Cache, specifically Jedis.
 */
public class JedisRedisCheckpointStore implements CheckpointStore {
    private static final ClientLogger LOGGER = new ClientLogger(JedisRedisCheckpointStore.class);
    private static final JsonSerializer DEFAULT_SERIALIZER = JsonSerializerProviders.createInstance(true);
    static final String CHECKPOINT = "checkpoint";
    static final String  PARTITION_OWNERSHIP = "partitionOwnership";
    private final JedisPool jedisPool;
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
        return Flux.fromIterable(new ArrayList<>());
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
            ArrayList<Checkpoint> listStoredCheckpoints = new ArrayList<>();
            Set<String> members = jedis.smembers(prefix);
            if (members.isEmpty()) {
                jedisPool.returnResource(jedis);
                return Flux.fromIterable(listStoredCheckpoints);
            }
            for (String member : members) {
                //get the associated JSON representation for each for the members
                List<String> checkpointJsonList = jedis.hmget(member, CHECKPOINT);
                if (checkpointJsonList == null) {
                    jedisPool.returnResource(jedis);
                    return Flux.error(new IllegalStateException("No checkpoints persist in Redis for the given parameters."));
                }
                if (checkpointJsonList.isEmpty()) {
                    jedisPool.returnResource(jedis);
                    return Flux.error(new IllegalStateException("No checkpoints persist in Redis for the given parameters."));
                } else {
                    String checkpointJson = checkpointJsonList.get(0);
                    Checkpoint checkpoint = DEFAULT_SERIALIZER.deserializeFromBytes(checkpointJson.getBytes(StandardCharsets.UTF_8), TypeReference.createInstance(Checkpoint.class));
                    listStoredCheckpoints.add(checkpoint);
                }
            }
            jedisPool.returnResource(jedis);
            return Flux.fromIterable(listStoredCheckpoints);
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
            ArrayList<PartitionOwnership> listStoredOwnerships = new ArrayList<>();
            if (members.isEmpty()) {
                jedisPool.returnResource(jedis);
                return Flux.fromIterable(listStoredOwnerships);
            }
            for (String member : members) {
                //get the associated JSON representation for each for the members
                List<String> partitionOwnershipJsonList = jedis.hmget(member, PARTITION_OWNERSHIP);

                // if PARTITION_OWNERSHIP field does not exist for member we will get a null
                if (partitionOwnershipJsonList == null) {
                    continue;
                }

                // if PARTITION_OWNERSHIP field exists but has no records than the list will be empty
                if (!partitionOwnershipJsonList.isEmpty()) {
                    String partitionOwnershipJson = partitionOwnershipJsonList.get(0);
                    PartitionOwnership partitionOwnership = DEFAULT_SERIALIZER.deserializeFromBytes(partitionOwnershipJson.getBytes(StandardCharsets.UTF_8), TypeReference.createInstance(PartitionOwnership.class));
                    listStoredOwnerships.add(partitionOwnership);
                }
            }
            jedisPool.returnResource(jedis);
            return Flux.fromIterable(listStoredOwnerships);
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
            //Case 1: new checkpoint
            if (!jedis.exists(prefix) || !jedis.exists(key)) {
                jedis.sadd(prefix, key);
                jedis.hset(key, CHECKPOINT, new String(DEFAULT_SERIALIZER.serializeToBytes(checkpoint), StandardCharsets.UTF_8));
            }
            //TO DO: Case 2: checkpoint already exists in Redis cache
            jedisPool.returnResource(jedis);
        }
        return Mono.empty();
    }

    static String prefixBuilder(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return fullyQualifiedNamespace + "/" + eventHubName + "/" + consumerGroup;
    }

    static String keyBuilder(String prefix, String partitionId) {
        return prefix + "/" + partitionId;
    }

    private static Boolean isCheckpointValid(Checkpoint checkpoint) {
        return !(checkpoint == null || (checkpoint.getOffset() == null && checkpoint.getSequenceNumber() == null));
    }

}
