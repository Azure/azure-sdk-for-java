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
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.ProgressReporter;
import io.clientcore.core.utils.ServerSentEventUtils;
import io.clientcore.http.netty4.implementation.ChannelInitializationProxyHandler;
import io.clientcore.http.netty4.implementation.Netty4AlpnHandler;
import io.clientcore.http.netty4.implementation.Netty4ChannelBinaryData;
import io.clientcore.http.netty4.implementation.Netty4ConnectionPool;
import io.clientcore.http.netty4.implementation.Netty4ConnectionPoolKey;
import io.clientcore.http.netty4.implementation.Netty4EagerConsumeChannelHandler;
import io.clientcore.http.netty4.implementation.Netty4PipelineCleanupHandler;
import io.clientcore.http.netty4.implementation.Netty4ProgressAndTimeoutHandler;
import io.clientcore.http.netty4.implementation.Netty4ResponseHandler;
import io.clientcore.http.netty4.implementation.ResponseBodyHandling;
import io.clientcore.http.netty4.implementation.ResponseStateInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.utils.ServerSentEventUtils.attemptRetry;
import static io.clientcore.core.utils.ServerSentEventUtils.processTextEventStream;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.ALPN;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_CODEC;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_RESPONSE;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PIPELINE_CLEANUP;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROGRESS_AND_TIMEOUT;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.SSL;
import static io.clientcore.http.netty4.implementation.Netty4Utility.awaitLatch;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
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

    NettyHttpClient(EventLoopGroup eventLoopGroup, Netty4ConnectionPool connectionPool, ProxyOptions proxyOptions,
        ChannelInitializationProxyHandler channelInitializationProxyHandler, long readTimeoutMillis,
        long responseTimeoutMillis, long writeTimeoutMillis) {
        this.eventLoopGroup = eventLoopGroup;
        this.connectionPool = connectionPool;
        this.proxyOptions = proxyOptions;
        this.channelInitializationProxyHandler = channelInitializationProxyHandler;
        this.readTimeoutMillis = readTimeoutMillis;
        this.responseTimeoutMillis = responseTimeoutMillis;
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    Bootstrap getBootstrap() {
        return connectionPool.getBootstrap();
    }

    @Override
    public Response<BinaryData> send(HttpRequest request) {
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

            Channel channel = future.getNow();
            try {
                configureRequestPipeline(channel, request, responseReference, errorReference, latch, isHttps);
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

        if (errorReference.get() != null) {
            throw LOGGER.throwableAtError().log(errorReference.get(), CoreException::from);
        }

        ResponseStateInfo info = responseReference.get();
        if (info == null) {
            throw LOGGER.throwableAtError().log(errorReference.get(), CoreException::from);
        }

        Response<BinaryData> response;
        Channel channelToRelease;

        if (info.isChannelConsumptionComplete()) {
            // The network response is already complete, handle creating our Response based on the request method and
            // response headers.
            BinaryData body = BinaryData.empty();
            ByteArrayOutputStream eagerContent = info.getEagerContent();
            if (info.getResponseBodyHandling() != ResponseBodyHandling.IGNORE && eagerContent.size() > 0) {
                // Set the response body as the first HttpContent received if the request wasn't a HEAD request and
                // there was body content.
                body = BinaryData.fromBytes(eagerContent.toByteArray());
            }
            channelToRelease = info.getResponseChannel();
            response = new Response<>(request, info.getStatusCode(), info.getHeaders(), body);
        } else {
            // Otherwise we aren't finished, handle the remaining content according to the documentation in
            // 'channelRead()'.
            BinaryData body = BinaryData.empty();
            ResponseBodyHandling bodyHandling = info.getResponseBodyHandling();
            Channel channel = info.getResponseChannel();
            if (bodyHandling == ResponseBodyHandling.IGNORE) {
                // We're ignoring the response content.
                CountDownLatch drainLatch = new CountDownLatch(1);
                channel.pipeline().addLast(new Netty4EagerConsumeChannelHandler(drainLatch, ignored -> {
                }, info.isHttp2()));
                channel.config().setAutoRead(true);
                awaitLatch(drainLatch);
                channelToRelease = channel;
            } else if (bodyHandling == ResponseBodyHandling.STREAM) {
                channelToRelease = null;
                // Body streaming uses a special BinaryData that tracks the firstContent read and the Channel it came
                // from so it can be consumed when the BinaryData is being used.
                // autoRead should have been disabled already but lets make sure that it is.
                channel.config().setAutoRead(false);
                String contentLength = info.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);
                Long length = null;
                if (!CoreUtils.isNullOrEmpty(contentLength)) {
                    try {
                        length = Long.parseLong(contentLength);
                    } catch (NumberFormatException ignored) {
                        // Ignore, we'll just read until the channel is closed.
                    }
                }

                body = new Netty4ChannelBinaryData(info.getEagerContent(), channel, length, info.isHttp2());
            } else {
                // All cases otherwise assume BUFFER.
                CountDownLatch drainLatch = new CountDownLatch(1);
                channel.pipeline().addLast(new Netty4EagerConsumeChannelHandler(drainLatch, buf -> {
                    try {
                        buf.readBytes(info.getEagerContent(), buf.readableBytes());
                    } catch (IOException ex) {
                        throw LOGGER.throwableAtError().log(ex, CoreException::from);
                    }
                }, info.isHttp2()));
                channel.config().setAutoRead(true);
                awaitLatch(drainLatch);
                channelToRelease = channel;

                body = BinaryData.fromBytes(info.getEagerContent().toByteArray());
            }

            response = new Response<>(request, info.getStatusCode(), info.getHeaders(), body);
        }

        if (channelToRelease != null) {
            channelToRelease.eventLoop().execute(() -> {
                Netty4PipelineCleanupHandler cleanupHandler
                    = channelToRelease.pipeline().get(Netty4PipelineCleanupHandler.class);
                if (cleanupHandler != null) {
                    cleanupHandler.cleanup(channelToRelease.pipeline().context(cleanupHandler), false);
                }
            });
        }

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
                throw LOGGER.throwableAtError().log(NO_LISTENER_ERROR_MESSAGE, IllegalStateException::new);
            }
        }

        return response;
    }

    private void configureRequestPipeline(Channel channel, HttpRequest request,
        AtomicReference<ResponseStateInfo> responseReference, AtomicReference<Throwable> errorReference,
        CountDownLatch latch, boolean isHttps) {

        // It's possible that the channel was closed between the time it was acquired and now.
        // This check ensures that we don't try to add handlers to a closed channel.
        if (!channel.isActive()) {
            LOGGER.atWarning().log("Channel acquired from the pool is inactive, failing the request.");
            setOrSuppressError(errorReference, new ClosedChannelException());
            latch.countDown();
            return;
        }

        ProgressReporter progressReporter = (request.getContext() == null)
            ? null
            : (ProgressReporter) request.getContext().getMetadata("progressReporter");
        boolean addProgressAndTimeoutHandler
            = progressReporter != null || writeTimeoutMillis > 0 || responseTimeoutMillis > 0 || readTimeoutMillis > 0;

        ChannelPipeline pipeline = channel.pipeline();

        // The first handler added is the cleanup handler. It will be the last to execute
        // in the outbound direction and the first in the inbound direction, but its main
        // purpose is to clean up all other request-specific handlers and release the channel.
        pipeline.addLast(PIPELINE_CLEANUP, new Netty4PipelineCleanupHandler(connectionPool, errorReference, latch));

        // Only add CoreProgressAndTimeoutHandler if it will do anything, ex it is reporting progress or is
        // applying timeouts.
        // This is done to keep the ChannelPipeline shorter, therefore more performant if this would
        // effectively be a no-op.
        if (addProgressAndTimeoutHandler) {
            pipeline.addLast(PROGRESS_AND_TIMEOUT, new Netty4ProgressAndTimeoutHandler(progressReporter,
                writeTimeoutMillis, responseTimeoutMillis, readTimeoutMillis));
        }

        // The SslHandler is already in the pipeline if this is an HTTPS request, as it's added
        // by the connection pool during the initial connection setup. The SSL handshake is also
        // guaranteed to be complete by the time we get the channel because the Netty4AlpnHandler
        // reacts to the result of the ALPN negotiation that happened during the SSL handshake.
        if (isHttps) {
            HttpProtocolVersion protocolVersion = channel.attr(Netty4AlpnHandler.HTTP_PROTOCOL_VERSION_KEY).get();
            if (protocolVersion != null) {
                // The Connection is being reused, ALPN is already done.
                // Manually configure the pipeline based on the stored protocol.
                boolean isHttp2 = protocolVersion == HttpProtocolVersion.HTTP_2;
                pipeline.addLast(HTTP_RESPONSE,
                    new Netty4ResponseHandler(request, responseReference, errorReference, latch));

                if (!isHttp2 && pipeline.get(HTTP_CODEC) == null) {
                    pipeline.addBefore(HTTP_RESPONSE, HTTP_CODEC, createCodec());
                }

                if (isHttp2) {
                    sendHttp2Request(request, channel, errorReference, latch);
                } else {
                    send(request, channel, errorReference, latch);
                }
            } else {
                // This is a new connection, let ALPN do the work.
                // For HTTPS, we delegate the addition of the response handler and codec to the ALPN handler.
                pipeline.addAfter(SSL, ALPN, new Netty4AlpnHandler(request, responseReference, errorReference, latch));
            }
        } else {
            // If there isn't an SslHandler, we can send the request immediately.
            // Add the HTTP/1.1 codec, as we only support HTTP/2 when using SSL ALPN.
            pipeline.addLast(HTTP_RESPONSE,
                new Netty4ResponseHandler(request, responseReference, errorReference, latch));
            String addBefore = addProgressAndTimeoutHandler ? PROGRESS_AND_TIMEOUT : HTTP_RESPONSE;
            pipeline.addBefore(addBefore, HTTP_CODEC, createCodec());
            send(request, channel, errorReference, latch);
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

    public void close() {
        if (connectionPool != null) {
            try {
                connectionPool.close();
            } catch (IOException e) {
                LOGGER.atWarning().setThrowable(e).log("Failed to close Netty4ConnectionPool.");
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
