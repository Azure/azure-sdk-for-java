// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AsynchronousFileChannelAdapterTest {

    private static final Random RANDOM = new Random();

    @Test
    public void closeDelegates() throws IOException {
        AsynchronousFileChannel fileChannelMock = Mockito.mock(AsynchronousFileChannel.class);
        AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(fileChannelMock, 0);

        channel.close();

        Mockito.verify(fileChannelMock).close();
    }

    @Test
    public void isOpenDelegates() throws IOException {
        AsynchronousFileChannel fileChannelMock = Mockito.mock(AsynchronousFileChannel.class);
        AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(fileChannelMock, 0);
        Mockito.when(fileChannelMock.isOpen()).thenReturn(true, false);

        assertTrue(channel.isOpen());
        assertFalse(channel.isOpen());

        Mockito.verify(fileChannelMock, Mockito.times(2)).isOpen();
    }

    @Test
    public void testReadWithCallback() throws IOException, InterruptedException {
        byte[] data = new byte[1024];
        RANDOM.nextBytes(data);
        Path tempFile = prepareForReading(data);

        ByteBuffer readData = ByteBuffer.allocate(data.length);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), 0)) {
            CountDownLatch latch = new CountDownLatch(1);
            readWithCallback(channel, readData, latch);
            latch.await(60, TimeUnit.SECONDS);
        }
        readData.flip();
        assertArrayEquals(data, readData.array());
    }

    @Test
    public void testReadWithCallbackAndOffset() throws IOException, InterruptedException {
        byte[] data = new byte[1024];
        RANDOM.nextBytes(data);
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
        assertArrayEquals(Arrays.copyOfRange(data, offset, data.length), readData.array());
    }

    private static void readWithCallback(AsynchronousByteChannel channel, ByteBuffer aggregator, CountDownLatch latch) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + RANDOM.nextInt(127));
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
        RANDOM.nextBytes(data);
        Path tempFile = prepareForReading(data);

        ByteBuffer readData = ByteBuffer.allocate(data.length);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), 0)) {
            int read;
            do {
                ByteBuffer buffer = ByteBuffer.allocate(1 + RANDOM.nextInt(127));
                read = channel.read(buffer).get();
                buffer.flip();
                readData.put(buffer);
            } while (read >= 0);
        }
        readData.flip();
        assertArrayEquals(data, readData.array());
    }

    @Test
    public void testReadWithWithFutureAndOffset() throws IOException, InterruptedException, ExecutionException {
        byte[] data = new byte[1024];
        RANDOM.nextBytes(data);
        Path tempFile = prepareForReading(data);
        int offset = 117;

        ByteBuffer readData = ByteBuffer.allocate(data.length - offset);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.READ), offset)) {
            int read;
            do {
                ByteBuffer buffer = ByteBuffer.allocate(1 + RANDOM.nextInt(127));
                read = channel.read(buffer).get();
                buffer.flip();
                readData.put(buffer);
            } while (read >= 0);
        }
        readData.flip();
        assertArrayEquals(Arrays.copyOfRange(data, offset, data.length), readData.array());
    }

    @Test
    public void testWriteWithCallback() throws IOException, InterruptedException {
        byte[] data = new byte[1024];
        RANDOM.nextBytes(data);
        Path tempFile = prepareForWriting();

        ByteBuffer writeData = ByteBuffer.wrap(data);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
            CountDownLatch latch = new CountDownLatch(1);
            writeWithCallback(channel, writeData, latch);
            latch.await(60, TimeUnit.SECONDS);
        }

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void testWriteWithCallbackAndOffset() throws IOException, InterruptedException {
        byte[] data = new byte[1024];
        RANDOM.nextBytes(data);
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


        assertArrayEquals(expectedData.array(), Files.readAllBytes(tempFile));
    }

    private static void writeWithCallback(AsynchronousByteChannel channel, ByteBuffer data, CountDownLatch latch) {
        if (!data.hasRemaining()) {
            latch.countDown();
            return;
        }
        byte[] buffer = new byte[Math.min(1 + RANDOM.nextInt(127), data.remaining())];
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
        RANDOM.nextBytes(data);
        Path tempFile = prepareForWriting();

        ByteBuffer writeData = ByteBuffer.wrap(data);
        try (AsynchronousByteChannel channel = new AsynchronousFileChannelAdapter(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
            while (writeData.hasRemaining()) {
                byte[] buffer = new byte[Math.min(1 + RANDOM.nextInt(127), writeData.remaining())];
                writeData.get(buffer);
                channel.write(ByteBuffer.wrap(buffer)).get();
            }
        }

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void testWriteWithFutureAndOffset() throws IOException, InterruptedException, ExecutionException {
        byte[] data = new byte[1024];
        RANDOM.nextBytes(data);
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
                byte[] buffer = new byte[Math.min(1 + RANDOM.nextInt(127), writeData.remaining())];
                writeData.get(buffer);
                channel.write(ByteBuffer.wrap(buffer)).get();
            }
        }


        assertArrayEquals(expectedData.array(), Files.readAllBytes(tempFile));
    }

    @Test
    public void doesNotAllowConcurrentOperations() {
        // Mock is no-op. It will not complete operations.
        AsynchronousFileChannel fileChannelMock = Mockito.mock(AsynchronousFileChannel.class);
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
