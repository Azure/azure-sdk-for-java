// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link  JedisRedisCheckpointStore}
 */
public class JedisRedisCheckpointStoreTests {
    private JedisPool jedisPool;
    private JedisRedisCheckpointStore store;
    private Jedis jedis;
    private JsonSerializer jsonSerializer;

    private static final String FULLY_QUALIFIED_NAMESPACE = "fullyQualifiedNamespace";
    private static final String EVENT_HUB_NAME = "eventHubName";
    private static final String CONSUMER_GROUP = "consumerGroup";
    private static final String PARTITION_ID = "1";
    private static final String PREFIX = JedisRedisCheckpointStore.prefixBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP);
    private static final String KEY = JedisRedisCheckpointStore.keyBuilder(PREFIX, PARTITION_ID);
    @BeforeEach
    public void setup() {
        jedisPool = mock(JedisPool.class);
        jedis = mock(Jedis.class);
        store = new JedisRedisCheckpointStore(jedisPool);
        jsonSerializer = JsonSerializerProviders.createInstance(true);
    }

    @Test
    public void testListCheckpoints() {
        //arrange
        Checkpoint checkpoint = new Checkpoint()
            .setConsumerGroup(CONSUMER_GROUP)
            .setEventHubName(EVENT_HUB_NAME)
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setPartitionId(PARTITION_ID)
            .setSequenceNumber(1L);

        Set<String> value = new HashSet<>();
        value.add(KEY);

        byte[] bytes = jsonSerializer.serializeToBytes(checkpoint);
        List<String> list = Collections.singletonList(new String(bytes, StandardCharsets.UTF_8));

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.CHECKPOINT))).thenReturn(list);

        StepVerifier.create(store.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .assertNext(checkpointTest -> {
                assertEquals(FULLY_QUALIFIED_NAMESPACE, checkpointTest.getFullyQualifiedNamespace());
                assertEquals(EVENT_HUB_NAME, checkpointTest.getEventHubName());
                assertEquals(CONSUMER_GROUP, checkpointTest.getConsumerGroup());
            })
            .verifyComplete();
    }

    @Test
    public void testListCheckpointsEmptyList() {
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(new HashSet<>());

        StepVerifier.create(store.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .assertNext(checkpointTest -> {
                    Assertions.assertNull(checkpointTest);
                }
            );
    }

    @Test
    public void testCheckpointKeyNotStored() {

        Set<String> value = new HashSet<>();
        value.add(KEY);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.CHECKPOINT))).thenReturn(null);

        StepVerifier.create(store.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    public void testListOwnership() {
        PartitionOwnership partitionOwnership = new PartitionOwnership()
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP)
            .setPartitionId(PARTITION_ID)
            .setOwnerId("ownerOne")
            .setETag("eTag");

        Set<String> value = new HashSet<>();
        value.add(KEY);
        byte[] bytes = jsonSerializer.serializeToBytes(partitionOwnership);
        List<String> list = Collections.singletonList(new String(bytes, StandardCharsets.UTF_8));

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.PARTITION_OWNERSHIP))).thenReturn(list);

        StepVerifier.create(store.listOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .assertNext(partitionOwnershipTest -> {
                assertEquals(FULLY_QUALIFIED_NAMESPACE, partitionOwnershipTest.getFullyQualifiedNamespace());
                assertEquals(EVENT_HUB_NAME, partitionOwnershipTest.getEventHubName());
                assertEquals(CONSUMER_GROUP, partitionOwnershipTest.getConsumerGroup());
                assertEquals("ownerOne", partitionOwnershipTest.getOwnerId());
            })
            .verifyComplete();
    }

    @Test
    public void testListOwnershipEmptyList() {
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(new HashSet<>());

        StepVerifier.create(store.listOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .assertNext(partitionOwnershipTest -> {
                    Assertions.assertNull(partitionOwnershipTest);
                }
            );
    }
    @Test
    public void testListOwnershipKeyNotStored() {

        Set<String> value = new HashSet<>();
        value.add(KEY);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.PARTITION_OWNERSHIP))).thenReturn(null);

        StepVerifier.create(store.listOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .expectError(IllegalStateException.class)
            .verify();
    }

    @Test
    public void testClaimOwnership() {
        List<PartitionOwnership> partitionOwnershipList = new ArrayList<>();
        StepVerifier.create(store.claimOwnership(partitionOwnershipList))
            .assertNext(partitionOwnership -> {
                Assertions.assertNull(partitionOwnership);
            });
    }

    @Test
    public void testUpdateCheckpoint() {
        Checkpoint checkpoint = new Checkpoint()
            .setConsumerGroup(CONSUMER_GROUP)
            .setEventHubName(EVENT_HUB_NAME)
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setPartitionId(PARTITION_ID)
            .setSequenceNumber((long) 1);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists(PREFIX)).thenReturn(true);
    }
}
