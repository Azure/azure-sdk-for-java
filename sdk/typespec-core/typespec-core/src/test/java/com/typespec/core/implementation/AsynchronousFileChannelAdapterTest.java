// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.util.mocking.MockAsynchronousFileChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.typespec.core.CoreTestUtils.assertArraysEqual;
import static com.typespec.core.CoreTestUtils.fillArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AsynchronousFileChannelAdapterTest {

    @Test
    public void closeDelegates() throws IOException {
        AtomicInteger closeCalls = new AtomicInteger();
        AsynchronousFileChannel fileChannelMock = new MockAsynchronousFileChannel() {
            @Override
            public void close() throws IOException {
                closeCalls.incrementAndGet();
                super.close();
            }
        };
        AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(fileChannelMock, 0);

        channel.close();

        assertEquals(1, closeCalls.get());
    }

    @Test
    public void isOpenDelegates() {
        AtomicInteger openCalls = new AtomicInteger();
        AsynchronousFileChannel fileChannelMock = new MockAsynchronousFileChannel() {
            @Override
            public boolean isOpen() {
                return openCalls.getAndIncrement() == 0;
            }
        };
        AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(fileChannelMock, 0);

        assertTrue(channel.isOpen());
        assertFalse(channel.isOpen());

        assertEquals(2, openCalls.get());
    }

    @Test
    public void testReadWithCallback() throws IOException, InterruptedException {
        byte[] data = new byte[1024];
        fillArray(data);
        Path tempFile = prepareForReading(data);

        ByteBuffer readData = ByteBuffer.allocate(data.length);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), 0)) {
            CountDownLatch latch = new CountDownLatch(1);
            readWithCallback(channel, readData, latch);
            latch.await(60, TimeUnit.SECONDS);
        }
        readData.flip();
        assertArraysEqual(data, readData.array());
    }

    @Test
    public void testReadWithCallbackAndOffset() throws IOException, InterruptedException {
        byte[] data = new byte[1024];
        fillArray(data);
        Path tempFile = prepareForReading(data);
        int offset = 117;

        ByteBuffer readData = ByteBuffer.allocate(data.length - offset);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), offset)) {
            CountDownLatch latch = new CountDownLatch(1);
            readWithCallback(channel, readData, latch);
            latch.await(60, TimeUnit.SECONDS);
        }
        readData.flip();
        assertArraysEqual(data, offset, data.length - offset, readData.array(), data.length - offset);
    }

    private static void readWithCallback(AsynchronousByteChannel channel, ByteBuffer aggregator, CountDownLatch latch) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + ThreadLocalRandom.current().nextInt(127));
        channel.read(buffer, "foo", new CompletionHandler<Integer, String>() {
            @Override
            public void completed(Integer result, String attachment) {
                assertEquals("foo", attachment);
                if (result >= 0) {
                    buffer.flip();
                    aggregator.put(buffer);
                    readWithCallback(channel, aggregator, latch);
                } else {
                    latch.countDown();
                }
            }

            @Override
            public void failed(Throwable exc, String attachment) {
                latch.countDown();
                fail("Unexpected failure");
            }
        });
    }

    @Test
    public void testReadWithFuture() throws IOException, InterruptedException, ExecutionException {
        byte[] data = new byte[1024];
        fillArray(data);
        Path tempFile = prepareForReading(data);

        ByteBuffer readData = ByteBuffer.allocate(data.length);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), 0)) {
            int read;
            do {
                ByteBuffer buffer = ByteBuffer.allocate(1 + ThreadLocalRandom.current().nextInt(127));
                read = channel.read(buffer).get();
                buffer.flip();
                readData.put(buffer);
            } while (read >= 0);
        }
        readData.flip();
        assertArraysEqual(data, readData.array());
    }

    @Test
    public void testReadWithWithFutureAndOffset() throws IOException, InterruptedException, ExecutionException {
        byte[] data = new byte[1024];
        fillArray(data);
        Path tempFile = prepareForReading(data);
        int offset = 117;

        ByteBuffer readData = ByteBuffer.allocate(data.length - offset);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), offset)) {
            int read;
            do {
                ByteBuffer buffer = ByteBuffer.allocate(1 + ThreadLocalRandom.current().nextInt(127));
                read = channel.read(buffer).get();
                buffer.flip();
                readData.put(buffer);
            } while (read >= 0);
        }
        readData.flip();
        assertArraysEqual(data, offset, data.length - offset, readData.array(), data.length - offset);
    }

    @Test
    public void testWriteWithCallback() throws IOException, InterruptedException {
        byte[] data = new byte[1024];
        fillArray(data);
        Path tempFile = prepareForWriting();

        ByteBuffer writeData = ByteBuffer.wrap(data);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
            CountDownLatch latch = new CountDownLatch(1);
            writeWithCallback(channel, writeData, latch);
            latch.await(60, TimeUnit.SECONDS);
        }

        assertArraysEqual(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void testWriteWithCallbackAndOffset() throws IOException, InterruptedException {
        byte[] data = new byte[1024];
        fillArray(data);
        Path tempFile = prepareForWriting();
        int offset = 117;
        ByteBuffer expectedData = ByteBuffer.allocate(data.length + offset);
        expectedData.put(new byte[offset]);
        expectedData.put(data);
        expectedData.flip();

        ByteBuffer writeData = ByteBuffer.wrap(data);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), offset)) {
            CountDownLatch latch = new CountDownLatch(1);
            writeWithCallback(channel, writeData, latch);
            latch.await(60, TimeUnit.SECONDS);
        }


        assertArraysEqual(expectedData.array(), Files.readAllBytes(tempFile));
    }

    private static void writeWithCallback(AsynchronousByteChannel channel, ByteBuffer data, CountDownLatch latch) {
        if (!data.hasRemaining()) {
            latch.countDown();
            return;
        }
        byte[] buffer = new byte[Math.min(1 + ThreadLocalRandom.current().nextInt(127), data.remaining())];
        data.get(buffer);
        channel.write(ByteBuffer.wrap(buffer), "foo", new CompletionHandler<Integer, String>() {
            @Override
            public void completed(Integer result, String attachment) {
                assertEquals("foo", attachment);
                writeWithCallback(channel, data, latch);
            }

            @Override
            public void failed(Throwable exc, String attachment) {
                latch.countDown();
                fail("Unexpected failure");
            }
        });
    }

    @Test
    public void testWriteFuture() throws IOException, InterruptedException, ExecutionException {
        byte[] data = new byte[1024];
        fillArray(data);
        Path tempFile = prepareForWriting();

        ByteBuffer writeData = ByteBuffer.wrap(data);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
            while (writeData.hasRemaining()) {
                byte[] buffer = new byte[Math.min(1 + ThreadLocalRandom.current().nextInt(127), writeData.remaining())];
                writeData.get(buffer);
                channel.write(ByteBuffer.wrap(buffer)).get();
            }
        }

        assertArraysEqual(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void testWriteWithFutureAndOffset() throws IOException, InterruptedException, ExecutionException {
        byte[] data = new byte[1024];
        fillArray(data);
        Path tempFile = prepareForWriting();
        int offset = 117;
        ByteBuffer expectedData = ByteBuffer.allocate(data.length + offset);
        expectedData.put(new byte[offset]);
        expectedData.put(data);
        expectedData.flip();

        ByteBuffer writeData = ByteBuffer.wrap(data);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), offset)) {
            while (writeData.hasRemaining()) {
                byte[] buffer = new byte[Math.min(1 + ThreadLocalRandom.current().nextInt(127), writeData.remaining())];
                writeData.get(buffer);
                channel.write(ByteBuffer.wrap(buffer)).get();
            }
        }


        assertArraysEqual(expectedData.array(), Files.readAllBytes(tempFile));
    }

    @Test
    public void doesNotAllowConcurrentOperations() {
        // Mock is no-op. It will not complete operations.
        AsynchronousFileChannel fileChannelMock = new MockAsynchronousFileChannel();
        ByteBuffer buffer = ByteBuffer.allocate(0);

        {
            AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(fileChannelMock, 0);
            channel.write(buffer);
            assertThrows(WritePendingException.class, () -> channel.write(buffer));
            assertThrows(WritePendingException.class, () -> channel.write(buffer, null, null));
            assertThrows(WritePendingException.class, () -> channel.read(buffer));
            assertThrows(WritePendingException.class, () -> channel.read(buffer, null, null));
        }

        {
            AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(fileChannelMock, 0);
            channel.write(buffer, null, null);
            assertThrows(WritePendingException.class, () -> channel.write(buffer));
            assertThrows(WritePendingException.class, () -> channel.write(buffer, null, null));
            assertThrows(WritePendingException.class, () -> channel.read(buffer));
            assertThrows(WritePendingException.class, () -> channel.read(buffer, null, null));
        }

        {
            AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(fileChannelMock, 0);
            channel.read(buffer);
            assertThrows(ReadPendingException.class, () -> channel.write(buffer));
            assertThrows(ReadPendingException.class, () -> channel.write(buffer, null, null));
            assertThrows(ReadPendingException.class, () -> channel.read(buffer));
            assertThrows(ReadPendingException.class, () -> channel.read(buffer, null, null));
        }

        {
            AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(fileChannelMock, 0);
            channel.read(buffer, null, null);
            assertThrows(ReadPendingException.class, () -> channel.write(buffer));
            assertThrows(ReadPendingException.class, () -> channel.write(buffer, null, null));
            assertThrows(ReadPendingException.class, () -> channel.read(buffer));
            assertThrows(ReadPendingException.class, () -> channel.read(buffer, null, null));
        }

    }

    private Path prepareForReading(byte[] data) throws IOException {
        Path tempFile = Files.createTempFile("channeladaptertest", null);
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, data);
        return tempFile;
    }

    private Path prepareForWriting() throws IOException {
        Path tempFile = Files.createTempFile("channeladaptertest", null);
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}
