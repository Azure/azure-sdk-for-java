// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;

final class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    private final AtomicReference<ClientLogger> loggerReference;
    private final MessageDecoder messageDecoder;
    private final Consumer<WebPubSubMessage> messageHandler;

    private volatile OpenTextFrame wip;

    private static final AtomicReferenceFieldUpdater<WebSocketClientHandler, OpenTextFrame> UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(WebSocketClientHandler.class, OpenTextFrame.class, "wip");

    WebSocketClientHandler(WebSocketClientHandshaker handshaker, AtomicReference<ClientLogger> loggerReference,
        MessageDecoder messageDecoder, Consumer<WebPubSubMessage> messageHandler) {
        this.handshaker = handshaker;
        this.loggerReference = loggerReference;
        this.messageDecoder = messageDecoder;
        this.messageHandler = messageHandler;
    }

    ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (handshakeFuture != null && !handshaker.isHandshakeComplete()) {
            Channel ch = ctx.channel();

            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof WebSocketFrame) {
            channelRead0(ctx, (WebSocketFrame) msg);
        } else {
            loggerReference.get().atWarning().addKeyValue("messageType", msg).log("Unknown message type. Skipping.");
        }
    }

    private void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        Channel ch = ctx.channel();

        if (msg instanceof PingWebSocketFrame) {
            // Ping, reply Pong
            loggerReference.get().atVerbose().log("Received PingWebSocketFrame");
            loggerReference.get().atVerbose().log("Send PongWebSocketFrame");
            ch.writeAndFlush(new PongWebSocketFrame());
        } else if (msg instanceof PongWebSocketFrame) {
            // Pong
            loggerReference.get().atVerbose().log("Received PongWebSocketFrame");
        } else if (msg instanceof CloseWebSocketFrame) {
            // Close
            CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) msg;
            loggerReference.get()
                .atVerbose()
                .addKeyValue("statusCode", closeFrame.statusCode())
                .addKeyValue("reasonText", closeFrame.reasonText())
                .log("Received CloseWebSocketFrame");

            this.serverCloseWebSocketFrame = closeFrame.retain();   // retain for SessionNettyImpl

            if (closeCallbackFuture == null) {
                // close initiated from server, reply CloseWebSocketFrame, then close connection
                loggerReference.get().atVerbose().log("Send CloseWebSocketFrame");
                closeFrame.retain();    // retain before write it back
                ch.writeAndFlush(closeFrame).addListener(future -> ch.close());
            } else {
                // close initiated from client, client already sent CloseWebSocketFrame
                ch.close();
            }
        } else if (msg instanceof TextWebSocketFrame || msg instanceof ContinuationWebSocketFrame) {
            processFrame(msg);
        } else {
            loggerReference.get().atWarning().addKeyValue("frameType", msg).log("Unknown WebSocketFrame type.");
        }
    }

    private void processFrame(WebSocketFrame frame) {
        updateOpenTextFrame(frame.content());

        if (!frame.isFinalFragment()) {
            return;
        }

        final OpenTextFrame existing = UPDATER.getAndSet(this, null);

        if (existing == null) {
            final WebPubSubMessage message = messageDecoder.decode(frame.content().toString());
            messageHandler.accept(message);

            return;
        }

        existing.close();

        final BinaryData collectedFrame = existing.getCollectedData();

        if (collectedFrame != null) {
            final String collected = collectedFrame.toString();

            try {
                final WebPubSubMessage deserialized = messageDecoder.decode(collected);
                messageHandler.accept(deserialized);
            } finally {
                existing.dispose();
            }
        }
    }

    private void updateOpenTextFrame(ByteBuf content) {
        content.retain();

        final OpenTextFrame frame = new OpenTextFrame();
        frame.add(content);

        if (!UPDATER.compareAndSet(this, null, frame)) {
            UPDATER.getAndUpdate(this, existing -> {
                existing.add(content);
                return existing;
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (handshakeFuture != null && !handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    // as side effect, if it is not null, the close (aka CloseWebSocketFrame) is initiated by client
    private CompletableFuture<Void> closeCallbackFuture = null;
    // the CloseWebSocketFrame from server
    private CloseWebSocketFrame serverCloseWebSocketFrame = null;

    void setClientCloseCallbackFuture(CompletableFuture<Void> callbackFuture) {
        this.closeCallbackFuture = callbackFuture;
    }

    public CompletableFuture<Void> getClientCloseCallbackFuture() {
        return closeCallbackFuture;
    }

    CloseWebSocketFrame getServerCloseWebSocketFrame() {
        return this.serverCloseWebSocketFrame;
    }

    private static final class OpenTextFrame {
        private final ArrayList<ByteBuf> collected = new ArrayList<>();
        private final AtomicBoolean isClosed = new AtomicBoolean(false);

        private volatile BinaryData collectedData;

        private void add(ByteBuf buffer) {
            if (isClosed.get()) {
                throw new IllegalStateException("Cannot add more buffers when closed.");
            }

            buffer.retain();
            collected.add(buffer);
        }

        private void close() {
            if (isClosed.getAndSet(true)) {
                getCollectedData();
            }
        }

        private void dispose() {
            if (isClosed.get()) {
                collected.forEach(b -> b.release());
                collected.clear();
            }
        }

        private BinaryData getCollectedData() {
            if (!isClosed.get()) {
                throw new IllegalStateException("Cannot get data when frame not closed.");
            }

            final BinaryData e = collectedData;

            if (e != null) {
                return collectedData;
            }

            final List<ByteBuffer> list = collected.stream()
                .map(b -> b.nioBuffer())
                .toList();

            collectedData = BinaryData.fromListByteBuffer(list);

            return collectedData;
        }
    }
}
