// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Allows events to be resolved to partitions using common patterns such as round-robin assignment and hashing of
 * partitions keys.
 */
class PartitionResolver {
    private static final ClientLogger LOGGER = new ClientLogger(PartitionResolver.class);
    private static final int STARTING_INDEX = -1;

    private final AtomicInteger partitionAssignmentIndex = new AtomicInteger(STARTING_INDEX);

    /**
     * Assigns a partition using a round-robin approach.
     *
     * @param partitions The set of available partitions.
     *
     * @return The zero-based index of the selected partition from the available set.
     */
    String assignRoundRobin(String[] partitions) {
        Objects.requireNonNull(partitions, "'partitions' cannot be null.");

        if (partitions.length == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'partitions' cannot be empty."));
        }

        final int currentIndex = partitionAssignmentIndex.accumulateAndGet(1,
            (current, added) -> {
                try {
                    return Math.addExact(current, added);
                } catch (ArithmeticException e) {
                    LOGGER.info("Overflowed incrementing index. Rolling over.", e);

                    return STARTING_INDEX + added;
                }
            });

        return partitions[(currentIndex % partitions.length)];
    }

    /**
     * Assigns a partition using a hash-based approach based on the provided {@code partitionKey}.
     *
     * @param partitionKey The partition key.
     * @param partitions The set of available partitions.
     *
     * @return The zero-based index of the selected partition from the available set.
     */
    String assignForPartitionKey(String partitionKey, String[] partitions) {
        final short hashValue = generateHashCode(partitionKey);
        final int index = Math.abs(hashValue % partitions.length);

        return partitions[index];
    }

    /**
     * Generates a hashcode for the partition key using Jenkins' lookup3 algorithm.
     *
     * This implementation is a direct port of the Event Hubs service code; it is intended to match the gateway hashing
     * algorithm as closely as possible and should not be adjusted without careful consideration.
     *
     * @param partitionKey The partition key.
     *
     * @return The generated hash code.
     */
    static short generateHashCode(String partitionKey) {
        if (partitionKey == null) {
            return 0;
        }

        final byte[] bytes = partitionKey.getBytes(StandardCharsets.UTF_8);

        final Hashed hashed = computeHash(bytes, 0, 0);
        final int i = hashed.getHash1() ^ hashed.getHash2();

        return Integer.valueOf(i).shortValue();
    }

    /**
     * Computes a hash value using Jenkins' lookup3 algorithm.
     *
     * This implementation is a direct port of the Event Hubs service code; it is intended to match the gateway hashing
     * algorithm as closely as possible and should not be adjusted without careful consideration.
     *
     * @param data The data to base the hash on.
     * @param seed1 Seed value for the first hash.
     * @param seed2 Seed value for the second hash.
     *
     * @return An object containing the computed hash for {@code seed1} and {@code seed2}.
     */
    private static Hashed computeHash(byte[] data, int seed1, int seed2) {
        int a;
        int b;
        int c;

        a = b = c = (0xdeadbeef + data.length + seed1);
        c += seed2;

        final ByteBuffer buffer = ByteBuffer.allocate(data.length)
            .put(data)
            .flip()
            .order(ByteOrder.LITTLE_ENDIAN)
            .asReadOnlyBuffer();

        int index = 0;
        int size = data.length;

        while (size > 12) {
            a += buffer.getInt(index);
            b += buffer.getInt(index + 4);
            c += buffer.getInt(index + 8);

            a -= c;
            a ^= (c << 4) | (c >>> 28);
            c += b;

            b -= a;
            b ^= (a << 6) | (a >>> 26);
            a += c;

            c -= b;
            c ^= (b << 8) | (b >>> 24);
            b += a;

            a -= c;
            a ^= (c << 16) | (c >>> 16);
            c += b;

            b -= a;
            b ^= (a << 19) | (a >>> 13);
            a += c;

            c -= b;
            c ^= (b << 4) | (b >>> 28);
            b += a;

            index += 12;
            size -= 12;
        }

        switch (size) {
            case 12:
                a += buffer.getInt(index);
                b += buffer.getInt(index + 4);
                c += buffer.getInt(index + 8);
                break;
            case 11:
                c += data[index + 10] << 16;
            case 10:
                c += data[index + 9] << 8;
            case 9:
                c += data[index + 8];
            case 8:
                b += buffer.getInt(index + 4);
                a += buffer.getInt(index);
                break;
            case 7:
                b += data[index + 6] << 16;
            case 6:
                b += data[index + 5] << 8;
            case 5:
                b += data[index + 4];
            case 4:
                a += buffer.getInt(index);
                break;
            case 3:
                a += data[index + 2] << 16;
            case 2:
                a += data[index + 1] << 8;
            case 1:
                a += data[index];
                break;
            case 0:
                return new Hashed(c, b);
        }

        c ^= b;
        c -= (b << 14) | (b >>> 18);

        a ^= c;
        a -= (c << 11) | (c >>> 21);

        b ^= a;
        b -= (a << 25) | (a >>> 7);

        c ^= b;
        c -= (b << 16) | (b >>> 16);

        a ^= c;
        a -= (c << 4) | (c >>> 28);

        b ^= a;
        b -= (a << 14) | (a >>> 18);

        c ^= b;
        c -= (b << 24) | (b >>> 8);

        return new Hashed(c, b);
    }

    /**
     * Class that holds the hash values from the lookup algorithm.
     */
    private static class Hashed {
        private final int hash1;
        private final int hash2;

        Hashed(int hash1, int hash2) {
            this.hash1 = hash1;
            this.hash2 = hash2;
        }

        public int getHash1() {
            return hash1;
        }

        public int getHash2() {
            return hash2;
        }
    }
}
