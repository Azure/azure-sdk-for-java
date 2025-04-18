// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.AuthUtils;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.http.netty4.NettyHttpClientTests;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.PendingWriteQueue;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
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
import io.netty.handler.proxy.ProxyHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.ConnectionPendingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.utils.AuthUtils.parseAuthenticationOrAuthorizationHeader;
import static io.clientcore.http.netty4.NettyHttpClientTests.uri;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.PROXY_TO_ADDRESS;
import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests a complicated issue being seen in macOS pipelines where
 * {@link NettyHttpClientTests#failedProxyAuthenticationReturnsCorrectError()} fails with
 * {@code StacklessClosedChannelException}. This attempts to replicate the problem without using the full
 * {@code NettyHttpClient}.
 */
@Isolated
public class ComplicatedProxyTests {
    private static final ClientLogger LOGGER = new ClientLogger(ComplicatedProxyTests.class);

    @RepeatedTest(1000)
    public void complicatedProxyIssue(RepetitionInfo repetitionInfo) {
        try (MockProxyServer mockProxyServer = new MockProxyServer("1", "1")) {
            ProxyOptions proxyOptions
                = new ProxyOptions(ProxyOptions.Type.HTTP, mockProxyServer.socketAddress()).setCredentials("2", "2");
            AtomicReference<List<AuthenticateChallenge>> proxyChallenges = new AtomicReference<>();
            int repetition = repetitionInfo.getCurrentRepetition();

            Bootstrap bootstrap = new Bootstrap().channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup(new DefaultThreadFactory("complicated-proxy-issue")))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000);
            // Disable auto-read as we want to control when and how data is read from the channel.
            bootstrap.option(ChannelOption.AUTO_READ, false);

            HttpRequest request = new HttpRequest().setUri(uri(PROXY_TO_ADDRESS));

            URI uri = request.getUri();
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();

            AtomicReference<Response<BinaryData>> responseReference = new AtomicReference<>();
            AtomicReference<Throwable> errorReference = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            // Configure an immutable ChannelInitializer in the builder with everything that can be added on a non-per
            // request basis.
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    ch.pipeline()
                        .addFirst(
                            new ComplicatedProxyHandler(proxyOptions, proxyChallenges, repetition, errorReference));
                }
            });

            try {
                bootstrap.connect(host, port).addListener((ChannelFutureListener) connectListener -> {
                    if (!connectListener.isSuccess()) {
                        LOGGER.atError().setThrowable(connectListener.cause()).log("Failed to connect to host/proxy");
                        errorReference.set(connectListener.cause());
                        connectListener.channel().close();
                        latch.countDown();
                        return;
                    }

                    Channel channel = connectListener.channel();
                    channel.closeFuture().addListener(closeListener -> {
                        if (!closeListener.isSuccess()) {
                            LOGGER.atInfo().addKeyValue("repetition", repetition).log("Channel closed with error");
                            setOrSuppressError(errorReference, closeListener.cause());
                        }
                    });

                    Throwable earlyError = errorReference.get();
                    if (earlyError != null) {
                        // If an error occurred between the connect and the request being sent, don't proceed with sending
                        // the request.
                        LOGGER.atInfo()
                            .addKeyValue("repetition", repetition)
                            .log("Channel had error before sending request");
                        latch.countDown();
                        return;
                    }

                    sendRequest(request, channel, errorReference, repetition)
                        .addListener((ChannelFutureListener) sendListener -> {
                            if (!sendListener.isSuccess()) {
                                setOrSuppressError(errorReference, sendListener.cause());
                                sendListener.channel().close();
                                latch.countDown();
                            } else {
                                sendListener.channel().read();
                            }
                        });
                });

                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw LOGGER.logThrowableAsError(CoreException.from("Request interrupted", e));
            }

            Response<BinaryData> response = responseReference.get();
            if (response == null) {
                Throwable exception = errorReference.get();
                assertInstanceOf(ProxyConnectException.class, exception, () -> {
                    StringWriter stringWriter = new StringWriter();
                    stringWriter.write(exception.toString());
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    exception.printStackTrace(printWriter);

                    return stringWriter.toString();
                });
            } else {
                fail("Request should've failed as the proxy required authentication that wasn't possible.");
            }
        }
    }

    private ChannelFuture sendRequest(HttpRequest request, Channel channel, AtomicReference<Throwable> errorReference,
        int repetition) throws InterruptedException {
        String uri = request.getUri().toString();
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(request.getHeaders());

        wrappedHttpHeaders.getCoreHeaders().set(HttpHeaderName.HOST, request.getUri().getHost());

        Throwable error = errorReference.get();
        if (error != null) {
            LOGGER.atInfo().addKeyValue("repetition", repetition).log("Channel had error before writing request");
            return channel.newFailedFuture(error);
        }

        return channel.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri,
            Unpooled.EMPTY_BUFFER, wrappedHttpHeaders, trailersFactory().newHeaders()));
    }

    /**
     * Copy of both Netty's base {@link ProxyHandler} and our {@link Netty4HttpProxyHandler} to allow for richer logging
     * and debugging of the flaky test.
     */
    private static final class ComplicatedProxyHandler extends ChannelDuplexHandler {
        static final String VALIDATION_ERROR_TEMPLATE = "The '%s' returned in the 'Proxy-Authentication-Info' "
            + "header doesn't match the value sent in the 'Proxy-Authorization' header. Sent: %s, received: %s.";

        private static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";
        private static final HttpHeaderName PROXY_AUTHENTICATION_INFO_NAME
            = HttpHeaderName.fromString(PROXY_AUTHENTICATION_INFO);

        private final ProxyOptions proxyOptions;
        private final AtomicReference<List<AuthenticateChallenge>> proxyChallengeHolder;
        private final HttpClientCodecWrapper wrapper;
        private final SocketAddress proxyAddress;
        private final int repetition;
        private final AtomicReference<Throwable> errorReference;

        private volatile SocketAddress destinationAddress;

        private volatile ChannelHandlerContext ctx;
        private PendingWriteQueue pendingWrites;
        private boolean finished;
        private Boolean wasSuccess;
        private boolean suppressChannelReadComplete;
        private boolean flushedPrematurely;
        private final LazyChannelPromise connectPromise = new LazyChannelPromise();
        private Future<?> connectTimeoutFuture;
        private final ChannelFutureListener writeListener;

        private String authScheme = "none";
        private String lastUsedAuthorizationHeader;

        private HttpResponseStatus status;
        private HttpHeaders innerHeaders;

        private ComplicatedProxyHandler(ProxyOptions proxyOptions,
            AtomicReference<List<AuthenticateChallenge>> proxyChallengeHolder, int repetition,
            AtomicReference<Throwable> errorReference) {
            this.proxyAddress = proxyOptions.getAddress();
            this.proxyOptions = proxyOptions;
            this.proxyChallengeHolder = proxyChallengeHolder;
            this.wrapper = new HttpClientCodecWrapper();
            this.repetition = repetition;
            this.writeListener = future -> {
                if (!future.isSuccess()) {
                    LOGGER.atInfo().addKeyValue("repetition", repetition).log("Failed to send request to proxy server");
                    setConnectFailure(future.cause());
                }
            };
            this.errorReference = errorReference;
        }

        /**
         * Returns the name of the proxy protocol in use.
         */
        public String protocol() {
            return "http";
        }

        /**
         * Returns the name of the authentication scheme in use.
         */
        public String authScheme() {
            return authScheme;
        }

        /**
         * Returns the address of the proxy server.
         */
        @SuppressWarnings("unchecked")
        public <T extends SocketAddress> T proxyAddress() {
            return (T) proxyAddress;
        }

        /**
         * Returns the address of the destination to connect to via the proxy server.
         */
        @SuppressWarnings("unchecked")
        public <T extends SocketAddress> T destinationAddress() {
            return (T) destinationAddress;
        }

        /**
         * Returns {@code true} if and only if the connection to the destination has been established successfully.
         */
        public boolean isConnected() {
            return connectPromise.isSuccess();
        }

        /**
         * Returns a {@link Future} that is notified when the connection to the destination has been established
         * or the connection attempt has failed.
         */
        public Future<Channel> connectFuture() {
            return connectPromise;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
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

        /**
         * Adds the codec handlers required to communicate with the proxy server.
         */
        private void addCodec(ChannelHandlerContext ctx) {
            ctx.pipeline().addBefore(ctx.name(), "Netty4-Proxy-Codec", this.wrapper);
        }

        /**
         * Removes the encoders added in {@link #addCodec(ChannelHandlerContext)}.
         */
        private void removeEncoder() {
            this.wrapper.codec.removeOutboundHandler();
        }

        /**
         * Removes the decoders added in {@link #addCodec(ChannelHandlerContext)}.
         */
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
        public void channelActive(ChannelHandlerContext ctx) {
            sendInitialMessage(ctx);
            ctx.fireChannelActive();
        }

        /**
         * Sends the initial message to be sent to the proxy server. This method also starts a timeout task which marks
         * the {@link #connectPromise} as failure if the connection attempt does not success within the timeout.
         */
        private void sendInitialMessage(final ChannelHandlerContext ctx) {
            connectTimeoutFuture = ctx.executor().schedule(() -> {
                if (!connectPromise.isDone()) {
                    setConnectFailure(new ProxyConnectException(exceptionMessage("timeout")));
                }
            }, 10_000, TimeUnit.MILLISECONDS);

            sendToProxyServer(newInitialMessage());
            readIfNeeded(ctx);
        }

        /**
         * Returns a new message that is sent at first time when the connection to the proxy server has been established.
         *
         * @return the initial message, or {@code null} if the proxy server is expected to send the first message instead
         */
        @SuppressWarnings("deprecation")
        private Object newInitialMessage() {
            // This needs to handle no authorization proxying.
            InetSocketAddress destinationAddress = this.destinationAddress();
            String hostString = HttpUtil.formatHostnameForHttp(destinationAddress);
            int port = destinationAddress.getPort();
            String url = hostString + ":" + port;
            String hostHeader = (port == 80 || port == 443) ? url : hostString;
            FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, url,
                Unpooled.EMPTY_BUFFER, false);

            request.headers().set(HttpHeaderNames.HOST, hostHeader);

            List<AuthenticateChallenge> authenticateChallenges = proxyChallengeHolder.get();
            if (!CoreUtils.isNullOrEmpty(authenticateChallenges)) {
                Map.Entry<String, AuthenticateChallenge> handledChallenge = proxyOptions.getChallengeHandler()
                    .handleChallenge("CONNECT", URI.create(hostHeader), authenticateChallenges);
                lastUsedAuthorizationHeader = handledChallenge.getKey();
                authScheme = handledChallenge.getValue().getScheme();

                request.headers().set(HttpHeaderNames.PROXY_AUTHORIZATION, lastUsedAuthorizationHeader);
            }

            return request;
        }

        /**
         * Sends the specified message to the proxy server.  Use this method to send a response to the proxy server in
         * {@link #handleResponse(Object)}.
         */
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
                setOrSuppressError(errorReference, cause);
            } else {
                // Exception was raised before the connection attempt is finished.
                setConnectFailure(cause);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
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

        /**
         * Handles the message received from the proxy server.
         *
         * @return {@code true} if the connection to the destination has been established,
         *         {@code false} if the connection to the destination has not been established and more messages are
         *         expected from the proxy server
         */
        private boolean handleResponse(Object o) throws ProxyConnectException {
            if (o instanceof HttpResponse) {
                if (status != null) {
                    throw LOGGER
                        .logThrowableAsWarning(new RuntimeException("Received too many responses for a request"));
                }

                HttpResponse response = (HttpResponse) o;
                status = response.status();
                innerHeaders = response.headers();

                if (response.status().code() == 407) {
                    proxyChallengeHolder.set(extractChallengesFromHeaders(response.headers()));
                } else if (response.status().code() == 200) {
                    validateProxyAuthenticationInfo(Netty4Utility.get(response.headers(), PROXY_AUTHENTICATION_INFO,
                        PROXY_AUTHENTICATION_INFO_NAME), lastUsedAuthorizationHeader);
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

        private void setConnectSuccess() {
            wasSuccess = true;
            finished = true;
            cancelConnectTimeoutFuture();

            if (!connectPromise.isDone()) {
                boolean removedCodec = true;

                removedCodec &= safeRemoveEncoder();

                ctx.fireUserEventTriggered(
                    new ProxyConnectionEvent(protocol(), authScheme(), proxyAddress, destinationAddress));

                removedCodec &= safeRemoveDecoder();

                if (removedCodec) {
                    writePendingWrites();

                    if (flushedPrematurely) {
                        ctx.flush();
                    }
                    connectPromise.trySuccess(ctx.channel());
                } else {
                    // We are at inconsistent state because we failed to remove all codec handlers.
                    Exception cause = new ProxyConnectException(
                        "failed to remove all codec handlers added by the proxy handler; bug?");
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
            wasSuccess = false;
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
            setOrSuppressError(errorReference, cause);
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

            StringBuilder buf = new StringBuilder(128 + msg.length()).append(protocol())
                .append(", ")
                .append(authScheme())
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
        public void channelReadComplete(ChannelHandlerContext ctx) {
            if (suppressChannelReadComplete) {
                suppressChannelReadComplete = false;

                readIfNeeded(ctx);
            } else {
                ctx.fireChannelReadComplete();
            }
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            if (finished) {
                LOGGER.atInfo()
                    .addKeyValue("repetition", repetition)
                    .addKeyValue("wasSuccess", wasSuccess)
                    .log("Write called after proxy finished");
                writePendingWrites();
                ctx.write(msg, promise);
            } else {
                addPendingWrite(ctx, msg, promise);
            }
        }

        @Override
        public void flush(ChannelHandlerContext ctx) {
            if (finished) {
                LOGGER.atInfo()
                    .addKeyValue("repetition", repetition)
                    .addKeyValue("wasSuccess", wasSuccess)
                    .log("Flush called after proxy finished");
                writePendingWrites();
                ctx.flush();
            } else {
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

        // Cannot share instances of CoreHttpProxyHandler across ChannelPipelines.
        @Override
        public boolean isSharable() {
            return false;
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
        private static void validateProxyAuthenticationInfoValue(String name,
            Map<String, String> authenticationInfoPieces, Map<String, String> authorizationPieces) {
            if (authenticationInfoPieces.containsKey(name)) {
                String sentValue = authorizationPieces.get(name);
                String receivedValue = authenticationInfoPieces.get(name);

                if (!receivedValue.equalsIgnoreCase(sentValue)) {
                    throw LOGGER.logThrowableAsError(new IllegalStateException(
                        String.format(VALIDATION_ERROR_TEMPLATE, name, sentValue, receivedValue)));
                }
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
}
