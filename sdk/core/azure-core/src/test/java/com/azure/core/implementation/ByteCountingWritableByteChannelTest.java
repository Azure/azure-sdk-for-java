// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.PartialWriteChannel;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.mocking.MockWritableByteChannel;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ByteCountingWritableByteChannelTest {

    private static final Random RANDOM = new Random();

    @Test
    public void testCtor() {
        assertThrows(NullPointerException.class, () -> new ByteCountingWritableByteChannel(null, null));
    }

    @Test
    public void isOpenDelegates() {
        AtomicInteger openCalls = new AtomicInteger();
        WritableByteChannel writableByteChannel = new MockWritableByteChannel() {
            @Override
            public boolean isOpen() {
                return openCalls.getAndIncrement() == 0;
            }
        };
        ByteCountingWritableByteChannel channel = new ByteCountingWritableByteChannel(writableByteChannel, null);

        assertTrue(channel.isOpen());
        assertFalse(channel.isOpen());

        assertEquals(2, openCalls.get());
    }

    @Test
    public void closeDelegates() throws IOException {
        AtomicInteger closeCalls = new AtomicInteger();
        WritableByteChannel writableByteChannel = new MockWritableByteChannel() {
            @Override
            public void close() throws IOException {
                closeCalls.incrementAndGet();
                super.close();
            }
        };
        ByteCountingWritableByteChannel channel = new ByteCountingWritableByteChannel(writableByteChannel, null);

        channel.close();
        channel.close();

        assertEquals(2, closeCalls.get());
    }

    @Test
    public void canWriteAndCountBytes() throws IOException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteCountingWritableByteChannel channel = new ByteCountingWritableByteChannel(Channels.newChannel(bos), null);

        int position = 0;
        while (position < data.length) {
            int size = 1 + RANDOM.nextInt(128);
            size = Math.min(size, data.length - position);
            ByteBuffer buffer = ByteBuffer.wrap(data, position, size);
            int written = channel.write(buffer);
            position += written;
            assertEquals(position, channel.getBytesWritten());
        }

        assertArrayEquals(data, bos.toByteArray());
    }

    @Test
    public void canWriteAndCountBytesWithPartialWrites() throws IOException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteCountingWritableByteChannel channel = new ByteCountingWritableByteChannel(
            new PartialWriteChannel(Channels.newChannel(bos)), null);

        int position = 0;
        while (position < data.length) {
            int size = 1 + RANDOM.nextInt(128);
            size = Math.min(size, data.length - position);
            ByteBuffer buffer = ByteBuffer.wrap(data, position, size);
            int written = channel.write(buffer);
            position += written;
            assertEquals(position, channel.getBytesWritten());
        }

        assertArrayEquals(data, bos.toByteArray());
    }

    @Test
    public void canWriteAndCountBytesWithProgressReporting() throws IOException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ConcurrentLinkedQueue<Long> progresses = new ConcurrentLinkedQueue<>();
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(progresses::add);
        ByteCountingWritableByteChannel channel = new ByteCountingWritableByteChannel(Channels.newChannel(bos), progressReporter);

        int position = 0;
        while (position < data.length) {
            int size = 1 + RANDOM.nextInt(128);
            size = Math.min(size, data.length - position);
            ByteBuffer buffer = ByteBuffer.wrap(data, position, size);
            int written = channel.write(buffer);
            position += written;
            assertEquals(position, progresses.poll());
            assertEquals(position, channel.getBytesWritten());
        }

        assertArrayEquals(data, bos.toByteArray());
    }

    @Test
    public void canWriteAndCountBytesWithProgressReportingWithPartialWrites() throws IOException {
        byte[] data = new byte[10 * 1204 + 127];
        RANDOM.nextBytes(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ConcurrentLinkedQueue<Long> progresses = new ConcurrentLinkedQueue<>();
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(progresses::add);
        ByteCountingWritableByteChannel channel = new ByteCountingWritableByteChannel(
            new PartialWriteChannel(Channels.newChannel(bos)), progressReporter);

        int position = 0;
        while (position < data.length) {
            int size = 1 + RANDOM.nextInt(128);
            size = Math.min(size, data.length - position);
            ByteBuffer buffer = ByteBuffer.wrap(data, position, size);
            int written = channel.write(buffer);
            position += written;
            assertEquals(position, progresses.poll());
            assertEquals(position, channel.getBytesWritten());
        }

        assertArrayEquals(data, bos.toByteArray());
    }
}
