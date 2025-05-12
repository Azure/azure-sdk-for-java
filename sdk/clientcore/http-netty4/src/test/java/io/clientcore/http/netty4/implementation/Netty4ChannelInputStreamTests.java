// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.http.netty4.mocking.MockChannel;
import io.clientcore.http.netty4.mocking.MockChannelHandlerContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http.LastHttpContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.clientcore.http.netty4.TestUtils.assertArraysEqual;
import static io.clientcore.http.netty4.TestUtils.createChannelWithReadHandling;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link Netty4ChannelInputStream}.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class Netty4ChannelInputStreamTests {
    @Test
    public void nullEagerContentResultsInEmptyInitialCurrentBuffer() {
        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(null, createCloseableChannel())) {
            assertEquals(0, channelInputStream.getCurrentBuffer().length);
        }
    }

    @Test
    public void emptyEagerContentResultsInEmptyInitialCurrentBuffer() {
        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(new ByteArrayOutputStream(), createCloseableChannel())) {
            assertEquals(0, channelInputStream.getCurrentBuffer().length);
        }
    }

    @Test
    public void readConsumesCurrentBufferAndHasNoMoreData() throws IOException {
        byte[] expected = new byte[32];
        ThreadLocalRandom.current().nextBytes(expected);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(expected);

        // MockChannels aren't active by default, so once the eagerContent is consumed the stream will be done.
        Netty4ChannelInputStream channelInputStream = new Netty4ChannelInputStream(eagerContent, new MockChannel());

        // Make sure the Netty4ChannelInputStream copied the eager content correctly.
        assertArraysEqual(expected, channelInputStream.getCurrentBuffer());

        int index = 0;
        byte[] actual = new byte[32];
        int b;
        while ((b = channelInputStream.read()) != -1) {
            actual[index++] = (byte) b;
        }

        assertArraysEqual(expected, actual);
    }

    @Test
    public void readConsumesCurrentBufferAndRequestsMoreData() {
        byte[] expected = new byte[32];
        ThreadLocalRandom.current().nextBytes(expected);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(expected, 0, 16);

        // MockChannels aren't active by default, so once the eagerContent is consumed the stream will be done.
        Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(eagerContent, createChannelWithReadHandling((ignored, channel) -> {
                Netty4InitiateOneReadHandler handler = channel.pipeline().get(Netty4InitiateOneReadHandler.class);
                MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);

                handler.channelRead(ctx, wrappedBuffer(expected, 16, 16));
                handler.channelRead(ctx, LastHttpContent.EMPTY_LAST_CONTENT);
                handler.channelReadComplete(ctx);
            }));

        int index = 0;
        byte[] actual = new byte[32];
        int b;
        while ((b = channelInputStream.read()) != -1) {
            actual[index++] = (byte) b;
        }

        assertArraysEqual(expected, actual);
    }

    @Test
    public void multipleSmallerSkips() throws IOException {
        byte[] expected = new byte[32];
        ThreadLocalRandom.current().nextBytes(expected);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(expected);

        // MockChannels aren't active by default, so once the eagerContent is consumed the stream will be done.
        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(eagerContent, createCloseableChannel())) {
            long skipped = channelInputStream.skip(16);
            assertEquals(16, skipped);

            // Won't be enough content remaining to skip 32 bytes.
            skipped = channelInputStream.skip(32);
            assertEquals(16, skipped);

            assertEquals(0, channelInputStream.skip(1));
        }
    }

    /**
     * Tests that when {@link Netty4ChannelInputStream#read(byte[])} or
     * {@link Netty4ChannelInputStream#read(byte[], int, int)} are called with large {@code byte[]}s the Netty Channel
     * will have reads triggered in an attempt to satisfy the read buffer.
     */
    @Test
    public void largeReadTriggersMultipleChannelReads() {
        byte[] expected = new byte[8192];
        ThreadLocalRandom.current().nextBytes(expected);

        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(null, createChannelThatReads8Kb(expected))) {
            byte[] actual = new byte[8192];
            int read = channelInputStream.read(actual);

            assertEquals(8192, read);
            assertArraysEqual(expected, actual);

            assertEquals(-1, channelInputStream.read());
        }
    }

    /**
     * Tests that when {@link Netty4ChannelInputStream#skip(long)}} is called with large skip amount the Netty Channel
     * will have reads triggered in an attempt to satisfy the skip amount.
     */
    @Test
    public void largeSkipTriggersMultipleChannelReads() {
        byte[] expected = new byte[8192];
        ThreadLocalRandom.current().nextBytes(expected);

        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(null, createChannelThatReads8Kb(expected))) {
            long skipped = channelInputStream.skip(8192);
            assertEquals(8192, skipped);

            assertEquals(-1, channelInputStream.read());
            assertEquals(0, channelInputStream.skip(1));
        }
    }

    @Test
    public void closingStreamClosesChannel() {
        AtomicInteger closeCount = new AtomicInteger();

        new Netty4ChannelInputStream(null, createCloseableChannel(closeCount::incrementAndGet)).close();

        assertEquals(1, closeCount.get());
    }

    private static Channel createChannelThatReads8Kb(byte[] eightKbOfBytes) {
        return createChannelWithReadHandling((count, channel) -> {
            Netty4InitiateOneReadHandler handler = channel.pipeline().get(Netty4InitiateOneReadHandler.class);
            MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);
            if (count == 0) {
                // First 4 KB.
                handler.channelRead(ctx, wrappedBuffer(eightKbOfBytes, 0, 2048));
                handler.channelRead(ctx, wrappedBuffer(eightKbOfBytes, 2048, 2048));
            } else if (count == 1) {
                // Empty set of buffers without Channel being done, should result in another read happening eagerly.
                handler.channelRead(ctx, Unpooled.EMPTY_BUFFER);
                handler.channelRead(ctx, Unpooled.EMPTY_BUFFER);
            } else if (count == 2) {
                // Last 4 KB and Channel completion.
                handler.channelRead(ctx, wrappedBuffer(eightKbOfBytes, 4096, 2048));
                handler.channelRead(ctx, wrappedBuffer(eightKbOfBytes, 6144, 2048));
                handler.channelRead(ctx, LastHttpContent.EMPTY_LAST_CONTENT);
            }

            handler.channelReadComplete(ctx);
        });
    }

    private static Channel createCloseableChannel() {
        return createCloseableChannel(() -> {
        });
    }

    private static Channel createCloseableChannel(Runnable onClose) {
        return new MockChannel() {
            @Override
            public ChannelFuture close() {
                onClose.run();
                return new DefaultChannelPromise(this);
            }
        };
    }
}
