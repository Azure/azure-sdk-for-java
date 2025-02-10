// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.test.utils.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BufferAggregatorTests {
    @ParameterizedTest
    @MethodSource("readFirstNBytesSupplier")
    public void readFirstNBytes(List<byte[]> bufferArray) {
        BufferAggregator aggregator = new BufferAggregator(100);

        // Get original data
        byte[] data = new byte[100];
        ThreadLocalRandom.current().nextBytes(data);

        // Fill the individual buffers and add them to the aggregator
        int totalDataWritten = 0;
        for (byte[] bytes : bufferArray) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes).put(ByteBuffer.wrap(data, totalDataWritten, bytes.length));
            buffer.flip();
            aggregator.append(buffer);
            totalDataWritten += bytes.length;
        }

        byte[] outArr = aggregator.getFirstNBytes(50);
        assertEquals(50, outArr.length);
        TestUtils.assertArraysEqual(data, 0, outArr, 0, 50);

        outArr = aggregator.getFirstNBytes(50);
        assertEquals(50, outArr.length);
        TestUtils.assertArraysEqual(data, 50, outArr, 0, 50);
    }

    private static Stream<List<byte[]>> readFirstNBytesSupplier() {
        return Stream.of(Arrays.asList(new byte[5], new byte[95]),
            Arrays.asList(new byte[10], new byte[10], new byte[10], new byte[70]),
            Collections.singletonList(new byte[100]), Arrays.asList(new byte[50], new byte[50]),
            Arrays.asList(new byte[70], new byte[20], new byte[10]));
    }
}
