// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.util.PartialWriteAsynchronousChannel;
import com.typespec.core.util.ProgressReporter;
import com.typespec.core.util.io.IOUtils;
import com.typespec.core.util.mocking.MockAsynchronousByteChannel;
import com.typespec.core.util.mocking.MockAsynchronousFileChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static com.typespec.core.CoreTestUtils.assertArraysEqual;
import static com.typespec.core.CoreTestUtils.fillArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ByteCountingAsynchronousByteChannelTest {

    @Test
    public void testCtor() {
        assertThrows(NullPointerException.class, () -> new ByteCountingAsynchronousByteChannel(null, null, null));
    }

    @Test
    public void isOpenDelegates() {
        AtomicInteger openCalls = new AtomicInteger();
        AsynchronousByteChannel asynchronousByteChannel = new MockAsynchronousByteChannel() {
            @Override
            public boolean isOpen() {
                return openCalls.getAndIncrement() == 0;
            }
        };

        ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(asynchronousByteChannel,
            null, null);

        assertTrue(channel.isOpen());
        assertFalse(channel.isOpen());

        assertEquals(2, openCalls.get());
    }

    @Test
    public void closeDelegates() throws IOException {
        AtomicInteger closeCalls = new AtomicInteger();
        AsynchronousByteChannel asynchronousByteChannel = new MockAsynchronousByteChannel() {
            @Override
            public void close() throws IOException {
                closeCalls.incrementAndGet();
                super.close();
            }
        };
        ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(asynchronousByteChannel,
            null, null);

        channel.close();
        channel.close();

        assertEquals(2, closeCalls.get());
    }

    @Test
    public void canWriteAndCountBytes() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(written);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0), null, null)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                CompletableFuture<Integer> future = new CompletableFuture<>();
                channel.write(buffer, "foo", new CompletionHandler<Integer, String>() {
                    @Override
                    public void completed(Integer result, String attachment) {
                        assertEquals("foo", attachment);
                        future.complete(result);
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {
                        future.completeExceptionally(exc);
                    }
                });

                position += future.get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(0, channel.getBytesRead());
            }
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canWriteAndCountBytesWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(written);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0), null, null)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);
                position += channel.write(buffer).get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(0, channel.getBytesRead());
            }
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canWriteAndCountBytesWithPartialWrites() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(written);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            new PartialWriteAsynchronousChannel(IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0)),
            null, null)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                CompletableFuture<Integer> future = new CompletableFuture<>();
                channel.write(buffer, "foo", new CompletionHandler<Integer, String>() {
                    @Override
                    public void completed(Integer result, String attachment) {
                        assertEquals("foo", attachment);
                        future.complete(result);
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {
                        future.completeExceptionally(exc);
                    }
                });

                position += future.get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(0, channel.getBytesRead());
            }
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canWriteAndCountBytesWithPartialWritesWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(written);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            new PartialWriteAsynchronousChannel(IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0)),
            null, null)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                position += channel.write(buffer).get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(0, channel.getBytesRead());
            }
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canWriteAndCountBytesWithProgressReporting() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(written);

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0), readProgressReporter,
            writeProgressReporter)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                CompletableFuture<Integer> future = new CompletableFuture<>();
                channel.write(buffer, "foo", new CompletionHandler<Integer, String>() {
                    @Override
                    public void completed(Integer result, String attachment) {
                        assertEquals("foo", attachment);
                        future.complete(result);
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {
                        future.completeExceptionally(exc);
                    }
                });

                position += future.get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(position, writeProgresses.poll());
                assertEquals(0, channel.getBytesRead());
                assertEquals(0, readProgresses.size());
            }
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canWriteAndCountBytesWithProgressReportingWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(written);

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0),
            readProgressReporter, writeProgressReporter)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                position += channel.write(buffer).get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(position, writeProgresses.poll());
                assertEquals(0, channel.getBytesRead());
                assertEquals(0, readProgresses.size());
            }
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canWriteAndCountBytesWithProgressReportingWithPartialWrites() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(written);

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            new PartialWriteAsynchronousChannel(IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0)),
                readProgressReporter, writeProgressReporter)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                CompletableFuture<Integer> future = new CompletableFuture<>();
                channel.write(buffer, "foo", new CompletionHandler<Integer, String>() {
                    @Override
                    public void completed(Integer result, String attachment) {
                        assertEquals("foo", attachment);
                        future.complete(result);
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {
                        future.completeExceptionally(exc);
                    }
                });

                position += future.get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(position, writeProgresses.poll());
                assertEquals(0, channel.getBytesRead());
                assertEquals(0, readProgresses.size());
            }
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canWriteAndCountBytesWithProgressReportingWithPartialWritesWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        byte[] written = new byte[data.length];
        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(written);

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            new PartialWriteAsynchronousChannel(IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0)),
            readProgressReporter, writeProgressReporter)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                position += channel.write(buffer).get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(position, writeProgresses.poll());
                assertEquals(0, channel.getBytesRead());
                assertEquals(0, readProgresses.size());
            }
        }

        assertArraysEqual(data, written);
    }

    @Test
    public void canReadAndCountBytes() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(data, data.length);
        ByteBuffer readData = ByteBuffer.allocate(data.length);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0), null, null)) {

            int position = 0;
            int read = 0;
            while (read >= 0) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                ByteBuffer buffer = ByteBuffer.allocate(size);
                CompletableFuture<Integer> future = new CompletableFuture<>();
                channel.read(buffer, "foo", new CompletionHandler<Integer, String>() {
                    @Override
                    public void completed(Integer result, String attachment) {
                        assertEquals("foo", attachment);
                        future.complete(result);
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {
                        future.completeExceptionally(exc);
                    }
                });

                read = future.get();
                if (read >= 0) {
                    buffer.flip();
                    readData.put(buffer);
                    position += read;
                    assertEquals(position, channel.getBytesRead());
                    assertEquals(0, channel.getBytesWritten());
                }
            }
        }

        assertArraysEqual(data, readData.array());
    }

    @Test
    public void canReadAndCountBytesWithProgressReporting() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(data, data.length);
        ByteBuffer readData = ByteBuffer.allocate(data.length);

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0),
            readProgressReporter, writeProgressReporter)) {

            int position = 0;
            int read = 0;
            while (read >= 0) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                ByteBuffer buffer = ByteBuffer.allocate(size);
                CompletableFuture<Integer> future = new CompletableFuture<>();
                channel.read(buffer, "foo", new CompletionHandler<Integer, String>() {
                    @Override
                    public void completed(Integer result, String attachment) {
                        assertEquals("foo", attachment);
                        future.complete(result);
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {
                        future.completeExceptionally(exc);
                    }
                });

                read = future.get();
                if (read >= 0) {
                    buffer.flip();
                    readData.put(buffer);
                    position += read;
                    assertEquals(position, channel.getBytesRead());
                    assertEquals(position, readProgresses.poll());
                    assertEquals(0, channel.getBytesWritten());
                    assertEquals(0, writeProgresses.size());
                }
            }
        }

        assertArraysEqual(data, readData.array());
    }

    @Test
    public void canReadAndCountBytesWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(data, data.length);
        ByteBuffer readData = ByteBuffer.allocate(data.length);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0), null, null)) {

            int position = 0;
            int read = 0;
            while (read >= 0) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                ByteBuffer buffer = ByteBuffer.allocate(size);
                read = channel.read(buffer).get();
                if (read >= 0) {
                    buffer.flip();
                    readData.put(buffer);
                    position += read;
                    assertEquals(position, channel.getBytesRead());
                    assertEquals(0, channel.getBytesWritten());
                }
            }
        }

        assertArraysEqual(data, readData.array());
    }

    @Test
    public void canReadAndCountBytesWithFutureWithProgressReporting() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        fillArray(data);

        MockAsynchronousFileChannel mockAsynchronousByteChannel = new MockAsynchronousFileChannel(data, data.length);
        ByteBuffer readData = ByteBuffer.allocate(data.length);

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(mockAsynchronousByteChannel, 0),
            readProgressReporter, writeProgressReporter)) {

            int position = 0;
            int read = 0;
            while (read >= 0) {
                int size = 1 + ThreadLocalRandom.current().nextInt(128);
                ByteBuffer buffer = ByteBuffer.allocate(size);
                read = channel.read(buffer).get();
                if (read >= 0) {
                    buffer.flip();
                    readData.put(buffer);
                    position += read;
                    assertEquals(position, channel.getBytesRead());
                    assertEquals(position, readProgresses.poll());
                    assertEquals(0, channel.getBytesWritten());
                    assertEquals(0, writeProgresses.size());
                }
            }
        }

        assertArraysEqual(data, readData.array());
    }
}
