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
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Jedis;

/**
 * Implementation of {@link CheckpointStore} that uses Azure Redis Cache, specifically Jedis.
 */
public class JedisRedisCheckpointStore implements CheckpointStore {
    private static final ClientLogger LOGGER = new ClientLogger(JedisRedisCheckpointStore.class);
    private final JedisPool jedisPool;
    JedisRedisCheckpointStore(RedisClientConfig config) {
        JedisPoolConfig poolConfig = this.createPoolConfig(config);
        jedisPool = new JedisPool(poolConfig, RedisClientConfig.getHostName(), config.getPort(), config.getConnectTimeoutMills(), config.getOperationTimeoutMills(), RedisClientConfig.getPassword(), Protocol.DEFAULT_DATABASE, RedisClientConfig.getClientName(), config.getUseSSL(), null, null, null);
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
        if (!isCheckpointValid(checkpoint)) {
            throw LOGGER.logExceptionAsWarning(Exceptions
                .propagate(new IllegalStateException(
                    "Checkpoint is either null, or both the offset and the sequence number are null.")));
        }

        String key = keyBuilder(checkpoint.getFullyQualifiedNamespace(), checkpoint.getEventHubName(), checkpoint.getConsumerGroup(), checkpoint.getPartitionId());
        Map<String, String> checkpointStorageMap = new HashMap<>();
        try (Jedis jedis = jedisPool.getResource()) {
            //Here is where you add everything to the checkpointStorageMap
            String sequenceNumber = checkpoint.getSequenceNumber() == null ? null
                : String.valueOf(checkpoint.getSequenceNumber());
            checkpointStorageMap.put("SEQUENCE_NUMBER", sequenceNumber);

            String offset = checkpoint.getOffset() == null ? null
                : String.valueOf(checkpoint.getOffset());
            checkpointStorageMap.put("OFFSET", offset);
            jedis.hmset(key, checkpointStorageMap); // key -> { field: value, field:value}
            jedisPool.returnResource(jedis);
        }
        return null;
    }
    private String keyBuilder(String fullyQualifiedNamespace, String eventHubName,  String consumerGroup, String partitionId) {
        return fullyQualifiedNamespace + "/" + eventHubName + "/" + consumerGroup + "/" + partitionId;
    }
    private Boolean isCheckpointValid(Checkpoint checkpoint) {
        return !(checkpoint == null || (checkpoint.getOffset() == null && checkpoint.getSequenceNumber() == null));
    }
    private JedisPoolConfig createPoolConfig(RedisClientConfig config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getPoolMaxTotal());
        poolConfig.setMaxIdle(config.getPoolMaxIdle());
        poolConfig.setBlockWhenExhausted(config.getPoolBlockWhenExhausted());
        poolConfig.setMinIdle(config.getPoolMinIdle());
        return poolConfig;
    }
}
