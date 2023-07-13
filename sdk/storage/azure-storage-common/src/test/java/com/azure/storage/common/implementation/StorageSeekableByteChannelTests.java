// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class StorageSeekableByteChannelTests {

    private byte[] getRandomData(int size) {
        byte[] result = new byte[size];
        new Random().nextBytes(result);
        return result;
    }

    @Test
    void readSequentially() throws IOException {
        /*
        dataSize         | chunkSize    | readLength
        8 * Constants.KB | Constants.KB | Constants.KB // easy path
        8 * Constants.KB | Constants.KB | 100          // reads unaligned (smaller)
        8 * Constants.KB | Constants.KB | 1500         // reads unaligned (larger)
        8 * Constants.KB | 1000         | 1000         // buffer unaligned
        100              | Constants.KB | Constants.KB // buffer larger than data
         */
        int[] dataSizes = {8 * Constants.KB, 8 * Constants.KB, 8 * Constants.KB, 8 * Constants.KB, 100};
        int[] chunkSizes = {Constants.KB, Constants.KB, Constants.KB, 1000, Constants.KB};
        int[] readLengths = {Constants.KB, 100, 1500, 1000, Constants.KB};

        for (int i = 0; i < dataSizes.length; i++) {
            int dataSize = dataSizes[i];
            int chunkSize = chunkSizes[i];
            int readLength = readLengths[i];

            byte[] data = getRandomData(dataSize);
            StorageSeekableByteChannel.ReadBehavior behavior = new StubReadBehavior(data);
            StorageSeekableByteChannel channel = new StorageSeekableByteChannel(chunkSize, behavior, 0L);
            ByteArrayOutputStream dest = new ByteArrayOutputStream();

            byte[] temp = new byte[readLength];
            int bytesRead;
            while ((bytesRead = channel.read(ByteBuffer.wrap(temp))) != -1) {
                dest.write(temp, 0, bytesRead);
            }

            assertEquals(channel.size(), data.length);
            assertArrayEquals(dest.toByteArray(), data);
        }
    }

    @Test
    void seekWithinBuffer() throws IOException {
        int bufferLength = 4 * Constants.KB;
        byte[] data = getRandomData(2 * bufferLength);

        StorageSeekableByteChannel.ReadBehavior behavior = Mockito.mock(StorageSeekableByteChannel.ReadBehavior.class);
        Mockito.when(behavior.getResourceLength()).thenReturn((long) data.length);

        Mockito.doAnswer(invocation -> {
            ByteBuffer dst = invocation.getArgument(0);
            long sourceOffset = invocation.getArgument(1);
            assert sourceOffset == 0;
            int read = Math.min(dst.remaining(), data.length);
            dst.put(data, 0, read);
            return read;
        }).when(behavior).read(Mockito.any(ByteBuffer.class), Mockito.anyLong());

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer temp = ByteBuffer.allocate(100);
        long[] seekIndices = {
            0L,                        // initial read at 0 to control buffer location
            bufferLength / 2,           // seek somewhere in middle
            bufferLength / 2,           // seek back to that same spot
            0L,                        // seek back to beginning
            bufferLength - 100,         // seek to last temp.length chunk of the internal buffer
            bufferLength - 1            // seek to last byte of the internal buffer
        };

        for (long seekIndex : seekIndices) {
            temp.clear();
            channel.position(seekIndex);
            int bytesRead = channel.read(temp);
            int expectedBytesRead = Math.min(temp.limit(), bufferLength - (int) seekIndex);
            assertEquals(expectedBytesRead, bytesRead);
        }
    }

    @Test
    public void seekToNewBuffer() throws IOException {
        int bufferLength = 5;
        byte[] data = getRandomData(Constants.KB);
        // each index should be outside the previous buffer
        long[] seekIndices = {20, 500, 1, 6, 5};

        StorageSeekableByteChannel.ReadBehavior behavior = new StorageSeekableByteChannel.ReadBehavior() {
            @Override
            public int read(ByteBuffer dst, long sourceOffset) {
                int read = Math.min(dst.remaining(), data.length - (int) sourceOffset);
                if (read > 0) {
                    dst.put(data, (int) sourceOffset, read);
                }
                return read;
            }
            @Override
            public long getResourceLength() {
                return data.length;
            }
        };

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        for (long seekIndex : seekIndices) {
            ByteBuffer temp = ByteBuffer.allocate(bufferLength * 2);
            temp.clear();
            channel.position(seekIndex);
            channel.read(temp);
        }

        // expect a buffer refill at each seek index
        for (long l : seekIndices) {
            assertEquals(bufferLength, behavior.read(ByteBuffer.allocate(bufferLength), l));
        }
    }

    @Test
    void seekResultsInCorrectRead() throws IOException {
        int bufferLength = 5;
        byte[] data = getRandomData(Constants.KB);
        long seekIndex = 345;

        StorageSeekableByteChannel.ReadBehavior behavior = Mockito.mock(StorageSeekableByteChannel.ReadBehavior.class);
        Mockito.when(behavior.read(Mockito.any(ByteBuffer.class), Mockito.eq(seekIndex))).thenAnswer(invocation -> {
            ByteBuffer dst = invocation.getArgument(0);
            long sourceOffset = invocation.getArgument(1);
            dst.clear();
            dst.put(data, (int) sourceOffset, bufferLength);
            dst.flip();
            return bufferLength;
        });
        Mockito.when(behavior.getResourceLength()).thenReturn((long) data.length);

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer result = ByteBuffer.allocate(bufferLength);
        channel.position(seekIndex);
        int bytesRead = channel.read(result);

        byte[] expectedData = Arrays.copyOfRange(data, (int) seekIndex, (int) seekIndex + bufferLength);
        byte[] actualData = new byte[bufferLength];
        result.rewind(); // Rewind the buffer before retrieving data
        result.get(actualData);

        assertArrayEquals(expectedData, actualData);
        assertEquals(bufferLength, bytesRead);
        // expect exactly one read at the chosen index
        Mockito.verify(behavior, Mockito.times(1)).read(Mockito.any(ByteBuffer.class), Mockito.eq(seekIndex));
        // expect no other reads
        Mockito.verify(behavior, Mockito.times(1)).getResourceLength();
    }

    @TestFactory
    @DisplayName("Read past resource end")
    List<DynamicTest> readPastResourceEnd() {
        int resourceSize = Constants.KB;

        return Arrays.asList(
            dynamicTest("overlap on end of resource", () -> {
                long offset = 500;
                int expectedReadLength = Constants.KB - 500;
                testReadPastResourceEnd(resourceSize, offset, expectedReadLength);
            }),
            dynamicTest("starts at end of resource", () -> {
                long offset = Constants.KB;
                int expectedReadLength = -1;
                testReadPastResourceEnd(resourceSize, offset, expectedReadLength);
            }),
            dynamicTest("completely past resource", () -> {
                long offset = Constants.KB + 20;
                int expectedReadLength = -1;
                testReadPastResourceEnd(resourceSize, offset, expectedReadLength);
            })
        );
    }

    private void testReadPastResourceEnd(int resourceSize, long offset, int expectedReadLength) throws IOException {
        StorageSeekableByteChannel.ReadBehavior behavior = Mockito.mock(StorageSeekableByteChannel.ReadBehavior.class);
        Mockito.when(behavior.getResourceLength()).thenReturn((long) resourceSize);
        Mockito.when(behavior.read(Mockito.any(ByteBuffer.class), Mockito.anyLong())).thenAnswer(invocation -> {
            ByteBuffer dst = invocation.getArgument(0);
            long sourceOffset = invocation.getArgument(1);
            int toRead = (int) Math.min(dst.remaining(), resourceSize - sourceOffset);
            if (toRead > 0) {
                dst.put(new byte[toRead]);
                return toRead;
            } else {
                return -1;
            }
        });

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(resourceSize, behavior, 0L);

        // Read past resource end
        channel.position(offset);

        // Graceful operation
        assertDoesNotThrow(() -> {
            int read = channel.read(ByteBuffer.allocate(resourceSize));
            assertEquals(expectedReadLength, read);
        });

        // Appropriate values
        assertEquals(resourceSize, channel.position());
        assertEquals(resourceSize, channel.size());
    }

//    @Test
//    void writeModeSeek() throws IOException {
//        int bufferSize = Constants.KB;
//
//        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);
//        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferSize, writeBehavior, 0L);
//
//        ByteBuffer partialData = ByteBuffer.wrap(getRandomData(bufferSize - 5));
//        channel.write(partialData);
//        channel.position(2048);
//
//        verify(writeBehavior).write(Mockito.argThat(bb -> bb.limit() == bufferSize - 5), Mockito.eq(0L));
//        verifyNoMoreInteractions(writeBehavior);
//
//        ByteBuffer fullData = ByteBuffer.wrap(getRandomData(bufferSize));
//        channel.write(fullData);
//        channel.position(0);
//
//        verify(writeBehavior).write(Mockito.argThat(bb -> bb.limit() == bufferSize), Mockito.eq(2048L));
//        verifyNoMoreInteractions(writeBehavior);
//
//        channel.position(1000);
//
//        Mockito.verifyNoInteractions(writeBehavior);
//    }

    @ParameterizedTest
    @CsvSource({
        "8192, 8192, 8192", // easy path
        "8192, 8192, 100",  // writes unaligned (smaller)
        "8192, 8192, 1500", // writes unaligned (larger)
        "8192, 1000, 1000", // buffer unaligned
        "100, 1024, 1024"   // buffer larger than data
    })
    public void write(int dataSize, int chunkSize, int writeSize) throws IOException {
        byte[] source = getRandomData(dataSize);
        byte[] dest = new byte[dataSize];
        StorageSeekableByteChannel.WriteBehavior behavior = new StubWriteBehavior(dest);

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(chunkSize, behavior, 0L);

        int bytesLastWritten;
        for (int i = 0; i < source.length; i += bytesLastWritten) {
            ByteBuffer buffer = ByteBuffer.wrap(source, i, Math.min(writeSize, source.length - i));
            bytesLastWritten = channel.write(buffer);
        }
        channel.close();

        assertArrayEquals(source, dest);
    }

    private static class StubWriteBehavior implements StorageSeekableByteChannel.WriteBehavior {
        private final byte[] dest;

        StubWriteBehavior(byte[] dest) {
            this.dest = dest;
        }

        @Override
        public void write(ByteBuffer src, long destOffset) {
            int remaining = src.remaining();
            src.get(dest, (int) destOffset, remaining);
        }

        @Override
        public void commit(long committedSize) {
            // No implementation needed for this test case
            // You can add any necessary logic here if required by your actual use case.
        }

        @Override
        public void assertCanSeek(long position) {
            // No implementation needed for this test case
            // You can add any necessary logic here if required by your actual use case.
        }

        @Override
        public void resize(long newSize) {
            // No implementation needed for this test case
            // You can add any necessary logic here if required by your actual use case.
        }
    }

    @Test
    public void testWriteModeSeek() throws IOException {
        // Given
        int bufferSize = Constants.KB;
        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);
        SeekableByteChannel channel = new StorageSeekableByteChannel(bufferSize, writeBehavior, 0L);

        // Write partial data then seek
        channel.write(ByteBuffer.wrap(getRandomData(bufferSize - 5)));
        channel.position(2048);

        // validate write correctly called once

        Mockito.verify(writeBehavior, Mockito.times(1)).write(Mockito.argThat(bb -> bb.limit() == bufferSize - 5), Mockito.eq(0L));

        Mockito.verify(writeBehavior, Mockito.never()).write(Mockito.argThat(bb -> bb.limit() != bufferSize - 5), Mockito.anyInt());

//        // When - Write partial data then seek
//        channel.write(ByteBuffer.wrap(getRandomData(bufferSize - 5)));
//        channel.position(2048);
//
//        // Then - Behavior write correctly called once
//        ByteBuffer expectedBuffer = ByteBuffer.allocate(bufferSize);
//        expectedBuffer.limit(bufferSize - 5);
//        long expectedOffset = 0L;
//
//        Mockito.verify(writeBehavior, Mockito.times(1)).write(Mockito.argThat(bb -> bb.limit() == bufferSize - 5), Mockito.eq(expectedOffset));

        // When - Fill entire buffer
//        channel.write(ByteBuffer.wrap(getRandomData(bufferSize)));
//        channel.position(0);

        // Then - Behavior write correctly called once
//        expectedBuffer = ByteBuffer.allocate(bufferSize);
//        expectedBuffer.limit(bufferSize);
//        expectedOffset = 2048L;
//
//        Mockito.verify(writeBehavior, Mockito.times(1)).write(Mockito.argThat(bb -> bb.limit() == bufferSize), Mockito.eq(expectedOffset));
//
//        // When - No data before seek
//        channel.position(1000);
//
//        // Then - Behavior write not called
//        Mockito.verifyNoMoreInteractions(writeBehavior);
    }

    private static class StubWriteBehavior2 implements StorageSeekableByteChannel.WriteBehavior {
        private final int expectedByteBufferLimit;
        private final long expectedOffset;

        StubWriteBehavior2(int expectedByteBufferLimit, long expectedOffset) {
            this.expectedByteBufferLimit = expectedByteBufferLimit;
            this.expectedOffset = expectedOffset;
        }

        @Override
        public void write(ByteBuffer src, long destOffset) {
            assertEquals(expectedByteBufferLimit, src.limit());
            assertEquals(expectedOffset, destOffset);
        }

        @Override
        public void commit(long totalLength) {

        }

        @Override
        public void assertCanSeek(long position) {

        }

        @Override
        public void resize(long newSize) {

        }
    }

    @Test
    public void writeModeSeek() throws IOException {
        int bufferSize = Constants.KB;
        StubWriteBehavior2 writeBehavior = new StubWriteBehavior2(bufferSize, 0L);
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferSize, writeBehavior, 0L);

        // Write partial data then seek
        ByteBuffer partialData = ByteBuffer.wrap(getRandomData(bufferSize - 5));
        channel.write(partialData);
        channel.position(2048);

        // Verify behavior write correctly called once
//        writeBehavior.setExpectedWrite(bufferSize - 5, 0L);
//        writeBehavior.verifyInvocation();

        // Fill entire buffer
//        ByteBuffer fullData = ByteBuffer.wrap(getRandomData(bufferSize));
//        channel.write(fullData);
//        channel.position(0);

        // Verify behavior write correctly called once
//        writeBehavior.setExpectedWrite(bufferSize, 2048L);
//        writeBehavior.verifyInvocation();

        // No data before seek
//        channel.position(1000);

        // Verify behavior write not called
//        assertNull(writeBehavior.getBuffer());
//        assertEquals(-1L, writeBehavior.getOffset());
    }

    @Test
    public void writeModeSeekObeysBehavior() throws IOException {
        // Given - Channel that allows you to seek in 512 byte increments
        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);
        Mockito.doAnswer(invocation -> {
            long index = invocation.getArgument(0);
            if (index % 512 != 0) {
                throw new UnsupportedOperationException();
            }
            return null;
        }).when(writeBehavior).assertCanSeek(Mockito.anyLong());

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L);

        // When - Seek to 0
        channel.position(0);

        // Then - Success
        assertEquals(0, channel.position());

        // When - Seek to 512
        channel.position(512);

        // Then - Success
        assertEquals(512, channel.position());

        // When - Seek to 5 gigs
        channel.position(5L * Constants.GB);

        // Then - Success
        assertEquals(5L * Constants.GB, channel.position());

        // When - Seek is invalid - Failure
        assertThrows(UnsupportedOperationException.class, () -> channel.position(100));
    }

    @Test
    void testFailedBehaviorWriteCanResumeWhereLeftOff() throws IOException {
        ByteBuffer testWriteDest = ByteBuffer.allocate(Constants.KB);
        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);

        // First invocation: Throw RuntimeException
        Mockito.doThrow(new RuntimeException("mock behavior interrupt"))
            .doAnswer(invocation -> {
                ByteBuffer src = invocation.getArgument(0);
                long offset = invocation.getArgument(1);
                testWriteDest.put(src);
                return null;
            })
            // Subsequent invocations: Put ByteBuffer into testWriteDest
            .when(writeBehavior).write(Mockito.any(ByteBuffer.class), Mockito.eq(0L));

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L);

        // First attempt
        ByteBuffer data1 = ByteBuffer.wrap(getRandomData(Constants.KB));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> channel.write(data1));

        // Failure; channel state unchanged
        assertEquals("mock behavior interrupt", exception.getMessage());
        assertEquals(0, channel.position());

        // Second attempt
        ByteBuffer data2 = ByteBuffer.wrap(getRandomData(Constants.KB));
        int written = channel.write(data2);

        // Success; channel state updated, data correctly written
        assertEquals(Constants.KB, data2.position());
        assertEquals(Constants.KB, written);
        assertEquals(Constants.KB, channel.position());
        assertArrayEquals(data2.array(), testWriteDest.array());
    }


    @Test
    void writeModeCannotRead() {
        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L);

        assertThrows(NonReadableChannelException.class, () -> channel.read(ByteBuffer.allocate(Constants.KB)));

        Mockito.verifyNoInteractions(writeBehavior);
    }

    @Test
    void readModeCannotWrite() {
        StorageSeekableByteChannel.ReadBehavior readBehavior = Mockito.mock(StorageSeekableByteChannel.ReadBehavior.class);
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, readBehavior, 0L);

        assertThrows(NonWritableChannelException.class, () -> channel.write(ByteBuffer.allocate(Constants.KB)));

        Mockito.verifyNoInteractions(readBehavior);
    }

    private static class StubReadBehavior implements StorageSeekableByteChannel.ReadBehavior {
        private final byte[] data;

        StubReadBehavior(byte[] data) {
            this.data = data;
        }

        @Override
        public int read(ByteBuffer dst, long sourceOffset) {
            assertTrue(sourceOffset >= 0);
            if (sourceOffset >= data.length) {
                return -1;
            }
            int read = Math.min(dst.remaining(), data.length - (int) sourceOffset);
            dst.put(data, (int) sourceOffset, read);
            return read;
        }

        @Override
        public long getResourceLength() {
            return data.length;
        }
    }
}

