// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.fillArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StreamUtilTest {

    @Test
    public void testReadStreamToListOfByteBuffersValidations() {
        assertThrows(NullPointerException.class, () -> StreamUtil.readStreamToListOfByteBuffers(null, null, 1, 2));
        assertThrows(IllegalArgumentException.class,
            () -> StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(new byte[0]), -1L, 1, 2));
        assertThrows(IllegalArgumentException.class,
            () -> StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(new byte[0]), 1L, 0, 2));
        assertThrows(IllegalArgumentException.class,
            () -> StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(new byte[0]), 1L, 2, 1));
    }

    @Test
    public void testReadsInputStreamWithIncreasingChunkSize() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        List<ByteBuffer> byteBuffers
            = StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(bytes), null, 8, 128).getT2();

        assertEquals(12, byteBuffers.size());
        assertEquals(8, byteBuffers.get(0).capacity());
        assertEquals(16, byteBuffers.get(1).capacity());
        assertEquals(32, byteBuffers.get(2).capacity());
        assertEquals(64, byteBuffers.get(3).capacity());
        assertEquals(128, byteBuffers.get(4).capacity());
        for (int i = 5; i < byteBuffers.size(); i++) {
            assertEquals(128, byteBuffers.get(i).capacity());
        }
        for (int i = 0; i < byteBuffers.size() - 1; i++) {
            // assert that buffers before last are full.
            assertEquals(byteBuffers.get(i).capacity(), byteBuffers.get(i).remaining());
        }

        // assert that collection carries original bytes.
        byte[] readBytes = new byte[bytes.length];
        new IterableOfByteBuffersInputStream(byteBuffers).read(readBytes);
        assertArraysEqual(bytes, readBytes);
    }

    @Test
    public void testEmptyStream() throws IOException {
        byte[] bytes = new byte[0];

        List<ByteBuffer> byteBuffers
            = StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(bytes), null, 8, 128).getT2();

        assertEquals(0, byteBuffers.size());
    }

    @Test
    public void testEmptyStreamWithLength() throws IOException {
        byte[] bytes = new byte[0];

        List<ByteBuffer> byteBuffers
            = StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(bytes), 0L, 8, 128).getT2();

        assertEquals(0, byteBuffers.size());
    }

    @Test
    public void testStreamLengthProvidedSmallStream() throws IOException {
        byte[] bytes = new byte[64];
        fillArray(bytes);

        List<ByteBuffer> byteBuffers
            = StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(bytes), (long) bytes.length, 8, 128)
                .getT2();

        assertEquals(1, byteBuffers.size());
        assertEquals(bytes.length, byteBuffers.get(0).capacity());
        assertEquals(bytes.length, byteBuffers.get(0).remaining());

        // assert that collection carries original bytes.
        byte[] readBytes = new byte[bytes.length];
        new IterableOfByteBuffersInputStream(byteBuffers).read(readBytes);
        assertArraysEqual(bytes, readBytes);
    }

    @Test
    public void testStreamLengthProvidedLargeStream() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        List<ByteBuffer> byteBuffers
            = StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(bytes), (long) bytes.length, 8, 128)
                .getT2();

        assertEquals(8, byteBuffers.size());
        for (ByteBuffer byteBuffer : byteBuffers) {
            // all max since stream is longer than max.
            assertEquals(128, byteBuffer.capacity());
        }
        for (int i = 0; i < byteBuffers.size() - 1; i++) {
            // assert that buffers before last are full.
            assertEquals(byteBuffers.get(i).capacity(), byteBuffers.get(i).remaining());
        }

        // assert that collection carries original bytes.
        byte[] readBytes = new byte[bytes.length];
        new IterableOfByteBuffersInputStream(byteBuffers).read(readBytes);
        assertArraysEqual(bytes, readBytes);
    }

    @Test
    public void testStreamLengthProvidedIsSmallerThanRealStreamSize() throws IOException {
        byte[] bytes = new byte[1025];
        fillArray(bytes);

        List<ByteBuffer> byteBuffers
            = StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(bytes), (long) bytes.length - 1, 8, 128)
                .getT2();

        assertEquals(9, byteBuffers.size());
        for (ByteBuffer byteBuffer : byteBuffers) {
            // all max since stream is longer than max.
            assertEquals(128, byteBuffer.capacity());
        }
        for (int i = 0; i < byteBuffers.size() - 1; i++) {
            // assert that buffers before last are full.
            assertEquals(byteBuffers.get(i).capacity(), byteBuffers.get(i).remaining());
        }

        // assert that collection carries original bytes.
        byte[] readBytes = new byte[bytes.length];
        new IterableOfByteBuffersInputStream(byteBuffers).read(readBytes);
        assertArraysEqual(bytes, readBytes);
    }

    @Test
    public void testReadsInputStreamWithSameChunkSizeNoLengthProvided() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        List<ByteBuffer> byteBuffers
            = StreamUtil.readStreamToListOfByteBuffers(new ByteArrayInputStream(bytes), null, 128, 128).getT2();

        assertEquals(8, byteBuffers.size());
        for (ByteBuffer byteBuffer : byteBuffers) {
            assertEquals(128, byteBuffer.capacity());
        }
        for (int i = 0; i < byteBuffers.size() - 1; i++) {
            // assert that buffers before last are full.
            assertEquals(byteBuffers.get(i).capacity(), byteBuffers.get(i).remaining());
        }

        // assert that collection carries original bytes.
        byte[] readBytes = new byte[bytes.length];
        new IterableOfByteBuffersInputStream(byteBuffers).read(readBytes);
        assertArraysEqual(bytes, readBytes);
    }

    @Test
    public void testBuffersGetFilledAggressively() throws IOException {
        byte[] bytes = new byte[1024];
        fillArray(bytes);

        ByteArrayInputStream delegate = new ByteArrayInputStream(bytes);
        InputStream inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return delegate.read();
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (len > 2) {
                    // read less, to trigger aggressive read in the util.
                    return delegate.read(b, off, len - 1);
                } else {
                    return delegate.read(b, off, len);
                }
            }
        };

        List<ByteBuffer> byteBuffers = StreamUtil.readStreamToListOfByteBuffers(inputStream, null, 128, 128).getT2();

        assertEquals(8, byteBuffers.size());
        for (ByteBuffer byteBuffer : byteBuffers) {
            assertEquals(128, byteBuffer.capacity());
        }
        for (int i = 0; i < byteBuffers.size() - 1; i++) {
            // assert that buffers before last are full.
            assertEquals(byteBuffers.get(i).capacity(), byteBuffers.get(i).remaining());
        }

        // assert that collection carries original bytes.
        byte[] readBytes = new byte[bytes.length];
        new IterableOfByteBuffersInputStream(byteBuffers).read(readBytes);
        assertArraysEqual(bytes, readBytes);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataRoundTripParameters")
    public void testDataRoundTrip(int dataSize, Long length, int initialBufferSize, int maxBufferSize)
        throws IOException {
        byte[] bytes = new byte[dataSize];
        fillArray(bytes);

        List<ByteBuffer> byteBuffers = StreamUtil
            .readStreamToListOfByteBuffers(new ByteArrayInputStream(bytes), length, initialBufferSize, maxBufferSize)
            .getT2();

        // assert that collection carries original bytes.
        byte[] readBytes = new byte[bytes.length];
        new IterableOfByteBuffersInputStream(byteBuffers).read(readBytes);
        assertArraysEqual(bytes, readBytes);
    }

    public static Stream<Arguments> provideTestDataRoundTripParameters() {
        List<Arguments> args = new ArrayList<>();
        for (int dataSize = 0; dataSize <= 16; dataSize++) {
            for (int minBufferSize = 1; minBufferSize <= Math.max(1, dataSize); minBufferSize++) {
                for (int maxBufferSize = minBufferSize; maxBufferSize <= Math.max(1, dataSize); maxBufferSize++) {
                    args.add(Arguments.of(dataSize, null, minBufferSize, maxBufferSize));
                    args.add(Arguments.of(dataSize, (long) dataSize, minBufferSize, maxBufferSize));
                    args.add(Arguments.of(dataSize, dataSize + 1L, minBufferSize, maxBufferSize));
                    if (dataSize > 0) {
                        args.add(Arguments.of(dataSize, dataSize - 1L, minBufferSize, maxBufferSize));
                    }
                }
            }
        }
        return args.stream();
    }

}
