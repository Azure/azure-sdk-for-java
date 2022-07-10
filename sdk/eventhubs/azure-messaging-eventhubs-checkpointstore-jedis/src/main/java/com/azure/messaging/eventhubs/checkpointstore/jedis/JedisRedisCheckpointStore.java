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
import redis.clients.jedis.Transaction;

/**
 * Implementation of {@link CheckpointStore} that uses Azure Redis Cache, specifically Jedis.
 */
public class JedisRedisCheckpointStore implements CheckpointStore {

    private static final ClientLogger LOGGER = new ClientLogger(JedisRedisCheckpointStore.class);
    static final JsonSerializer DEFAULT_SERIALIZER = JsonSerializerProviders.createInstance(true);
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
     * @return Flux of PartitionOwnership objects
     */
    @Override
    public Flux<PartitionOwnership> claimOwnership(List<PartitionOwnership> requestedPartitionOwnerships) {

        return Flux.fromIterable(requestedPartitionOwnerships).handle(((partitionOwnership, sink) -> {

            String partitionId = partitionOwnership.getPartitionId();
            String key = keyBuilder(prefixBuilder(partitionOwnership.getFullyQualifiedNamespace(), partitionOwnership.getEventHubName(), partitionOwnership.getConsumerGroup()), partitionId);

            try (Jedis jedis = jedisPool.getResource()) {
                List<String> keyInformation = jedis.hmget(key, PARTITION_OWNERSHIP);
                String currentPartitionOwnership = keyInformation.get(0);

                if (currentPartitionOwnership == null) {
                    // if PARTITION_OWNERSHIP field does not exist for member we will get a null, and we must add the field
                    jedis.hset(key, PARTITION_OWNERSHIP, new String(DEFAULT_SERIALIZER.serializeToBytes(partitionOwnership), StandardCharsets.UTF_8));
                } else {
                    // otherwise we have to change the ownership and "watch" the transaction
                    jedis.watch(key);

                    Transaction transaction = jedis.multi();
                    transaction.hset(key, PARTITION_OWNERSHIP, new String(DEFAULT_SERIALIZER.serializeToBytes(partitionOwnership), StandardCharsets.UTF_8));
                    List<Object> executionResponse = transaction.exec();

                    if (executionResponse == null) {
                        //This means that the transaction did not execute, which implies that another client has changed the ownership during this transaction
                        sink.error(new RuntimeException());
                    }
                }
                jedisPool.returnResource(jedis);
            }
            sink.next(partitionOwnership);
        }));
    }

    /**
     * This method returns the list of checkpoints from the underlying data store, and if no checkpoints are available, then it returns empty results.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the current instance  Event Hub
     * @param eventHubName The Event Hub name from which checkpoint information is acquired
     * @param consumerGroup The consumer group name associated with the checkpoint
     * @return Flux of Checkpoint objects
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

                if (!checkpointJsonList.isEmpty()) {
                    String checkpointJson = checkpointJsonList.get(0);

                    if (checkpointJson == null) {
                        LOGGER.verbose("No checkpoint persists yet.");
                        continue;
                    }
                    Checkpoint checkpoint = DEFAULT_SERIALIZER.deserializeFromBytes(checkpointJson.getBytes(StandardCharsets.UTF_8), TypeReference.createInstance(Checkpoint.class));
                    listStoredCheckpoints.add(checkpoint);
                } else {
                    LOGGER.verbose("No checkpoint persists yet.");
                }
            }
            jedisPool.returnResource(jedis);
            return Flux.fromIterable(listStoredCheckpoints);
        }
    }

    /**
     * This method returns the list of ownership records from the underlying data store, and if no ownership records are available, then it returns empty results.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the current instance of Event Hub
     * @param eventHubName The Event Hub name from which checkpoint information is acquired
     * @param consumerGroup The consumer group name associated with the checkpoint
     * @return Flux of PartitionOwnership objects
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

                // if PARTITION_OWNERSHIP field exists but has no records than the list will be empty
                if (!partitionOwnershipJsonList.isEmpty()) {
                    String partitionOwnershipJson = partitionOwnershipJsonList.get(0);
                    // if PARTITION_OWNERSHIP field does not exist for member we will get a null
                    if (partitionOwnershipJson == null) {
                        LOGGER.verbose("No partition ownership records exist for this checkpoint yet.");
                        continue;
                    }
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
     * @return Mono that completes if no errors take place
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
            if (!jedis.exists(prefix) || !jedis.exists(key)) {
                //Case 1: new checkpoint
                jedis.sadd(prefix, key);
                jedis.hset(key, CHECKPOINT, new String(DEFAULT_SERIALIZER.serializeToBytes(checkpoint), StandardCharsets.UTF_8));
            } else {
                //Case 2: checkpoint already exists in Redis cache
                jedis.hset(key, CHECKPOINT, new String(DEFAULT_SERIALIZER.serializeToBytes(checkpoint), StandardCharsets.UTF_8));
            }

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
