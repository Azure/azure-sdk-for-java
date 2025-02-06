// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.test.utils.TestUtils;
import com.azure.storage.common.implementation.mocking.MockReadBehavior;
import com.azure.storage.common.implementation.mocking.MockWriteBehavior;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("resource")
public class StorageSeekableByteChannelTests {
    private static byte[] getRandomData(int size) {
        byte[] result = new byte[size];
        ThreadLocalRandom.current().nextBytes(result);
        return result;
    }

    @ParameterizedTest
    @MethodSource("readSequentiallySupplier")
    public void readSequentially(int dataSize, int chunkSize, int readLength) throws IOException {
        byte[] data = getRandomData(dataSize);
        StorageSeekableByteChannel.ReadBehavior behavior = new MockReadBehavior(data.length, (dst, sourceOffset) -> {
            assert sourceOffset >= 0;
            if (sourceOffset >= data.length) {
                return -1;
            }

            int read = Math.min(dst.remaining(), data.length - sourceOffset.intValue());
            dst.put(data, sourceOffset.intValue(), read);
            return read;
        });

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(chunkSize, behavior, 0L);
        ByteArrayOutputStream dest = new ByteArrayOutputStream();

        byte[] temp = new byte[readLength];
        int read;
        while ((read = channel.read(ByteBuffer.wrap(temp))) != -1) {
            dest.write(temp, 0, read);
        }

        assertEquals(data.length, channel.size());
        TestUtils.assertArraysEqual(data, dest.toByteArray());
    }

    private static Stream<Arguments> readSequentiallySupplier() {
        // dataSize, chunkSize, readLength
        return Stream.of(Arguments.of(8 * Constants.KB, Constants.KB, Constants.KB), // easy path
            Arguments.of(8 * Constants.KB, Constants.KB, 100),          // reads unaligned (smaller)
            Arguments.of(8 * Constants.KB, Constants.KB, 1500),         // reads unaligned (larger)
            Arguments.of(8 * Constants.KB, 1000, 1000),                 // buffer unaligned
            Arguments.of(100, Constants.KB, Constants.KB)               // buffer larger than data
        );
    }

    @Test
    public void seekWithinBuffer() throws IOException {
        int bufferLength = 4 * Constants.KB;
        byte[] data = getRandomData(2 * bufferLength);

        AtomicBoolean firstRead = new AtomicBoolean(true);
        StorageSeekableByteChannel.ReadBehavior behavior = new MockReadBehavior(data.length, (dst, sourceOffset) -> {
            // expect only one read call of any parameter set
            if (!firstRead.compareAndSet(true, false)) {
                throw new IllegalStateException("read called more than once");
            }

            assert sourceOffset >= 0; // test is designed to have its buffer at position 0, ensure we do this
            int read = Math.min(dst.remaining(), data.length - sourceOffset.intValue());
            dst.put(data, sourceOffset.intValue(), read);
            return read;
        });

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer temp = ByteBuffer.allocate(100);
        for (long seekIndex : Arrays.asList(0,                  // initial read at 0 to control buffer location
            bufferLength / 2,   // seek somewhere in middle
            bufferLength / 2,   // seek back to that same spot
            0,                  // seek back to beginning
            bufferLength - 100, // seek to last temp.length chunk of the internal buffer
            bufferLength - 1    // seek to last byte of the internal buffer
        )) {
            temp.clear();
            channel.position(seekIndex);
            assertEquals(Math.min(temp.limit(), bufferLength - seekIndex), channel.read(temp));
        }
    }

    @Test
    public void seekToNewBuffer() throws IOException {
        int bufferLength = 5;
        byte[] data = getRandomData(Constants.KB);
        // each index should be outside the previous buffer
        long[] seekIndices = new long[] { 20, 500, 1, 6, 5 };

        Set<Long> seekIndexMap = new HashSet<>();
        StorageSeekableByteChannel.ReadBehavior behavior = new MockReadBehavior(data.length, (dst, sourceOffset) -> {
            // expect a single buffer refill at each seek index
            if (seekIndexMap.contains(sourceOffset)) {
                throw new IllegalStateException("read called more than once at index " + sourceOffset);
            } else {
                seekIndexMap.add(sourceOffset);
            }

            dst.put(data, sourceOffset.intValue(), bufferLength);
            return bufferLength;
        });

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer temp = ByteBuffer.allocate(bufferLength * 2);
        for (long seekIndex : seekIndices) {
            temp.clear();
            channel.position(seekIndex);
            channel.read(temp);
        }
    }

    @Test
    public void seekResultsInCorrectRead() throws IOException {
        int bufferLength = 5;
        byte[] data = getRandomData(Constants.KB);
        long seekIndex = 345;

        AtomicBoolean firstRead = new AtomicBoolean(true);
        StorageSeekableByteChannel.ReadBehavior behavior = new MockReadBehavior(data.length, (dst, sourceOffset) -> {
            if (sourceOffset != seekIndex) {
                throw new IllegalStateException("read called at unexpected index " + sourceOffset);
            }

            // expect only one read call of any parameter set
            if (!firstRead.compareAndSet(true, false)) {
                throw new IllegalStateException("read called more than once");
            }

            dst.put(data, sourceOffset.intValue(), bufferLength);
            return bufferLength;
        });

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer result = ByteBuffer.allocate(bufferLength);
        channel.position(seekIndex);
        channel.read(result);

        TestUtils.assertArraysEqual(result.array(), 0, data, (int) seekIndex, bufferLength);
    }

    @ParameterizedTest
    @MethodSource("readPastResourceEndSupplier")
    public void readPastResourceEnd(int resourceSize, int offset, int expectedReadLength) throws IOException {
        StorageSeekableByteChannel.ReadBehavior behavior = new MockReadBehavior(resourceSize, (dst, sourceOffset) -> {
            int toRead = Math.min(dst.remaining(), resourceSize - sourceOffset.intValue());
            if (toRead > 0) {
                dst.put(new byte[toRead]);
                return toRead;
            } else {
                return -1;
            }
        });

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(resourceSize, behavior, 0L);

        // read past resource end, graceful operation
        channel.position(offset);
        int read = assertDoesNotThrow(() -> channel.read(ByteBuffer.allocate(resourceSize)));
        assertEquals(expectedReadLength, read);
        assertEquals(resourceSize, channel.size());
        assertEquals(resourceSize, channel.position());
    }

    private static Stream<Arguments> readPastResourceEndSupplier() {
        // resourceSize, offset, expectedReadLength
        return Stream.of(Arguments.of(Constants.KB, 500, Constants.KB - 500), // overlap on end of resource
            Arguments.of(Constants.KB, Constants.KB, -1),         // starts at end of resource
            Arguments.of(Constants.KB, Constants.KB + 20, -1)     // completely past resource
        );
    }

    @ParameterizedTest
    @MethodSource("writeSupplier")
    public void write(int dataSize, int chunkSize, int writeSize) throws IOException {
        byte[] source = getRandomData(dataSize);
        byte[] dest = new byte[dataSize];
        StorageSeekableByteChannel.WriteBehavior behavior = new MockWriteBehavior() {
            @Override
            public void write(ByteBuffer src, long destOffset) {
                src.get(dest, (int) destOffset, src.remaining());
            }
        };

        try (StorageSeekableByteChannel channel = new StorageSeekableByteChannel(chunkSize, behavior, 0L)) {
            for (int i = 0, bytesLastWritten; i < source.length; i += bytesLastWritten) {
                bytesLastWritten = channel.write(ByteBuffer.wrap(source, i, Math.min(writeSize, source.length - i)));
            }
        }

        TestUtils.assertArraysEqual(source, dest);
    }

    private static Stream<Arguments> writeSupplier() {
        // dataSize, chunkSize, writeSize
        return Stream.of(Arguments.of(8 * Constants.KB, Constants.KB, Constants.KB), // easy path
            Arguments.of(8 * Constants.KB, Constants.KB, 100),          // writes unaligned (smaller)
            Arguments.of(8 * Constants.KB, Constants.KB, 1500),         // writes unaligned (larger)
            Arguments.of(8 * Constants.KB, 1000, 1000),                 // buffer unaligned
            Arguments.of(100, Constants.KB, Constants.KB)               // buffer larger than data
        );
    }

    @Test
    public void writeModeSeek() throws IOException {
        int bufferSize = Constants.KB;
        AtomicInteger wroteBufferSizeMinus5 = new AtomicInteger();
        AtomicInteger wroteBufferSize = new AtomicInteger();
        StorageSeekableByteChannel.WriteBehavior writeBehavior = new MockWriteBehavior() {
            @Override
            public void write(ByteBuffer src, long destOffset) {
                if (src.limit() == bufferSize - 5 && destOffset == 0) {
                    wroteBufferSizeMinus5.incrementAndGet();
                } else if (src.limit() == bufferSize && destOffset == 2048) {
                    wroteBufferSize.incrementAndGet();
                } else {
                    throw new IllegalStateException("write called with unexpected buffer size " + src.limit());
                }
            }
        };

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferSize, writeBehavior, 0L);

        // Write partial data then seek
        channel.write(ByteBuffer.wrap(getRandomData(bufferSize - 5)));
        channel.position(2048);

        // behavior write correctly called once
        assertEquals(1, wroteBufferSizeMinus5.get());

        // Fill entire buffer
        channel.write(ByteBuffer.wrap(getRandomData(bufferSize)));
        channel.position(0);

        // behavior write correctly called once
        assertEquals(1, wroteBufferSize.get());

        // No data before seek
        channel.position(1000);
    }

    @Test
    public void writeModeSeekObeysBehavior() throws IOException {
        // Channel that allows you to seek in 512 byte increments
        StorageSeekableByteChannel.WriteBehavior writeBehavior = new MockWriteBehavior() {
            @Override
            public void assertCanSeek(long position) {
                if (position % 512 != 0) {
                    throw new UnsupportedOperationException("position must be a multiple of 512, was " + position);
                }
            }
        };

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L);

        // Seek to 0
        assertDoesNotThrow(() -> channel.position(0));
        assertEquals(0, channel.position());

        // Seek to 512
        assertDoesNotThrow(() -> channel.position(512));
        assertEquals(512, channel.position());

        // Seek to 5 gigs
        assertDoesNotThrow(() -> channel.position(5L * Constants.GB));
        assertEquals(5L * Constants.GB, channel.position());

        // Seek is invalid
        assertThrows(UnsupportedOperationException.class, () -> channel.position(100));
    }

    @Test
    public void failedBehaviorWriteCanResumeWhereLeftOff() throws IOException {
        // Channel with behavior that throws first write attempt
        ByteBuffer testWriteDest = ByteBuffer.allocate(Constants.KB);
        AtomicBoolean firstWrite = new AtomicBoolean(true);
        StorageSeekableByteChannel.WriteBehavior writeBehavior = new MockWriteBehavior() {
            @Override
            public void write(ByteBuffer src, long destOffset) {
                if (destOffset != 0) {
                    throw new IllegalStateException("destOffset should never be non-zero, was " + destOffset);
                }

                if (firstWrite.getAndSet(false)) {
                    throw new RuntimeException("mock behavior interrupt");
                } else {
                    testWriteDest.put(src);
                }
            }
        };

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L);

        // first attempt, failure; channel state unchanged
        ByteBuffer data1 = ByteBuffer.wrap(getRandomData(Constants.KB));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> channel.write(data1));
        assertEquals("mock behavior interrupt", ex.getMessage());
        assertEquals(0, channel.position());

        // second attempt, success; channel state updated, data correctly written
        ByteBuffer data2 = ByteBuffer.wrap(getRandomData(Constants.KB));
        int written = assertDoesNotThrow(() -> channel.write(data2));

        assertEquals(Constants.KB, data2.position());
        assertEquals(Constants.KB, written);
        assertEquals(Constants.KB, channel.position());
        TestUtils.assertArraysEqual(data2.array(), testWriteDest.array());
    }

    @Test
    public void writeModeCannotRead() {
        assertThrows(NonReadableChannelException.class,
            () -> new StorageSeekableByteChannel(Constants.KB, new MockWriteBehavior(), 0L)
                .read(ByteBuffer.allocate(Constants.KB)));
    }

    @Test
    public void readModeCannotWrite() {
        assertThrows(NonWritableChannelException.class,
            () -> new StorageSeekableByteChannel(Constants.KB, new MockReadBehavior(), 0L)
                .write(ByteBuffer.allocate(Constants.KB)));
    }
}
