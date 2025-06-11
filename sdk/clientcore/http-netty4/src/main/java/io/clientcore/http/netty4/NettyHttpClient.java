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
import io.clientcore.core.models.binarydata.FileBinaryData;
import io.clientcore.core.models.binarydata.InputStreamBinaryData;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.clientcore.core.utils.ProgressReporter;
import io.clientcore.core.utils.ServerSentEventUtils;
import io.clientcore.http.netty4.implementation.ChannelInitializationProxyHandler;
import io.clientcore.http.netty4.implementation.Netty4AlpnHandler;
import io.clientcore.http.netty4.implementation.Netty4HandlerNames;
import io.clientcore.http.netty4.implementation.Netty4ProgressAndTimeoutHandler;
import io.clientcore.http.netty4.implementation.Netty4ResponseHandler;
import io.clientcore.http.netty4.implementation.Netty4SslInitializationHandler;
import io.clientcore.http.netty4.implementation.WrappedHttpHeaders;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2DataChunkedInput;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.clientcore.core.utils.ServerSentEventUtils.attemptRetry;
import static io.clientcore.core.utils.ServerSentEventUtils.processTextEventStream;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;
import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;

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

        AtomicReference<Response<BinaryData>> responseReference = new AtomicReference<>();
        AtomicReference<Throwable> errorReference = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Configure an immutable ChannelInitializer in the builder with everything that can be added on a non-per
        // request basis.
        bootstrap.handler(createChannelInitializer(errorReference, isHttps, host, port));

        try {
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
                            .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_1_1_CODEC,
                                codec);
                    } else {
                        channel.pipeline()
                            .addBefore(Netty4HandlerNames.RESPONSE, Netty4HandlerNames.HTTP_1_1_CODEC, codec);
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

            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw LOGGER.throwableAtError().log("Request interrupted.", e, CoreException::from);
        }

        Response<BinaryData> response = responseReference.get();
        if (response == null) {
            throw LOGGER.throwableAtError().log(errorReference.get(), CoreException::from);
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

    private ChannelInitializer<Channel> createChannelInitializer(AtomicReference<Throwable> errorReference,
        boolean isHttps, String host, int port) {
        return new ChannelInitializer<Channel>() {
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

                if (isHttps && maximumHttpVersion == HttpProtocolVersion.HTTP_2) {
                    channel.pipeline().addLast(new Netty4AlpnHandler());
                }
            }
        };
    }

    private ChannelFuture sendHttp11Request(HttpRequest request, Channel channel,
        boolean progressAndTimeoutHandlerAdded, AtomicReference<Throwable> errorReference) {
        HttpMethod nettyMethod = HttpMethod.valueOf(request.getHttpMethod().toString());
        String uri = request.getUri().toString();
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(request.getHeaders());

        // TODO (alzimmer): This will mutate the underlying ClientCore HttpHeaders. Will need to think about this design
        //  more once it's closer to completion.
        wrappedHttpHeaders.getCoreHeaders().set(HttpHeaderName.HOST, request.getUri().getHost());

        BinaryData requestBody = request.getBody();
        if (requestBody instanceof FileBinaryData) {
            FileBinaryData fileBinaryData = (FileBinaryData) requestBody;
            try {
                return sendChunkedHttp11(channel,
                    new ChunkedNioFile(FileChannel.open(fileBinaryData.getFile(), StandardOpenOption.READ),
                        fileBinaryData.getPosition(), fileBinaryData.getLength(), 8192),
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttpHeaders),
                    progressAndTimeoutHandlerAdded, errorReference);
            } catch (IOException ex) {
                return channel.newFailedFuture(ex);
            }
        } else if (requestBody instanceof InputStreamBinaryData) {
            return sendChunkedHttp11(channel, new ChunkedStream(requestBody.toStream()),
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttpHeaders),
                progressAndTimeoutHandlerAdded, errorReference);
        } else {
            ByteBuf body = Unpooled.EMPTY_BUFFER;
            if (requestBody != null && requestBody != BinaryData.empty()) {
                // Longer term, see if there is a way to have BinaryData act as the ByteBuf body to further eliminate
                // copying of byte[]s.
                body = Unpooled.wrappedBuffer(requestBody.toBytes());
            }
            if (body.readableBytes() > 0) {
                // TODO (alzimmer): Should we be setting Content-Length here again? Shouldn't this be handled externally
                //  by the creator of the HttpRequest?
                wrappedHttpHeaders.getCoreHeaders()
                    .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(body.readableBytes()));
            }

            Throwable error = errorReference.get();
            if (error != null) {
                return channel.newFailedFuture(error);
            }

            return channel.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, body,
                wrappedHttpHeaders, trailersFactory().newHeaders()));
        }
    }

    private <T> ChannelFuture sendChunkedHttp11(Channel channel, ChunkedInput<T> chunkedInput,
        io.netty.handler.codec.http.HttpRequest initialLineAndHeaders, boolean progressAndTimeoutHandlerAdded,
        AtomicReference<Throwable> errorReference) {
        if (channel.pipeline().get(Netty4HandlerNames.CHUNKED_WRITER) == null) {
            // Add the ChunkedWriteHandler which will handle sending the chunkedInput.
            ChunkedWriteHandler chunkedWriteHandler = new ChunkedWriteHandler();
            if (progressAndTimeoutHandlerAdded) {
                channel.pipeline()
                    .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.CHUNKED_WRITER,
                        chunkedWriteHandler);
            } else {
                channel.pipeline().addLast(Netty4HandlerNames.CHUNKED_WRITER, chunkedWriteHandler);
            }
        }

        Throwable error = errorReference.get();
        if (error != null) {
            return channel.newFailedFuture(error);
        }

        channel.write(initialLineAndHeaders);
        return channel.writeAndFlush(chunkedInput);
    }

    private ChannelFuture sendHttp2Request(HttpRequest request, Channel channel, boolean progressAndTimeoutHandlerAdded,
        AtomicReference<Throwable> errorReference) {
        // HTTP/2 requests are more complicated than HTTP/1.1 as they are a stream of frames with specific purposes.
        // Additionally, since we're using multiplexing, we need to associate a stream ID with each frame.

        // Send the headers frame(s).
        // Unlike in HTTP/1.1, there isn't a status line on requests. Rather pseudo headers are used.
        // TODO (alzimmer): Create an Http2Headers implementations similar to WrappedHttpHeaders.
        Http2Headers headers = new DefaultHttp2Headers();
        headers.method(request.getHttpMethod().toString());
        headers.scheme(request.getUri().getScheme());
        headers.authority(request.getUri().getAuthority());
        if (request.getUri().getPath() != null) {
            headers.path(request.getUri().getPath());
        }

        // If the request doesn't have a body or is a HEAD request, only a headers frame should be sent before the
        // client indicates closure of its half of the stream.
        BinaryData requestBody = request.getBody();
        Long bodyLength = requestBody.getLength();
        boolean headersOnly = (bodyLength != null && bodyLength == 0)
            || request.getHttpMethod() == io.clientcore.core.http.models.HttpMethod.HEAD;

        request.getHeaders()
            .stream()
            .forEach(httpHeader -> headers.add(httpHeader.getName().getCaseInsensitiveName(), httpHeader.getValues()));
        Http2HeadersFrame headersFrame = new DefaultHttp2HeadersFrame(headers, headersOnly);

        if (headersOnly) {
            return channel.write(headersFrame);
        }

        channel.write(headersFrame);

        // Now it's time to write the data frames.
        if (requestBody instanceof FileBinaryData) {
            FileBinaryData fileBinaryData = (FileBinaryData) requestBody;
            try {
                return sendChunkedHttp2(channel,
                    new ChunkedNioFile(FileChannel.open(fileBinaryData.getFile(), StandardOpenOption.READ),
                        fileBinaryData.getPosition(), fileBinaryData.getLength(), 8192),
                    progressAndTimeoutHandlerAdded, errorReference);
            } catch (IOException ex) {
                return channel.newFailedFuture(ex);
            }
        } else if (requestBody instanceof InputStreamBinaryData) {
            return sendChunkedHttp2(channel, new ChunkedStream(requestBody.toStream()), progressAndTimeoutHandlerAdded,
                errorReference);
        } else {
            ByteBuf body = Unpooled.wrappedBuffer(requestBody.toBytes());

            Throwable error = errorReference.get();
            if (error != null) {
                return channel.newFailedFuture(error);
            }

            return channel.writeAndFlush(new DefaultHttp2DataFrame(body, true));
        }
    }

    private ChannelFuture sendChunkedHttp2(Channel channel, ChunkedInput<ByteBuf> chunkedInput,
        boolean progressAndTimeoutHandlerAdded, AtomicReference<Throwable> errorReference) {
        if (channel.pipeline().get(Netty4HandlerNames.CHUNKED_WRITER) == null) {
            // Add the ChunkedWriteHandler which will handle sending the chunkedInput.
            ChunkedWriteHandler chunkedWriteHandler = new ChunkedWriteHandler();
            if (progressAndTimeoutHandlerAdded) {
                channel.pipeline()
                    .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.CHUNKED_WRITER,
                        chunkedWriteHandler);
            } else {
                channel.pipeline().addLast(Netty4HandlerNames.CHUNKED_WRITER, chunkedWriteHandler);
            }
        }

        Throwable error = errorReference.get();
        if (error != null) {
            return channel.newFailedFuture(error);
        }

        return channel.writeAndFlush(new Http2DataChunkedInput(chunkedInput, null));
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
