// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.models.ConnectFailedException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebSocketSessionInterruptTests {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void interruptedClosePreservesInterruptAndReleasesGroup(boolean graceful) throws Exception {
        WebSocketSessionNettyImpl session = new WebSocketSessionNettyImpl(
            new ClientEndpointConfiguration("json.webpubsub.azure.v1", "test"), "ws://localhost/",
            new AtomicReference<>(new ClientLogger(WebSocketSessionInterruptTests.class)), message -> {
            }, opened -> {
            }, reason -> {
            });
        EventLoopGroup group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        Channel channel = mock(Channel.class);
        ChannelFuture closeFuture = mock(ChannelFuture.class);
        InterruptedException interrupted = new InterruptedException("Close interrupted");
        when(channel.isOpen()).thenReturn(true);
        when(channel.closeFuture()).thenReturn(closeFuture);
        when(channel.eventLoop()).thenReturn(mock(EventLoop.class));
        when(closeFuture.sync()).thenThrow(interrupted);
        when(channel.writeAndFlush(any())).thenAnswer(invocation -> {
            ReferenceCountUtil.release(invocation.getArgument(0));
            return closeFuture;
        });
        WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);
        when(handshaker.close(any(Channel.class), any(CloseWebSocketFrame.class))).thenAnswer(invocation -> {
            ReferenceCountUtil.release(invocation.getArgument(1));
            return closeFuture;
        });
        setField(session, "group", group);
        setField(session, "handshaker", handshaker);
        setField(session, "ch", channel);
        setField(session, "clientHandler",
            new WebSocketClientHandler(handshaker,
                new AtomicReference<>(new ClientLogger(WebSocketSessionInterruptTests.class)), new MessageDecoder(),
                message -> {
                }));
        try {
            ConnectFailedException error = assertThrows(ConnectFailedException.class, () -> {
                if (graceful) {
                    session.close();
                } else {
                    session.closeSocket();
                }
            });
            assertSame(interrupted, error.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(group.isShuttingDown());
        } finally {
            Thread.interrupted();
            group.shutdownGracefully(0, 0, TimeUnit.SECONDS).syncUninterruptibly();
        }
    }

    private void setField(WebSocketSessionNettyImpl session, String name, Object value) throws Exception {
        Field field = WebSocketSessionNettyImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(session, value);
    }
}
