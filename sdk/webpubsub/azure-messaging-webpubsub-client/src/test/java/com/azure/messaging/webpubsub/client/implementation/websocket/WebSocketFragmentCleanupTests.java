// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebSocketFragmentCleanupTests {
    @Test
    public void closingChannelDiscardsUnfinishedMessage() {
        assertUnfinishedMessageIsDiscarded(false);
    }

    @Test
    public void removingHandlerDiscardsUnfinishedMessage() {
        assertUnfinishedMessageIsDiscarded(true);
    }

    private void assertUnfinishedMessageIsDiscarded(boolean removeHandler) {
        WebSocketClientHandshaker handshaker = mock(WebSocketClientHandshaker.class);
        when(handshaker.isHandshakeComplete()).thenReturn(true);
        when(handshaker.handshake(any(Channel.class))).thenAnswer(invocation -> {
            Channel connectedChannel = invocation.getArgument(0);
            return connectedChannel.newSucceededFuture();
        });
        List<WebPubSubMessage> messages = new ArrayList<>();
        WebSocketClientHandler handler = new WebSocketClientHandler(handshaker,
            new AtomicReference<>(new ClientLogger(WebSocketFragmentCleanupTests.class)), new MessageDecoder(),
            messages::add);
        EmbeddedChannel channel = new EmbeddedChannel(handler);
        // The JSON is valid, but FIN is false: a WebSocket message has not completed.
        TextWebSocketFrame frame = new TextWebSocketFrame(false, 0, "{\"type\":\"ack\",\"ackId\":1,\"success\":true}");
        try {
            channel.writeInbound(frame);
            assertTrue(messages.isEmpty());
            if (removeHandler) {
                channel.pipeline().remove(handler);
            } else {
                channel.close();
            }
            assertTrue(messages.isEmpty());
            assertEquals(0, frame.refCnt());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
