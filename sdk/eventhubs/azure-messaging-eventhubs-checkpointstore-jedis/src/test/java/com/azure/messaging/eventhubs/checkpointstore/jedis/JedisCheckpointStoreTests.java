// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;

import com.azure.core.exception.AzureException;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;
import redis.clients.jedis.JedisPool;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.azure.messaging.eventhubs.checkpointstore.jedis.JedisCheckpointStore.keyBuilder;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link JedisCheckpointStore}
 */
@SuppressWarnings("resource")
public class JedisCheckpointStoreTests {
    private static final JsonSerializer JSON_SERIALIZER = JsonSerializerProviders.createInstance(true);
    private static final String FULLY_QUALIFIED_NAMESPACE = "fullyQualifiedNamespace";
    private static final String EVENT_HUB_NAME = "eventHubName";
    private static final String CONSUMER_GROUP = "consumerGroup";
    private static final long MODIFIED_TIME = 10000000L;
    private static final String PARTITION_ID = "1";
    private static final byte[] PREFIX
        = JedisCheckpointStore.prefixBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP);
    private static final byte[] KEY
        = keyBuilder(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP, PARTITION_ID);

    @Test
    public void testListCheckpoints() {
        Checkpoint checkpoint = createCheckpoint();
        byte[] checkpointInBytes = JSON_SERIALIZER.serializeToBytes(checkpoint);

        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public Set<byte[]> smembers(byte[] key) {
                        return Arrays.equals(PREFIX, key) ? Collections.singleton(KEY) : null;
                    }

                    @Override
                    public List<byte[]> hmget(byte[] key, byte[]... fields) {
                        return Arrays.equals(key, KEY) && Arrays.equals(fields[0], JedisCheckpointStore.CHECKPOINT)
                            ? Collections.singletonList(checkpointInBytes)
                            : null;
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .assertNext(checkpointTest -> {
                assertEquals(FULLY_QUALIFIED_NAMESPACE, checkpointTest.getFullyQualifiedNamespace());
                assertEquals(EVENT_HUB_NAME, checkpointTest.getEventHubName());
                assertEquals(CONSUMER_GROUP, checkpointTest.getConsumerGroup());
            })
            .verifyComplete();
    }

    @Test
    public void testListCheckpointsEmptyList() {
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public Set<byte[]> smembers(byte[] key) {
                        return Arrays.equals(PREFIX, key) ? new HashSet<>() : null;
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .verifyComplete();
    }

    @Test
    public void testCheckpointKeyNotStored() {
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public Set<byte[]> smembers(byte[] key) {
                        return Arrays.equals(PREFIX, key) ? Collections.singleton(KEY) : null;
                    }

                    @Override
                    public List<byte[]> hmget(byte[] key, byte[]... fields) {
                        return Arrays.equals(key, KEY) && Arrays.equals(fields[0], JedisCheckpointStore.CHECKPOINT)
                            ? Collections.singletonList(null)
                            : null;
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.listCheckpoints(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .verifyComplete();
    }

    @Test
    public void testListOwnership() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        byte[] partitionOwnershipToBytes = JSON_SERIALIZER.serializeToBytes(partitionOwnership);

        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public Set<byte[]> smembers(byte[] key) {
                        return Arrays.equals(PREFIX, key) ? Collections.singleton(KEY) : null;
                    }

                    @Override
                    public List<byte[]> hmget(byte[] key, byte[]... fields) {
                        return Arrays.equals(key, KEY) && Arrays.equals(fields[0], PARTITION_OWNERSHIP)
                            ? Collections.singletonList(partitionOwnershipToBytes)
                            : null;
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.listOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
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
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public Set<byte[]> smembers(byte[] key) {
                        return Arrays.equals(PREFIX, key) ? new HashSet<>() : null;
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.listOwnership(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, CONSUMER_GROUP))
            .verifyComplete();
    }

    @Test
    public void testClaimOwnershipEmptyField() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        TestTransactionWrapper transaction = new TestTransactionWrapper() {
            @Override
            public List<Object> exec() {
                return Collections.singletonList(1L);
            }
        };

        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public void sadd(byte[] key, byte[]... members) {
                        assertArrayEquals(PREFIX, key);
                        assertArrayEquals(KEY, members[0]);
                    }

                    @Override
                    public List<String> time() {
                        return Collections.singletonList("10000000");
                    }

                    @Override
                    public TransactionWrapper multi() {
                        return transaction;
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.claimOwnership(partitionOwnershipList))
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
        TestTransactionWrapper transaction = new TestTransactionWrapper() {
            @Override
            public List<Object> exec() {
                return Collections.singletonList(1L);
            }
        };

        AtomicInteger saddCallCount = new AtomicInteger();
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public List<byte[]> hmget(byte[] key, byte[]... fields) {
                        return Arrays.equals(key, KEY) && Arrays.equals(fields[0], JedisCheckpointStore.CHECKPOINT)
                            ? Collections.singletonList("oldOwnershipRecord".getBytes(StandardCharsets.UTF_8))
                            : null;
                    }

                    @Override
                    public List<String> time() {
                        return Collections.singletonList("10000000");
                    }

                    @Override
                    public TransactionWrapper multi() {
                        return transaction;
                    }

                    @Override
                    public void sadd(byte[] key, byte[]... members) {
                        assertArrayEquals(PREFIX, key);
                        assertArrayEquals(KEY, members[0]);
                        saddCallCount.incrementAndGet();
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.claimOwnership(partitionOwnershipList))
            .assertNext(partitionOwnershipTest -> {
                assertEquals(EVENT_HUB_NAME, partitionOwnershipTest.getEventHubName());
                assertEquals(CONSUMER_GROUP, partitionOwnershipTest.getConsumerGroup());
                assertEquals(FULLY_QUALIFIED_NAMESPACE, partitionOwnershipTest.getFullyQualifiedNamespace());
                assertEquals("ownerOne", partitionOwnershipTest.getOwnerId());
            })
            .verifyComplete();

        assertEquals(1, saddCallCount.get());
    }

    /**
     * Verifies we can create an ownership when no existing ownerships exist.
     */
    @Test
    public void claimOwnershipNoExistingOwnerships() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        TestTransactionWrapper transaction = new TestTransactionWrapper();

        AtomicInteger saddCallCount = new AtomicInteger();
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public List<byte[]> hmget(byte[] key, byte[]... fields) {
                        return Arrays.equals(key, KEY) && Arrays.equals(fields[0], PARTITION_OWNERSHIP)
                            ? Collections.singletonList(null) // No existing partition ownerships.
                            : null;
                    }

                    @Override
                    public void watch(byte[]... keys) {
                        assertArrayEquals(KEY, keys[0]);
                    }

                    @Override
                    public List<String> time() {
                        return Collections.singletonList("1678740344");
                    }

                    @Override
                    public TransactionWrapper multi() {
                        return transaction;
                    }

                    @Override
                    public void sadd(byte[] key, byte[]... members) {
                        assertArrayEquals(PREFIX, key);
                        assertArrayEquals(KEY, members[0]);
                        saddCallCount.incrementAndGet();
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.claimOwnership(partitionOwnershipList))
            .expectError(AzureException.class)
            .verify();

        assertEquals(1, saddCallCount.get());
    }

    /**
     * Verifies when No existing ownerships and an ownership gets added as just we're about to add one, an error is
     * thrown.
     */
    @Test
    public void claimOwnershipNoExistingOwnershipsFails() {
        PartitionOwnership partitionOwnership = createPartitionOwnership(PARTITION_ID);
        List<PartitionOwnership> partitionOwnershipList = Collections.singletonList(partitionOwnership);
        TestTransactionWrapper transaction = new TestTransactionWrapper();

        AtomicInteger saddCallCount = new AtomicInteger();
        AtomicInteger unwatchCallCount = new AtomicInteger();
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public List<byte[]> hmget(byte[] key, byte[]... fields) {
                        return Arrays.equals(key, KEY) && Arrays.equals(fields[0], PARTITION_OWNERSHIP)
                            ? Collections.singletonList(null) // No existing partition ownerships.
                            : null;
                    }

                    @Override
                    public void watch(byte[]... keys) {
                        assertArrayEquals(KEY, keys[0]);
                    }

                    @Override
                    public void unwatch() {
                        unwatchCallCount.incrementAndGet();
                    }

                    @Override
                    public List<String> time() {
                        return Collections.singletonList("1678740344");
                    }

                    @Override
                    public TransactionWrapper multi() {
                        return transaction;
                    }

                    @Override
                    public void sadd(byte[] key, byte[]... members) {
                        assertArrayEquals(PREFIX, key);
                        assertArrayEquals(KEY, members[0]);
                        saddCallCount.incrementAndGet();
                    }
                };
            }
        };

        // Act & Assert
        StepVerifier.create(checkpointStore.claimOwnership(partitionOwnershipList))
            .expectError(AzureException.class)
            .verify();

        assertEquals(1, unwatchCallCount.get());
        assertEquals(1, saddCallCount.get());
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

        // All partitions are currently owned by owner1.
        byte[] existing1 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId1).setOwnerId(owner1));
        byte[] existing2 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId2).setOwnerId(owner1));
        byte[] existing3 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId3).setOwnerId(owner1));

        AtomicInteger execCallCount = new AtomicInteger();
        AtomicReference<byte[]> actualBytes1 = new AtomicReference<>();
        AtomicReference<byte[]> actualBytes2 = new AtomicReference<>();
        TestTransactionWrapper transaction = new TestTransactionWrapper() {
            @Override
            public List<Object> exec() {
                execCallCount.incrementAndGet();
                return Collections.singletonList(0);
            }

            @Override
            public void hset(byte[] key, byte[] value) {
                if (Arrays.equals(key, key1)) {
                    actualBytes1.set(value);
                } else if (Arrays.equals(key, key3)) {
                    actualBytes2.set(value);
                }
            }
        };

        long lastModifiedTime = 1678740344L;

        AtomicInteger multiCallCount = new AtomicInteger();
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    void sadd(byte[] key, byte[]... members) {
                    }

                    @Override
                    public List<byte[]> hmget(byte[] key, byte[]... fields) {
                        if (!Arrays.equals(fields[0], PARTITION_OWNERSHIP)) {
                            return null;
                        }

                        if (Arrays.equals(key, key1)) {
                            return Collections.singletonList(existing1);
                        } else if (Arrays.equals(key, key2)) {
                            return Collections.singletonList(existing2);
                        } else if (Arrays.equals(key, key3)) {
                            return Collections.singletonList(existing3);
                        } else {
                            throw new IllegalArgumentException(
                                "Unknown key: " + new String(key, StandardCharsets.UTF_8));
                        }
                    }

                    @Override
                    public void watch(byte[]... keys) {
                    }

                    @Override
                    public List<String> time() {
                        return Arrays.asList(String.valueOf(lastModifiedTime), "0");
                    }

                    @Override
                    public TransactionWrapper multi() {
                        multiCallCount.incrementAndGet();
                        return transaction;
                    }
                };
            }
        };

        // Act & Assert
        StepVerifier.create(checkpointStore.claimOwnership(partitionsToClaim)).assertNext(first -> {
            assertEquals(owner2, first.getOwnerId());
            assertEquals(lastModifiedTime * 1000, first.getLastModifiedTime());
        }).assertNext(second -> {
            assertEquals(owner3, second.getOwnerId());
            assertEquals(lastModifiedTime * 1000, second.getLastModifiedTime());
        }).expectComplete().verify();

        assertEquals(2, execCallCount.get());
        assertEquals(2, multiCallCount.get());

        // Assert ownership information.
        // Verify for ownership request 1 (partitionId1)
        assertNotNull(actualBytes1.get());

        PartitionOwnership actual = JSON_SERIALIZER.deserializeFromBytes(actualBytes1.get(), type);
        assertEquals(owner2, actual.getOwnerId());

        // Verify for ownership request 2 (partitionId3)
        assertNotNull(actualBytes2.get());

        PartitionOwnership actual2 = JSON_SERIALIZER.deserializeFromBytes(actualBytes2.get(), type);
        assertEquals(owner3, actual2.getOwnerId());
    }

    public static Stream<List<Object>> claimOwnershipExistingOwnershipsFails() {
        return Stream.of(Collections.emptyList(), Collections.singletonList(null), null);
    }

    /**
     * Verifies when we try to claim ownership: 1. There are existing ownerships 2. Ownership is updated just as we are
     * going to claim it. Should result in an error.
     * <p>
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

        // All partitions are currently owned by owner1.
        byte[] existing1 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId1).setOwnerId(owner1));
        byte[] existing2 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId2).setOwnerId(owner1));
        byte[] existing3 = JSON_SERIALIZER.serializeToBytes(createPartitionOwnership(partitionId3).setOwnerId(owner1));

        AtomicInteger execCallCount = new AtomicInteger();
        AtomicReference<byte[]> actualBytes1 = new AtomicReference<>();
        TestTransactionWrapper transaction = new TestTransactionWrapper() {
            @Override
            public List<Object> exec() {
                int number = execCallCount.getAndIncrement();
                if (number == 0) {
                    return Collections.singletonList(0);
                } else if (number == 1) {
                    return unsuccessfulReturns;
                } else {
                    throw new IllegalArgumentException("Did not expect so many invocations: " + number);
                }
            }

            @Override
            public void hset(byte[] key, byte[] value) {
                if (Arrays.equals(key, key1)) {
                    actualBytes1.set(value);
                }
            }
        };

        long lastModifiedTime = 1678740344L;

        AtomicInteger multiCallCount = new AtomicInteger();
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    void sadd(byte[] key, byte[]... members) {
                    }

                    @Override
                    public List<byte[]> hmget(byte[] key, byte[]... fields) {
                        if (!Arrays.equals(fields[0], PARTITION_OWNERSHIP)) {
                            return null;
                        }

                        if (Arrays.equals(key, key1)) {
                            return Collections.singletonList(existing1);
                        } else if (Arrays.equals(key, key2)) {
                            return Collections.singletonList(existing2);
                        } else if (Arrays.equals(key, key3)) {
                            return Collections.singletonList(existing3);
                        } else {
                            throw new IllegalArgumentException(
                                "Unknown key: " + new String(key, StandardCharsets.UTF_8));
                        }
                    }

                    @Override
                    public void watch(byte[]... keys) {
                    }

                    @Override
                    public List<String> time() {
                        return Arrays.asList(String.valueOf(lastModifiedTime), "0");
                    }

                    @Override
                    public TransactionWrapper multi() {
                        multiCallCount.incrementAndGet();
                        return transaction;
                    }
                };
            }
        };

        // Act & Assert
        StepVerifier.create(checkpointStore.claimOwnership(partitionsToClaim)).assertNext(first -> {
            assertEquals(owner2, first.getOwnerId());
            assertEquals(lastModifiedTime * 1000, first.getLastModifiedTime());
        }).expectErrorMatches(error -> {
            // The scenario where unsuccessfulReturns is given when the transaction is executed.
            return error instanceof AzureException;
        }).verify();

        assertEquals(2, execCallCount.get());
        assertEquals(2, multiCallCount.get());

        // Assert the information we tried to persist into the key store.
        assertNotNull(actualBytes1.get());

        PartitionOwnership actual = JSON_SERIALIZER.deserializeFromBytes(actualBytes1.get(), type);
        assertEquals(lastModifiedTime * 1000, actual.getLastModifiedTime());
        assertEquals(owner2, actual.getOwnerId());
    }

    @Test
    public void testUpdateCheckpoint() {
        Checkpoint checkpoint = createCheckpoint();
        JedisCheckpointStore checkpointStore = new JedisCheckpointStore(new JedisPool()) {
            @Override
            JedisWrapper getJedisWrapper() {
                return new TestJedisWrapper() {
                    @Override
                    public void hset(byte[] key, byte[] value) {
                    }
                };
            }
        };

        StepVerifier.create(checkpointStore.updateCheckpoint(checkpoint)).verifyComplete();
    }

    @Test
    public void updateInvalidCheckpoint() {
        // Arrange
        Checkpoint invalidCheckpoint = createCheckpoint().setOffset(null).setSequenceNumber(null);

        // Act
        StepVerifier.create(new JedisCheckpointStore(new JedisPool()).updateCheckpoint(invalidCheckpoint))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    public void updateNullCheckpoint() {
        StepVerifier.create(new JedisCheckpointStore(new JedisPool()).updateCheckpoint(null))
            .expectError(NullPointerException.class)
            .verify();
    }

    private static PartitionOwnership createPartitionOwnership(String partitionId) {
        return new PartitionOwnership().setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP)
            .setPartitionId(partitionId)
            .setLastModifiedTime(MODIFIED_TIME)
            .setOwnerId("ownerOne")
            .setETag("eTag");
    }

    private static Checkpoint createCheckpoint() {
        return new Checkpoint().setFullyQualifiedNamespace(FULLY_QUALIFIED_NAMESPACE)
            .setEventHubName(EVENT_HUB_NAME)
            .setConsumerGroup(CONSUMER_GROUP)
            .setPartitionId(PARTITION_ID)
            .setSequenceNumber(1L);
    }

    private static class TestJedisWrapper extends JedisCheckpointStore.JedisWrapper {
        TestJedisWrapper() {
            super(null);
        }

        @Override
        void sadd(byte[] key, byte[]... members) {
        }

        @Override
        void watch(byte[]... keys) {
        }

        @Override
        List<String> time() {
            return null;
        }

        @Override
        JedisCheckpointStore.TransactionWrapper multi() {
            return null;
        }

        @Override
        Set<byte[]> smembers(byte[] key) {
            return null;
        }

        @Override
        List<byte[]> hmget(byte[] key, byte[]... fields) {
            return null;
        }

        @Override
        void hset(byte[] key, byte[] value) {
        }

        @Override
        void unwatch() {
        }

        @Override
        public void close() {
        }
    }

    private static class TestTransactionWrapper extends JedisCheckpointStore.TransactionWrapper {
        TestTransactionWrapper() {
            super(null);
        }

        @Override
        public List<Object> exec() {
            return null;
        }

        @Override
        public void hset(byte[] key, byte[] value) {
        }
    }
}
