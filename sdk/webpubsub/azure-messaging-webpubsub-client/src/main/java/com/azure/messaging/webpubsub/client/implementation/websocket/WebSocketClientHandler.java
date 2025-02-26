// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
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
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

final class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private final AtomicReference<ClientLogger> loggerReference;
    private final MessageDecoder messageDecoder;
    private final Consumer<WebPubSubMessage> messageHandler;

    private ChannelPromise handshakeFuture;
    private CompositeByteBuf compositeByteBuf;

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
    public void handlerAdded(ChannelHandlerContext context) {
        handshakeFuture = context.newPromise();
        compositeByteBuf = context.alloc().compositeBuffer();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        publishBuffer();
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        handshaker.handshake(context.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object message) {
        if (handshakeFuture != null && !handshaker.isHandshakeComplete()) {
            Channel channel = context.channel();

            try {
                handshaker.finishHandshake(channel, (FullHttpResponse) message);
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (!(message instanceof WebSocketFrame) || !processMessage(context, (WebSocketFrame) message)) {
            loggerReference.get()
                .atWarning()
                .addKeyValue("messageType", message.getClass())
                .log("Unknown message type. Skipping.");

            context.fireChannelRead(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (handshakeFuture != null && !handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
        release(compositeByteBuf);
    }

    /**
     * Attempts to process the web socket frame.
     *
     * @param context Channel for message.
     * @param webSocketFrame Frame to process.
     * @return true if the frame was processed, false otherwise.
     */
    private boolean processMessage(ChannelHandlerContext context, WebSocketFrame webSocketFrame) {
        Channel channel = context.channel();

        if (webSocketFrame instanceof PingWebSocketFrame) {
            // Ping, reply Pong
            loggerReference.get().atVerbose().log("Received PingWebSocketFrame");
            loggerReference.get().atVerbose().log("Send PongWebSocketFrame");
            channel.writeAndFlush(new PongWebSocketFrame());
            return true;
        } else if (webSocketFrame instanceof PongWebSocketFrame) {
            // Pong
            loggerReference.get().atVerbose().log("Received PongWebSocketFrame");
            return true;
        } else if (webSocketFrame instanceof CloseWebSocketFrame) {
            // Close
            final CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) webSocketFrame;

            loggerReference.get()
                .atVerbose()
                .addKeyValue("statusCode", closeFrame.statusCode())
                .addKeyValue("reasonText", closeFrame.reasonText())
                .log("Received CloseWebSocketFrame");
            serverCloseWebSocketFrame = closeFrame.retain();   // retain for SessionNettyImpl

            if (closeCallbackFuture == null) {
                // close initiated from server, reply CloseWebSocketFrame, then close connection
                loggerReference.get().atVerbose().log("Send CloseWebSocketFrame");
                closeFrame.retain();    // retain before write it back
                channel.writeAndFlush(closeFrame).addListener(future -> channel.close());
            } else {
                // close initiated from client, client already sent CloseWebSocketFrame
                channel.close();
            }

            return true;
        } else if (webSocketFrame instanceof TextWebSocketFrame
            || webSocketFrame instanceof ContinuationWebSocketFrame) {
            if (compositeByteBuf == null) {
                compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
            }

            compositeByteBuf.addComponent(true, webSocketFrame.content().retain());

            if (!webSocketFrame.isFinalFragment()) {
                return true;
            }

            publishBuffer();
            return true;
        } else {
            return false;
        }
    }

    private void publishBuffer() {
        final ByteBuffer[] nioBuffers = compositeByteBuf.nioBuffers();

        if (nioBuffers.length == 0) {
            return;
        }

        try {
            final BinaryData data = BinaryData.fromListByteBuffer(Arrays.asList(nioBuffers));
            final String collected = data.toString();
            final WebPubSubMessage deserialized = messageDecoder.decode(collected);

            messageHandler.accept(deserialized);
        } finally {
            release(compositeByteBuf);
        }
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

    private static void release(CompositeByteBuf buffer) {
        if (buffer.refCnt() > 0) {
            buffer.release();
            buffer.clear();
        }
    }
}
