// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.binarydata;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * IO benchmarks that require Java 11 or later. Run with the packaged library JAR, rather than target/classes, to
 * exercise multi-release implementations. Each materialization uses a fresh BinaryData instance to avoid its cache.
 */
@Fork(
    value = 2,
    jvmArgsAppend = {
        "-Xms256m",
        "-Xmx256m",
        "-XX:+UseG1GC",
        "-XX:G1HeapRegionSize=4m",
        "-XX:ReservedCodeCacheSize=64m",
        "-XX:ActiveProcessorCount=2" })
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class BinaryDataIOBenchmark {
    private static final MethodHandle READ_ALL_BYTES;
    private static final MethodHandle TRANSFER_TO;

    static {
        try {
            READ_ALL_BYTES = MethodHandles.publicLookup()
                .findVirtual(InputStream.class, "readAllBytes", MethodType.methodType(byte[].class));
            TRANSFER_TO = MethodHandles.publicLookup()
                .findVirtual(InputStream.class, "transferTo", MethodType.methodType(long.class, OutputStream.class));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("These benchmarks require Java 11 or later.", exception);
        }
    }

    @Benchmark
    public byte[] streamToBytes(StreamState state) {
        return BinaryData.fromStream(state.openStream()).toBytes();
    }

    @Benchmark
    public long streamToOutput(StreamState state, Blackhole blackhole) {
        state.output.reset(blackhole);
        BinaryData.fromStream(state.openStream()).writeTo(state.output);
        return state.output.count;
    }

    @Benchmark
    public byte[] bufferedReadAllBytes(BufferState state) throws Throwable {
        return (byte[]) READ_ALL_BYTES.invokeExact(state.data.toStream());
    }

    @Benchmark
    public long bufferedTransferTo(BufferState state, Blackhole blackhole) throws Throwable {
        state.output.reset(blackhole);
        return (long) TRANSFER_TO.invokeExact(state.data.toStream(), (OutputStream) state.output);
    }

    @Benchmark
    public byte[] listToBytes(BufferState state) {
        return BinaryData.fromListByteBuffer(state.buffers).toBytes();
    }

    @State(Scope.Thread)
    public static class StreamState {
        @Param({ "4096", "1048576" })
        int size;

        @Param({ "byteArray", "generic" })
        String streamType;

        private byte[] bytes;
        private final BlackholeOutputStream output = new BlackholeOutputStream();

        @Setup
        public void setup() {
            requirePackagedLibrary();
            bytes = payload(size);
        }

        private InputStream openStream() {
            return "byteArray".equals(streamType) ? new ByteArrayInputStream(bytes) : new GenericInputStream(bytes);
        }
    }

    @State(Scope.Thread)
    public static class BufferState {
        @Param({ "4096", "1048576" })
        int size;

        @Param({ "1", "128" })
        int bufferCount;

        @Param({ "heap", "direct", "readOnly" })
        String bufferType;

        private List<ByteBuffer> buffers;
        private BinaryData data;
        private final BlackholeOutputStream output = new BlackholeOutputStream();

        @Setup
        public void setup() {
            requirePackagedLibrary();
            byte[] bytes = payload(size);
            buffers = new ArrayList<>(bufferCount);
            int offset = 0;
            for (int i = 0; i < bufferCount; i++) {
                int count = (size - offset) / (bufferCount - i);
                ByteBuffer buffer;
                if ("direct".equals(bufferType)) {
                    buffer = ByteBuffer.allocateDirect(count);
                    buffer.put(bytes, offset, count);
                    buffer.flip();
                } else {
                    buffer = ByteBuffer.wrap(bytes, offset, count).slice();
                    if ("readOnly".equals(bufferType)) {
                        buffer = buffer.asReadOnlyBuffer();
                    }
                }
                buffers.add(buffer);
                offset += count;
            }
            data = BinaryData.fromListByteBuffer(buffers);
        }
    }

    private static byte[] payload(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (i * 31);
        }
        return bytes;
    }

    private static void requirePackagedLibrary() {
        if (!BinaryData.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar")) {
            throw new IllegalStateException("Use the packaged library JAR, not target/classes, for IO benchmarks.");
        }
    }

    private static final class BlackholeOutputStream extends OutputStream {
        private Blackhole blackhole;
        private long count;

        private void reset(Blackhole blackhole) {
            this.blackhole = blackhole;
            count = 0;
        }

        @Override
        public void write(int value) {
            blackhole.consume(value);
            count++;
        }

        @Override
        public void write(byte[] bytes, int offset, int length) {
            blackhole.consume(bytes);
            blackhole.consume(offset);
            blackhole.consume(length);
            count += length;
        }
    }

    private static final class GenericInputStream extends InputStream {
        private final byte[] bytes;
        private int position;

        private GenericInputStream(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int read() {
            return position < bytes.length ? bytes[position++] & 0xff : -1;
        }

        @Override
        public int read(byte[] destination, int offset, int length) {
            if (length == 0) {
                return 0;
            }
            if (position == bytes.length) {
                return -1;
            }
            int count = Math.min(length, bytes.length - position);
            System.arraycopy(bytes, position, destination, offset, count);
            position += count;
            return count;
        }
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }
}
