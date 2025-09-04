// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Netty4EagerConsumeChannelHandler}.
 */
public class Netty4EagerConsumeChannelHandlerTests {
    private static final byte[] HELLO_BYTES = "Hello".getBytes(StandardCharsets.UTF_8);

    @Test
    public void syncDrainConsumesHttp1Content() throws InterruptedException {
        ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
        CountDownLatch latch = new CountDownLatch(1);
        Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(latch,
            buf -> buf.readBytes(receivedBytes, buf.readableBytes()), false);

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.writeInbound(new DefaultHttpContent(Unpooled.wrappedBuffer(HELLO_BYTES)));
        assertFalse(latch.await(50, TimeUnit.MILLISECONDS), "Latch should not count down on partial content.");

        channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Latch should count down on last content.");

        assertArrayEquals(HELLO_BYTES, receivedBytes.toByteArray());
        assertNull(channel.pipeline().get(Netty4EagerConsumeChannelHandler.class));
    }

    @Test
    public void syncDrainConsumesHttp2Content() throws InterruptedException {
        ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
        CountDownLatch latch = new CountDownLatch(1);
        Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(latch,
            buf -> buf.readBytes(receivedBytes, buf.readableBytes()), true);

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.writeInbound(new DefaultHttp2DataFrame(Unpooled.wrappedBuffer(HELLO_BYTES), false));
        assertFalse(latch.await(50, TimeUnit.MILLISECONDS), "Latch should not count down on partial content.");

        channel.writeInbound(new DefaultHttp2DataFrame(true));
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Latch should count down on last content.");

        assertArrayEquals(HELLO_BYTES, receivedBytes.toByteArray());
        assertNull(channel.pipeline().get(Netty4EagerConsumeChannelHandler.class));
    }

    @Test
    public void asyncDrainCallsOnComplete() {
        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        Runnable onComplete = () -> onCompleteCalled.set(true);
        Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(onComplete, false);

        EmbeddedChannel channel = new EmbeddedChannel(handler);
        channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);

        channel.runPendingTasks();

        assertTrue(onCompleteCalled.get(), "onComplete should have been called.");
        assertNull(channel.pipeline().get(Netty4EagerConsumeChannelHandler.class));
    }

    @Test
    public void consumerExceptionIsCapturedByHandler() {
        IOException testException = new IOException("test");
        CountDownLatch latch = new CountDownLatch(1);
        Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(latch, buf -> {
            throw testException;
        }, false);

        EmbeddedChannel channel = new EmbeddedChannel(handler);
        ByteBuf content = Unpooled.wrappedBuffer(HELLO_BYTES);

        channel.writeInbound(content);

        Throwable capturedException = handler.channelException();

        assertNotNull(capturedException);
        assertEquals(testException, capturedException);

        assertNull(channel.pipeline().get(Netty4EagerConsumeChannelHandler.class));
    }

    @Test
    public void exceptionCaughtSignalsCompletion() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(latch, buf -> {
        }, false);

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.pipeline().fireExceptionCaught(new RuntimeException("test"));

        assertTrue(latch.await(1, TimeUnit.SECONDS), "Latch should count down on exceptionCaught.");
        assertNotNull(handler.channelException());
    }

    @Test
    public void channelInactiveSignalsCompletion() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(latch, buf -> {
        }, false);

        EmbeddedChannel channel = new EmbeddedChannel(handler);
        assertTrue(channel.isActive());

        channel.close().awaitUninterruptibly();

        assertTrue(latch.await(1, TimeUnit.SECONDS), "Latch should count down on channelInactive.");
    }

    @Test
    public void addingToInactiveChannelFiresException() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.close().awaitUninterruptibly();

        Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(() -> {
        }, false);

        channel.pipeline().addLast(handler);

        assertThrows(ClosedChannelException.class, channel::checkException);
    }

    @Test
    public void handlesByteBufMessage() {
        ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
        CountDownLatch latch = new CountDownLatch(1);
        Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(latch,
            buf -> buf.readBytes(receivedBytes, buf.readableBytes()), false);

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        channel.writeInbound(Unpooled.wrappedBuffer(HELLO_BYTES));
        channel.finishAndReleaseAll();

        assertEquals(0, latch.getCount());
        assertArrayEquals(HELLO_BYTES, receivedBytes.toByteArray());
    }
}
