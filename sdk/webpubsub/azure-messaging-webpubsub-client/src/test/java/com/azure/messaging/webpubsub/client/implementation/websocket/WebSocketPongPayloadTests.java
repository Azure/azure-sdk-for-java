// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebSocketPongPayloadTests {
    @ParameterizedTest
    @ValueSource(ints = { 0, 125 })
    public void pongEchoesPingPayloadAndReleasesIt(int payloadSize) {
        byte[] expected = new byte[payloadSize];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = (byte) (i + 128);
        }
        WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);
        when(handshaker.isHandshakeComplete()).thenReturn(true);
        when(handshaker.handshake(any(Channel.class))).thenAnswer(invocation -> {
            Channel connectedChannel = invocation.getArgument(0);
            return connectedChannel.newSucceededFuture();
        });
        WebSocketClientHandler handler = new WebSocketClientHandler(handshaker,
            new AtomicReference<>(new ClientLogger(WebSocketPongPayloadTests.class)), new MessageDecoder(), message -> {
            });
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        ByteBuf payload = Unpooled.buffer(payloadSize + 1).writeByte(1).writeBytes(expected);
        payload.readByte();
        try {
            channel.writeInbound(new PingWebSocketFrame(payload));
            PongWebSocketFrame pong = channel.readOutbound();
            try {
                byte[] actual = new byte[pong.content().readableBytes()];
                pong.content().readBytes(actual);
                assertArrayEquals(expected, actual);
            } finally {
                pong.release();
            }
            assertEquals(0, payload.refCnt());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
