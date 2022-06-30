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

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        List<String> list = new ArrayList<>();
        value.add(JedisRedisCheckpointStore.keyBuilder(PREFIX, PARTITION_ID));
        byte[] bytes = jsonSerializer.serializeToBytes(checkpoint);
        list.add(new String(bytes));
        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.CHECKPOINT))).thenReturn(list);
        //assert
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
        when(jedis.smembers("//")).thenThrow(new IllegalArgumentException());

        Assertions.assertThrows(IllegalArgumentException.class, () -> store.listCheckpoints("", "", ""));
    }

    @Test
    public void testCheckpointKeyNotStored() {

        Set<String> value = new HashSet<>();
        value.add(KEY);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.CHECKPOINT))).thenThrow(new NoSuchElementException());

        Assertions.assertThrows(NoSuchElementException.class, () -> store.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP));
    }

    @Test
    public void testListOwnership() {
        //arrange
        PartitionOwnership partitionOwnership = new PartitionOwnership()
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP)
            .setPartitionId(PARTITION_ID)
            .setOwnerId("ownerOne")
            .setETag("eTag");

        Set<String> value = new HashSet<>();
        List<String> list = new ArrayList<>();
        value.add(KEY);
        byte[] bytes = jsonSerializer.serializeToBytes(partitionOwnership);
        list.add(new String(bytes));

        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);

        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.PARTITION_OWNERSHIP))).thenReturn(list);
        //assert
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
        //arrange
        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers("//")).thenThrow(new IllegalArgumentException());
        //assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> store.listOwnership("", "", ""));
    }
    @Test
    public void testListOwnershipKeyNotStored() {
        //arrange
        Set<String> value = new HashSet<>();
        value.add(KEY);
        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.PARTITION_OWNERSHIP))).thenThrow(new NoSuchElementException());
        //assert
        Assertions.assertThrows(NoSuchElementException.class, () -> store.listOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP));
    }

    @Test
    public void testClaimOwnership() {

        List<PartitionOwnership> partitionOwnershipList = new ArrayList<>();
        Assertions.assertNull(store.claimOwnership(partitionOwnershipList));
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
        Assertions.assertNull(store.updateCheckpoint(checkpoint));
    }
}
