// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

final class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    private final AtomicReference<ClientLogger> loggerReference;
    private final MessageDecoder messageDecoder;
    private final Consumer<Object> messageHandler;

    WebSocketClientHandler(WebSocketClientHandshaker handshaker,
                           AtomicReference<ClientLogger> loggerReference,
                           MessageDecoder messageDecoder,
                           Consumer<Object> messageHandler) {
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
        Channel ch = ctx.channel();
        if (handshakeFuture != null && !handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw loggerReference.get().logExceptionAsError(new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')'));
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            // Text
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            loggerReference.get().atVerbose()
                .addKeyValue("text", textFrame.text())
                .log("Received TextWebSocketFrame");
            Object wpsMessage = messageDecoder.decode(textFrame.text());
            messageHandler.accept(wpsMessage);
        } else if (frame instanceof PingWebSocketFrame) {
            // Ping, reply Pong
            loggerReference.get().atVerbose().log("Received PingWebSocketFrame");
            loggerReference.get().atVerbose().log("Send PongWebSocketFrame");
            ch.writeAndFlush(new PongWebSocketFrame());
        } else if (frame instanceof PongWebSocketFrame) {
            // Pong
            loggerReference.get().atVerbose().log("Received PongWebSocketFrame");
        } else if (frame instanceof CloseWebSocketFrame) {
            // Close
            CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
            loggerReference.get().atVerbose()
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
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
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
}
