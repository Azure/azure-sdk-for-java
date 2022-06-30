// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link PartitionResolver}.
 */
public class PartitionResolverTests {

    public static Stream<List<String>> distributesRoundRobinFairly() {
        final ArrayList<List<String>> arguments = new ArrayList<>();

        for (var index = 1; index < 8; ++index) {
            final List<String> partitions = IntStream.range(0, index).mapToObj(i -> String.valueOf(i))
                .collect(Collectors.toList());

            arguments.add(partitions);
        }

        // Build sets for 16, 32, and 2000 partitions for more extreme cases.

        for (int count : new int[]{16, 32, 2000}) {
            final List<String> partitions = IntStream.range(0, count).mapToObj(i -> String.valueOf(i))
                .collect(Collectors.toList());

            arguments.add(partitions);
        }

        return arguments.stream();
    }

    @ParameterizedTest
    @MethodSource
    public void distributesRoundRobinFairly(List<String> partitions) {
        // Arrange
        final PartitionResolver resolver = new PartitionResolver();
        final String[] available = partitions.toArray(new String[0]);

        // Act & Assert
        for (int i = 0; i < 100; i++) {
            final String expected = available[i % available.length];
            final String actual = resolver.assignRoundRobin(available);

            assertEquals(expected, actual, "The assignment was unexpected for index: " + i);
        }
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

    @ParameterizedTest(name = "{index} Partition Key: {0}")
    @MethodSource
    public void partitionHashTestCases(String partitionKey, short expectedHash) {
        // Act
        short actual = PartitionResolver.generateHashCode(partitionKey);

        assertEquals(expectedHash, actual);
    }
}
