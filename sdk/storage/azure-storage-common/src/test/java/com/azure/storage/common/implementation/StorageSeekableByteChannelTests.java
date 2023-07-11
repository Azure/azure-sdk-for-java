// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.test.TestProxyTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageSeekableByteChannelTests extends TestProxyTestBase {

    private byte[] getRandomData(int size) {
        byte[] result = new byte[size];
        new Random().nextBytes(result);
        return result;
    }

    @Test
    void readSequentially() throws IOException {
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
    public void seekWithinBuffer() throws IOException {
        int bufferLength = 4 * Constants.KB;
        byte[] data = getRandomData(2 * bufferLength);

        StorageSeekableByteChannel.ReadBehavior behavior = new StorageSeekableByteChannel.ReadBehavior() {
            @Override
            public int read(ByteBuffer dst, long sourceOffset) {
                assertEquals(0L, sourceOffset); // Ensure sourceOffset is 0
                int read = Math.min(dst.remaining(), data.length - (int) sourceOffset);
                dst.put(data, (int) sourceOffset, read);
                return read;
            }

            @Override
            public long getResourceLength() {
                return data.length;
            }
        };

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer temp = ByteBuffer.allocate(100);
        long[] seekIndices = {
            0,                  // initial read at 0 to control buffer location
            bufferLength / 2,   // seek somewhere in middle
            bufferLength / 2,   // seek back to that same spot
            0,                  // seek back to beginning
            bufferLength - 100, // seek to last temp.length chunk of the internal buffer
            bufferLength - 1    // seek to last byte of the internal buffer
        };

        for (long seekIndex : seekIndices) {
            temp.clear();
            channel.position(seekIndex);
            assertEquals(Math.min(temp.limit(), bufferLength - seekIndex), channel.read(temp));
        }
    }

    private static class MockReadBehavior implements StorageSeekableByteChannel.ReadBehavior {
        private final byte[] data;

        MockReadBehavior(byte[] data) {
            this.data = data;
        }

        @Override
        public int read(ByteBuffer dst, long sourceOffset) {
            //assertEquals(sourceOffset, 0); // Test is designed to have gotten its buffer at position 0, ensure we do this
            int read = Math.min(dst.remaining(), data.length - (int) sourceOffset);
            dst.put(data, (int) sourceOffset, read);
            return read;
        }

        @Override
        public long getResourceLength() {
            return data.length;
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
    public void seekResultsInCorrectRead() throws IOException {
        int bufferLength = 5;
        byte[] data = getRandomData(Constants.KB);
        long seekIndex = 345;

        // Custom implementation of ReadBehavior
        StorageSeekableByteChannel.ReadBehavior behavior = new StorageSeekableByteChannel.ReadBehavior() {
            private boolean readCalled = false;

            @Override
            public int read(ByteBuffer dst, long sourceOffset) {
                if (!readCalled && sourceOffset == seekIndex) {
                    byte[] expectedData = Arrays.copyOfRange(data, (int) seekIndex, (int) seekIndex + bufferLength);
                    int read = Math.min(dst.remaining(), expectedData.length);
                    dst.put(expectedData, 0, read);
                    readCalled = true;
                    return read;
                } else {
                    return -1;  // Indicate end of data
                }
            }

            @Override
            public long getResourceLength() {
                return data.length;
            }
        };

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer result = ByteBuffer.allocate(bufferLength);
        channel.position(seekIndex);
        int bytesRead = channel.read(result);

        byte[] expectedData = Arrays.copyOfRange(data, (int) seekIndex, (int) seekIndex + bufferLength);

        assertArrayEquals(expectedData, Arrays.copyOf(result.array(), bytesRead));
    }

    @Test
    public void readPastResourceEnd() throws IOException {
        int offset;
        int expectedReadLength;

        // Test cases
        int[][] testData = {
            { Constants.KB, 500, Constants.KB - 500 },           // Overlap on end of resource
            { Constants.KB, Constants.KB, -1 },                  // Starts at end of resource
            { Constants.KB, Constants.KB + 20, -1 }              // Completely past resource
        };

        for (int[] testCase : testData) {
            final int resourceSize = testCase[0];
            offset = testCase[1];
            expectedReadLength = testCase[2];

            StorageSeekableByteChannel.ReadBehavior behavior = new StorageSeekableByteChannel.ReadBehavior() {

                @Override
                public int read(ByteBuffer dst, long sourceOffset) {
                    int toRead = Math.min(dst.remaining(), resourceSize - (int) sourceOffset);
                    if (toRead > 0) {
                        dst.put(new byte[toRead]);
                        return toRead;
                    } else {
                        return -1;
                    }
                }

                @Override
                public long getResourceLength() {
                    return resourceSize;
                }
            };

            StorageSeekableByteChannel channel = new StorageSeekableByteChannel(resourceSize, behavior, 0L);

            ByteBuffer buffer = ByteBuffer.allocate(resourceSize);
            channel.position(offset);
            int bytesRead = channel.read(buffer);

            // Assertions
            assertEquals(expectedReadLength, bytesRead);
            assertEquals(resourceSize, channel.position());
            assertEquals(resourceSize, channel.size());
        }
    }



    private static class StubReadBehavior2 implements StorageSeekableByteChannel.ReadBehavior {
        private ResourceLengthFunction resourceLengthFunction;
        private ReadFunction readFunction;

        public void setResourceLength(ResourceLengthFunction resourceLengthFunction) {
            this.resourceLengthFunction = resourceLengthFunction;
        }

        public void setReadFunction(ReadFunction readFunction) {
            this.readFunction = readFunction;
        }

        @Override
        public int read(ByteBuffer dst, long sourceOffset) {
            return readFunction.read(dst, sourceOffset);
        }

        @Override
        public long getResourceLength() {
            return resourceLengthFunction.getResourceLength();
        }

        @FunctionalInterface
        interface ResourceLengthFunction {
            long getResourceLength();
        }

        @FunctionalInterface
        interface ReadFunction {
            int read(ByteBuffer dst, long sourceOffset);
        }
    }

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

        StubWriteBehavior() {
            this(new byte[0]);
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
    public void writeModeSeek() throws IOException {
        int bufferSize = Constants.KB;
        StubWriteBehavior2 writeBehavior = new StubWriteBehavior2(bufferSize);
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferSize, writeBehavior, 0L);

        // Write partial data then seek
        ByteBuffer partialData = ByteBuffer.wrap(getRandomData(bufferSize - 5));
        channel.write(partialData);
        channel.position(2048);

        // Verify behavior write correctly called once
        writeBehavior.setExpectedWrite(bufferSize - 5, 0L);
        writeBehavior.verifyInvocation();

        // Fill entire buffer
        ByteBuffer fullData = ByteBuffer.wrap(getRandomData(bufferSize));
        channel.write(fullData);
        channel.position(0);

        // Verify behavior write correctly called once
        writeBehavior.setExpectedWrite(bufferSize, 2048L);
        writeBehavior.verifyInvocation();

        // No data before seek
        channel.position(1000);

        // Verify behavior write not called
        assertNull(writeBehavior.getBuffer());
        assertEquals(-1L, writeBehavior.getOffset());
    }


//    private void verifyWriteInvocation(StubWriteBehavior2 writeBehavior, int expectedSize, long expectedOffset) {
//        ByteBuffer capturedBuffer = writeBehavior.getBuffer();
//        long capturedOffset = writeBehavior.getOffset();
//        assertEquals(expectedSize, capturedBuffer.remaining());
//        assertEquals(expectedOffset, capturedOffset);
//    }
//
//    private void verifyNoMoreInteractions(StubWriteBehavior2 writeBehavior) {
//        assertNull(writeBehavior.getBuffer());
//        assertEquals(-1L, writeBehavior.getOffset());
//    }
//
//    private void verifyNoInteractions(StubWriteBehavior2 writeBehavior) {
//        assertNull(writeBehavior.getBuffer());
//        assertEquals(-1L, writeBehavior.getOffset());
//    }



    private static class StubWriteBehavior2 implements StorageSeekableByteChannel.WriteBehavior {
        private ByteBuffer buffer;
        private long offset;
        private final int bufferSize;
        private int expectedLength;
        private long expectedOffset;

        StubWriteBehavior2(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        public void setExpectedWrite(int length, long offset) {
            this.expectedLength = length;
            this.expectedOffset = offset;
        }

        @Override
        public void write(ByteBuffer src, long targetOffset) {
            buffer = src;
            offset = targetOffset;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }

        public long getOffset() {
            return offset;
        }

        public int getBufferSize() {
            return bufferSize;
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

        public void verifyInvocation() {
            assertEquals(expectedLength, buffer.limit());
            assertEquals(expectedOffset, offset);
        }
    }




    @Test
    void writeModeSeekObeysBehavior() throws IOException {
        // Channel that allows seeking in 512-byte increments
        StorageSeekableByteChannel.WriteBehavior writeBehavior = new StubWriteBehavior();
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L);

        // Seek to 0
        channel.position(0);
        assertEquals(0, channel.position());

        // Seek to 512
        channel.position(512);
        assertEquals(512, channel.position());

        // Seek to 5 gigs
        channel.position(5L * Constants.GB);
        assertEquals(5L * Constants.GB, channel.position());

        // Seek is invalid
        assertThrows(UnsupportedOperationException.class, () -> channel.position(100));
    }

    @Test
    void failedBehaviorWriteCanResumeWhereLeftOff() throws IOException {
        // Channel with behavior that throws first write attempt
        ByteBuffer testWriteDest = ByteBuffer.allocate(Constants.KB);
        StorageSeekableByteChannel.WriteBehavior writeBehavior = new StubWriteBehavior() {
            private boolean firstAttempt = true;

            @Override
            public void write(ByteBuffer src, long destOffset) {
                if (firstAttempt) {
                    firstAttempt = false;
                    throw new RuntimeException("mock behavior interrupt");
                } else {
                    testWriteDest.put(src);
                }
            }
        };
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L);

        // First attempt
        ByteBuffer data1 = ByteBuffer.wrap(getRandomData(Constants.KB));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> channel.write(data1));
        assertEquals("mock behavior interrupt", exception.getMessage());
        assertEquals(0, channel.position());

        // Second attempt
        ByteBuffer data2 = ByteBuffer.wrap(getRandomData(Constants.KB));
        int written = channel.write(data2);
        assertFalse(exception instanceof Throwable);
        assertEquals(Constants.KB, data2.position());
        assertEquals(Constants.KB, written);
        assertEquals(Constants.KB, channel.position());
        assertArrayEquals(data2.array(), testWriteDest.array());
    }

    @Test
    void writeModeCannotRead() {
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, new StubWriteBehavior(), 0L);

        assertThrows(NonReadableChannelException.class, () -> channel.read(ByteBuffer.allocate(Constants.KB)));
    }

    @Test
    void readModeCannotWrite() {
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, new StubReadBehavior2(), 0L);

        assertThrows(NonWritableChannelException.class, () -> channel.write(ByteBuffer.allocate(Constants.KB)));
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
