// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.ServerSentResult;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.ProgressReporter;
import io.clientcore.core.utils.ServerSentEventUtils;
import io.clientcore.http.netty4.implementation.ChannelInitializationProxyHandler;
import io.clientcore.http.netty4.implementation.Netty4AlpnHandler;
import io.clientcore.http.netty4.implementation.Netty4ChannelBinaryData;
import io.clientcore.http.netty4.implementation.Netty4ConnectionPool;
import io.clientcore.http.netty4.implementation.Netty4ConnectionPoolKey;
import io.clientcore.http.netty4.implementation.Netty4PipelineCleanupEvent;
import io.clientcore.http.netty4.implementation.Netty4PipelineCleanupHandler;
import io.clientcore.http.netty4.implementation.Netty4ProgressAndTimeoutHandler;
import io.clientcore.http.netty4.implementation.Netty4ResponseHandler;
import io.clientcore.http.netty4.implementation.Netty4SslInitializationHandler;
import io.clientcore.http.netty4.implementation.ResponseBodyHandling;
import io.clientcore.http.netty4.implementation.ResponseStateInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static io.clientcore.core.utils.ServerSentEventUtils.attemptRetry;
import static io.clientcore.core.utils.ServerSentEventUtils.processTextEventStream;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.ALPN;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP2_GOAWAY;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_CODEC;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_RESPONSE;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PIPELINE_CLEANUP;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.POOL_CONNECTION_HEALTH;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROGRESS_AND_TIMEOUT;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROXY;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROXY_EXCEPTION_WARNING_SUPPRESSION;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.SSL;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.SSL_INITIALIZER;
import static io.clientcore.http.netty4.implementation.Netty4Utility.awaitLatch;
import static io.clientcore.http.netty4.implementation.Netty4Utility.buildSslContext;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createHttp2Codec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.sendHttp11Request;
import static io.clientcore.http.netty4.implementation.Netty4Utility.sendHttp2Request;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;

/**
 * HttpClient implementation using synchronous Netty operations.
 */
class NettyHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(NettyHttpClient.class);

    /**
     * Error message for when no {@link ServerSentEventListener} is attached to the {@link HttpRequest}.
     */
    private static final String NO_LISTENER_ERROR_MESSAGE
        = "No ServerSentEventListener attached to HttpRequest to handle the text/event-stream response";

    private final EventLoopGroup eventLoopGroup;
    private final Netty4ConnectionPool connectionPool;
    private final ProxyOptions proxyOptions;
    private final ChannelInitializationProxyHandler channelInitializationProxyHandler;
    private final long readTimeoutMillis;
    private final long responseTimeoutMillis;
    private final long writeTimeoutMillis;

    private final Bootstrap bootstrap;
    private final Consumer<SslContextBuilder> sslContextModifier;
    private final HttpProtocolVersion maximumHttpVersion;

    NettyHttpClient(Bootstrap bootstrap, EventLoopGroup eventLoopGroup, Netty4ConnectionPool connectionPool,
        ProxyOptions proxyOptions, ChannelInitializationProxyHandler channelInitializationProxyHandler,
        Consumer<SslContextBuilder> sslContextModifier, HttpProtocolVersion maximumHttpVersion, long readTimeoutMillis,
        long responseTimeoutMillis, long writeTimeoutMillis) {
        this.bootstrap = bootstrap;
        this.eventLoopGroup = eventLoopGroup;
        this.connectionPool = connectionPool;
        this.proxyOptions = proxyOptions;
        this.channelInitializationProxyHandler = channelInitializationProxyHandler;
        this.sslContextModifier = sslContextModifier;
        this.maximumHttpVersion = maximumHttpVersion;
        this.readTimeoutMillis = readTimeoutMillis;
        this.responseTimeoutMillis = responseTimeoutMillis;
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    Bootstrap getBootstrap() {
        return connectionPool != null ? connectionPool.getBootstrap() : bootstrap;
    }

    @Override
    public Response<BinaryData> send(HttpRequest request) {
        return connectionPool != null ? sendWithConnectionPool(request) : sendWithoutConnectionPool(request);
    }

    private Response<BinaryData> sendWithConnectionPool(HttpRequest request) {
        final URI uri = request.getUri();
        final boolean isHttps = "https".equalsIgnoreCase(uri.getScheme());
        final int port = uri.getPort() == -1 ? (isHttps ? 443 : 80) : uri.getPort();
        final SocketAddress finalDestination = new InetSocketAddress(uri.getHost(), port);

        final Netty4ConnectionPoolKey connectionPoolKey = constructConnectionPoolKey(finalDestination, isHttps);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<ResponseStateInfo> responseReference = new AtomicReference<>();
        final AtomicReference<Throwable> errorReference = new AtomicReference<>();

        Future<Channel> channelFuture = connectionPool.acquire(connectionPoolKey, isHttps);

        channelFuture.addListener((GenericFutureListener<Future<Channel>>) future -> {
            if (!future.isSuccess()) {
                LOGGER.atError().setThrowable(future.cause()).log("Failed connection.");
                errorReference.set(future.cause());
                latch.countDown();
                return;
            }

            final Channel channel = future.getNow();
            try {
                configurePooledRequestPipeline(channel, request, responseReference, errorReference, latch, isHttps);
            } catch (Exception e) {
                // An exception occurred during the pipeline setup.
                // We fire the exception through the pipeline to trigger the cleanup handler,
                // which will ensure the channel is properly closed and not returned to the pool.
                setOrSuppressError(errorReference, e);
                if (channel.isActive()) {
                    channel.pipeline().fireExceptionCaught(e);
                }
                latch.countDown();
            }
        });

        awaitLatch(latch);

        ResponseStateInfo info = responseReference.get();
        if (info != null) {
            return createResponse(request, info);
        }

        if (errorReference.get() != null) {
            throw LOGGER.throwableAtError().log(errorReference.get(), CoreException::from);
        } else {
            throw LOGGER.throwableAtError()
                .log("The request pipeline completed without producing a response or an error.",
                    IllegalStateException::new);
        }
    }

    private Response<BinaryData> sendWithoutConnectionPool(HttpRequest request) {
        URI uri = request.getUri();
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();
        boolean isHttps = "https".equalsIgnoreCase(uri.getScheme());
        ProgressReporter progressReporter = (request.getContext() == null)
            ? null
            : (ProgressReporter) request.getContext().getMetadata("progressReporter");
        boolean addProgressAndTimeoutHandler
            = progressReporter != null || writeTimeoutMillis > 0 || responseTimeoutMillis > 0 || readTimeoutMillis > 0;

        AtomicReference<ResponseStateInfo> responseReference = new AtomicReference<>();
        AtomicReference<Throwable> errorReference = new AtomicReference<>();
        AtomicReference<List<AuthenticateChallenge>> proxyChallenges = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Configure an immutable ChannelInitializer in the builder with everything that can be added on a non-per
        // request basis.
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws SSLException {
                // Test whether proxying should be applied to this Channel. If so, add it.
                boolean hasProxy = channelInitializationProxyHandler.test(channel.remoteAddress());
                if (hasProxy) {
                    ProxyHandler proxyHandler = channelInitializationProxyHandler.createProxy(proxyChallenges);
                    proxyHandler.connectFuture().addListener(future -> {
                        if (!future.isSuccess()) {
                            setOrSuppressError(errorReference, future.cause());
                        }
                    });

                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addFirst(PROXY, proxyHandler);
                    pipeline.addAfter(PROXY, PROXY_EXCEPTION_WARNING_SUPPRESSION,
                        Netty4ConnectionPool.SuppressProxyConnectExceptionWarningHandler.INSTANCE);
                }

                // Add SSL handling if the request is HTTPS.
                if (isHttps) {
                    SslContext ssl = buildSslContext(maximumHttpVersion, sslContextModifier);
                    // SSL handling is added last here. This is done as proxying could require SSL handling too.
                    channel.pipeline().addLast(SSL, ssl.newHandler(channel.alloc(), host, port));
                    channel.pipeline().addLast(SSL_INITIALIZER, new Netty4SslInitializationHandler());

                    channel.pipeline()
                        .addLast(ALPN, new Netty4AlpnHandler(request, responseReference, errorReference, latch));
                }
            }
        });

        bootstrap.connect(host, port).addListener((ChannelFutureListener) connectListener -> {
            if (!connectListener.isSuccess()) {
                LOGGER.atError().setThrowable(connectListener.cause()).log("Failed connection.");
                errorReference.set(connectListener.cause());
                connectListener.channel().close();
                latch.countDown();
                return;
            }

            Channel channel = connectListener.channel();
            channel.closeFuture().addListener(closeListener -> {
                if (!closeListener.isSuccess()) {
                    LOGGER.atError().setThrowable(closeListener.cause()).log("Channel closed with error");
                    setOrSuppressError(errorReference, closeListener.cause());
                }
            });

            // Only add CoreProgressAndTimeoutHandler if it will do anything, ex it is reporting progress or is
            // applying timeouts.
            // This is done to keep the ChannelPipeline shorter, therefore more performant if this
            // effectively is a no-op.
            if (addProgressAndTimeoutHandler) {
                channel.pipeline()
                    .addLast(PROGRESS_AND_TIMEOUT, new Netty4ProgressAndTimeoutHandler(progressReporter,
                        writeTimeoutMillis, responseTimeoutMillis, readTimeoutMillis));
            }

            Throwable earlyError = errorReference.get();
            if (earlyError != null) {
                // If an error occurred between the connecting and the request being sent, don't proceed with sending
                // the request.
                latch.countDown();
                return;
            }

            // What I basically want here is the following logic in Netty:
            // 1. If a proxy exists, it should be added first. When the connection is activated, we should connect
            //    to the proxy (with or without SSL). If there is no proxy, skip this step.
            // 2. Once step 1 is complete, we should wait until the SSL handshake is complete (if applicable).
            //    If SSL isn't being used, skip this step.
            // 3. Once step 2 is complete, we should send the request.
            //
            // None of the steps should block the event loop, so we need to use listeners to ensure that the next
            // step is only executed once the previous step is complete.
            SslHandler sslHandler = channel.pipeline().get(SslHandler.class);
            if (sslHandler != null) {
                // If the SslHandler is present, trigger the SSL handshake to complete before sending the request.
                sslHandler.handshakeFuture().addListener(handshakeListener -> {
                    if (!handshakeListener.isSuccess()) {
                        LOGGER.atError().setThrowable(handshakeListener.cause()).log("Failed SSL handshake.");
                        errorReference.set(handshakeListener.cause());
                        latch.countDown();
                    }
                });
                channel.write(Unpooled.EMPTY_BUFFER);
            } else {
                // If there isn't an SslHandler, we can send the request immediately.
                // Add the HTTP/1.1 codec, as we only support HTTP/2 when using SSL ALPN.
                Netty4ResponseHandler responseHandler
                    = new Netty4ResponseHandler(request, responseReference, errorReference, latch);
                channel.pipeline().addLast(HTTP_RESPONSE, responseHandler);

                String addBefore = addProgressAndTimeoutHandler ? PROGRESS_AND_TIMEOUT : HTTP_RESPONSE;
                channel.pipeline().addBefore(addBefore, HTTP_CODEC, createCodec());

                sendHttp11Request(request, channel, errorReference)
                    .addListener((ChannelFutureListener) sendListener -> {
                        if (!sendListener.isSuccess()) {
                            setOrSuppressError(errorReference, sendListener.cause());
                            sendListener.channel().close();
                            latch.countDown();
                        } else {
                            sendListener.channel().read();
                        }
                    });
            }
        });

        awaitLatch(latch);

        ResponseStateInfo info = responseReference.get();
        if (info == null) {
            throw LOGGER.throwableAtError().log(errorReference.get(), CoreException::from);
        }

        return createResponse(request, info);
    }

    private void configurePooledRequestPipeline(Channel channel, HttpRequest request,
        AtomicReference<ResponseStateInfo> responseReference, AtomicReference<Throwable> errorReference,
        CountDownLatch latch, boolean isHttps) {

        ReentrantLock lock = channel.attr(Netty4ConnectionPool.CHANNEL_LOCK).get();
        lock.lock();
        try {
            channel.config().setAutoRead(false);

            // It's possible that the channel was closed between the time it was acquired and now.
            // This check ensures that we don't try to add handlers to a closed channel.
            // Read handlers are responsible after this check for not being added in a closed channel.
            if (!channel.isActive()) {
                LOGGER.atWarning().log("Channel acquired from the pool is inactive, failing the request.");
                setOrSuppressError(errorReference, new ClosedChannelException());
                latch.countDown();
                return;
            }

            final Object pipelineOwnerToken = new Object();
            channel.attr(Netty4ConnectionPool.PIPELINE_OWNER_TOKEN).set(pipelineOwnerToken);
            ChannelPipeline pipeline = channel.pipeline();

            HttpProtocolVersion protocol = channel.attr(Netty4AlpnHandler.HTTP_PROTOCOL_VERSION_KEY).get();
            boolean isHttp2 = protocol == HttpProtocolVersion.HTTP_2;

            if (protocol == null) {
                // Ideally, this should never happen, but as a safeguard.
                setOrSuppressError(errorReference, new IllegalStateException("Channel from pool is missing protocol."));
                latch.countDown();
                return;
            }

            if (isHttp2) {
                // For H2 (which is always HTTPS), the codec is persistent.
                // Add it only if it's not already there (first request).
                if (pipeline.get(HTTP_CODEC) == null) {
                    pipeline.addAfter(SSL, HTTP_CODEC, createHttp2Codec());
                    pipeline.addAfter(HTTP_CODEC, HTTP2_GOAWAY, new Netty4ConnectionPool.Http2GoAwayHandler());
                }
            } else { // HTTP/1.1 (can be HTTP or HTTPS)
                // For H1, the codec is transient and must be added for every request.
                // The cleanup handler is responsible for removing it.
                String after = isHttps ? SSL : POOL_CONNECTION_HEALTH;
                pipeline.addAfter(after, HTTP_CODEC, createCodec());
            }

            ProgressReporter progressReporter = request.getContext() == null
                ? null
                : (ProgressReporter) request.getContext().getMetadata("progressReporter");

            boolean addProgressAndTimeoutHandler = progressReporter != null
                || writeTimeoutMillis > 0
                || responseTimeoutMillis > 0
                || readTimeoutMillis > 0;

            Netty4ResponseHandler responseHandler
                = new Netty4ResponseHandler(request, responseReference, errorReference, latch);

            if (addProgressAndTimeoutHandler) {
                Netty4ProgressAndTimeoutHandler progressAndTimeoutHandler = new Netty4ProgressAndTimeoutHandler(
                    progressReporter, writeTimeoutMillis, responseTimeoutMillis, readTimeoutMillis);

                pipeline.addAfter(HTTP_CODEC, PROGRESS_AND_TIMEOUT, progressAndTimeoutHandler);
                pipeline.addAfter(PROGRESS_AND_TIMEOUT, HTTP_RESPONSE, responseHandler);
            } else {
                pipeline.addAfter(HTTP_CODEC, HTTP_RESPONSE, responseHandler);
            }

            pipeline.addLast(PIPELINE_CLEANUP,
                new Netty4PipelineCleanupHandler(connectionPool, errorReference, pipelineOwnerToken));

            channel.eventLoop().execute(() -> {
                if (isHttp2) {
                    sendHttp2Request(request, channel, errorReference, latch);
                } else { // HTTP/1.1
                    send(request, channel, errorReference, latch);
                }
            });
        } finally {
            lock.unlock();
        }
    }

    private void send(HttpRequest request, Channel channel, AtomicReference<Throwable> errorReference,
        CountDownLatch latch) {
        sendHttp11Request(request, channel, errorReference).addListener(f -> {
            if (f.isSuccess()) {
                channel.read();
            } else {
                setOrSuppressError(errorReference, f.cause());
                channel.pipeline().fireExceptionCaught(f.cause());
                latch.countDown();
            }
        });
    }

    private Response<BinaryData> createResponse(HttpRequest request, ResponseStateInfo info) {
        BinaryData body;
        Response<BinaryData> response;
        Channel channelToCleanup = info.getResponseChannel();

        channelToCleanup.eventLoop().execute(() -> {
            if (channelToCleanup.pipeline().get(Netty4ResponseHandler.class) != null) {
                channelToCleanup.pipeline().remove(Netty4ResponseHandler.class);
            }
        });

        final Runnable cleanupTask = () -> {
            if (connectionPool != null) {
                channelToCleanup.pipeline().fireUserEventTriggered(Netty4PipelineCleanupEvent.CLEANUP_PIPELINE);
            } else {
                channelToCleanup.close();
            }
        };

        if (info.isChannelConsumptionComplete()) {
            ByteArrayOutputStream eagerContent = info.getEagerContent();

            body = (info.getResponseBodyHandling() != ResponseBodyHandling.IGNORE
                && eagerContent != null
                && eagerContent.size() > 0) ? BinaryData.fromBytes(eagerContent.toByteArray()) : BinaryData.empty();

            channelToCleanup.eventLoop().execute(cleanupTask);
        } else {
            // For all other cases, create a streaming response body.
            // This delegates all body consumption and cleanup logic to Netty4ChannelBinaryData.
            String contentLength = info.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
            Long length = null;
            if (!CoreUtils.isNullOrEmpty(contentLength)) {
                try {
                    length = Long.parseLong(contentLength);
                } catch (NumberFormatException ignored) {
                    // Ignore, we'll just read until the channel is closed.
                }
            }
            body = new Netty4ChannelBinaryData(info.getEagerContent(), info.getResponseChannel(), length,
                info.isHttp2(), cleanupTask);
        }

        response = new Response<>(request, info.getStatusCode(), info.getHeaders(), body);

        if (response.getValue() != BinaryData.empty()
            && ServerSentEventUtils
                .isTextEventStreamContentType(response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE))) {
            ServerSentEventListener listener = request.getServerSentEventListener();

            if (listener != null) {
                try {
                    ServerSentResult serverSentResult
                        = processTextEventStream(response.getValue().toStream(), listener);

                    if (serverSentResult.getException() != null) {
                        // If an exception occurred while processing the text event stream, emit listener onError.
                        listener.onError(serverSentResult.getException());
                    }

                    // If an error occurred or we want to reconnect
                    if (!Thread.currentThread().isInterrupted() && attemptRetry(serverSentResult, request)) {
                        response.close();
                        return this.send(request);
                    }

                    response = new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                        createBodyFromServerSentResult(serverSentResult));
                } catch (IOException ex) {
                    throw LOGGER.throwableAtError().log(ex, CoreException::from);
                }
            } else {
                response.close();
                throw LOGGER.throwableAtError().log(NO_LISTENER_ERROR_MESSAGE, IllegalStateException::new);
            }
        }
        return response;
    }

    public void close() {
        if (connectionPool != null) {
            try {
                connectionPool.close();
            } catch (IOException e) {
                LOGGER.atWarning().setThrowable(e).log("Failed to close the Netty Connection pool.");
            }
        }
        if (eventLoopGroup != null && !eventLoopGroup.isShuttingDown()) {
            eventLoopGroup.shutdownGracefully();
        }
    }

    private static BinaryData createBodyFromServerSentResult(ServerSentResult serverSentResult) {
        return (serverSentResult != null && serverSentResult.getData() != null)
            ? BinaryData.fromString(String.join("\n", serverSentResult.getData()))
            : BinaryData.empty();
    }

    private Netty4ConnectionPoolKey constructConnectionPoolKey(SocketAddress finalDestination, boolean isHttps) {
        final Netty4ConnectionPoolKey key;

        final boolean useProxy = channelInitializationProxyHandler.test(finalDestination);
        if (useProxy) {
            SocketAddress proxyAddress = proxyOptions.getAddress();
            if (isHttps) {
                // For proxied HTTPS, the pool is keyed by the unique combination of the proxy
                // and the final destination. This creates dedicated pools for each tunneled destination.
                key = new Netty4ConnectionPoolKey(proxyAddress, finalDestination);
            } else {
                // For proxied plain HTTP, the pool is keyed only by the proxy address.
                // This allows reusing the same connection to the proxy for different final destinations.
                key = new Netty4ConnectionPoolKey(proxyAddress, proxyAddress);
            }
        } else {
            key = new Netty4ConnectionPoolKey(finalDestination, finalDestination);
        }

        return key;
    }

}
