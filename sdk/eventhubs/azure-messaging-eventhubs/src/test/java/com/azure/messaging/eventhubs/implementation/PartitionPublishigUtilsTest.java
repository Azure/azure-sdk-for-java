// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartitionPublishigUtilsTest {
    @ParameterizedTest
    @MethodSource("increaseNumberParameterProvider")
    void increaseNumber(int value, int delta, int expected) {
        assertEquals(PartitionPublishingUtils.incrementSequenceNumber(value, delta), expected);
    }

    static Stream<Arguments> increaseNumberParameterProvider() {
        return Stream.of(
            Arguments.arguments(0, 10, 10),
            Arguments.arguments(Integer.MAX_VALUE, 10, 9),
            Arguments.arguments(Integer.MAX_VALUE - 3, 10, 6),
            Arguments.arguments(0, 0, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("increaseNumberByOneParameterProvider")
    void increaseNumberByOne(int value, int expected) {
        assertEquals(PartitionPublishingUtils.incrementSequenceNumber(value), expected);
    }

    static Stream<Arguments> increaseNumberByOneParameterProvider() {
        return Stream.of(
            Arguments.arguments(0, 1),
            Arguments.arguments(Integer.MAX_VALUE, 0),
            Arguments.arguments(Integer.MAX_VALUE - 1, Integer.MAX_VALUE)
        );
    }
}
