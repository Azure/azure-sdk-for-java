// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.ByteBufferUtils;
import com.azure.core.implementation.util.InputStreamUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BinaryDataIOTests {
    @Test
    public void materializationPreservesCachingReplayabilityAndOwnership() throws IOException {
        byte[] expected = payload(1025);
        TrackingInputStream input = new TrackingInputStream(expected);
        BinaryData data = BinaryData.fromStream(input, 3L);

        byte[] actual = data.toBytes();
        assertArrayEquals(expected, actual);
        assertSame(actual, data.toBytes());
        assertEquals(Long.valueOf(expected.length), data.getLength());
        assertArrayEquals(expected, InputStreamUtils.readAllBytes(data.toStream(), null));
        assertFalse(input.closed);
    }

    @Test
    public void transferDoesNotTruncateTheLengthHintOrCloseStreams() throws IOException {
        byte[] expected = payload(1025);
        TrackingInputStream input = new TrackingInputStream(expected);
        TrackingOutputStream output = new TrackingOutputStream();

        BinaryData.fromStream(input, 3L).writeTo(output);

        assertArrayEquals(expected, output.toByteArray());
        assertFalse(input.closed);
        assertFalse(output.closed);
    }

    @Test
    public void replayableStreamUsesBulkOperations() throws IOException {
        byte[] expected = payload(32785);
        TrackingInputStream input = new TrackingInputStream(expected);
        BinaryData data = BinaryData.fromStream(input).toReplayableBinaryData();

        assertTrue(data.isReplayable());
        assertArrayEquals(expected, InputStreamUtils.readAllBytes(data.toStream(), null));
        TrackingOutputStream output = new TrackingOutputStream();
        InputStreamUtils.transferTo(data.toStream(), output, null);
        assertArrayEquals(expected, output.toByteArray());
        assertFalse(input.closed);
        assertFalse(output.closed);
    }

    @ParameterizedTest
    @ValueSource(strings = { "heap", "direct", "readOnly" })
    public void bulkReadsPreserveBufferStateAfterPartialReads(String kind) throws IOException {
        byte[] expected = payload(32785);
        List<ByteBuffer> buffers = buffers(expected, kind);
        InputStream input = InputStreamUtils.createInputStream(buffers);

        assertEquals(expected[0] & 0xff, input.read());
        byte[] prefix = new byte[9];
        assertEquals(prefix.length, input.read(prefix));
        assertArrayEquals(Arrays.copyOfRange(expected, 1, 10), prefix);
        assertArrayEquals(Arrays.copyOfRange(expected, 10, expected.length),
            InputStreamUtils.readAllBytes(input, null));
        assertEquals(-1, input.read());
        assertEquals(0, input.available());
        assertBufferState(buffers);
    }

    @ParameterizedTest
    @ValueSource(strings = { "heap", "direct", "readOnly" })
    public void bulkTransfersPreserveBufferStateAndDoNotHoldTheStreamMonitor(String kind) throws IOException {
        byte[] expected = payload(32785);
        List<ByteBuffer> buffers = buffers(expected, kind);
        InputStream input = InputStreamUtils.createInputStream(buffers);
        assertEquals(expected[0] & 0xff, input.read());
        ByteArrayOutputStream output = new MonitorCheckingOutputStream(input);

        InputStreamUtils.transferTo(input, output, null);

        assertArrayEquals(Arrays.copyOfRange(expected, 1, expected.length), output.toByteArray());
        assertEquals(-1, input.read());
        assertBufferState(buffers);
    }

    @ParameterizedTest
    @ValueSource(strings = { "heap", "direct", "readOnly" })
    public void bufferCopiesPreservePositionLimitAndMark(String kind) {
        byte[] expected = payload(35);
        List<ByteBuffer> buffers = buffers(expected, kind);
        byte[] actual = new byte[expected.length + 6];
        Arrays.fill(actual, (byte) 0x55);
        int offset = 3;
        for (ByteBuffer buffer : buffers) {
            offset += ByteBufferUtils.copyToArray(buffer, actual, offset);
        }

        assertEquals(expected.length + 3, offset);
        assertArrayEquals(expected, Arrays.copyOfRange(actual, 3, actual.length - 3));
        assertArrayEquals(new byte[] { 0x55, 0x55, 0x55 }, Arrays.copyOf(actual, 3));
        assertArrayEquals(new byte[] { 0x55, 0x55, 0x55 },
            Arrays.copyOfRange(actual, actual.length - 3, actual.length));
        assertBufferState(buffers);

        assertArrayEquals(expected, BinaryData.fromListByteBuffer(buffers).toBytes());
        assertBufferState(buffers);
    }

    @Test
    public void emptyBuffersAndInvalidDestinations() throws IOException {
        ByteBuffer empty = ByteBuffer.allocate(0);
        assertEquals(0, ByteBufferUtils.copyToArray(empty, new byte[0], 0));
        InputStream input = InputStreamUtils.createInputStream(Arrays.asList(empty, empty));
        assertArrayEquals(new byte[0], InputStreamUtils.readAllBytes(input, null));
        InputStreamUtils.transferTo(input, new ByteArrayOutputStream(), null);
        assertThrows(NullPointerException.class, () -> InputStreamUtils.transferTo(input, null, null));

        ByteBuffer buffer = ByteBuffer.wrap(payload(3));
        buffer.mark();
        assertThrows(IndexOutOfBoundsException.class, () -> ByteBufferUtils.copyToArray(buffer, new byte[2], 0));
        assertEquals(0, buffer.position());
        buffer.reset();
    }

    @Test
    public void readAndWriteFailuresArePropagated() {
        IOException failure = new IOException("expected failure");
        InputStream input = new InputStream() {
            @Override
            public int read() throws IOException {
                throw failure;
            }
        };
        assertSame(failure, assertThrows(IOException.class, () -> InputStreamUtils.readAllBytes(input, null)));

        OutputStream output = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw failure;
            }
        };
        InputStream buffered = InputStreamUtils.createInputStream(buffers(payload(35), "heap"));
        assertSame(failure, assertThrows(IOException.class, () -> InputStreamUtils.transferTo(buffered, output, null)));
    }

    @Test
    public void aTinyDirectBufferDoesNotForceTinyWritesForLaterBuffers() throws IOException {
        byte[] expected = payload(32785);
        ByteBuffer first = ByteBuffer.allocateDirect(1);
        first.put(expected[0]);
        first.flip();
        ByteBuffer rest = ByteBuffer.allocateDirect(expected.length - 1);
        rest.put(expected, 1, expected.length - 1);
        rest.flip();
        InputStream input = InputStreamUtils.createInputStream(Arrays.asList(first, rest));
        CountingOutputStream output = new CountingOutputStream();

        InputStreamUtils.transferTo(input, output, null);

        assertArrayEquals(expected, output.toByteArray());
        assertTrue(output.writes <= 1 + (expected.length + 8191) / 8192);
        assertEquals(0, first.position());
        assertEquals(0, rest.position());
    }

    @Test
    public void transfersCoalesceSmallHeapBuffers() throws IOException {
        byte[] expected = payload(4096);
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int offset = 0; offset < expected.length; offset += 32) {
            buffers.add(ByteBuffer.wrap(expected, offset, 32).slice());
        }
        CountingOutputStream output = new CountingOutputStream();

        InputStreamUtils.transferTo(InputStreamUtils.createInputStream(buffers), output, null);

        assertArrayEquals(expected, output.toByteArray());
        assertEquals(1, output.writes);
    }

    static byte[] payload(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (i * 31);
        }
        return bytes;
    }

    static List<ByteBuffer> buffers(byte[] bytes, String kind) {
        int middle = bytes.length / 2;
        ByteBuffer empty = ByteBuffer.allocate(0);
        empty.mark();
        return Arrays.asList(buffer(bytes, 0, middle, kind), empty, buffer(bytes, middle, bytes.length - middle, kind));
    }

    private static ByteBuffer buffer(byte[] bytes, int offset, int length, String kind) {
        ByteBuffer buffer
            = "direct".equals(kind) ? ByteBuffer.allocateDirect(length + 5) : ByteBuffer.allocate(length + 5);
        buffer.position(2);
        buffer.put((byte) -1);
        buffer.put(bytes, offset, length);
        buffer.limit(3 + length);
        buffer.position(2);
        buffer = buffer.slice();
        buffer.position(1);
        if ("readOnly".equals(kind)) {
            buffer = buffer.asReadOnlyBuffer();
        }
        buffer.mark();
        return buffer;
    }

    private static void assertBufferState(List<ByteBuffer> buffers) {
        for (ByteBuffer buffer : buffers) {
            int position = buffer.capacity() == 0 ? 0 : 1;
            assertEquals(position, buffer.position());
            assertEquals(buffer.capacity(), buffer.limit());
            buffer.reset();
            assertEquals(position, buffer.position());
        }
    }

    private static final class TrackingInputStream extends ByteArrayInputStream {
        private boolean closed;

        private TrackingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static final class TrackingOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }

    static final class MonitorCheckingOutputStream extends ByteArrayOutputStream {
        private final InputStream input;

        MonitorCheckingOutputStream(InputStream input) {
            this.input = input;
        }

        @Override
        public void write(byte[] bytes, int offset, int length) {
            assertFalse(Thread.holdsLock(input));
            super.write(bytes, offset, length);
        }
    }

    private static final class CountingOutputStream extends ByteArrayOutputStream {
        private int writes;

        @Override
        public void write(byte[] bytes, int offset, int length) {
            writes++;
            super.write(bytes, offset, length);
        }
    }
}
