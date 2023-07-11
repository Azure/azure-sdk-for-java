// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
    void seekToNewBuffer() throws IOException {
        int bufferLength = 5;
        byte[] data = getRandomData(Constants.KB);
        long[] seekIndices = new long[]{20, 500, 1, 6, 5};

        StorageSeekableByteChannel.ReadBehavior behavior = Mockito.mock(StorageSeekableByteChannel.ReadBehavior.class);
        when(behavior.getResourceLength()).thenReturn((long) data.length);

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer temp = ByteBuffer.allocate(bufferLength * 2);
        for (long seekIndex : seekIndices) {
            temp.clear();
            channel.position(seekIndex);
            channel.read(temp);
        }

        for (long l : seekIndices) {
            verify(behavior).read(Mockito.any(ByteBuffer.class), Mockito.eq(l));
        }
    }

    @Test
    void seekResultsInCorrectRead() throws IOException {
        int bufferLength = 5;
        byte[] data = getRandomData(Constants.KB);
        long seekIndex = 345;

        StorageSeekableByteChannel.ReadBehavior behavior = Mockito.mock(StorageSeekableByteChannel.ReadBehavior.class);
        when(behavior.getResourceLength()).thenReturn((long) data.length);
        when(behavior.read(Mockito.any(ByteBuffer.class), Mockito.eq(seekIndex))).thenAnswer(invocation -> {
            ByteBuffer dst = invocation.getArgument(0);
            long sourceOffset = invocation.getArgument(1);

            dst.put(data, (int) sourceOffset, bufferLength);
            return bufferLength;
        });

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferLength, behavior, 0L);

        ByteBuffer result = ByteBuffer.allocate(bufferLength);
        channel.position(seekIndex);
        channel.read(result);

        assertArrayEquals(data, result.array());
        verify(behavior).read(Mockito.any(ByteBuffer.class), Mockito.eq(seekIndex));
        verify(behavior, Mockito.never()).read(Mockito.any(ByteBuffer.class), Mockito.anyLong());
    }

    @Test
    void readPastResourceEnd() throws IOException {
        int resourceSize = Constants.KB;
        int offset = 500;
        int expectedReadLength = Constants.KB - 500;

        StorageSeekableByteChannel.ReadBehavior behavior = Mockito.mock(StorageSeekableByteChannel.ReadBehavior.class);
        when(behavior.getResourceLength()).thenReturn((long) resourceSize);
        when(behavior.read(Mockito.any(ByteBuffer.class), Mockito.anyLong())).thenAnswer(invocation -> {
            ByteBuffer dst = invocation.getArgument(0);
            long sourceOffset = invocation.getArgument(1);

            int toRead = Math.min(dst.remaining(), resourceSize - (int) sourceOffset);
            if (toRead > 0) {
                dst.put(new byte[toRead]);
                return toRead;
            } else {
                return -1;
            }
        });

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(resourceSize, behavior, 0L);

        channel.position(offset);
        int read = channel.read(ByteBuffer.allocate(resourceSize));

        assertNull(assertThrows(Throwable.class, () -> { }));
        assertEquals(expectedReadLength, read);
        assertEquals(resourceSize, channel.position());
        assertEquals(resourceSize, channel.size());
    }

    @Test
    void writeModeSeek() throws IOException {
        int bufferSize = Constants.KB;

        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(bufferSize, writeBehavior, 0L);

        ByteBuffer partialData = ByteBuffer.wrap(getRandomData(bufferSize - 5));
        channel.write(partialData);
        channel.position(2048);

        verify(writeBehavior).write(Mockito.argThat(bb -> bb.limit() == bufferSize - 5), Mockito.eq(0L));
        verifyNoMoreInteractions(writeBehavior);

        ByteBuffer fullData = ByteBuffer.wrap(getRandomData(bufferSize));
        channel.write(fullData);
        channel.position(0);

        verify(writeBehavior).write(Mockito.argThat(bb -> bb.limit() == bufferSize), Mockito.eq(2048L));
        verifyNoMoreInteractions(writeBehavior);

        channel.position(1000);

        Mockito.verifyNoInteractions(writeBehavior);
    }

    @Test
    void write() throws IOException {
        int dataSize = 8 * Constants.KB;
        int chunkSize = Constants.KB;
        int writeSize = Constants.KB;

        byte[] source = getRandomData(dataSize);
        byte[] dest = new byte[dataSize];

        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);
        doAnswer(invocation -> {
            ByteBuffer src = invocation.getArgument(0);
            long destOffset = invocation.getArgument(1);

            src.get(dest, (int) destOffset, src.remaining());
            return null;
        }).when(writeBehavior).write(Mockito.any(ByteBuffer.class), Mockito.anyLong());

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(chunkSize, writeBehavior, 0L);

        int bytesLastWritten;
        for (int i = 0; i < source.length; i += bytesLastWritten) {
            bytesLastWritten = channel.write(ByteBuffer.wrap(source, i, Math.min(writeSize, source.length - i)));
        }
        channel.close();

        assertArrayEquals(source, dest);
    }


    @Test
    void writeModeSeekObeysBehavior() throws IOException {
        int chunkSize = Constants.KB;

        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(chunkSize, writeBehavior, 0L);

        channel.position(0);
        assertEquals(0, channel.position());

        channel.position(512);
        assertEquals(512, channel.position());

        channel.position(5L * Constants.GB);
        assertEquals(5L * Constants.GB, channel.position());

        assertThrows(UnsupportedOperationException.class, () -> channel.position(100));

        verify(writeBehavior, Mockito.never()).write(Mockito.any(ByteBuffer.class), Mockito.anyLong());
    }

    @Test
    void failedBehaviorWriteCanResumeWhereLeftOff() throws IOException {
        ByteBuffer testWriteDest = ByteBuffer.allocate(Constants.KB);

        StorageSeekableByteChannel.WriteBehavior writeBehavior = Mockito.mock(StorageSeekableByteChannel.WriteBehavior.class);

        // Throw an exception on the first write attempt
        Mockito.doThrow(new RuntimeException("mock behavior interrupt")).doReturn(Constants.KB)
            .when(writeBehavior).write(Mockito.any(ByteBuffer.class), Mockito.eq(0L));

        // Perform the desired action on subsequent write attempts
        doAnswer(invocation -> {
            ByteBuffer src = invocation.getArgument(0);
            long offset = invocation.getArgument(1);
            testWriteDest.put(src);
            return src.remaining();
        }).when(writeBehavior).write(Mockito.any(ByteBuffer.class), Mockito.anyLong());

        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(Constants.KB, writeBehavior, 0L);

        ByteBuffer data1 = ByteBuffer.wrap(getRandomData(Constants.KB));
        assertThrows(RuntimeException.class, () -> channel.write(data1));
        assertEquals(0, channel.position());

        ByteBuffer data2 = ByteBuffer.wrap(getRandomData(Constants.KB));
        int written = channel.write(data2);

        assertNull(assertThrows(Throwable.class, () -> {
        }));
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

