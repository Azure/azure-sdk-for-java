// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class WebSocketCloseTimeoutTests {
    @Test
    public void unresponsivePeerDoesNotPreventClose() throws Exception {
        try (WebSocketSessionTestServer server = new WebSocketSessionTestServer("json.webpubsub.azure.v1", false)) {
            WebSocketSessionNettyImpl session = createSession(server.getUrl());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                session.connect();
                Future<?> closing = executor.submit(session::close);
                closing.get(15, TimeUnit.SECONDS);
                assertFalse(session.isOpen());
            } finally {
                executor.shutdownNow();
                WebSocketSessionTestServer.cleanup(session);
            }
        }
    }

    @Test
    public void failedCloseFrameWriteClosesTransportImmediately() throws Exception {
        try (WebSocketSessionTestServer server = new WebSocketSessionTestServer()) {
            WebSocketSessionNettyImpl session = createSession(server.getUrl());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                session.connect();
                Channel channel = WebSocketSessionTestServer.getField(session, "ch", Channel.class);
                channel.eventLoop().submit(() -> channel.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext context, Object message, ChannelPromise promise)
                        throws Exception {
                        if (message instanceof CloseWebSocketFrame) {
                            ReferenceCountUtil.release(message);
                            promise.setFailure(new IOException("Close frame write failed"));
                        } else {
                            super.write(context, message, promise);
                        }
                    }
                })).sync();
                Future<?> closing = executor.submit(session::close);
                closing.get(5, TimeUnit.SECONDS);
                assertFalse(channel.isOpen());
            } finally {
                executor.shutdownNow();
                WebSocketSessionTestServer.cleanup(session);
            }
        }
    }

    private WebSocketSessionNettyImpl createSession(String url) {
        return WebSocketSessionTestServer.createSession(url, opened -> {
        }, reason -> {
        });
    }
}
