// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.http.netty4.mocking.MockChannel;
import io.clientcore.http.netty4.mocking.MockChannelHandlerContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.LastHttpContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static io.clientcore.http.netty4.TestUtils.assertArraysEqual;
import static io.clientcore.http.netty4.TestUtils.createChannelWithReadHandling;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Netty4ChannelInputStream}.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class Netty4Http11ChannelInputStreamTests {
    @Test
    public void nullEagerContentResultsInEmptyInitialCurrentBuffer() throws IOException {
        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(null, createCloseableChannel(), false, null)) {
            assertEquals(0, channelInputStream.getCurrentBuffer().length);
        }
    }

    @Test
    public void emptyEagerContentResultsInEmptyInitialCurrentBuffer() throws IOException {
        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(new ByteArrayOutputStream(), createCloseableChannel(), false, null)) {
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
        Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(eagerContent, new MockChannel(), false, null);

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
    public void readConsumesCurrentBufferAndRequestsMoreData() throws IOException {
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
            }), false, null);

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
            = new Netty4ChannelInputStream(eagerContent, createCloseableChannel(), false, null)) {
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
    public void largeReadTriggersMultipleChannelReads() throws IOException {
        byte[] expected = new byte[8192];
        ThreadLocalRandom.current().nextBytes(expected);

        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(null, createChannelThatReads8Kb(expected), false, null)) {
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
    public void largeSkipTriggersMultipleChannelReads() throws IOException {
        byte[] expected = new byte[8192];
        ThreadLocalRandom.current().nextBytes(expected);

        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(null, createChannelThatReads8Kb(expected), false, null)) {
            long skipped = channelInputStream.skip(8192);
            assertEquals(8192, skipped);

            assertEquals(-1, channelInputStream.read());
            assertEquals(0, channelInputStream.skip(1));
        }
    }

    @Test
    public void closingStreamTriggersOnCloseCallback() throws IOException {
        AtomicBoolean onCloseCalled = new AtomicBoolean(false);

        try (Netty4ChannelInputStream channelInputStream
            = new Netty4ChannelInputStream(null, createCloseableChannel(), false, () -> onCloseCalled.set(true))) {
            assertNotNull(channelInputStream);
        }

        assertTrue(onCloseCalled.get());
    }

    @ParameterizedTest
    @MethodSource("errorSupplier")
    public void streamPropagatesErrorFiredInChannel(Throwable expected) {
        InputStream inputStream
            = new Netty4ChannelInputStream(null, createPartialReadThenErrorChannel(expected), false, null);

        Throwable actual = assertThrows(Throwable.class, () -> inputStream.read(new byte[8192]));

        if (expected instanceof Error) {
            assertInstanceOf(Error.class, actual);
            assertEquals(expected.getMessage(), actual.getMessage());
        } else if (expected instanceof IOException) {
            assertInstanceOf(IOException.class, actual);
            assertEquals(expected.getMessage(), actual.getMessage());
        } else {
            assertInstanceOf(IOException.class, actual);
            assertInstanceOf(expected.getClass(), actual.getCause());
            assertEquals(expected.getMessage(), actual.getCause().getMessage());
        }

    }

    private static Stream<Throwable> errorSupplier() {
        // The type of error thrown by the InputStream should be consistent even if the underlying error thrown by the
        // channel changes. Unless it is an Error type, those should be thrown as-is.
        return Stream.of(new IOException("Error in response stream."),
            new ChannelException("Remote host closed connection."), new IllegalStateException("Invalid count."),
            new OutOfMemoryError());
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
        }, () -> {
        });
    }

    private static Channel createCloseableChannel(Runnable onClose, Runnable onDisconnect) {
        return new MockChannel() {
            @Override
            public ChannelFuture close() {
                onClose.run();
                return new DefaultChannelPromise(this);
            }

            @Override
            public ChannelFuture disconnect() {
                onDisconnect.run();
                return new DefaultChannelPromise(this);
            }
        };
    }

    private static Channel createPartialReadThenErrorChannel(Throwable throwable) {
        EventLoop eventLoop = new DefaultEventLoop() {
            @Override
            public boolean inEventLoop(Thread thread) {
                return true;
            }
        };

        AtomicBoolean hasRead = new AtomicBoolean();
        Channel channel = new MockChannel() {
            @Override
            public Channel read() {
                Netty4InitiateOneReadHandler handler = this.pipeline().get(Netty4InitiateOneReadHandler.class);
                MockChannelHandlerContext ctx = new MockChannelHandlerContext(this);
                if (hasRead.compareAndSet(false, true)) {
                    byte[] fourKb = new byte[4096];
                    ThreadLocalRandom.current().nextBytes(fourKb);
                    handler.channelRead(ctx, wrappedBuffer(fourKb, 0, 2048));
                    handler.channelRead(ctx, wrappedBuffer(fourKb, 2048, 2048));
                    handler.channelReadComplete(ctx);
                } else {
                    handler.exceptionCaught(ctx, throwable);
                }
                return this;
            }

            @Override
            public boolean isActive() {
                return true;
            }
        };

        try {
            eventLoop.register(channel).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return channel;
    }
}
