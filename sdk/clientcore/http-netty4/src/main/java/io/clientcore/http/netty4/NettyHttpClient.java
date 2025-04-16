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
import io.clientcore.core.utils.ProgressReporter;
import io.clientcore.core.utils.ServerSentEventUtils;
import io.clientcore.http.netty4.implementation.ChannelInitializationProxyHandler;
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
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
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

import static io.clientcore.core.utils.ServerSentEventUtils.attemptRetry;
import static io.clientcore.core.utils.ServerSentEventUtils.processTextEventStream;
import static io.clientcore.http.netty4.implementation.Netty4Utility.PROGRESS_AND_TIMEOUT_HANDLER_NAME;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
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

        // Configure an immutable ChannelInitializer in the builder with everything that can be added on a non-per
        // request basis.
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws SSLException {
                // Test whether proxying should be applied to this Channel. If so, add it.
                if (channelInitializationProxyHandler.test(ch.remoteAddress())) {
                    ch.pipeline().addFirst(channelInitializationProxyHandler.createProxy(proxyChallenges));
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

        AtomicReference<Response<BinaryData>> responseReference = new AtomicReference<>();
        AtomicReference<Throwable> errorReference = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Channel channel = bootstrap.connect(host, port).sync().channel();

            // Only add CoreProgressAndTimeoutHandler if it will do anything, ex it is reporting progress or is
            // applying timeouts.
            // This is done to keep the ChannelPipeline shorter, therefore more performant, if this would
            // effectively be a no-op.
            if (addProgressAndTimeoutHandler) {
                channel.pipeline()
                    .addLast(PROGRESS_AND_TIMEOUT_HANDLER_NAME, new Netty4ProgressAndTimeoutHandler(progressReporter,
                        writeTimeoutMillis, responseTimeoutMillis, readTimeoutMillis));
            }

            Netty4ResponseHandler responseHandler = new Netty4ResponseHandler(request, responseReference, latch);
            channel.pipeline().addLast(responseHandler);

            sendRequest(request, channel, addProgressAndTimeoutHandler).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    LOGGER.atError().setThrowable(future.cause()).log("Failed to send request");
                    errorReference.set(future.cause());
                    future.channel().close();
                    latch.countDown();
                } else {
                    future.channel().read();
                }
            });

            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw LOGGER.logThrowableAsError(CoreException.from("Request interrupted", e));
        }

        Response<BinaryData> response = responseReference.get();
        if (response == null) {
            throw CoreException.from(errorReference.get());
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
                    throw LOGGER.logThrowableAsError(CoreException.from(ex));
                }
            } else {
                throw LOGGER.logThrowableAsError(new IllegalStateException(NO_LISTENER_ERROR_MESSAGE));
            }
        }

        return response;
    }

    private ChannelFuture sendRequest(HttpRequest request, Channel channel, boolean progressAndTimeoutHandlerAdded) {
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
                    progressAndTimeoutHandlerAdded);
            } catch (IOException ex) {
                return channel.newFailedFuture(ex);
            }
        } else if (requestBody instanceof InputStreamBinaryData) {
            return sendChunked(channel, new ChunkedStream(requestBody.toStream()),
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttpHeaders),
                progressAndTimeoutHandlerAdded);
        }

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

        return channel.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, body,
            wrappedHttpHeaders, trailersFactory().newHeaders()));
    }

    private <T> ChannelFuture sendChunked(Channel channel, ChunkedInput<T> chunkedInput,
        io.netty.handler.codec.http.HttpRequest initialLineAndHeaders, boolean progressAndTimeoutHandlerAdded) {
        if (channel.pipeline().get(ChunkedWriteHandler.class) == null) {
            // Add the ChunkedWriteHandler which will handle sending the chunkedInput.
            ChunkedWriteHandler chunkedWriteHandler = new ChunkedWriteHandler();
            if (progressAndTimeoutHandlerAdded) {
                channel.pipeline().addBefore(PROGRESS_AND_TIMEOUT_HANDLER_NAME, null, chunkedWriteHandler);
            } else {
                channel.pipeline().addLast(chunkedWriteHandler);
            }
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
