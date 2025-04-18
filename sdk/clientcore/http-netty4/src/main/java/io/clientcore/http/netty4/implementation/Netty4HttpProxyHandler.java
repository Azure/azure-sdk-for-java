// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.AuthUtils;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.clientcore.core.utils.CoreUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.PendingWriteQueue;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.proxy.ProxyConnectionEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.ConnectionPendingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.utils.AuthUtils.parseAuthenticationOrAuthorizationHeader;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;

/**
 * This class handles authorizing requests being sent through a proxy that requires authentication.
 */
public final class Netty4HttpProxyHandler extends ChannelDuplexHandler {
    static final String VALIDATION_ERROR_TEMPLATE = "The '%s' returned in the 'Proxy-Authentication-Info' "
        + "header doesn't match the value sent in the 'Proxy-Authorization' header. Sent: %s, received: %s.";

    private final SocketAddress proxyAddress;
    private volatile SocketAddress destinationAddress;

    private volatile ChannelHandlerContext ctx;
    private PendingWriteQueue pendingWrites;
    private boolean finished;
    private boolean suppressChannelReadComplete;
    private boolean flushedPrematurely;
    private final LazyChannelPromise connectPromise = new LazyChannelPromise();
    private Future<?> connectTimeoutFuture;
    private final ChannelFutureListener writeListener = future -> {
        if (!future.isSuccess()) {
            LOGGER.atInfo().log("Writing to proxy server failed");
            setConnectFailure(future.cause());
        }
    };

    private static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";
    private static final HttpHeaderName PROXY_AUTHENTICATION_INFO_NAME
        = HttpHeaderName.fromString(PROXY_AUTHENTICATION_INFO);

    // CoreHttpProxyHandler will be created for each network request that is using proxy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(Netty4HttpProxyHandler.class);

    private final ProxyOptions proxyOptions;
    private final AtomicReference<List<AuthenticateChallenge>> proxyChallengeHolder;
    private final HttpClientCodecWrapper wrapper;

    private String authScheme = "none";
    private String lastUsedAuthorizationHeader;

    private HttpResponseStatus status;
    private HttpHeaders innerHeaders;

    /**
     * Creates an instance of HttpProxyHandler.
     *
     * @param proxyOptions The configured {@link ProxyOptions} that will dictate the behavior of proxying.
     * @param proxyChallengeHolder An {@link AtomicReference} containing parsed {@code Proxy-Authenticate} challenges.
     */
    public Netty4HttpProxyHandler(ProxyOptions proxyOptions,
        AtomicReference<List<AuthenticateChallenge>> proxyChallengeHolder) {
        this.proxyAddress = proxyOptions.getAddress();
        this.proxyOptions = proxyOptions;
        this.proxyChallengeHolder = proxyChallengeHolder;
        this.wrapper = new HttpClientCodecWrapper();
    }

    // Cannot share instances of CoreHttpProxyHandler across ChannelPipelines.
    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        addCodec(ctx);

        if (ctx.channel().isActive()) {
            // channelActive() event has been fired already, which means this.channelActive() will
            // not be invoked. We have to initialize here instead.
            sendInitialMessage(ctx);
        }
        // channelActive() event has not been fired yet.  this.channelOpen() will be invoked
        // and initialization will occur there.
    }

    private void addCodec(ChannelHandlerContext ctx) {
        ctx.pipeline().addBefore(ctx.name(), "Netty4-Proxy-Codec", this.wrapper);
    }

    private void removeEncoder() {
        this.wrapper.codec.removeOutboundHandler();
    }

    private void removeDecoder() {
        this.wrapper.codec.removeInboundHandler();
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) {

        if (destinationAddress != null) {
            promise.setFailure(new ConnectionPendingException());
            return;
        }

        destinationAddress = remoteAddress;
        ctx.connect(proxyAddress, localAddress, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sendInitialMessage(ctx);
        ctx.fireChannelActive();
    }

    private void sendInitialMessage(final ChannelHandlerContext ctx) throws Exception {
        Integer channelOption = ctx.channel().config().getOption(ChannelOption.CONNECT_TIMEOUT_MILLIS);
        final long connectTimeoutMillis = (channelOption != null) ? channelOption : 10_000;
        if (connectTimeoutMillis > 0) {
            connectTimeoutFuture = ctx.executor().schedule(() -> {
                if (!connectPromise.isDone()) {
                    setConnectFailure(new ProxyConnectException(exceptionMessage("timeout")));
                }
            }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
        }

        sendToProxyServer(newInitialMessage(ctx));

        readIfNeeded(ctx);
    }

    @SuppressWarnings("deprecation")
    private Object newInitialMessage(ChannelHandlerContext ctx) throws ProxyConnectException {
        // This needs to handle no authorization proxying.
        InetSocketAddress destinationAddress = (InetSocketAddress) this.destinationAddress;
        String hostString = HttpUtil.formatHostnameForHttp(destinationAddress);
        int port = destinationAddress.getPort();
        String url = hostString + ":" + port;
        String hostHeader = (port == 80 || port == 443) ? url : hostString;
        FullHttpRequest request
            = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, url, Unpooled.EMPTY_BUFFER, false);

        request.headers().set(HttpHeaderNames.HOST, hostHeader);

        // If proxy authorization was already attempted with this handler attempt to use the previous authorization
        // again. This should be safe as this handler will always attempt to proxy against the same host, meaning there
        // is one of two outcomes here:
        // 1. The previous authorization is still valid and will result in another successful proxy connection.
        // 2. The previous authorization has gone stale and will result in the slower CONNECT attempt followed by
        //    processing the response for authorization.
        //
        // If proxy authorization wasn't already attempt but challenges have already been received, or in other terms
        // a CONNECT was attempted but failed on authorization requirements, create and cache the current authorization
        // header.
        //
        // If a CONNECT hasn't been attempted, where the current authorization header and challenges will be null, just
        // send a CONNECT request. If the proxy doesn't require authorization it should succeed. If the proxy does
        // require authorization it should send back a 407 with Proxy-Authenticate headers that will be parsed and used
        // to create an authorization header.
        List<AuthenticateChallenge> authenticateChallenges = proxyChallengeHolder.get();
        if (!CoreUtils.isNullOrEmpty(authenticateChallenges)) {
            Map.Entry<String, AuthenticateChallenge> handledChallenge = proxyOptions.getChallengeHandler()
                .handleChallenge("CONNECT", URI.create(hostHeader), authenticateChallenges);
            lastUsedAuthorizationHeader = handledChallenge.getKey();
            authScheme = handledChallenge.getValue().getScheme();

            request.headers().set(HttpHeaderNames.PROXY_AUTHORIZATION, lastUsedAuthorizationHeader);
        }

        if (ctx.channel().isActive()) {
            return request;
        } else {
            throw new HttpProxyHandler.HttpProxyConnectException(
                exceptionMessage("Channel became inactive before 'newInitialMessage' was sent"), null);
        }
    }

    private boolean handleResponse(Object o) throws ProxyConnectException {
        if (o instanceof HttpResponse) {
            if (status != null) {
                throw LOGGER.logThrowableAsWarning(new RuntimeException("Received too many responses for a request"));
            }

            HttpResponse response = (HttpResponse) o;
            status = response.status();
            innerHeaders = response.headers();

            if (response.status().code() == 407) {
                /*
                 * Attempt to CONNECT to the proxy resulted in a request for authentication, parse the
                 * Proxy-Authenticate headers returned for challenges and update the ChallengeHolder reference. This
                 * will allow subsequent requests to CONNECT to this proxy to use the challenges returned to create a
                 * Proxy-Authorization header.
                 */
                proxyChallengeHolder.set(extractChallengesFromHeaders(response.headers()));
            } else if (response.status().code() == 200) {
                /*
                 * Attempt to CONNECT to the proxy succeeded, retrieve the Proxy-Authorization header passed in the
                 * Channel's attributes to compare it against a potential Proxy-Authentication-Info response header.
                 * This header is used by the server to validate to the client that it received the correct request.
                 */
                validateProxyAuthenticationInfo(
                    Netty4Utility.get(response.headers(), PROXY_AUTHENTICATION_INFO, PROXY_AUTHENTICATION_INFO_NAME),
                    lastUsedAuthorizationHeader);
            }
        }

        boolean responseComplete = o instanceof LastHttpContent;
        if (responseComplete) {
            if (status == null) {
                throw new HttpProxyHandler.HttpProxyConnectException(exceptionMessage("missing response"),
                    innerHeaders);
            } else if (status.code() != 200) {
                throw new HttpProxyHandler.HttpProxyConnectException(exceptionMessage("status: " + status),
                    innerHeaders);
            }
        }

        return responseComplete;
    }

    /*
     * Search the response HttpHeaders for Proxy-Authenticate headers.
     */
    private static List<AuthenticateChallenge> extractChallengesFromHeaders(HttpHeaders headers) {
        List<String> proxyAuthenticateValues
            = Netty4Utility.getAll(headers, HttpHeaderNames.PROXY_AUTHENTICATE, HttpHeaderName.PROXY_AUTHENTICATE);

        List<AuthenticateChallenge> challenges = new ArrayList<>();
        for (String proxyAuthenticateValue : proxyAuthenticateValues) {
            challenges.addAll(AuthUtils.parseAuthenticateHeader(proxyAuthenticateValue));
        }

        return challenges;
    }

    /*
     * Validate the Proxy-Authorization header used in authentication against the server's Proxy-Authentication-Info
     * header. This header is an optional return by the server so this validation is guaranteed to happen.
     */
    static void validateProxyAuthenticationInfo(String infoHeader, String authorizationHeader) {
        // Client didn't send 'Proxy-Authorization' or server didn't return a 'Proxy-Authentication-Info' header,
        // nothing to validate.
        if (CoreUtils.isNullOrEmpty(authorizationHeader) || CoreUtils.isNullOrEmpty(infoHeader)) {
            return;
        }

        Map<String, String> authenticationInfoPieces = parseAuthenticationOrAuthorizationHeader(infoHeader);
        Map<String, String> authorizationPieces = parseAuthenticationOrAuthorizationHeader(authorizationHeader);

        /*
         * If the authentication info response contains a cnonce or nc value it MUST match the value sent in the
         * authorization header. This is the server performing validation to the client that it received the
         * information.
         */
        validateProxyAuthenticationInfoValue("cnonce", authenticationInfoPieces, authorizationPieces);
        validateProxyAuthenticationInfoValue("nc", authenticationInfoPieces, authorizationPieces);
    }

    /*
     * Validates that the value received in the 'Proxy-Authentication-Info' matches the value sent in the
     * 'Proxy-Authorization' header. If the values don't match an 'IllegalStateException' will be thrown with a message
     * outlining that the values didn't match.
     */
    private static void validateProxyAuthenticationInfoValue(String name, Map<String, String> authenticationInfoPieces,
        Map<String, String> authorizationPieces) {
        if (authenticationInfoPieces.containsKey(name)) {
            String sentValue = authorizationPieces.get(name);
            String receivedValue = authenticationInfoPieces.get(name);

            if (!receivedValue.equalsIgnoreCase(sentValue)) {
                throw LOGGER.logThrowableAsError(new IllegalStateException(
                    String.format(VALIDATION_ERROR_TEMPLATE, name, sentValue, receivedValue)));
            }
        }
    }

    private void sendToProxyServer(Object msg) {
        ctx.writeAndFlush(msg).addListener(writeListener);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (finished) {
            ctx.fireChannelInactive();
        } else {
            // Disconnected before connected to the destination.
            setConnectFailure(new ProxyConnectException(exceptionMessage("disconnected")));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (finished) {
            ctx.fireExceptionCaught(cause);
        } else {
            // Exception was raised before the connection attempt is finished.
            setConnectFailure(cause);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (finished) {
            // Received a message after the connection has been established; pass through.
            suppressChannelReadComplete = false;
            ctx.fireChannelRead(msg);
        } else {
            suppressChannelReadComplete = true;
            Throwable cause = null;
            try {
                boolean done = handleResponse(msg);
                if (done) {
                    setConnectSuccess();
                }
            } catch (Throwable t) {
                cause = t;
            } finally {
                ReferenceCountUtil.release(msg);
                if (cause != null) {
                    setConnectFailure(cause);
                }
            }
        }
    }

    private void setConnectSuccess() {
        finished = true;
        cancelConnectTimeoutFuture();

        if (!connectPromise.isDone()) {
            boolean removedCodec = true;

            removedCodec &= safeRemoveEncoder();

            ctx.fireUserEventTriggered(new ProxyConnectionEvent("http", authScheme, proxyAddress, destinationAddress));

            removedCodec &= safeRemoveDecoder();

            if (removedCodec) {
                writePendingWrites();

                if (flushedPrematurely) {
                    ctx.flush();
                }
                connectPromise.trySuccess(ctx.channel());
            } else {
                // We are at inconsistent state because we failed to remove all codec handlers.
                Exception cause
                    = new ProxyConnectException("failed to remove all codec handlers added by the proxy handler; bug?");
                failPendingWritesAndClose(cause);
            }
        }
    }

    private boolean safeRemoveDecoder() {
        try {
            removeDecoder();
            return true;
        } catch (Exception e) {
            LOGGER.atWarning().setThrowable(e).log("Failed to remove proxy decoders");
        }

        return false;
    }

    private boolean safeRemoveEncoder() {
        try {
            removeEncoder();
            return true;
        } catch (Exception e) {
            LOGGER.atWarning().setThrowable(e).log("Failed to remove proxy encoders");
        }

        return false;
    }

    private void setConnectFailure(Throwable cause) {
        finished = true;
        cancelConnectTimeoutFuture();

        if (!connectPromise.isDone()) {

            if (!(cause instanceof ProxyConnectException)) {
                cause = new ProxyConnectException(exceptionMessage(cause.toString()), cause);
            }

            safeRemoveDecoder();
            safeRemoveEncoder();
            failPendingWritesAndClose(cause);
        }
    }

    private void failPendingWritesAndClose(Throwable cause) {
        failPendingWrites(cause);
        connectPromise.tryFailure(cause);
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }

    private void cancelConnectTimeoutFuture() {
        if (connectTimeoutFuture != null) {
            connectTimeoutFuture.cancel(false);
            connectTimeoutFuture = null;
        }
    }

    /**
     * Decorates the specified exception message with the common information such as the current protocol,
     * authentication scheme, proxy address, and destination address.
     */
    private String exceptionMessage(String msg) {
        if (msg == null) {
            msg = "";
        }

        StringBuilder buf = new StringBuilder(128 + msg.length()).append("http, ")
            .append(authScheme)
            .append(", ")
            .append(proxyAddress)
            .append(" => ")
            .append(destinationAddress);
        if (!msg.isEmpty()) {
            buf.append(", ").append(msg);
        }

        return buf.toString();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (suppressChannelReadComplete) {
            suppressChannelReadComplete = false;

            readIfNeeded(ctx);
        } else {
            ctx.fireChannelReadComplete();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (finished) {
            LOGGER.atInfo().log("Writing to channel after proxy handling finished");
            writePendingWrites();
            ctx.write(msg, promise);
        } else {
            LOGGER.atInfo().log("Writing to channel before proxy handling finished");
            addPendingWrite(ctx, msg, promise);
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) {
        if (finished) {
            LOGGER.atInfo().log("Flushing channel after proxy handling finished");
            writePendingWrites();
            ctx.flush();
        } else {
            LOGGER.atInfo().log("Flushing channel before proxy handling finished");
            flushedPrematurely = true;
        }
    }

    private static void readIfNeeded(ChannelHandlerContext ctx) {
        if (!ctx.channel().config().isAutoRead()) {
            ctx.read();
        }
    }

    private void writePendingWrites() {
        if (pendingWrites != null) {
            pendingWrites.removeAndWriteAll();
            pendingWrites = null;
        }
    }

    private void failPendingWrites(Throwable cause) {
        if (pendingWrites != null) {
            pendingWrites.removeAndFailAll(cause);
            pendingWrites = null;
        }
    }

    private void addPendingWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        PendingWriteQueue pendingWrites = this.pendingWrites;
        if (pendingWrites == null) {
            this.pendingWrites = pendingWrites = new PendingWriteQueue(ctx);
        }
        pendingWrites.add(msg, promise);
    }

    private final class LazyChannelPromise extends DefaultPromise<Channel> {
        @Override
        protected EventExecutor executor() {
            if (ctx == null) {
                throw new IllegalStateException();
            }
            return ctx.executor();
        }
    }

    private static final class HttpClientCodecWrapper implements ChannelInboundHandler, ChannelOutboundHandler {
        final HttpClientCodec codec = createCodec();

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            codec.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            codec.handlerRemoved(ctx);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            codec.exceptionCaught(ctx, cause);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            codec.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            codec.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            codec.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            codec.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            codec.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            codec.channelReadComplete(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            codec.userEventTriggered(ctx, evt);
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            codec.channelWritabilityChanged(ctx);
        }

        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
            throws Exception {
            codec.bind(ctx, localAddress, promise);
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
            ChannelPromise promise) throws Exception {
            codec.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            codec.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            codec.close(ctx, promise);
        }

        @Override
        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            codec.deregister(ctx, promise);
        }

        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            codec.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            codec.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            codec.flush(ctx);
        }
    }
}
