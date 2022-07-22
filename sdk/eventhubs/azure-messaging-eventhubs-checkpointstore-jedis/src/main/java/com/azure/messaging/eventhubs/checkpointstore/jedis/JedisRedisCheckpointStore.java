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
import redis.clients.jedis.Pipeline;

/**
 * Implementation of {@link CheckpointStore} that uses Azure Redis Cache, specifically Jedis.
 */
public class JedisRedisCheckpointStore implements CheckpointStore {

    private static final ClientLogger LOGGER = new ClientLogger(JedisRedisCheckpointStore.class);
    static final JsonSerializer DEFAULT_SERIALIZER = JsonSerializerProviders.createInstance(true);
    static final byte[] CHECKPOINT = "checkpoint".getBytes(StandardCharsets.UTF_8);
    static final byte[] PARTITION_OWNERSHIP = "partitionOwnership".getBytes(StandardCharsets.UTF_8);
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
            byte[] key = keyBuilder(partitionOwnership.getFullyQualifiedNamespace(), partitionOwnership.getEventHubName(), partitionOwnership.getConsumerGroup(), partitionId);

            try (Jedis jedis = jedisPool.getResource()) {
                List<byte[]> keyInformation = jedis.hmget(key, PARTITION_OWNERSHIP);
                byte[] currentPartitionOwnership = keyInformation.get(0);

                if (currentPartitionOwnership == null) {
                    // if PARTITION_OWNERSHIP field does not exist for member we will get a null, and we must add the field
                    Long lastModifiedTimeSeconds = Long.parseLong(jedis.time().get(0));
                    partitionOwnership.setLastModifiedTime(lastModifiedTimeSeconds);
                    jedis.hset(key, PARTITION_OWNERSHIP, DEFAULT_SERIALIZER.serializeToBytes(partitionOwnership));
                } else {
                    // otherwise we have to change the ownership and "watch" the transaction
                    jedis.watch(key);
                    Long lastModifiedTimeSeconds = Long.parseLong(jedis.time().get(0)) - jedis.objectIdletime(key);
                    partitionOwnership.setLastModifiedTime(lastModifiedTimeSeconds);
                    Transaction transaction = jedis.multi();
                    transaction.hset(key, PARTITION_OWNERSHIP, DEFAULT_SERIALIZER.serializeToBytes(partitionOwnership));
                    List<Object> executionResponse = transaction.exec();

                    if (executionResponse == null) {
                        //This means that the transaction did not execute, which implies that another client has changed the ownership during this transaction
                        sink.error(new RuntimeException("Ownership records were changed by another client"));
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

        byte[] prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup);
        try (Jedis jedis = jedisPool.getResource()) {

            ArrayList<Checkpoint> listStoredCheckpoints = new ArrayList<>();
            Set<byte[]> members = jedis.smembers(prefix);

            if (members.isEmpty()) {
                jedisPool.returnResource(jedis);
                return Flux.fromIterable(listStoredCheckpoints);
            }
            for (byte[] member : members) {
                //get the associated JSON representation for each for the members
                List<byte[]> checkpointJsonList = jedis.hmget(member, CHECKPOINT);

                if (!checkpointJsonList.isEmpty()) {
                    byte[] checkpointJson = checkpointJsonList.get(0);

                    if (checkpointJson == null) {
                        LOGGER.verbose("No checkpoint persists yet.");
                        continue;
                    }
                    Checkpoint checkpoint = DEFAULT_SERIALIZER.deserializeFromBytes(checkpointJson, TypeReference.createInstance(Checkpoint.class));
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
        byte[] prefix = prefixBuilder(fullyQualifiedNamespace, eventHubName, consumerGroup);
        try (Jedis jedis = jedisPool.getResource()) {

            Set<byte[]> members = jedis.smembers(prefix);
            ArrayList<PartitionOwnership> listStoredOwnerships = new ArrayList<>();

            if (members.isEmpty()) {
                jedisPool.returnResource(jedis);
                return Flux.fromIterable(listStoredOwnerships);
            }
            for (byte[] member : members) {
                //get the associated JSON representation for each for the members
                List<byte[]> partitionOwnershipJsonList = jedis.hmget(member, PARTITION_OWNERSHIP);

                // if PARTITION_OWNERSHIP field exists but has no records than the list will be empty
                if (!partitionOwnershipJsonList.isEmpty()) {
                    byte[] partitionOwnershipJson = partitionOwnershipJsonList.get(0);
                    // if PARTITION_OWNERSHIP field does not exist for member we will get a null
                    if (partitionOwnershipJson == null) {
                        LOGGER.verbose("No partition ownership records exist for this checkpoint yet.");
                        continue;
                    }
                    PartitionOwnership partitionOwnership = DEFAULT_SERIALIZER.deserializeFromBytes(partitionOwnershipJson, TypeReference.createInstance(PartitionOwnership.class));
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
        return Mono.fromRunnable(() -> {
            byte[] prefix = prefixBuilder(checkpoint.getFullyQualifiedNamespace(), checkpoint.getEventHubName(), checkpoint.getConsumerGroup());
            byte[] key = keyBuilder(checkpoint.getFullyQualifiedNamespace(), checkpoint.getEventHubName(), checkpoint.getConsumerGroup(), checkpoint.getPartitionId());

            try (Jedis jedis = jedisPool.getResource()) {
                if (!jedis.exists(prefix) || !jedis.exists(key)) {
                    //Case 1: new checkpoint
                    jedis.sadd(prefix, key);
                    jedis.hset(key, CHECKPOINT, DEFAULT_SERIALIZER.serializeToBytes(checkpoint));
                } else {
                    //Case 2: checkpoint already exists in Redis cache
                    jedis.hset(key, CHECKPOINT, DEFAULT_SERIALIZER.serializeToBytes(checkpoint));
                }

                jedisPool.returnResource(jedis);
            }
        });
    }

    static byte[] prefixBuilder(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return (fullyQualifiedNamespace + "/" + eventHubName + "/" + consumerGroup).getBytes(StandardCharsets.UTF_8);
    }

    static byte[] keyBuilder(String fullyQualifiedNamespace, String eventHubName, String consumerGroup, String partitionId) {
        return (fullyQualifiedNamespace + "/" + eventHubName + "/" + consumerGroup + "/" + partitionId).getBytes(StandardCharsets.UTF_8);
    }

    private static Boolean isCheckpointValid(Checkpoint checkpoint) {
        return !(checkpoint == null || (checkpoint.getOffset() == null && checkpoint.getSequenceNumber() == null));
    }

}
