// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BufferAggregatorTests {

    @ParameterizedTest
    @MethodSource("readFirstNBytesSupplier")
    public void readFirstNBytes(List<byte[]> bufferArray) {
        BufferAggregator aggregator = new BufferAggregator(100);

        // Get original data
        Random rand = new Random(System.currentTimeMillis());
        byte[] data = new byte[100];
        rand.nextBytes(data);

        // Fill the individual buffers and add them to the aggregator
        int totalDataWritten = 0;
        for (byte[] bytes : bufferArray) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.put(ByteBuffer.wrap(data, totalDataWritten, bytes.length)).flip();
            aggregator.append(byteBuffer);

            totalDataWritten += bytes.length;
        }

        byte[] outArr = aggregator.getFirstNBytes(50);
        assertEquals(50, aggregator.length());
        assertEquals(ByteBuffer.wrap(data, 0, 50), ByteBuffer.wrap(outArr));

        outArr = aggregator.getFirstNBytes(50);
        assertEquals(0, aggregator.length());
        assertEquals(ByteBuffer.wrap(data, 50, 50), ByteBuffer.wrap(outArr));
    }

    private static Stream<List<byte[]>> readFirstNBytesSupplier() {
        return Stream.of(
            new ArrayList<byte[]>() {
                {
                    add(new byte[5]);
                    add(new byte[95]);
                }
            },
            new ArrayList<byte[]>() {
                {
                    add(new byte[10]);
                    add(new byte[10]);
                    add(new byte[10]);
                    add(new byte[70]);
                }
            },
            new ArrayList<byte[]>() {
                {
                    add(new byte[100]);
                }
            },
            new ArrayList<byte[]>() {
                {
                    add(new byte[50]);
                    add(new byte[50]);
                }
            },
            new ArrayList<byte[]>() {
                {
                    add(new byte[70]);
                    add(new byte[20]);
                    add(new byte[10]);
                }
            }
        );
    }
}
