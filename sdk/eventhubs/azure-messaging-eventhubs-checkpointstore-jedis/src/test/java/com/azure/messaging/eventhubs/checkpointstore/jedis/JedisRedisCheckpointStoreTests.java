// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
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
    private static final byte[] PREFIX = JedisRedisCheckpointStore.prefixBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP);
    private static final byte[] KEY = JedisRedisCheckpointStore.keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);

    @BeforeEach
    public void setup() {
        jedisPool = mock(JedisPool.class);
        jedis = mock(Jedis.class);
        store = new JedisRedisCheckpointStore(jedisPool);
        jsonSerializer = JsonSerializerProviders.createInstance(true);
    }

    @Test
    public void testListCheckpoints() {
        Checkpoint checkpoint = createCheckpoint(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);
        Set<byte[]> value = new HashSet<>();
        value.add(KEY);
        byte[] checkpointInBytes = jsonSerializer.serializeToBytes(checkpoint);
        List<byte[]> list = Collections.singletonList(checkpointInBytes);

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
            .verifyComplete();
    }

    @Test
    public void testCheckpointKeyNotStored() {
        Set<byte[]> value = new HashSet<>();
        List<byte[]> nullList = Collections.singletonList(null);
        value.add(KEY);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisRedisCheckpointStore.CHECKPOINT))).thenReturn(nullList);

        StepVerifier.create(store.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .verifyComplete();
    }

    @Test
    public void testListOwnership() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);
        Set<byte[]> value = new HashSet<>();
        value.add(KEY);
        byte[] partitionOwnershipToBytes = jsonSerializer.serializeToBytes(partitionOwnership);
        List<byte[]> list = Collections.singletonList(partitionOwnershipToBytes);

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
            .verifyComplete();
    }

    @Test
    public void testClaimOwnershipEmptyField() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.hmget(KEY, JedisRedisCheckpointStore.PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList(null));
        when(jedis.time()).thenReturn(Collections.singletonList("10000000"));

        StepVerifier.create(store.claimOwnership(partitionOwnershipList))
            .assertNext(partitionOwnershipTest -> {
                assertEquals(EVENT_HUB_NAME, partitionOwnershipTest.getEventHubName());
                assertEquals(CONSUMER_GROUP, partitionOwnershipTest.getConsumerGroup());
                assertEquals(FULLY_QUALIFIED_NAMESPACE, partitionOwnershipTest.getFullyQualifiedNamespace());
                assertEquals("ownerOne", partitionOwnershipTest.getOwnerId());
            })
            .verifyComplete();
    }

    @Test
    public void testClaimOwnershipNonEmptyField() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        Transaction transaction = mock(Transaction.class, RETURNS_SMART_NULLS);
        when(transaction.exec()).thenReturn(Collections.singletonList(1L));

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.hmget(KEY, JedisRedisCheckpointStore.PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList("oldOwnershipRecord".getBytes(StandardCharsets.UTF_8)));
        when(jedis.multi()).thenReturn(transaction);
        when(jedis.time()).thenReturn(Collections.singletonList("10000000"));
        when(transaction.exec()).thenReturn(Collections.singletonList(1L));

        StepVerifier.create(store.claimOwnership(partitionOwnershipList))
            .assertNext(partitionOwnershipTest -> {
                assertEquals(EVENT_HUB_NAME, partitionOwnershipTest.getEventHubName());
                assertEquals(CONSUMER_GROUP, partitionOwnershipTest.getConsumerGroup());
                assertEquals(FULLY_QUALIFIED_NAMESPACE, partitionOwnershipTest.getFullyQualifiedNamespace());
                assertEquals("ownerOne", partitionOwnershipTest.getOwnerId());
            })
            .verifyComplete();
    }

    @Test
    public void transactionFailsClaimOwnership() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        Transaction transaction = mock(Transaction.class);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.hmget(KEY, JedisRedisCheckpointStore.PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList(null));
        when(jedis.watch(KEY)).thenThrow(RuntimeException.class);

        StepVerifier.create(store.claimOwnership(partitionOwnershipList))
            .expectError(RuntimeException.class);
    }

    @Test
    public void testUpdateCheckpoint() {
        Checkpoint checkpoint = createCheckpoint(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists(PREFIX)).thenReturn(true);

        StepVerifier.create(store.updateCheckpoint(checkpoint))
            .verifyComplete();
    }

    private static PartitionOwnership createPartitionOwnership(String fullyQualifiedNamespace, String eventHubName, String consumerGroup, String partitionId) {
        return new PartitionOwnership()
            .setFullyQualifiedNamespace(fullyQualifiedNamespace)
            .setEventHubName(eventHubName)
            .setConsumerGroup(consumerGroup)
            .setPartitionId(partitionId)
            .setOwnerId("ownerOne")
            .setETag("eTag");
    }

    private static Checkpoint createCheckpoint(String fullyQualifiedNamespace, String eventHubName, String consumerGroup, String partitionId) {
        return new Checkpoint()
            .setFullyQualifiedNamespace(fullyQualifiedNamespace)
            .setEventHubName(eventHubName)
            .setConsumerGroup(consumerGroup)
            .setPartitionId(partitionId)
            .setSequenceNumber(1L);
    }
}
