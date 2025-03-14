// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handShaker;
    private ChannelPromise handshakeFuture;

    private static final ClientLogger LOGGER = new ClientLogger(WebSocketClientHandler.class);
    private final MessageDecoder messageDecoder;
    private final Consumer<Object> messageHandler;

    WebSocketClientHandler(WebSocketClientHandshaker handShaker, MessageDecoder messageDecoder,
        Consumer<Object> messageHandler) {
        this.handShaker = handShaker;
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
        handShaker.handshake(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if (handshakeFuture != null && !handShaker.isHandshakeComplete()) {
            try {
                handShaker.finishHandshake(ch, (FullHttpResponse) msg);
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                handshakeFuture.setFailure(LOGGER.atError().log(e));
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw LOGGER.logExceptionAsError(new IllegalStateException("Unexpected FullHttpResponse (getStatus="
                + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')'));
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        LOGGER.atInfo().log("Processing frame: " + frame.toString());

        if (frame instanceof TextWebSocketFrame) {
            // Text
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            LOGGER.atVerbose().addKeyValue("text", textFrame.text()).log(() -> "Received TextWebSocketFrame");

            Object wpsMessage = messageDecoder.decode(textFrame.text());
            messageHandler.accept(wpsMessage);
        } else if (frame instanceof CloseWebSocketFrame) {
            // Close
            CloseWebSocketFrame closeFrame = (CloseWebSocketFrame) frame;
            LOGGER.atVerbose()
                .addKeyValue("statusCode", closeFrame.statusCode())
                .addKeyValue("reasonText", closeFrame.reasonText())
                .log(() -> "Received CloseWebSocketFrame");

            this.serverCloseWebSocketFrame = closeFrame.retain();   // retain for SessionNettyImpl

            if (closeCallbackFuture == null) {
                // close initiated from server, reply CloseWebSocketFrame, then close connection
                LOGGER.atVerbose().log(() -> "Sending CloseWebSocketFrame");
                closeFrame.retain();    // retain before write it back
                ch.writeAndFlush(closeFrame).addListener(future -> ch.close());
            } else {
                // close initiated from client, client already sent CloseWebSocketFrame
                ch.close();
            }
        } else {
            // Pass other frames down the pipeline
            // We only pass down the pipeline messages this handler doesn't process
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (handshakeFuture != null && !handshakeFuture.isDone()) {
            handshakeFuture.setFailure(LOGGER.atError().log(cause));
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
