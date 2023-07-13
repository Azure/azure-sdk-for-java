// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BufferStagingAreaTests {

    private static Tuple2<ByteBuffer[], ByteBuffer[]> generateData(int numBuffs, int minBuffSize, int maxBuffSize) {
        Random random = new Random();
        // Generate random sizes between minBuffSize and maxBuffSize for the buffers
        int totalSize = 0;
        int[] sizes = new int[numBuffs];
        for (int i = 0; i < numBuffs; i++) {
            int size = minBuffSize;
            if (maxBuffSize != minBuffSize) {
                size += random.nextInt(maxBuffSize - minBuffSize);
            }
            sizes[i] = size;
            totalSize += size;
        }
        // Generate random data
        byte[] bytes = new byte[totalSize];
        random.nextBytes(bytes);

        // Partition the data based off random sizes
        ByteBuffer[] data = new ByteBuffer[numBuffs];
        int begin = 0;
        for (int i = 0; i < numBuffs; i++) {
            int end = begin + sizes[i];
            data[i] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, begin, end));
            begin += sizes[i];
        }

        // Expected data is partitioned by maxBuffSize
        int expectedDataLength = (totalSize + maxBuffSize - 1) / maxBuffSize; // Adjusted calculation
        ByteBuffer[] expectedData = new ByteBuffer[expectedDataLength];
        for (int i = 0; i < expectedDataLength; i++) {
            begin = i * maxBuffSize;
            int end = Math.min((i + 1) * maxBuffSize, totalSize);
            expectedData[i] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, begin, end));
        }
        return Tuples.of(data, expectedData);
    }

    private static byte[] byteBufferListToByteArray(List<ByteBuffer> buffers) {
        int totalSize = 0;
        for (ByteBuffer b : buffers) {
            totalSize += b.remaining();
        }
        byte[] bytes = new byte[totalSize];
        int begin = 0;
        for (ByteBuffer b : buffers) {
            System.arraycopy(b.array(), b.position(), bytes, begin, b.remaining());
            begin += b.remaining();
        }
        return bytes;
    }

    @Test
    public void bufferStagingArea() {
        int[] numBuffsArray = {10, 100, 1000, 10000, 10000, 100, 100};
        int[] minBuffSizeArray = {1000, 1000, 1000, 1000, 1, 1, Constants.MB * 4};
        int[] maxBuffSizeArray = {1000, 1000, 1000, 1000, 1000, Constants.MB * 4, Constants.MB * 8};
        int testCases = numBuffsArray.length;

        for (int i = 0; i < testCases; i++) {
            int numBuffs = numBuffsArray[i];
            int minBuffSize = minBuffSizeArray[i];
            int maxBuffSize = maxBuffSizeArray[i];

            BufferStagingArea stagingArea = new BufferStagingArea(maxBuffSize, maxBuffSize);
            Tuple2<ByteBuffer[], ByteBuffer[]> generatedData = generateData(numBuffs, minBuffSize, maxBuffSize);
            Flux<ByteBuffer> data = Flux.fromArray(generatedData.getT1());
            ByteBuffer[] expectedData = generatedData.getT2();

            List<List<ByteBuffer>> recoveredData = data.flatMapSequential(stagingArea::write, 1)
                .concatWith(Flux.defer(stagingArea::flush))
                .flatMap(
                    aggregator -> aggregator.asFlux().collectList())
                .collectList()
                .block();

            assertNotNull(recoveredData);
            assertNotNull(expectedData);
            assertEquals(expectedData.length, recoveredData.size());
            for (int j = 0; j < expectedData.length; j++) {
                assertArrayEquals(expectedData[j].array(), byteBufferListToByteArray(recoveredData.get(j)));
            }
        }
    }
}
