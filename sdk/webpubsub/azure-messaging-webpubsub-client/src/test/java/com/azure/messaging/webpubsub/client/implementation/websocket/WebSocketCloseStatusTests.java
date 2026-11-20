// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebSocketCloseStatusTests {
    @Test
    public void missingCloseFrameReportsAbnormalClosure() throws Exception {
        try (WebSocketSessionTestServer server = new WebSocketSessionTestServer()) {
            CompletableFuture<CloseReason> closed = new CompletableFuture<>();
            WebSocketSessionNettyImpl session = WebSocketSessionTestServer.createSession(server.getUrl(), opened -> {
            }, closed::complete);
            try {
                session.connect();
                Channel serverChannel = server.getConnection();
                serverChannel.eventLoop().submit(() -> {
                    // Bypass the server protocol handler's graceful closing handshake.
                    serverChannel.pipeline().remove(WebSocketServerProtocolHandler.class);
                    serverChannel.close();
                }).sync();
                assertEquals(1006, closed.get(5, TimeUnit.SECONDS).getCloseCode());
            } finally {
                WebSocketSessionTestServer.cleanup(session);
            }
        }
    }

    @Test
    public void receivedCloseFramePreservesStatusAndReason() throws Exception {
        try (WebSocketSessionTestServer server = new WebSocketSessionTestServer()) {
            CompletableFuture<CloseReason> closed = new CompletableFuture<>();
            WebSocketSessionNettyImpl session = WebSocketSessionTestServer.createSession(server.getUrl(), opened -> {
            }, closed::complete);
            try {
                session.connect();
                server.getConnection().writeAndFlush(new CloseWebSocketFrame(1001, "Server restarting")).sync();
                CloseReason reason = closed.get(5, TimeUnit.SECONDS);
                assertEquals(1001, reason.getCloseCode());
                assertEquals("Server restarting", reason.getReasonPhrase());
            } finally {
                WebSocketSessionTestServer.cleanup(session);
            }
        }
    }
}
