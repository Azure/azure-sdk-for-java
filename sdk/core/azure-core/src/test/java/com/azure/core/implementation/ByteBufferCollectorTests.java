// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests {@link ByteBufferCollector}.
 */
public class ByteBufferCollectorTests {
    @Test
    public void throwsOnNegativeInitialSize() {
        assertThrows(IllegalArgumentException.class, () -> new ByteBufferCollector(-1));
    }

    @Test
    public void throwsIllegalStateExceptionOnBufferRequirementTooLarge() {
        /*
         * This assumption validates that the JVM running this test has a maximum heap size large enough for the test
         * to run without triggering an OutOfMemoryError.
         */
        assumeTrue(Runtime.getRuntime().maxMemory() > (Integer.MAX_VALUE * 1.5),
            "JVM doesn't have the requisite max heap size to support running this test.");

        ByteBuffer buffer = ByteBuffer.allocate((Integer.MAX_VALUE / 2) + 1);

        ByteBufferCollector collector = new ByteBufferCollector();
        collector.write(buffer.duplicate());

        assertThrows(IllegalStateException.class, () -> collector.write(buffer.duplicate()));
    }

    @ParameterizedTest
    @MethodSource("combineBuffersSupplier")
    public void combineBuffers(List<ByteBuffer> buffers, byte[] expected) {
        ByteBufferCollector collector = new ByteBufferCollector(expected.length);

        buffers.forEach(collector::write);

        assertArraysEqual(expected, collector.toByteArray());
    }

    private static Stream<Arguments> combineBuffersSupplier() {
        byte[] helloWorldBytes = "Hello world!".getBytes(StandardCharsets.UTF_8);
        ByteBuffer helloBuffer = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        ByteBuffer worldBuffer = ByteBuffer.wrap(" world!".getBytes(StandardCharsets.UTF_8));

        int helloWorldLength = helloWorldBytes.length;
        byte[] manyHelloWorldsBytes = new byte[helloWorldLength * 100];
        List<ByteBuffer> manyHelloWorldsByteBuffers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            System.arraycopy(helloWorldBytes, 0, manyHelloWorldsBytes, i * helloWorldLength, helloWorldLength);
            manyHelloWorldsByteBuffers.add(helloBuffer.duplicate());
            manyHelloWorldsByteBuffers.add(worldBuffer.duplicate());
        }

        return Stream.of(
            // All buffers are null.
            Arguments.of(Arrays.asList(null, null), new byte[0]),

            // All buffers are empty.
            Arguments.of(Arrays.asList(ByteBuffer.allocate(0), ByteBuffer.allocate(0)), new byte[0]),

            // Hello world buffers.
            Arguments.of(Arrays.asList(helloBuffer.duplicate(), worldBuffer.duplicate()), helloWorldBytes),

            // Many hello world buffers.
            Arguments.of(manyHelloWorldsByteBuffers, manyHelloWorldsBytes));
    }
}
