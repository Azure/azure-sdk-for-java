// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.ByteBufferUtils;
import com.azure.core.implementation.util.InputStreamUtils;
import com.azure.core.util.mocking.MockFile;
import com.azure.core.util.mocking.MockFileInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 1025, 32785 })
    public void materializationHonorsByteArrayInputStreamReadOverrides(int size) throws IOException {
        byte[] bytes = payload(size);
        byte[] expected = bytes.clone();
        for (int i = 0; i < expected.length; i++) {
            expected[i] ^= 0xff;
        }

        try (InputStream input = new TransformingInputStream(bytes)) {
            assertArrayEquals(expected, BinaryData.fromStream(input).toBytes());
            assertEquals(-1, input.read());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 1025, 32785 })
    public void materializationHonorsFileInputStreamReadOverrides(int size) throws IOException {
        byte[] expected = payload(size);
        try (InputStream input = new MockFileInputStream(new MockFile("unused", expected, size))) {
            assertArrayEquals(expected, BinaryData.fromStream(input).toBytes());
            assertEquals(-1, input.read());
        }
    }

    @ParameterizedTest
    @CsvSource({ "false, false", "true, false", "false, true", "true, true" })
    public void transfersHonorFileStreamReadAndWriteOverrides(boolean transformInput, boolean transformOutput,
        @TempDir Path directory) throws IOException {
        byte[] bytes = payload(32785);
        byte[] expected = bytes.clone();
        int mask = (transformInput ? 0x55 : 0) ^ (transformOutput ? 0xaa : 0);
        for (int i = 0; i < expected.length; i++) {
            expected[i] ^= mask;
        }

        Path source = Files.write(directory.resolve("source.bin"), bytes);
        Path destination = directory.resolve("destination.bin");
        try (
            InputStream input
                = transformInput ? new TransformingFileInputStream(source) : new FileInputStream(source.toFile());
            OutputStream output = transformOutput
                ? new TransformingFileOutputStream(destination)
                : new FileOutputStream(destination.toFile())) {
            BinaryData.fromStream(input, 3L).writeTo(output);
        }

        assertArrayEquals(expected, Files.readAllBytes(destination));
    }

    @ParameterizedTest
    @CsvSource({
        "buffered, false",
        "buffered, true",
        "pushback, false",
        "pushback, true",
        "sequence, false",
        "sequence, true" })
    public void wrappedTransfersHonorReadOverrides(String kind, boolean partialRead) throws IOException {
        byte[] bytes = payload(32785);
        byte[] expected = bytes.clone();
        for (int i = 0; i < expected.length; i++) {
            expected[i] ^= 0xff;
        }

        InputStream transformed = new TransformingInputStream(bytes);
        try (InputStream input = wrapInputStream(transformed, kind)) {
            if (partialRead) {
                assertEquals(expected[0] & 0xff, input.read());
                expected = Arrays.copyOfRange(expected, 1, expected.length);
            }
            ByteArrayOutputStream output = new MonitorCheckingOutputStream(transformed);
            BinaryData.fromStream(input, 3L).writeTo(output);
            assertArrayEquals(expected, output.toByteArray());
            assertEquals(-1, input.read());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 8191, 8192, 8193, 32785 })
    public void pushbackTransfersKeepDestinationWritesBounded(int size) throws IOException {
        byte[] expected = payload(size);
        try (PushbackInputStream input = new PushbackInputStream(new ByteArrayInputStream(new byte[0]), size)) {
            input.unread(expected);
            CountingOutputStream output = new CountingOutputStream();
            BinaryData.fromStream(input).writeTo(output);
            assertArrayEquals(expected, output.toByteArray());
            assertTrue(output.largestWrite <= 8192);
            assertEquals(-1, input.read());
        }
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
    public void bulkReadsPreserveOrderAndBufferStateForFragmentedBuffers(String kind) throws IOException {
        // Thousands of tiny and empty buffers, which bulk reads mostly coalesce, between buffers around 1 KiB and 8 KiB.
        List<Integer> sizes = new ArrayList<>();
        for (int size : new int[] { 1025, 1023, 1024, 8191, 8192, 8193, 20000 }) {
            sizes.add(size);
            for (int i = 0; i < 3000; i++) {
                sizes.add(i % 4);
            }
        }
        byte[] expected = new byte[sizes.stream().mapToInt(Integer::intValue).sum()];
        for (int i = 0; i < expected.length; i++) {
            // Unlike payload(), this doesn't repeat every 256 bytes, so misordered content can't go unnoticed.
            expected[i] = (byte) (i ^ (i >>> 8) ^ (i >>> 16));
        }
        List<ByteBuffer> buffers = new ArrayList<>(sizes.size());
        int offset = 0;
        for (int size : sizes) {
            buffers.add(buffer(expected, offset, size, kind));
            offset += size;
        }
        InputStream input = InputStreamUtils.createInputStream(buffers);

        assertEquals(expected[0] & 0xff, input.read());
        byte[] prefix = new byte[1100];
        assertEquals(prefix.length, input.read(prefix));
        assertArrayEquals(Arrays.copyOfRange(expected, 1, 1101), prefix);
        assertArrayEquals(Arrays.copyOfRange(expected, 1101, expected.length),
            InputStreamUtils.readAllBytes(input, null));
        assertEquals(-1, input.read());
        assertBufferState(buffers);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void pipedFileInputStreamsAreReadCompletely(boolean writeTo, @TempDir Path directory) throws Exception {
        // A child JVM's standard input is a pipe on every platform. Before JDK 24, file bulk reads and file-to-file
        // transfers fail for non-seekable files, and Windows file-to-file transfers can stop early or fail for pipes.
        byte[] expected = payload(1024 * 1024 + 17);
        Path actual = directory.resolve("actual.bin");
        Path log = directory.resolve("child.log");
        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        // Surefire separates production classes and dependencies onto the module path on Java 9+.
        String modulePath = System.getProperty("jdk.module.path");
        if (modulePath != null && !modulePath.isEmpty()) {
            classPath += File.pathSeparator + modulePath;
        }
        Process process = new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
            "-Xmx64m", "-cp", classPath, PipeReader.class.getName(), String.valueOf(writeTo), actual.toString())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();
        Thread writer = new Thread(() -> {
            try (OutputStream childInput = process.getOutputStream()) {
                childInput.write(expected);
            } catch (IOException ignored) {
                // The child's exit status and log explain why it stopped reading.
            }
        });
        writer.setDaemon(true);
        writer.start();

        boolean completed;
        try {
            completed = process.waitFor(60, TimeUnit.SECONDS);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(10, TimeUnit.SECONDS), "The child JVM did not terminate.");
            }
        }

        String diagnostics = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
        assertTrue(completed, "The child JVM did not complete.\n" + diagnostics);
        assertEquals(0, process.exitValue(), diagnostics);
        assertArrayEquals(expected, Files.readAllBytes(actual));
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

    @ParameterizedTest
    @ValueSource(ints = { 35, 32785 })
    public void readAndWriteFailuresArePropagated(int size) {
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
        InputStream buffered = InputStreamUtils.createInputStream(buffers(payload(size), "heap"));
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

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 8191, 8192, 8193, 32785 })
    public void heapBufferTransfersKeepDestinationWritesBounded(int size) throws IOException {
        byte[] expected = payload(size);
        ByteBuffer buffer = ByteBuffer.wrap(expected);
        buffer.mark();
        try (InputStream input = InputStreamUtils.createInputStream(Collections.singletonList(buffer))) {
            CountingOutputStream output = new CountingOutputStream();
            BinaryData.fromStream(input).writeTo(output);
            assertArrayEquals(expected, output.toByteArray());
            assertTrue(output.largestWrite <= 8192);
            assertEquals(-1, input.read());
        }
        assertEquals(0, buffer.position());
        buffer.reset();
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

    private static InputStream wrapInputStream(InputStream input, String kind) {
        switch (kind) {
            case "buffered":
                return new BufferedInputStream(input);

            case "pushback":
                return new PushbackInputStream(input);

            case "sequence":
                return new SequenceInputStream(input, new ByteArrayInputStream(new byte[0]));

            default:
                throw new IllegalArgumentException("Unknown stream wrapper: " + kind);
        }
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

    /**
     * Reads standard input through {@link BinaryData} and writes the content to the file named by the second argument.
     */
    public static final class PipeReader {
        public static void main(String[] args) throws IOException {
            try (InputStream input = new FileInputStream(FileDescriptor.in);
                OutputStream output = new FileOutputStream(args[1])) {
                BinaryData data = BinaryData.fromStream(input);
                if (Boolean.parseBoolean(args[0])) {
                    data.writeTo(output);
                } else {
                    output.write(data.toBytes());
                }
            }
        }
    }

    private static final class TransformingInputStream extends ByteArrayInputStream {
        private TransformingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public synchronized int read() {
            int value = super.read();
            return value == -1 ? -1 : ~value & 0xff;
        }

        @Override
        public synchronized int read(byte[] bytes, int offset, int length) {
            int read = super.read(bytes, offset, length);
            for (int i = 0; i < read; i++) {
                bytes[offset + i] ^= 0xff;
            }
            return read;
        }
    }

    private static final class TransformingFileInputStream extends FileInputStream {
        private TransformingFileInputStream(Path path) throws IOException {
            super(path.toFile());
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            return value == -1 ? -1 : value ^ 0x55;
        }

        @Override
        public int read(byte[] bytes) throws IOException {
            return read(bytes, 0, bytes.length);
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            int read = super.read(bytes, offset, length);
            for (int i = 0; i < read; i++) {
                bytes[offset + i] ^= 0x55;
            }
            return read;
        }
    }

    private static final class TransformingFileOutputStream extends FileOutputStream {
        private TransformingFileOutputStream(Path path) throws IOException {
            super(path.toFile());
        }

        @Override
        public void write(int value) throws IOException {
            super.write(value ^ 0xaa);
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            write(bytes, 0, bytes.length);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            byte[] transformed = Arrays.copyOfRange(bytes, offset, offset + length);
            for (int i = 0; i < transformed.length; i++) {
                transformed[i] ^= 0xaa;
            }
            super.write(transformed, 0, transformed.length);
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
        private int largestWrite;

        @Override
        public void write(byte[] bytes, int offset, int length) {
            writes++;
            largestWrite = Math.max(largestWrite, length);
            super.write(bytes, offset, length);
        }
    }
}
