// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WebSocketSessionCallbackTests {
    @Test
    public void openCallbackFailureCompletesConnect() throws Exception {
        try (WebSocketSessionTestServer server = new WebSocketSessionTestServer()) {
            IllegalStateException failure = new IllegalStateException("Open callback failed");
            WebSocketSessionNettyImpl session = WebSocketSessionTestServer.createSession(server.getUrl(), opened -> {
                throw failure;
            }, reason -> {
            });
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<Void> connecting = executor.submit(() -> {
                    session.connect();
                    return null;
                });
                ExecutionException error
                    = assertThrows(ExecutionException.class, () -> connecting.get(5, TimeUnit.SECONDS));
                assertSame(failure, rootCause(error));
            } finally {
                executor.shutdownNow();
                WebSocketSessionTestServer.cleanup(session);
            }
        }
    }

    @Test
    public void closeCallbackFailureCompletesCloseAndReleasesFrame() throws Exception {
        try (WebSocketSessionTestServer server = new WebSocketSessionTestServer()) {
            IllegalStateException failure = new IllegalStateException("Close callback failed");
            WebSocketSessionNettyImpl session = WebSocketSessionTestServer.createSession(server.getUrl(), opened -> {
            }, reason -> {
                throw failure;
            });
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                session.connect();
                Future<?> closing = executor.submit(session::close);
                ExecutionException error
                    = assertThrows(ExecutionException.class, () -> closing.get(5, TimeUnit.SECONDS));
                assertSame(failure, rootCause(error));
                WebSocketClientHandler handler
                    = WebSocketSessionTestServer.getField(session, "clientHandler", WebSocketClientHandler.class);
                Channel channel = WebSocketSessionTestServer.getField(session, "ch", Channel.class);
                channel.eventLoop().submit(() -> {
                }).sync();
                assertEquals(0, handler.getServerCloseWebSocketFrame().refCnt());
            } finally {
                executor.shutdownNow();
                WebSocketSessionTestServer.cleanup(session);
            }
        }
    }

    private Throwable rootCause(Throwable failure) {
        while (failure.getCause() != null) {
            failure = failure.getCause();
        }
        return failure;
    }
}
