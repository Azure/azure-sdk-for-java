// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link PartitionResolver}. Taken from .NET's to confirm cross-language compatibility.
 *
 * @see <a href="https://github.com/Azure/azure-sdk-for-net/blob/main/sdk/eventhub/Azure.Messaging.EventHubs/tests/Core/PartitionResolverTests.cs">PartitionResolverTests.cs</a>
 */
public class PartitionResolverTests {
    public static Stream<List<String>> partitionSetTestCases() {
        final ArrayList<List<String>> arguments = new ArrayList<>();

        for (int index = 1; index < 8; ++index) {
            final List<String> partitions = IntStream.range(0, index).mapToObj(String::valueOf)
                .collect(Collectors.toList());

            arguments.add(partitions);
        }

        // Build sets for 16, 32, and 2000 partitions for more extreme cases.

        for (int count : new int[]{16, 32, 2000}) {
            final List<String> partitions = IntStream.range(0, count).mapToObj(String::valueOf)
                .collect(Collectors.toList());

            arguments.add(partitions);
        }

        return arguments.stream();
    }

    public static Stream<Arguments> partitionHashTestCases() {

        final ArrayList<Arguments> arguments = new ArrayList<>();

        arguments.add(Arguments.of("7", (short) -15263));
        arguments.add(Arguments.of("131", (short) 30562));
        arguments.add(Arguments.of("7149583486996073602", (short) 12977));
        arguments.add(Arguments.of("FWfAT", (short) -22341));
        arguments.add(Arguments.of("sOdeEAsyQoEuEFPGerWO", (short) -6503));
        arguments.add(Arguments.of(
            "FAyAIctPeCgmiwLKbJcyswoHglHVjQdvtBowLACDNORsYvOcLddNJYDmhAVkbyLOrHTKLneMNcbgWVlasVywOByANjs",
            (short) 5226));
        arguments.add(Arguments.of("1XYM6!(7(lF5wq4k4m*e$Nc!1ezLJv*1YK1Y-C^*&B$O)lq^iUkG(TNzXG;Zi#z2Og*Qq0#^*k)"
            + ":vXh$3,C7We7%W0meJ;b3,rQCg^J;^twXgs5E$$hWKxqp", (short) 23950));
        arguments.add(Arguments.of("E(x;RRIaQcJs*P;D&jTPau-4K04oqr:lF6Z):ERpo&;"
            + "9040qyV@G1_c9mgOs-8_8/10Fwa-7b7-yP!T-!IH&968)FWuI;(^g$2fN;)HJ^^yTn:", (short) -29304));
        arguments.add(Arguments.of("!c*_!I@1^c", (short) 15372));
        arguments.add(Arguments.of("p4*!jioeO/z-!-;w:dh", (short) -3104));
        arguments.add(Arguments.of("$0cb", (short) 26269));
        arguments.add(Arguments.of("-4189260826195535198", (short) 453));

        return arguments.stream();
    }

    @ParameterizedTest
    @MethodSource("partitionSetTestCases")
    public void distributesRoundRobinFairly(List<String> partitionsList) {
        // Arrange
        final String[] partitions = partitionsList.toArray(new String[0]);
        final PartitionResolver resolver = new PartitionResolver();

        // Act & Assert
        for (int i = 0; i < 100; i++) {
            final String expected = partitions[i % partitions.length];
            final String actual = resolver.assignRoundRobin(partitions);

            assertEquals(expected, actual, "The assignment was unexpected for index: " + i);
        }
    }

    @ParameterizedTest
    @MethodSource("partitionSetTestCases")
    public void distributesRoundRobinFairlyConcurrent(List<String> partitionsList) {
        // Arrange
        final String[] partitions = partitionsList.toArray(new String[0]);

        final int concurrentCount = 4;
        final int assignmentsPerPartition = 20;
        final int expectedAssignmentCount = (concurrentCount * assignmentsPerPartition);
        final int iterationCount = partitions.length * assignmentsPerPartition;

        final PartitionResolver resolver = new PartitionResolver();
        final ArrayList<String> assigned = new ArrayList<>();
        final ArrayList<Mono<Void>> activeTasks = new ArrayList<>();

        // Create a function that assigns partitions in a loop and track them.
        Mono<Void> roundRobin = Mono.fromRunnable(() -> {
            for (int index = 0; index < iterationCount; index++) {
                assigned.add(resolver.assignRoundRobin(partitions));
            }
        });

        // Create concurrent round-robin tasks.
        IntStream.range(0, concurrentCount).forEach(index -> activeTasks.add(roundRobin));

        // Assert
        // Wait for them all to complete.
        StepVerifier.create(Mono.when(activeTasks))
            .verifyComplete();

        // Assert

        // When grouped, the count of each partition should equal the iteration count for each
        // concurrent invocation.
        final HashMap<String, Integer> partitionAssignments = assigned.stream().collect(HashMap::new,
            (map, value) -> map.compute(value, (key, existingValue) -> existingValue == null ? 1 : (existingValue + 1)),
            (map1, map2) -> {
                map2.forEach((key, value) -> {
                    map1.compute(key, (existingKey, existingValue) -> {
                        // It did not exist in map1, so we use the total from map2. Otherwise, combine the two.
                        return existingValue == null ? value : (existingValue + value);
                    });
                });
            });

        // Verify that each assignment is for a valid partition and has the expected distribution.
        partitionAssignments.forEach((partitionId, numberAssigned) -> {
            assertEquals(expectedAssignmentCount, numberAssigned,
                String.format("The count for key: [%s] should match the total iterations.", partitionId));
        });

        // Verify that all partitions were assigned.
        for (String id : partitions) {
            assertTrue(partitionAssignments.containsKey(id), "Partition " + id + " should have had an assignment.");
        }
    }

    /**
     * Verifies that the same partition key is assigned to the same partition id.
     */
    @ParameterizedTest
    @MethodSource("partitionSetTestCases")
    public void partitionKeyAssignmentIsStable(List<String> partitionsList) {
        // Arrange
        final String[] partitions = partitionsList.toArray(new String[0]);

        final int iterationCount = 25;
        final String key = "this-is-a-key-1";
        final PartitionResolver resolver = new PartitionResolver();
        final String expected = resolver.assignForPartitionKey(key, partitions);

        // Act & Assert
        IntStream.range(0, iterationCount).forEach(index -> {
            final String actual = resolver.assignForPartitionKey(key, partitions);
            assertEquals(expected, actual, "The assignment for iteration: [" + index + "] was unstable.");
        });
    }

    @ParameterizedTest
    @MethodSource("partitionSetTestCases")
    public void partitionKeyAssignmentDistributesKeysToDifferentPartitions(List<String> partitionsList) {
        // Arrange
        final String[] partitions = partitionsList.toArray(new String[0]);

        final int keyLength = 20;
        final int requiredAssignments = (int) Math.floor(partitions.length * 0.67);
        final HashSet<String> assignedHash = new HashSet<>();
        final PartitionResolver resolver = new PartitionResolver();

        // Create the random number generator using a constant seed; this is
        // intended to allow for randomization but will also keep a consistent
        // pattern each time the tests are run.
        final Random random = new Random(412);

        for (int index = 0; index < Integer.MAX_VALUE; index++) {
            final StringBuilder keyBuilder = new StringBuilder(keyLength);

            for (int charIndex = 0; charIndex < keyLength; charIndex++) {
                keyBuilder.append((char) random.nextInt(256));
            }

            final String key = keyBuilder.toString();
            final String partition = resolver.assignForPartitionKey(key, partitions);

            assignedHash.add(partition);

            // If keys were distributed to more than one partition and the minimum number of
            // iterations was satisfied, break the loop.

            if (assignedHash.size() > requiredAssignments) {
                break;
            }
        }

        assertTrue(assignedHash.size() >= requiredAssignments, String.format(
            "Partition keys should have had some level of distribution among partitions. Assigned: %d. Required: %d",
            assignedHash.size(), requiredAssignments));
    }

    /**
     * Verifies functionality of hash code generation for the {@link PartitionResolver}.
     *
     * @param partitionKey Partition key
     * @param expectedHash Expected value
     */
    @ParameterizedTest(name = "{index} Partition Key: {0}")
    @MethodSource("partitionHashTestCases")
    public void hashCodeAssignmentIsStable(String partitionKey, short expectedHash) {
        // Act
        short actual = PartitionResolver.generateHashCode(partitionKey);

        // Assert
        assertEquals(expectedHash, actual);
    }
}
