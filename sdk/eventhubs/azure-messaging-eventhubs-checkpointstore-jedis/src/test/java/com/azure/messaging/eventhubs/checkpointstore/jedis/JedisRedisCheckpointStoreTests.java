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
            .setConsumerGroup("consumerGroup")
            .setEventHubName("eventHubName")
            .setFullyQualifiedNamespace("fullyQualifiedNamespace")
            .setPartitionId("one")
            .setSequenceNumber((long) 1);
        Set<String> value = new HashSet<>();
        List<String> list = new ArrayList<>();
        value.add("fullyQualifiedNamespace/eventHubNamespace/consumerGroup/one");
        byte[] bytes = jsonSerializer.serializeToBytes(checkpoint);
        list.add(new String(bytes));
        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers("fullyQualifiedNamespace/eventHubName/consumerGroup")).thenReturn(value);
        when(jedis.hmget(eq("fullyQualifiedNamespace/eventHubNamespace/consumerGroup/one"),
            eq("checkpoint"))).thenReturn(list);
        //assert
        StepVerifier.create(store.listCheckpoints("fullyQualifiedNamespace", "eventHubName", "consumerGroup"))
            .assertNext(checkpointTest -> {
                assertEquals("fullyQualifiedNamespace", checkpointTest.getFullyQualifiedNamespace());
                assertEquals("eventHubName", checkpointTest.getEventHubName());
                assertEquals("consumerGroup", checkpointTest.getConsumerGroup());
            })
            .verifyComplete();
    }

    @Test
    public void testListCheckpointsEmptyList() {
        //arrange
        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers("//")).thenThrow(new IllegalArgumentException());
        //assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> store.listCheckpoints("", "", ""));
    }

    @Test
    public void testCheckpointKeyNotStored() {
        //arrange
        Set<String> value = new HashSet<>();
        value.add("fullyQualifiedNamespace/eventHubNamespace/consumerGroup/one");
        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers("fullyQualifiedNamespace/eventHubName/consumerGroup")).thenReturn(value);
        when(jedis.hmget(eq("fullyQualifiedNamespace/eventHubNamespace/consumerGroup/one"),
            eq("checkpoint"))).thenThrow(new NoSuchElementException());
        //assert
        Assertions.assertThrows(NoSuchElementException.class, () -> store.listCheckpoints("fullyQualifiedNamespace", "eventHubName", "consumerGroup"));
    }

    @Test
    public void testListOwnership() {
        //arrange
        PartitionOwnership partitionOwnership = new PartitionOwnership()
            .setFullyQualifiedNamespace("fullyQualifiedNamespace")
            .setEventHubName("eventHubName")
            .setConsumerGroup("consumerGroup")
            .setPartitionId("one")
            .setOwnerId("ownerOne")
            .setETag("eTag");

        Set<String> value = new HashSet<>();
        List<String> list = new ArrayList<>();
        value.add("fullyQualifiedNamespace/eventHubNamespace/consumerGroup/one");
        byte[] bytes = jsonSerializer.serializeToBytes(partitionOwnership);
        list.add(new String(bytes));

        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers("fullyQualifiedNamespace/eventHubName/consumerGroup")).thenReturn(value);

        when(jedis.hmget(eq("fullyQualifiedNamespace/eventHubNamespace/consumerGroup/one"),
            eq("partitionOwnership"))).thenReturn(list);
        //assert
        StepVerifier.create(store.listOwnership("fullyQualifiedNamespace", "eventHubName", "consumerGroup"))
            .assertNext(partitionOwnershipTest -> {
                assertEquals("fullyQualifiedNamespace", partitionOwnershipTest.getFullyQualifiedNamespace());
                assertEquals("eventHubName", partitionOwnershipTest.getEventHubName());
                assertEquals("consumerGroup", partitionOwnershipTest.getConsumerGroup());
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
        value.add("fullyQualifiedNamespace/eventHubNamespace/consumerGroup/one");
        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers("fullyQualifiedNamespace/eventHubName/consumerGroup")).thenReturn(value);
        when(jedis.hmget(eq("fullyQualifiedNamespace/eventHubNamespace/consumerGroup/one"),
            eq("partitionOwnership"))).thenThrow(new NoSuchElementException());
        //assert
        Assertions.assertThrows(NoSuchElementException.class, () -> store.listOwnership("fullyQualifiedNamespace", "eventHubName", "consumerGroup"));
    }

    @Test
    public void testClaimOwnership() {

        List<PartitionOwnership> partitionOwnershipList = new ArrayList<>();
        Assertions.assertNull(store.claimOwnership(partitionOwnershipList));
    }

    @Test
    public void testUpdateCheckpoint() {
        Checkpoint checkpoint = new Checkpoint()
            .setConsumerGroup("consumerGroup")
            .setEventHubName("eventHubName")
            .setFullyQualifiedNamespace("fullyQualifiedNamespace")
            .setPartitionId("one")
            .setSequenceNumber((long) 1);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists("fullyQualifiedNamespace/eventHubName/consumerGroup")).thenReturn(true);
        Assertions.assertNull(store.updateCheckpoint(checkpoint));
    }
}
