// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.test.utils.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BufferStagingAreaTests {
    static Tuple2<ByteBuffer[], ByteBuffer[]> generateData(int numBuffs, int minBuffSize, int maxBuffSize) {
        // Generate random sizes between minBuffSize and maxBuffSize for the buffers
        int totalSize = 0;
        int[] sizes = new int[numBuffs];
        for (int i = 0; i < numBuffs; i++) {
            int size = minBuffSize;
            if (maxBuffSize != minBuffSize) {
                size += ThreadLocalRandom.current().nextInt(maxBuffSize - minBuffSize);
            }
            sizes[i] = size;
            totalSize += size;
        }
        // Generate random data
        byte[] bytes = new byte[totalSize];
        ThreadLocalRandom.current().nextBytes(bytes);

        // Partition the data based off random sizes
        ByteBuffer[] data = new ByteBuffer[numBuffs];
        int begin = 0;
        for (int i = 0; i < numBuffs; i++) {
            int end = begin + sizes[i];
            data[i] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, begin, end));
            begin += sizes[i];
        }

        // Expected data is partitioned by maxBuffSize
        int expectedNumBuffs = (int) Math.ceil((double) totalSize / maxBuffSize);
        ByteBuffer[] expectedData = new ByteBuffer[expectedNumBuffs];
        for (int i = 0; i < expectedNumBuffs; i++) {
            begin = i * maxBuffSize;
            int end = Math.min((i + 1) * maxBuffSize, totalSize);
            expectedData[i] = ByteBuffer.wrap(Arrays.copyOfRange(bytes, begin, end));
        }

        return Tuples.of(data, expectedData);
    }

    static byte[] byteBufferListToByteArray(List<ByteBuffer> buffers) {
        int totalSize = 0;
        for (ByteBuffer b: buffers) {
            totalSize += b.remaining();
        }

        byte[] bytes = new byte[totalSize];
        int begin = 0;
        for (ByteBuffer b: buffers) {
            System.arraycopy(b.array(), b.position(), bytes, begin, b.remaining());
            begin += b.remaining();
        }

        return bytes;
    }

    @ParameterizedTest
    @MethodSource("bufferStagingAreaSupplier")
    public void bufferStagingArea(int numBuffs, int minBuffSize, int maxBuffSize) {
        BufferStagingArea stagingArea = new BufferStagingArea(maxBuffSize, maxBuffSize);
        Tuple2<ByteBuffer[], ByteBuffer[]> generatedData = generateData(numBuffs, minBuffSize, maxBuffSize);
        Flux<ByteBuffer> data = Flux.fromArray(generatedData.getT1());
        ByteBuffer[] expectedData = generatedData.getT2();

        List<List<ByteBuffer>> recoveredData = data.flatMapSequential(stagingArea::write, 1)
            .concatWith(Flux.defer(stagingArea::flush)).flatMap(aggregator -> aggregator.asFlux().collectList())
            .collectList().block();

        assertEquals(expectedData.length, recoveredData.size());
        for (int i = 0; i < expectedData.length; i++) {
            TestUtils.assertArraysEqual(expectedData[i].array(), byteBufferListToByteArray(recoveredData.get(i)));
        }
    }

    private static Stream<Arguments> bufferStagingAreaSupplier() {
        return Stream.of(
            // numBuffs | minBuffSize       | maxBuffSize       || _
            Arguments.of(10, 1000, 1000), // These test no variation in buffSize.
            Arguments.of(100, 1000, 1000), // _
            Arguments.of(1000, 1000, 1000), // _
            Arguments.of(10000, 1000, 1000), // _
            Arguments.of(10000, 1, 1000), // These test variation in buffSize.
            Arguments.of(100, 1, Constants.MB * 4), // _
            Arguments.of(100, Constants.MB * 4, Constants.MB * 8) // _
        );
    }

    @ParameterizedTest
    @MethodSource("bufferStagingAreaWithSmallerBufferSupplier")
    public void bufferStagingAreaWithSmallerBuffer(int stagingSize, int dataSize) {
        // Generate random data
        byte[] bytes = new byte[dataSize];
        ThreadLocalRandom.current().nextBytes(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Flux<ByteBuffer> byteBufferFlux = Flux.just(buffer);

        BufferStagingArea stagingArea = new BufferStagingArea(stagingSize, stagingSize);
        List<ByteBuffer> collectedBuffers = byteBufferFlux.flatMapSequential(stagingArea::write, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            .flatMap(BufferAggregator::asFlux)
            .collectList().block();

        assertNotNull(collectedBuffers);
        assertFalse(collectedBuffers.isEmpty());

        // Convert list of ByteBuffers to a single byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int sizeRemaining = dataSize % stagingSize;

        for (int i = 0; i < collectedBuffers.size() - 1; i++) {
            ByteBuffer bb = collectedBuffers.get(i);
            byte[] array = new byte[bb.remaining()];
            assertEquals(stagingSize, array.length);
            bb.get(array);
            try {
                outputStream.write(array);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Handle the last buffer separately
        ByteBuffer lastBuffer = collectedBuffers.get(collectedBuffers.size() - 1);
        byte[] lastArray = new byte[lastBuffer.remaining()];
        lastBuffer.get(lastArray);
        if (sizeRemaining != 0) {
            assertEquals(sizeRemaining, lastArray.length, "The last buffer's size should match the remaining size.");
        } else {
            assertEquals(stagingSize, lastArray.length, "The last buffer should match the staging size if no remainder.");
        }
        try {
            outputStream.write(lastArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] reconstructedData = outputStream.toByteArray();

        // Assert that the original data and the reconstructed data are the same
        assertArrayEquals(bytes, reconstructedData);
    }

    private static Stream<Arguments> bufferStagingAreaWithSmallerBufferSupplier() {
        return Stream.of(
            Arguments.of(4 * Constants.KB, 4 * Constants.MB),
            Arguments.of(Constants.KB, 4 * Constants.MB),
            Arguments.of(25, Constants.KB),
            Arguments.of(2 * Constants.KB, 4 * Constants.MB),
            Arguments.of(10 * Constants.KB, 4 * Constants.MB)
        );
    }
}
