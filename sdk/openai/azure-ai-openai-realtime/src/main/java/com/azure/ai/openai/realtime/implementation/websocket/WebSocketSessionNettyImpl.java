// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.ai.openai.realtime.models.ConnectFailedException;
import com.azure.ai.openai.realtime.models.RealtimeClientEvent;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class WebSocketSessionNettyImpl implements WebSocketSession {

    private static final int MAX_FRAME_SIZE = Configuration.getGlobalConfiguration().get("AZURE_MAX_FRAME_SIZE", 65536);

    private static final ClientLogger LOGGER = new ClientLogger(WebSocketSessionNettyImpl.class);
    private final MessageEncoder messageEncoder;
    private final MessageDecoder messageDecoder;

    private final URI uri;
    private final HttpHeaders headers;
    private final String subProtocol;

    private final Consumer<Object> messageHandler;
    private final Consumer<WebSocketSession> openHandler;
    private final Consumer<CloseReason> closeHandler;

    private EventLoopGroup group;
    private WebSocketClientHandshaker handshaker;
    private WebSocketClientHandler clientHandler;
    private Channel ch;

    private static final class WebSocketChannelHandler extends ChannelInitializer<SocketChannel> {

        private final String host;
        private final int port;
        private final SslContext sslCtx;
        private final WebSocketClientHandler handler;

        private WebSocketChannelHandler(String host, int port, SslContext sslCtx, WebSocketClientHandler handler) {
            this.host = host;
            this.port = port;
            this.sslCtx = sslCtx;
            this.handler = handler;
        }

        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
            }
            p.addLast(new HttpClientCodec(), new HttpObjectAggregator(MAX_FRAME_SIZE),
                WebSocketClientCompressionHandler.INSTANCE, new WebSocketFrameAggregator(MAX_FRAME_SIZE), handler);
            p.addLast(new KeepAliveHandler());
            p.addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
            p.addLast(new WebSocketPingHandler());
        }
    }

    WebSocketSessionNettyImpl(ClientEndpointConfiguration cec,
        Supplier<AuthenticationProvider.AuthenticationHeader> authenticationHeaderSupplier,
        Consumer<Object> messageHandler, Consumer<WebSocketSession> openHandler, Consumer<CloseReason> closeHandler) {
        this.uri = cec.getUri();
        this.messageEncoder = cec.getMessageEncoder();
        this.messageDecoder = cec.getMessageDecoder();
        this.subProtocol = cec.getSubProtocol();
        this.headers = cec.getHeaders(authenticationHeaderSupplier.get());
        this.messageHandler = messageHandler;
        this.openHandler = openHandler;
        this.closeHandler = closeHandler;
    }

    void connect() throws URISyntaxException, SSLException, InterruptedException, ExecutionException {
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Only WS(S) is supported.");
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        group = new NioEventLoopGroup();

        handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, this.subProtocol, true,
            this.headers);

        clientHandler = new WebSocketClientHandler(handshaker, messageDecoder, messageHandler);

        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)  // Connection establishment timeout
            .option(ChannelOption.SO_KEEPALIVE, true)            // Enable TCP-level keep-alive
            .option(ChannelOption.TCP_NODELAY, true)             // Disable Nagle's algorithm for low latency
            .option(ChannelOption.SO_REUSEADDR, true)            // Allow address reuse
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)  // Efficient memory allocation
            .handler(new WebSocketChannelHandler(host, port, sslCtx, clientHandler));

        final CompletableFuture<Void> handshakeCallbackFuture = new CompletableFuture<>();

        ch = b.connect(uri.getHost(), port).sync().channel();
        // callback for openHandler
        clientHandler.handshakeFuture().addListener(future -> {
            if (future.isSuccess()) {
                openHandler.accept(this);
            }
            handshakeCallbackFuture.complete(null);
        }).sync();

        // callback for closeHandler, on channel close
        ch.closeFuture().addListener(future -> {
            if (clientHandler != null) {
                if (future.isSuccess()) {
                    // the CloseWebSocketFrame from server
                    CloseWebSocketFrame closeFrame = clientHandler.getServerCloseWebSocketFrame();
                    if (closeFrame == null) {
                        closeFrame = new CloseWebSocketFrame(WebSocketCloseStatus.NORMAL_CLOSURE);
                    }
                    closeHandler.accept(new CloseReason(closeFrame.statusCode(), closeFrame.reasonText()));
                    closeFrame.release();   // release after use
                }

                // closeCallbackFuture exists if close is initiated from client
                CompletableFuture<Void> closeCallbackFuture = clientHandler.getClientCloseCallbackFuture();
                if (closeCallbackFuture != null) {
                    closeCallbackFuture.complete(null);
                }
            }
        });

        // make sure openHandler callback is completed before returns
        handshakeCallbackFuture.get();
    }

    @Override
    public boolean isOpen() {
        return ch != null && ch.isOpen() && handshaker != null && handshaker.isHandshakeComplete();
    }

    @Override
    public void sendObjectAsync(Object data, Consumer<SendResult> handler) {
        if (ch != null && ch.isOpen()) {
            String msg = messageEncoder.encode((RealtimeClientEvent) data);
            sendTextAsync(msg, handler);
        } else {
            handler.accept(new SendResult(new IllegalStateException("Channel is closed")));
        }
    }

    @Override
    public void sendTextAsync(String text, Consumer<SendResult> handler) {
        if (ch != null && ch.isOpen()) {
            TextWebSocketFrame frame = new TextWebSocketFrame(text);
            LOGGER.atVerbose().addKeyValue("text", frame.text()).log(() -> "Send TextWebSocketFrame");
            ch.writeAndFlush(frame).addListener(future -> {
                if (future.isSuccess()) {
                    handler.accept(new SendResult());
                } else {
                    handler.accept(new SendResult(future.cause()));
                }
            });
        } else {
            handler.accept(new SendResult(new IllegalStateException("Channel is closed")));
        }
    }

    @Override
    public void closeSocket() {
        if (group != null) {
            try {
                if (ch != null && ch.isOpen() && clientHandler != null) {
                    ch.close();
                    ch.closeFuture().sync();
                }

                group.shutdownGracefully();
            } catch (InterruptedException e) {
                throw LOGGER.logExceptionAsError(new ConnectFailedException("Failed to disconnect", e));
            }
        }
    }

    @Override
    public void close() {
        if (group != null) {
            try {
                CompletableFuture<Void> closeCallbackFuture = null;

                if (ch != null && ch.isOpen() && clientHandler != null) {
                    closeCallbackFuture = new CompletableFuture<>();
                    clientHandler.setClientCloseCallbackFuture(closeCallbackFuture);

                    CloseWebSocketFrame closeFrame = new CloseWebSocketFrame(WebSocketCloseStatus.NORMAL_CLOSURE);
                    LOGGER.atVerbose()
                        .addKeyValue("statusCode", closeFrame.statusCode())
                        .addKeyValue("reasonText", closeFrame.reasonText())
                        .log(() -> "Send CloseWebSocketFrame");
                    ch.writeAndFlush(closeFrame);
                    // server will reply a CloseWebSocketFrame and close TCP connection, wait for it
                    ch.closeFuture().sync();
                }

                group.shutdownGracefully();

                // make sure closeHandler callback is completed before returns
                if (closeCallbackFuture != null) {
                    closeCallbackFuture.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw LOGGER.logExceptionAsError(new ConnectFailedException("Failed to disconnect", e));
            }
        }
    }
}
