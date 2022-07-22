// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.io.IOUtils;
import com.azure.core.util.PartialWriteAsynchronousChannel;
import com.azure.core.util.ProgressReporter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ByteCountingAsynchronousByteChannelTest {

    private static final Random RANDOM = new Random();

    @Test
    public void testCtor() {
        assertThrows(NullPointerException.class, () -> new ByteCountingAsynchronousByteChannel(null, null, null));
    }

    @Test
    public void isOpenDelegates() {
        AsynchronousByteChannel asynchronousByteChannel = Mockito.mock(AsynchronousByteChannel.class);
        Mockito.when(asynchronousByteChannel.isOpen()).thenReturn(true, false);
        ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(asynchronousByteChannel, null, null);

        assertTrue(channel.isOpen());
        assertFalse(channel.isOpen());

        Mockito.verify(asynchronousByteChannel, Mockito.times(2)).isOpen();
    }

    @Test
    public void closeDelegates() throws IOException {
        AsynchronousByteChannel asynchronousByteChannel = Mockito.mock(AsynchronousByteChannel.class);
        ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(asynchronousByteChannel, null, null);

        channel.close();
        channel.close();

        Mockito.verify(asynchronousByteChannel, Mockito.times(2)).close();
    }

    @Test
    public void canWriteAndCountBytes() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
            null, null)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + RANDOM.nextInt(128);
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

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canWriteAndCountBytesWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
            null, null)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + RANDOM.nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);
                position += channel.write(buffer).get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(0, channel.getBytesRead());
            }
        }

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canWriteAndCountBytesWithPartialWrites() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();

        try (ByteCountingAsynchronousByteChannel channel =
                new ByteCountingAsynchronousByteChannel(new PartialWriteAsynchronousChannel(
                    IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)),
                    null, null)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + RANDOM.nextInt(128);
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

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canWriteAndCountBytesWithPartialWritesWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();

        try (ByteCountingAsynchronousByteChannel channel =
                new ByteCountingAsynchronousByteChannel(new PartialWriteAsynchronousChannel(
                    IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)),
                    null, null)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + RANDOM.nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                position += channel.write(buffer).get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(0, channel.getBytesRead());
            }
        }

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canWriteAndCountBytesWithProgressReporting() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
            readProgressReporter, writeProgressReporter)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + RANDOM.nextInt(128);
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

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canWriteAndCountBytesWithProgressReportingWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0),
            readProgressReporter, writeProgressReporter)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + RANDOM.nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                position += channel.write(buffer).get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(position, writeProgresses.poll());
                assertEquals(0, channel.getBytesRead());
                assertEquals(0, readProgresses.size());
            }
        }

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canWriteAndCountBytesWithProgressReportingWithPartialWrites() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel =
                new ByteCountingAsynchronousByteChannel(new PartialWriteAsynchronousChannel(
                    IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)),
                    readProgressReporter, writeProgressReporter)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + RANDOM.nextInt(128);
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

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canWriteAndCountBytesWithProgressReportingWithPartialWritesWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel =
                new ByteCountingAsynchronousByteChannel(new PartialWriteAsynchronousChannel(
                    IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)),
                    readProgressReporter, writeProgressReporter)) {

            int position = 0;
            while (position < data.length) {
                int size = 1 + RANDOM.nextInt(128);
                size = Math.min(size, data.length - position);
                ByteBuffer buffer = ByteBuffer.wrap(data, position, size);

                position += channel.write(buffer).get();
                assertEquals(position, channel.getBytesWritten());
                assertEquals(position, writeProgresses.poll());
                assertEquals(0, channel.getBytesRead());
                assertEquals(0, readProgresses.size());
            }
        }

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canReadAndCountBytes() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, data);
        ByteBuffer readData = ByteBuffer.allocate(data.length);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), 0),
            null, null)) {

            int position = 0;
            int read = 0;
            while (read >= 0) {
                int size = 1 + RANDOM.nextInt(128);
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

        assertArrayEquals(data, readData.array());
    }

    @Test
    public void canReadAndCountBytesWithProgressReporting() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, data);
        ByteBuffer readData = ByteBuffer.allocate(data.length);

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), 0),
            readProgressReporter, writeProgressReporter)) {

            int position = 0;
            int read = 0;
            while (read >= 0) {
                int size = 1 + RANDOM.nextInt(128);
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

        assertArrayEquals(data, readData.array());
    }

    @Test
    public void canReadAndCountBytesWithFuture() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, data);
        ByteBuffer readData = ByteBuffer.allocate(data.length);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), 0),
            null, null)) {

            int position = 0;
            int read = 0;
            while (read >= 0) {
                int size = 1 + RANDOM.nextInt(128);
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

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canReadAndCountBytesWithFutureWithProgressReporting() throws IOException, ExecutionException, InterruptedException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        Path tempFile = Files.createTempFile("bytecountingtest", null);
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, data);
        ByteBuffer readData = ByteBuffer.allocate(data.length);

        ConcurrentLinkedQueue<Long> writeProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter writeProgressReporter = ProgressReporter.withProgressListener(writeProgresses::add);
        ConcurrentLinkedQueue<Long> readProgresses = new ConcurrentLinkedQueue<>();
        ProgressReporter readProgressReporter = ProgressReporter.withProgressListener(readProgresses::add);

        try (ByteCountingAsynchronousByteChannel channel = new ByteCountingAsynchronousByteChannel(
            IOUtils.toAsynchronousByteChannel(AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), 0),
            readProgressReporter, writeProgressReporter)) {

            int position = 0;
            int read = 0;
            while (read >= 0) {
                int size = 1 + RANDOM.nextInt(128);
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

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }
}
