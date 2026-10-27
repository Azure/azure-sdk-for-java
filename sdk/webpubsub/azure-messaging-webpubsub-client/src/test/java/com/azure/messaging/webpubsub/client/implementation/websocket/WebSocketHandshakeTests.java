// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebSocketHandshakeTests {
    @Test
    public void handshakeTimesOutWhenServerDoesNotRespond() {
        WebSocketClientHandler handler = createHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        try {
            assertFalse(handler.handshakeFuture().isDone());
            channel.advanceTimeBy(11, TimeUnit.SECONDS);
            channel.runScheduledPendingTasks();
            assertInstanceOf(TimeoutException.class, handler.handshakeFuture().cause());
            assertFalse(channel.isOpen());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void disconnectFailsPendingHandshake() {
        WebSocketClientHandler handler = createHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        try {
            channel.close();
            assertInstanceOf(ClosedChannelException.class, handler.handshakeFuture().cause());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void failedHandshakeWriteClosesChannel() {
        IOException failure = new IOException("Handshake write failed");
        WebSocketClientHandler handler = createHandler(failure);
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        try {
            assertSame(failure, handler.handshakeFuture().cause());
            assertFalse(channel.isOpen());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void completedHandshakeCancelsTimeout() {
        WebSocketClientHandler handler = createHandler(null);
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        try {
            ((ChannelPromise) handler.handshakeFuture()).setSuccess();
            channel.advanceTimeBy(11, TimeUnit.SECONDS);
            channel.runScheduledPendingTasks();
            assertTrue(channel.isOpen());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private WebSocketClientHandler createHandler(IOException writeFailure) {
        WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);
        when(handshaker.handshake(any(Channel.class))).thenAnswer(invocation -> {
            Channel channel = invocation.getArgument(0);
            return writeFailure == null ? channel.newSucceededFuture() : channel.newFailedFuture(writeFailure);
        });
        return new WebSocketClientHandler(handshaker,
            new AtomicReference<>(new ClientLogger(WebSocketHandshakeTests.class)), new MessageDecoder(), message -> {
            });
    }
}
