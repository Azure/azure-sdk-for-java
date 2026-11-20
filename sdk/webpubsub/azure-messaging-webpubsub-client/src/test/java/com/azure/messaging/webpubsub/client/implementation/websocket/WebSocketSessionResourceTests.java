// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebSocketSessionResourceTests {
    @Test
    public void failedConnectionReleasesEventLoopGroup() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        WebSocketSessionNettyImpl session = createSession("ws://127.0.0.1:" + port + "/");
        try {
            assertThrows(ConnectException.class, session::connect);
            assertGroupTerminates(session);
        } finally {
            WebSocketSessionTestServer.cleanup(session);
        }
    }

    @Test
    public void failedHandshakeReleasesEventLoopGroup() throws Exception {
        try (WebSocketSessionTestServer server = new WebSocketSessionTestServer("unsupported", true)) {
            WebSocketSessionNettyImpl session = createSession(server.getUrl());
            try {
                assertThrows(WebSocketHandshakeException.class, session::connect);
                assertGroupTerminates(session);
            } finally {
                WebSocketSessionTestServer.cleanup(session);
            }
        }
    }

    @Test
    public void remoteDisconnectReleasesEventLoopGroup() throws Exception {
        try (WebSocketSessionTestServer server = new WebSocketSessionTestServer()) {
            WebSocketSessionNettyImpl session = createSession(server.getUrl());
            try {
                session.connect();
                server.getConnection().close().sync();
                assertGroupTerminates(session);
            } finally {
                WebSocketSessionTestServer.cleanup(session);
            }
        }
    }

    private WebSocketSessionNettyImpl createSession(String url) {
        return WebSocketSessionTestServer.createSession(url, session -> {
        }, reason -> {
        });
    }

    private void assertGroupTerminates(WebSocketSessionNettyImpl session) throws Exception {
        EventLoopGroup group = WebSocketSessionTestServer.getField(session, "group", EventLoopGroup.class);
        assertTrue(group.terminationFuture().await(5, TimeUnit.SECONDS), "Session event loops must terminate");
    }
}
