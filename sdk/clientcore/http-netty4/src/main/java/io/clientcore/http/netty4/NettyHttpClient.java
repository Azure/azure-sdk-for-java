// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
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
import io.clientcore.http.netty4.implementation.Netty4EagerConsumeChannelHandler;
import io.clientcore.http.netty4.implementation.Netty4HandlerNames;
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
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.clientcore.core.utils.ServerSentEventUtils.attemptRetry;
import static io.clientcore.core.utils.ServerSentEventUtils.processTextEventStream;
import static io.clientcore.http.netty4.implementation.Netty4Utility.awaitLatch;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.sendHttp11Request;
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

    private final Bootstrap bootstrap;
    private final Consumer<SslContextBuilder> sslContextModifier;
    private final ChannelInitializationProxyHandler channelInitializationProxyHandler;
    private final AtomicReference<List<AuthenticateChallenge>> proxyChallenges;
    private final long readTimeoutMillis;
    private final long responseTimeoutMillis;
    private final long writeTimeoutMillis;
    private final HttpProtocolVersion maximumHttpVersion;

    NettyHttpClient(Bootstrap bootstrap, Consumer<SslContextBuilder> sslContextModifier,
        HttpProtocolVersion maximumHttpVersion, ChannelInitializationProxyHandler channelInitializationProxyHandler,
        long readTimeoutMillis, long responseTimeoutMillis, long writeTimeoutMillis) {
        this.bootstrap = bootstrap;
        this.sslContextModifier = sslContextModifier;
        this.maximumHttpVersion = maximumHttpVersion;
        this.channelInitializationProxyHandler = channelInitializationProxyHandler;
        this.proxyChallenges = new AtomicReference<>();
        this.readTimeoutMillis = readTimeoutMillis;
        this.responseTimeoutMillis = responseTimeoutMillis;
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    Bootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    public Response<BinaryData> send(HttpRequest request) {
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

                    channel.pipeline().addFirst(Netty4HandlerNames.PROXY, proxyHandler);
                }

                // Add SSL handling if the request is HTTPS.
                if (isHttps) {
                    SslContextBuilder sslContextBuilder
                        = SslContextBuilder.forClient().endpointIdentificationAlgorithm("HTTPS");
                    if (maximumHttpVersion == HttpProtocolVersion.HTTP_2) {
                        // If HTTP/2 is the maximum version, we need to ensure that ALPN is enabled.
                        SslProvider sslProvider = SslContext.defaultClientProvider();
                        ApplicationProtocolConfig.SelectorFailureBehavior selectorBehavior;
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedBehavior;
                        if (sslProvider == SslProvider.JDK) {
                            selectorBehavior = ApplicationProtocolConfig.SelectorFailureBehavior.FATAL_ALERT;
                            selectedBehavior = ApplicationProtocolConfig.SelectedListenerFailureBehavior.FATAL_ALERT;
                        } else {
                            // Netty OpenSslContext doesn't support FATAL_ALERT, use NO_ADVERTISE and ACCEPT
                            // instead.
                            selectorBehavior = ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE;
                            selectedBehavior = ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
                        }

                        sslContextBuilder.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                            .applicationProtocolConfig(new ApplicationProtocolConfig(
                                ApplicationProtocolConfig.Protocol.ALPN, selectorBehavior, selectedBehavior,
                                ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1));
                    }
                    if (sslContextModifier != null) {
                        // Allow the caller to modify the SslContextBuilder before it is built.
                        sslContextModifier.accept(sslContextBuilder);
                    }

                    SslContext ssl = sslContextBuilder.build();
                    // SSL handling is added last here. This is done as proxying could require SSL handling too.
                    channel.pipeline().addLast(Netty4HandlerNames.SSL, ssl.newHandler(channel.alloc(), host, port));
                    channel.pipeline()
                        .addLast(Netty4HandlerNames.SSL_INITIALIZER, new Netty4SslInitializationHandler());
                }

                if (isHttps) {
                    channel.pipeline()
                        .addLast(new Netty4AlpnHandler(request, addProgressAndTimeoutHandler, errorReference, latch));
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
            // This is done to keep the ChannelPipeline shorter, therefore more performant, if this would
            // effectively be a no-op.
            if (addProgressAndTimeoutHandler) {
                channel.pipeline()
                    .addLast(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, new Netty4ProgressAndTimeoutHandler(
                        progressReporter, writeTimeoutMillis, responseTimeoutMillis, readTimeoutMillis));
            }

            Netty4ResponseHandler responseHandler
                = new Netty4ResponseHandler(request, responseReference, errorReference, latch);
            channel.pipeline().addLast(Netty4HandlerNames.RESPONSE, responseHandler);

            Throwable earlyError = errorReference.get();
            if (earlyError != null) {
                // If an error occurred between the connect and the request being sent, don't proceed with sending
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
                HttpClientCodec codec = createCodec();
                if (addProgressAndTimeoutHandler) {
                    channel.pipeline()
                        .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_1_1_CODEC, codec);
                } else {
                    channel.pipeline().addBefore(Netty4HandlerNames.RESPONSE, Netty4HandlerNames.HTTP_1_1_CODEC, codec);
                }

                sendHttp11Request(request, channel, addProgressAndTimeoutHandler, errorReference)
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

        Response<BinaryData> response;
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
                }));
                channel.config().setAutoRead(true);
                awaitLatch(drainLatch);
            } else if (bodyHandling == ResponseBodyHandling.STREAM) {
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

                body = new Netty4ChannelBinaryData(info.getEagerContent(), channel, length);
            } else {
                // All cases otherwise assume BUFFER.
                CountDownLatch drainLatch = new CountDownLatch(1);
                channel.pipeline().addLast(new Netty4EagerConsumeChannelHandler(drainLatch, buf -> {
                    try {
                        buf.readBytes(info.getEagerContent(), buf.readableBytes());
                    } catch (IOException ex) {
                        throw LOGGER.throwableAtError().log(ex, CoreException::from);
                    }
                }));
                channel.config().setAutoRead(true);
                awaitLatch(drainLatch);

                body = BinaryData.fromBytes(info.getEagerContent().toByteArray());
            }

            response = new Response<>(request, info.getStatusCode(), info.getHeaders(), body);
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

    public void close() {
        EventLoopGroup group = bootstrap.config().group();
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    private static BinaryData createBodyFromServerSentResult(ServerSentResult serverSentResult) {
        return (serverSentResult != null && serverSentResult.getData() != null)
            ? BinaryData.fromString(String.join("\n", serverSentResult.getData()))
            : BinaryData.empty();
    }

}
