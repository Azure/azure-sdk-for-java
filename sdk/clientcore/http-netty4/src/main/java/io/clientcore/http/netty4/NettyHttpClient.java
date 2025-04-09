// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.models.binarydata.FileBinaryData;
import io.clientcore.core.models.binarydata.InputStreamBinaryData;
import io.clientcore.core.utils.ProgressReporter;
import io.clientcore.http.netty4.implementation.CoreProgressAndTimeoutHandler;
import io.clientcore.http.netty4.implementation.CoreResponseHandler;
import io.clientcore.http.netty4.implementation.CoreSslInitializationHandler;
import io.clientcore.http.netty4.implementation.WrappedHttpHeaders;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpDecoderConfig;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeadersFactory;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;

/**
 * HttpClient implementation using synchronous Netty operations.
 */
class NettyHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(NettyHttpClient.class);

    private final Bootstrap bootstrap;
    private final SslContext sslContext;
    private final ProxyOptions proxyOptions;
    private final long readTimeoutMillis;
    private final long responseTimeoutMillis;
    private final long writeTimeoutMillis;

    NettyHttpClient(Bootstrap bootstrap, SslContext sslContext, ProxyOptions proxyOptions, long readTimeoutMillis,
        long responseTimeoutMillis, long writeTimeoutMillis) {
        this.bootstrap = bootstrap;
        this.sslContext = sslContext;
        this.proxyOptions = proxyOptions;
        this.readTimeoutMillis = readTimeoutMillis;
        this.responseTimeoutMillis = responseTimeoutMillis;
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    // For testing.
    ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    Bootstrap getBootstrap() {
        return bootstrap;
    }

    @Override
    public Response<BinaryData> send(HttpRequest request) throws IOException {
        URI uri = request.getUri();
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();
        boolean isHttps = "https".equalsIgnoreCase(uri.getScheme());
        ProgressReporter progressReporter = (request.getContext() == null)
            ? null
            : (ProgressReporter) request.getContext().getMetadata("progressReporter");
        CoreProgressAndTimeoutHandler progressAndTimeoutHandler = new CoreProgressAndTimeoutHandler(progressReporter,
            writeTimeoutMillis, responseTimeoutMillis, readTimeoutMillis);

        // Disable auto-read as we want to control when and how data is read from the channel.
        bootstrap.option(ChannelOption.AUTO_READ, false);
        // TODO (alzimmer): Next task is to add support for connection pooling.
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                if (proxyOptions != null) {
                    if (proxyOptions.getAddress() != null) {
                        // TODO (alzimmer): Need to come back to proxy handling as Netty only offers support for Basic
                        //  authentication and we want to provide broader support through ChallengeHandler.
                        //  Also, we need to support SOCKS here (unless we want to drop that in ClientCore).
                        ch.pipeline()
                            .addFirst(new HttpProxyHandler(
                                new InetSocketAddress(proxyOptions.getAddress().getHostName(),
                                    proxyOptions.getAddress().getPort()),
                                proxyOptions.getUsername(), proxyOptions.getPassword()));
                    }
                }

                if (isHttps) {
                    SslContext ssl = (sslContext != null)
                        ? sslContext
                        : SslContextBuilder.forClient().endpointIdentificationAlgorithm("HTTPS").build();
                    pipeline.addLast(ssl.newHandler(ch.alloc(), host, port));

                    // When SSL is being used we need to defer adding 'CoreResponseHandler' and
                    // 'CoreProgressAndTimeoutHandler' until after the SSL handshake is complete.
                    // This is because the SSL Handshake will require reading to complete and that reading will be SSL
                    // events rather than HTTP response and content events. So, if we add the handlers now, they will
                    // consume the incorrect information.
                    // Another possible option here would be having those handlers ignore SSL events. But that requires
                    // much more state management, therefore is more likely to have issues.
                    pipeline.addLast(new CoreSslInitializationHandler(progressAndTimeoutHandler));
                }

                HttpClientCodec httpClientCodec
                    = new HttpClientCodec(new HttpDecoderConfig().setHeadersFactory(new HttpHeadersFactory() {
                        @Override
                        public HttpHeaders newHeaders() {
                            return new WrappedHttpHeaders(new io.clientcore.core.http.models.HttpHeaders());
                        }

                        @Override
                        public HttpHeaders newEmptyHeaders() {
                            return new WrappedHttpHeaders(new io.clientcore.core.http.models.HttpHeaders());
                        }
                    }), HttpClientCodec.DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST,
                        HttpClientCodec.DEFAULT_FAIL_ON_MISSING_RESPONSE);

                pipeline.addLast(httpClientCodec);
            }
        });

        AtomicReference<Response<BinaryData>> responseReference = new AtomicReference<>();
        AtomicReference<Throwable> errorReference = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Channel channel = bootstrap.connect(host, port).sync().channel();

            if (!isHttps) {
                channel.pipeline().addLast(progressAndTimeoutHandler);
            }

            CoreResponseHandler responseHandler = new CoreResponseHandler(request, responseReference, latch);
            channel.pipeline().addLast(responseHandler);

            sendRequest(request, channel).addListener((ChannelFutureListener) future -> {
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
            throw LOGGER.logThrowableAsError(new IOException("Request interrupted", e));
        }

        Response<BinaryData> response = responseReference.get();
        if (response != null) {
            return response;
        } else {
            throw new IOException(errorReference.get());
        }
    }

    private ChannelFuture sendRequest(HttpRequest request, Channel channel) {
        HttpMethod nettyMethod = HttpMethod.valueOf(request.getHttpMethod().toString());
        String uri = request.getUri().toString();
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(request.getHeaders());

        // TODO (alzimmer): This will mutate the underlying ClientCore HttpHeaders. Will need to think about this design
        //  more once it's closer to completion.
        wrappedHttpHeaders.set(HttpHeaderNames.HOST, request.getUri().getHost());

        BinaryData requestBody = request.getBody();
        if (requestBody instanceof FileBinaryData) {
            FileBinaryData fileBinaryData = (FileBinaryData) requestBody;
            try {
                return sendChunked(channel,
                    new ChunkedNioFile(FileChannel.open(fileBinaryData.getFile(), StandardOpenOption.READ),
                        fileBinaryData.getPosition(), fileBinaryData.getLength(), 8192),
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttpHeaders));
            } catch (IOException ex) {
                return channel.newFailedFuture(ex);
            }
        } else if (requestBody instanceof InputStreamBinaryData) {
            return sendChunked(channel, new ChunkedStream(requestBody.toStream()),
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttpHeaders));
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
            wrappedHttpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, body.readableBytes());
        }

        return channel.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, body,
            wrappedHttpHeaders, trailersFactory().newHeaders()));
    }

    private <T> ChannelFuture sendChunked(Channel channel, ChunkedInput<T> chunkedInput,
        io.netty.handler.codec.http.HttpRequest initialLineAndHeaders) {
        if (channel.pipeline().get(ChunkedWriteHandler.class) == null) {
            // Add the ChunkedWriteHandler which will handle sending the chunkedInput.
            channel.pipeline().addLast(new ChunkedWriteHandler());
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
}
