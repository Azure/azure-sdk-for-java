// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.exception.AzureException;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.azure.messaging.eventhubs.checkpointstore.jedis.JedisCheckpointStore.PARTITION_OWNERSHIP;
import static com.azure.messaging.eventhubs.checkpointstore.jedis.JedisCheckpointStore.keyBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JedisCheckpointStore}
 */
public class JedisCheckpointStoreTests {
    private static final JsonSerializer JSON_SERIALIZER = JsonSerializerProviders.createInstance(true);
    private static final String FULLY_QUALIFIED_NAMESPACE = "fullyQualifiedNamespace";
    private static final String EVENT_HUB_NAME = "eventHubName";
    private static final String CONSUMER_GROUP = "consumerGroup";
    private static final long MODIFIED_TIME = 10000000L;
    private static final String PARTITION_ID = "1";
    private static final byte[] PREFIX = JedisCheckpointStore.prefixBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME,
        CONSUMER_GROUP);
    private static final byte[] KEY = keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);

    @Mock
    private JedisPool jedisPool;
    @Mock
    private Jedis jedis;
    @Captor
    private ArgumentCaptor<byte[]> argumentCaptor;

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
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        Set<byte[]> value = new HashSet<>();
        value.add(KEY);
        byte[] partitionOwnershipToBytes = JSON_SERIALIZER.serializeToBytes(partitionOwnership);
        List<byte[]> list = Collections.singletonList(partitionOwnershipToBytes);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.smembers(PREFIX)).thenReturn(value);
        when(jedis.hmget(eq(KEY),
            eq(PARTITION_OWNERSHIP))).thenReturn(list);

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
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.sadd(eq(PREFIX), eq(PARTITION_OWNERSHIP), any())).thenReturn(0L);
        when(jedis.hsetnx(eq(KEY), eq(PARTITION_OWNERSHIP), any())).thenReturn(1L);
        when(jedis.time()).thenReturn(Collections.singletonList("10000000"));
        Transaction transaction = mock(Transaction.class, RETURNS_SMART_NULLS);
        when(transaction.exec()).thenReturn(Collections.singletonList(1L));
        when(jedis.multi()).thenReturn(transaction);

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
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        Transaction transaction = mock(Transaction.class, RETURNS_SMART_NULLS);
        when(transaction.exec()).thenReturn(Collections.singletonList(1L));

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.hmget(KEY, PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList("oldOwnershipRecord".getBytes(StandardCharsets.UTF_8)));
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

        verify(jedis, times(1)).sadd(eq(PREFIX), eq(KEY));
    }

    /**
     * Verifies we can create an ownership when no existing ownerships exist.
     */
    @Test
    public void claimOwnershipNoExistingOwnerships() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        Transaction transaction = mock(Transaction.class);

        when(jedisPool.getResource()).thenReturn(jedis);

        // No existing partition ownerships.
        when(jedis.hmget(KEY, PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList(null));
        when(jedis.watch(KEY)).thenReturn("OK");
        when(jedis.multi()).thenReturn(transaction);

        when(jedis.time()).thenReturn(Arrays.asList("1678740344"));

        StepVerifier.create(store.claimOwnership(partitionOwnershipList))
            .expectError(AzureException.class)
            .verify();

        verify(jedis, times(1)).sadd(eq(PREFIX), eq(KEY));
    }

    /**
     * Verifies when No existing ownerships and an ownership gets added as just we're about to add one, an error is
     * thrown.
     */
    @Test
    public void claimOwnershipNoExistingOwnershipsFails() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        Transaction transaction = mock(Transaction.class);

        when(jedisPool.getResource()).thenReturn(jedis);

        // No existing partition ownerships.
        when(jedis.hmget(KEY, PARTITION_OWNERSHIP)).thenReturn(Collections.singletonList(null));
        when(jedis.watch(KEY)).thenReturn("OK");
        when(jedis.multi()).thenReturn(transaction);

        // 2023-03-13T20:45:44Z and 0 ms.
        when(jedis.time()).thenReturn(Arrays.asList("1678740344"));

        // Act & Assert
        StepVerifier.create(store.claimOwnership(partitionOwnershipList))
            .expectError(AzureException.class)
            .verify();

        verify(jedis).unwatch();
        verify(jedis, times(1)).sadd(eq(PREFIX), eq(KEY));
    }

    /**
     * Verifies when there are existing ownerships, and we claim one, it returns a success.
     */
    @Test
    public void claimOwnershipExistingOwnerships() {
        // Arrange
        TypeReference<PartitionOwnership> type = TypeReference.createInstance(PartitionOwnership.class);

        String owner1 = "owner1";
        String owner2 = "owner2";
        String owner3 = "owner3";
        String partitionId1 = "1";
        String partitionId2 = "2";
        String partitionId3 = "3";
        byte[] key1 = keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, partitionId1);
        byte[] key2 = keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, partitionId2);
        byte[] key3 = keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, partitionId3);

        PartitionOwnership ownership1 = createPartitionOwnership(partitionId1).setOwnerId(owner2);
        PartitionOwnership ownership3 = createPartitionOwnership(partitionId3).setOwnerId(owner3);
        List<PartitionOwnership> partitionsToClaim = Arrays.asList(ownership1, ownership3);

        when(jedisPool.getResource()).thenReturn(jedis);

        // All partitions are currently owned by owner1.
        byte[] existing1 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId1).setOwnerId(owner1));
        byte[] existing2 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId2).setOwnerId(owner1));
        byte[] existing3 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId3).setOwnerId(owner1));

        when(jedis.hmget(any(), eq(PARTITION_OWNERSHIP))).thenAnswer(invocationOnMock -> {
            byte[] queriedKey = invocationOnMock.getArgument(0);

            if (Arrays.equals(queriedKey, key1)) {
                return Collections.singletonList(existing1);
            } else if (Arrays.equals(queriedKey, key2)) {
                return Collections.singletonList(existing2);
            } else if (Arrays.equals(queriedKey, key3)) {
                return Collections.singletonList(existing3);
            } else {
                throw new IllegalArgumentException("Unknown key: " + new String(queriedKey, StandardCharsets.UTF_8));
            }
        });

        when(jedis.watch(any(byte[].class))).thenReturn("OK");

        Transaction transaction = mock(Transaction.class);
        when(jedis.multi()).thenReturn(transaction);

        // 2023-03-13T20:45:44Z and 0 ms.
        long lastModifiedTime = 1678740344L;
        when(jedis.time()).thenReturn(Arrays.asList(String.valueOf(lastModifiedTime), "0"));

        when(transaction.exec()).thenReturn(Collections.singletonList(0));

        // Act & Assert
        StepVerifier.create(store.claimOwnership(partitionsToClaim))
            .assertNext(first -> {
                assertEquals(owner2, first.getOwnerId());
                assertEquals(lastModifiedTime * 1000, first.getLastModifiedTime());
            })
            .assertNext(second -> {
                assertEquals(owner3, second.getOwnerId());
                assertEquals(lastModifiedTime * 1000, second.getLastModifiedTime());
            })
            .expectComplete()
            .verify();

        verify(transaction, times(2)).exec();
        verify(jedis, times(2)).multi();

        // Assert ownership information.
        // Verify for ownership request 1 (partitionId1)
        verify(transaction).hset(eq(key1), eq(PARTITION_OWNERSHIP), argumentCaptor.capture());

        byte[] actualBytes1 = argumentCaptor.getValue();
        assertNotNull(actualBytes1);

        PartitionOwnership actual = JSON_SERIALIZER.deserializeFromBytes(actualBytes1, type);
        assertEquals(owner2, actual.getOwnerId());

        // Verify for ownership request 2 (partitionId3)
        verify(transaction).hset(eq(key3), eq(PARTITION_OWNERSHIP), argumentCaptor.capture());

        byte[] actualBytes2 = argumentCaptor.getValue();
        assertNotNull(actualBytes2);

        PartitionOwnership actual2 = JSON_SERIALIZER.deserializeFromBytes(actualBytes2, type);
        assertEquals(owner3, actual2.getOwnerId());
    }

    public static Stream<List<Object>> claimOwnershipExistingOwnershipsFails() {
        return Stream.of(
            Collections.emptyList(),
            Collections.singletonList(null),
            null
        );
    }

    /**
     * Verifies when we try to claim ownership: 1. There are existing ownerships 2. Ownership is updated just as we are
     * going to claim it. Should result in an error.
     *
     * 1. the first one is successful, the second one should be an error. There shouldn't be a query for the third one.
     */
    @ParameterizedTest
    @MethodSource
    public void claimOwnershipExistingOwnershipsFails(List<Object> unsuccessfulReturns) {
        // Arrange
        TypeReference<PartitionOwnership> type = TypeReference.createInstance(PartitionOwnership.class);

        String owner1 = "owner1";
        String owner2 = "owner2";
        String owner3 = "owner3";
        String partitionId1 = "1";
        String partitionId2 = "2";
        String partitionId3 = "3";
        byte[] key1 = keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, partitionId1);
        byte[] key2 = keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, partitionId2);
        byte[] key3 = keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, partitionId3);

        PartitionOwnership ownership1 = createPartitionOwnership(partitionId1).setOwnerId(owner2);
        PartitionOwnership ownership3 = createPartitionOwnership(partitionId3).setOwnerId(owner3);
        List<PartitionOwnership> partitionsToClaim = Arrays.asList(ownership1, ownership3);

        when(jedisPool.getResource()).thenReturn(jedis);

        // All partitions are currently owned by owner1.
        byte[] existing1 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId1).setOwnerId(owner1));
        byte[] existing2 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId2).setOwnerId(owner1));
        byte[] existing3 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId3).setOwnerId(owner1));

        when(jedis.hmget(any(), eq(PARTITION_OWNERSHIP))).thenAnswer(invocationOnMock -> {
            byte[] queriedKey = invocationOnMock.getArgument(0);

            if (Arrays.equals(queriedKey, key1)) {
                return Collections.singletonList(existing1);
            } else if (Arrays.equals(queriedKey, key2)) {
                return Collections.singletonList(existing2);
            } else if (Arrays.equals(queriedKey, key3)) {
                return Collections.singletonList(existing3);
            } else {
                throw new IllegalArgumentException("Unknown key: " + new String(queriedKey, StandardCharsets.UTF_8));
            }
        });

        when(jedis.watch(any(byte[].class))).thenReturn("OK");

        Transaction transaction = mock(Transaction.class);
        when(jedis.multi()).thenReturn(transaction);

        // 2023-03-13T20:45:44Z and 0 ms.
        long lastModifiedTime = 1678740344L;
        when(jedis.time()).thenReturn(Arrays.asList(String.valueOf(lastModifiedTime), "0"));

        AtomicInteger invocations = new AtomicInteger();
        when(transaction.exec()).thenAnswer(invocation -> {
            int number = invocations.getAndIncrement();
            if (number == 0) {
                return Collections.singletonList(0);
            } else if (number == 1) {
                return unsuccessfulReturns;
            } else {
                throw new IllegalArgumentException("Did not expect so many invocations: " + number);
            }
        });

        // Act & Assert
        StepVerifier.create(store.claimOwnership(partitionsToClaim))
            .assertNext(first -> {
                assertEquals(owner2, first.getOwnerId());
                assertEquals(lastModifiedTime * 1000, first.getLastModifiedTime());
            })
            .expectErrorMatches(error -> {
                // The scenario where unsuccessfulReturns is given when the transaction is executed.
                return error instanceof AzureException;
            })
            .verify();

        verify(transaction, times(2)).exec();
        verify(jedis, times(2)).multi();

        // Assert the information we tried to persist into the key store.
        verify(transaction).hset(eq(key1), eq(PARTITION_OWNERSHIP), argumentCaptor.capture());

        byte[] actualBytes1 = argumentCaptor.getValue();
        assertNotNull(actualBytes1);

        PartitionOwnership actual = JSON_SERIALIZER.deserializeFromBytes(actualBytes1, type);
        assertEquals(lastModifiedTime * 1000, actual.getLastModifiedTime());
        assertEquals(owner2, actual.getOwnerId());
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

    private static PartitionOwnership createPartitionOwnership(String partitionId) {
        return new PartitionOwnership()
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP)
            .setPartitionId(partitionId)
            .setLastModifiedTime(MODIFIED_TIME)
            .setOwnerId("ownerOne")
            .setETag("eTag");
    }

    private static Checkpoint createCheckpoint() {
        return new Checkpoint()
            .setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP)
            .setPartitionId(PARTITION_ID)
            .setSequenceNumber(1L);
    }
}
