// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.MessageEncoder;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketClientHandler}.
 */
public class WebSocketClientHandlerTests {
    private static final ClientLogger LOGGER = new ClientLogger(WebSocketClientHandlerTests.class);

    private final AtomicReference<ClientLogger> loggerReference = new AtomicReference<>(LOGGER);
    private final MessageDecoder decoder = new MessageDecoder();
    private final MessageEncoder encoder = new MessageEncoder();

    @Test
    public void pingMessageReturnsPong() {
        // Arrange
        final Consumer<WebPubSubMessage> messageConsumer = (message) -> {
        };
        final WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);

        final ChannelHandlerContext handlerContext = mock(ChannelHandlerContext.class);
        final Channel channel = mock(Channel.class);
        final ChannelPromise channelPromise = mock(ChannelPromise.class);

        when(handlerContext.channel()).thenReturn(channel);
        when(channel.closeFuture()).thenReturn(channelPromise);

        final WebSocketFrame socketFrame = new PingWebSocketFrame();
        final WebSocketClientHandler handler
            = new WebSocketClientHandler(handshaker, loggerReference, decoder, messageConsumer);
        // Act
        handler.channelRead0(handlerContext, socketFrame);

        // Assert
        verify(channel).writeAndFlush(any(PongWebSocketFrame.class));
    }

    @Test
    public void closeMessageFromClientClosesChannel() {
        // Arrange
        final Consumer<WebPubSubMessage> messageConsumer = (message) -> {
        };
        final WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);

        final ChannelHandlerContext handlerContext = mock(ChannelHandlerContext.class);
        final Channel channel = mock(Channel.class);
        final ChannelPromise channelPromise = mock(ChannelPromise.class);

        when(handlerContext.channel()).thenReturn(channel);
        when(channel.closeFuture()).thenReturn(channelPromise);

        final CloseWebSocketFrame socketFrame = new CloseWebSocketFrame(WebSocketCloseStatus.NORMAL_CLOSURE, "Text");
        final WebSocketClientHandler handler
            = new WebSocketClientHandler(handshaker, loggerReference, decoder, messageConsumer);

        // Act
        // Initiates close from client.
        final CompletableFuture<Void> closeFuture = new CompletableFuture<>();
        handler.setClientCloseCallbackFuture(closeFuture);

        handler.channelRead0(handlerContext, socketFrame);

        // Assert
        verify(channel).close();
    }

    @Test
    public void closeMessageFromServerClosesChannel() {
        // Arrange
        final Consumer<WebPubSubMessage> messageConsumer = (message) -> {
        };
        final WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);

        final ChannelHandlerContext handlerContext = mock(ChannelHandlerContext.class);
        final Channel channel = mock(Channel.class);
        final ChannelPromise channelPromise = mock(ChannelPromise.class);

        when(handlerContext.channel()).thenReturn(channel);
        when(channel.closeFuture()).thenReturn(channelPromise);

        when(channel.writeAndFlush(any())).thenReturn(channelPromise);

        final CloseWebSocketFrame socketFrame = new CloseWebSocketFrame(WebSocketCloseStatus.NORMAL_CLOSURE, "Text");
        final WebSocketClientHandler handler
            = new WebSocketClientHandler(handshaker, loggerReference, decoder, messageConsumer);

        // Act
        handler.channelRead0(handlerContext, socketFrame);

        // Assert
        verify(channel).writeAndFlush(any(CloseWebSocketFrame.class));
    }

    @Test
    public void textSocketFrameReceive() {
        // Arrange
        final String groupMessage = "Group content";
        final GroupDataMessage groupDataMessage = new GroupDataMessage("group", WebPubSubDataFormat.TEXT,
            BinaryData.fromString(groupMessage), "userId", 10L);
        final String encoded = encoder.encode(groupDataMessage);

        final List<WebPubSubMessage> messagesList = new ArrayList<>();
        final Consumer<WebPubSubMessage> messageConsumer = (message) -> messagesList.add(message);
        final WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);

        final ChannelHandlerContext handlerContext = mock(ChannelHandlerContext.class);
        final Channel channel = mock(Channel.class);
        final ChannelPromise channelPromise = mock(ChannelPromise.class);

        when(handlerContext.channel()).thenReturn(channel);
        when(channel.closeFuture()).thenReturn(channelPromise);

        when(channel.writeAndFlush(any())).thenReturn(channelPromise);

        final TextWebSocketFrame textFrame = new TextWebSocketFrame(encoded);
        final WebSocketClientHandler handler
            = new WebSocketClientHandler(handshaker, loggerReference, decoder, messageConsumer);

        // Act
        handler.channelRead0(handlerContext, textFrame);

        // Assert
        assertEquals(1, messagesList.size());

        final WebPubSubMessage actual = messagesList.get(0);
        assertInstanceOf(GroupDataMessage.class, actual);

        final GroupDataMessage actualGroupMessage = (GroupDataMessage) actual;
        assertEquals(groupDataMessage.getDataType(), actualGroupMessage.getDataType());

        assertEquals(groupMessage, actualGroupMessage.getData().toString());
    }

    @Test
    public void textSocketFrameMultipleFrames() {
        // Arrange
        final String groupMessage = "Multiple frame message";
        final GroupDataMessage groupDataMessage = new GroupDataMessage("test-group", WebPubSubDataFormat.TEXT,
            BinaryData.fromString(groupMessage), "test-user-id", 10L);
        final String encoded = encoder.encode(groupDataMessage);
        final int split = Math.floorDiv(encoded.length(), 3);
        final int secondLength = 2 * split;

        final String part1 = encoded.substring(0, split);
        final String part2 = encoded.substring(split, secondLength);
        final String part3 = encoded.substring(secondLength);

        // Make sure it split properly.
        final String parts = part1 + part2 + part3;
        assertEquals(encoded, parts);

        final List<WebPubSubMessage> messagesList = new ArrayList<>();
        final Consumer<WebPubSubMessage> messageConsumer = (message) -> messagesList.add(message);
        final WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);

        final ChannelHandlerContext handlerContext = mock(ChannelHandlerContext.class);
        final Channel channel = mock(Channel.class);
        final ChannelPromise channelPromise = mock(ChannelPromise.class);

        when(handlerContext.channel()).thenReturn(channel);
        when(channel.closeFuture()).thenReturn(channelPromise);

        when(channel.writeAndFlush(any())).thenReturn(channelPromise);

        final TextWebSocketFrame textFrame = new TextWebSocketFrame(false, 0, part1);
        final ContinuationWebSocketFrame textFrame2 = new ContinuationWebSocketFrame(false, 0, part2);
        final ContinuationWebSocketFrame textFrame3 = new ContinuationWebSocketFrame(true, 0, part3);
        final WebSocketClientHandler handler
            = new WebSocketClientHandler(handshaker, loggerReference, decoder, messageConsumer);

        // Act
        handler.channelRead0(handlerContext, textFrame);
        handler.channelRead0(handlerContext, textFrame2);
        handler.channelRead0(handlerContext, textFrame3);

        // Assert
        assertEquals(1, messagesList.size());

        final WebPubSubMessage actual = messagesList.get(0);
        assertInstanceOf(GroupDataMessage.class, actual);

        final GroupDataMessage actualGroupMessage = (GroupDataMessage) actual;
        assertEquals(groupDataMessage.getDataType(), actualGroupMessage.getDataType());

        assertEquals(groupMessage, actualGroupMessage.getData().toString());
    }
}
