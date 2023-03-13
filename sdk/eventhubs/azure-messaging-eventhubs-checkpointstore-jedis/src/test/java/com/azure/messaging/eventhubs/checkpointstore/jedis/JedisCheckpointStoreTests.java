// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.exception.AzureException;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JedisCheckpointStore}
 */
public class JedisCheckpointStoreTests {
    private static final JsonSerializer JSON_SERIALIZER = JsonSerializerProviders.createInstance(true);
    private static final String FULLY_QUALIFIED_NAMESPACE = "fullyQualifiedNamespace";
    private static final String EVENT_HUB_NAME = "eventHubName";
    private static final String CONSUMER_GROUP = "consumerGroup";
    private static final String PARTITION_ID = "1";
    private static final byte[] PREFIX = JedisCheckpointStore.prefixBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP);
    private static final byte[] KEY = JedisCheckpointStore.keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);

    @Mock
    private JedisPool jedisPool;
    @Mock
    private Jedis jedis;
    private JedisCheckpointStore store;
    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.store = new JedisCheckpointStore(jedisPool);
    }

    @AfterEach
    public void teardown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testListCheckpoints() {
        Checkpoint checkpoint = createCheckpoint();
        Set<byte[]> value = new HashSet<>();
        value.add(KEY);
        byte[] checkpointInBytes = JSON_SERIALIZER.serializeToBytes(checkpoint);
        List<byte[]> list = Collections.singletonList(checkpointInBytes);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisCheckpointStore.CHECKPOINT))).thenReturn(list);

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
            eq(JedisCheckpointStore.CHECKPOINT))).thenReturn(nullList);

        StepVerifier.create(store.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .verifyComplete();
    }

    @Test
    public void testListOwnership() {
        PartitionOwnership partitionOwnership = createPartitionOwnership();
        Set<byte[]> value = new HashSet<>();
        value.add(KEY);
        byte[] partitionOwnershipToBytes = JSON_SERIALIZER.serializeToBytes(partitionOwnership);
        List<byte[]> list = Collections.singletonList(partitionOwnershipToBytes);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(JedisCheckpointStore.PARTITION_OWNERSHIP))).thenReturn(list);

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
        PartitionOwnership partitionOwnership = createPartitionOwnership(
        );
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.hmget(KEY, JedisCheckpointStore.PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList(null));
        when(jedis.hsetnx(eq(KEY), eq(JedisCheckpointStore.PARTITION_OWNERSHIP), any())).thenReturn(1L);
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
        PartitionOwnership partitionOwnership = createPartitionOwnership();
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        Transaction transaction = mock(Transaction.class, RETURNS_SMART_NULLS);
        when(transaction.exec()).thenReturn(Collections.singletonList(1L));

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.hmget(KEY, JedisCheckpointStore.PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList("oldOwnershipRecord".getBytes(StandardCharsets.UTF_8)));
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
    public void transactionFailsClaimOwnershipNoExistingOwnerships() {
        PartitionOwnership partitionOwnership = createPartitionOwnership();
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        Transaction transaction = mock(Transaction.class);

        when(jedisPool.getResource()).thenReturn(jedis);

        // No existing partition ownerships.
        when(jedis.hmget(KEY, JedisCheckpointStore.PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList(null));
        when(jedis.watch(KEY)).thenReturn("OK");
        when(jedis.multi()).thenReturn(transaction);

        // 2023-03-13T20:45:44Z and 0 ms.
        when(jedis.time()).thenReturn(Arrays.asList("1678740344", "0"));

        // Returns 1 on success. Any other value is an error.
        when(jedis.hsetnx(eq(KEY), eq(JedisCheckpointStore.PARTITION_OWNERSHIP), any(byte[].class))).thenReturn(0L);

        // WATCHed keys are monitored in order to detect changes against them. If at least one watched key is modified
        // before the EXEC command, the whole transaction aborts, and EXEC returns a Null reply to notify that the
        // transaction failed.
        when(transaction.exec()).thenReturn(null);

        StepVerifier.create(store.claimOwnership(partitionOwnershipList))
            .expectError(AzureException.class)
            .verify();
    }

    @Test
    public void testUpdateCheckpoint() {
        Checkpoint checkpoint = createCheckpoint();

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists(PREFIX)).thenReturn(true);

        StepVerifier.create(store.updateCheckpoint(checkpoint))
            .verifyComplete();
    }

    @Test
    public void updateInvalidCheckpoint() {
        // Arrange
        Checkpoint invalidCheckpoint = createCheckpoint()
            .setOffset(null)
            .setSequenceNumber(null);

        // Act
        StepVerifier.create(store.updateCheckpoint(invalidCheckpoint))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    public void updateNullCheckpoint() {
        StepVerifier.create(store.updateCheckpoint(null))
            .expectError(NullPointerException.class)
            .verify();
    }

    private static PartitionOwnership createPartitionOwnership() {
        return new PartitionOwnership()
            .setFullyQualifiedNamespace(JedisCheckpointStoreTests.FULLY_QUALIFIED_NAMESPACE)
            .setEventHubName(JedisCheckpointStoreTests.EVENT_HUB_NAME)
            .setConsumerGroup(JedisCheckpointStoreTests.CONSUMER_GROUP)
            .setPartitionId(JedisCheckpointStoreTests.PARTITION_ID)
            .setOwnerId("ownerOne")
            .setETag("eTag");
    }

    private static Checkpoint createCheckpoint() {
        return new Checkpoint()
            .setFullyQualifiedNamespace(JedisCheckpointStoreTests.FULLY_QUALIFIED_NAMESPACE)
            .setEventHubName(JedisCheckpointStoreTests.EVENT_HUB_NAME)
            .setConsumerGroup(JedisCheckpointStoreTests.CONSUMER_GROUP)
            .setPartitionId(JedisCheckpointStoreTests.PARTITION_ID)
            .setSequenceNumber(1L);
    }
}
