// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
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
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.ProgressReporter;
import io.clientcore.core.utils.ServerSentEventUtils;
import io.clientcore.http.netty4.implementation.ChannelInitializationProxyHandler;
import io.clientcore.http.netty4.implementation.Netty4ChannelBinaryData;
import io.clientcore.http.netty4.implementation.Netty4EagerConsumeChannelHandler;
import io.clientcore.http.netty4.implementation.Netty4ProgressAndTimeoutHandler;
import io.clientcore.http.netty4.implementation.Netty4ResponseHandler;
import io.clientcore.http.netty4.implementation.Netty4SslInitializationHandler;
import io.clientcore.http.netty4.implementation.ResponseBodyHandling;
import io.clientcore.http.netty4.implementation.ResponseStateInfo;
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
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.utils.ServerSentEventUtils.attemptRetry;
import static io.clientcore.core.utils.ServerSentEventUtils.processTextEventStream;
import static io.clientcore.http.netty4.implementation.Netty4Utility.PROGRESS_AND_TIMEOUT_HANDLER_NAME;
import static io.clientcore.http.netty4.implementation.Netty4Utility.awaitLatch;
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
    private final SslContext sslContext;
    private final ChannelInitializationProxyHandler channelInitializationProxyHandler;
    private final AtomicReference<List<AuthenticateChallenge>> proxyChallenges;
    private final long readTimeoutMillis;
    private final long responseTimeoutMillis;
    private final long writeTimeoutMillis;

    NettyHttpClient(Bootstrap bootstrap, SslContext sslContext,
        ChannelInitializationProxyHandler channelInitializationProxyHandler, long readTimeoutMillis,
        long responseTimeoutMillis, long writeTimeoutMillis) {
        this.bootstrap = bootstrap;
        this.sslContext = sslContext;
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
            protected void initChannel(Channel ch) throws SSLException {
                // Test whether proxying should be applied to this Channel. If so, add it.
                boolean hasProxy = channelInitializationProxyHandler.test(ch.remoteAddress());
                if (hasProxy) {
                    ProxyHandler proxyHandler = channelInitializationProxyHandler.createProxy(proxyChallenges);
                    proxyHandler.connectFuture().addListener(future -> {
                        if (!future.isSuccess()) {
                            setOrSuppressError(errorReference, future.cause());
                        }
                    });

                    ch.pipeline().addFirst(proxyHandler);
                }

                // Add SSL handling if the request is HTTPS.
                if (isHttps) {
                    SslContext ssl = (sslContext != null)
                        ? sslContext
                        : SslContextBuilder.forClient().endpointIdentificationAlgorithm("HTTPS").build();
                    // SSL handling is added last here. This is done as proxying could require SSL handling too.
                    ch.pipeline().addLast(ssl.newHandler(ch.alloc(), host, port), new Netty4SslInitializationHandler());
                }

                // Finally add the HttpClientCodec last as it will need to handle processing request and response
                // writes and reads for not only the actual request but any proxy or SSL handling.
                ch.pipeline().addLast(createCodec());
            }
        });

        bootstrap.connect(host, port).addListener((ChannelFutureListener) connectListener -> {
            if (!connectListener.isSuccess()) {
                LOGGER.atError().setThrowable(connectListener.cause()).log("Failed to send request");
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
                    .addLast(PROGRESS_AND_TIMEOUT_HANDLER_NAME, new Netty4ProgressAndTimeoutHandler(progressReporter,
                        writeTimeoutMillis, responseTimeoutMillis, readTimeoutMillis));
            }

            Netty4ResponseHandler responseHandler
                = new Netty4ResponseHandler(request, responseReference, errorReference, latch);
            channel.pipeline().addLast(responseHandler);

            Throwable earlyError = errorReference.get();
            if (earlyError != null) {
                // If an error occurred between the connect and the request being sent, don't proceed with sending
                // the request.
                latch.countDown();
                return;
            }

            sendRequest(request, channel, addProgressAndTimeoutHandler, errorReference)
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
                channel.pipeline().addLast(new Netty4EagerConsumeChannelHandler(latch, ignored -> {
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
                CountDownLatch drainLath = new CountDownLatch(1);
                channel.pipeline().addLast(new Netty4EagerConsumeChannelHandler(latch, buf -> {
                    try {
                        buf.readBytes(info.getEagerContent(), buf.readableBytes());
                    } catch (IOException ex) {
                        throw LOGGER.throwableAtError().log(ex, CoreException::from);
                    }
                }));
                channel.config().setAutoRead(true);
                awaitLatch(drainLath);

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

    private ChannelFuture sendRequest(HttpRequest request, Channel channel, boolean progressAndTimeoutHandlerAdded,
        AtomicReference<Throwable> errorReference) {
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
                return sendChunked(channel,
                    new ChunkedNioFile(FileChannel.open(fileBinaryData.getFile(), StandardOpenOption.READ),
                        fileBinaryData.getPosition(), fileBinaryData.getLength(), 8192),
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttpHeaders),
                    progressAndTimeoutHandlerAdded, errorReference);
            } catch (IOException ex) {
                return channel.newFailedFuture(ex);
            }
        } else if (requestBody instanceof InputStreamBinaryData) {
            return sendChunked(channel, new ChunkedStream(requestBody.toStream()),
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

    private <T> ChannelFuture sendChunked(Channel channel, ChunkedInput<T> chunkedInput,
        io.netty.handler.codec.http.HttpRequest initialLineAndHeaders, boolean progressAndTimeoutHandlerAdded,
        AtomicReference<Throwable> errorReference) {
        if (channel.pipeline().get(ChunkedWriteHandler.class) == null) {
            // Add the ChunkedWriteHandler which will handle sending the chunkedInput.
            ChunkedWriteHandler chunkedWriteHandler = new ChunkedWriteHandler();
            if (progressAndTimeoutHandlerAdded) {
                channel.pipeline().addBefore(PROGRESS_AND_TIMEOUT_HANDLER_NAME, null, chunkedWriteHandler);
            } else {
                channel.pipeline().addLast(chunkedWriteHandler);
            }
        }

        Throwable error = errorReference.get();
        if (error != null) {
            return channel.newFailedFuture(error);
        }

        channel.write(initialLineAndHeaders);
        return channel.writeAndFlush(chunkedInput);
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
