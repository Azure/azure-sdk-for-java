// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.eventhubs.models.Checkpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


/**
 * Unit tests for {@link  JedisRedisCheckpointStore}
 */
public class JedisRedisCheckpointStoreTests {
    JedisPool jedisPool;
    JedisRedisCheckpointStore store;
    Jedis jedis;

    @BeforeEach
    public void setup() {
        jedisPool = mock(JedisPool.class);
        jedis = mock(Jedis.class);
        store = new JedisRedisCheckpointStore(jedisPool);
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
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        value.add("fullyQualifiedNamespace/eventHubNamespace/consumerGroup");

        try {
            list.add(jacksonAdapter.serialize(checkpoint, SerializerEncoding.JSON));
        }
        catch (IOException e) {
            System.out.println("Hello");
        }

        //act
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(anyString())).thenReturn(value);
        when(jedis.hmget(anyString(), anyString())).thenReturn(list);

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
        when(jedis.smembers(anyString())).thenThrow(new IllegalArgumentException());
        //assert
        try {
            store.listCheckpoints("fullyQualifiedNamespace", "eventHubName", "consumerGroup");
        } catch (IllegalArgumentException e) {
            assert (true);
            return;
        }
        assert (false);
    }
}
