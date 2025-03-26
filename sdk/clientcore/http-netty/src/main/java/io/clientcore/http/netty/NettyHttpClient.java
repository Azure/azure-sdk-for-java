// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.http.netty.implementation.CoreHandler;
import io.clientcore.http.netty.implementation.NettyHttpResponse;
import io.clientcore.http.netty.implementation.WrappedHttpHeaders;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.http.models.HttpMethod.HEAD;

/**
 * HttpClient implementation using synchronous Netty operations.
 */
class NettyHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(NettyHttpClient.class);

    private final Bootstrap bootstrap;
    private final ProxyOptions proxyOptions;
    private final long readTimeoutMillis;
    private final long responseTimeoutMillis;
    private final long writeTimeoutMillis;

    NettyHttpClient(Bootstrap bootstrap, ProxyOptions proxyOptions, long readTimeoutMillis, long responseTimeoutMillis,
        long writeTimeoutMillis) {
        this.bootstrap = bootstrap;
        this.proxyOptions = proxyOptions;
        this.readTimeoutMillis = readTimeoutMillis;
        this.responseTimeoutMillis = responseTimeoutMillis;
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    @Override
    public Response<BinaryData> send(HttpRequest request) throws IOException {
        URI uri = request.getUri();
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();

        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                if (proxyOptions != null) {
                    if (proxyOptions.getAddress() != null) {
                        ch.pipeline()
                            .addFirst(new HttpProxyHandler(
                                new InetSocketAddress(proxyOptions.getAddress().getHostName(),
                                    proxyOptions.getAddress().getPort()), proxyOptions.getUsername(),
                                proxyOptions.getPassword()));
                    }
                }

                if ("https".equalsIgnoreCase(uri.getScheme())) {
                    SslContext sslContext
                        = SslContextBuilder.forClient().endpointIdentificationAlgorithm("HTTPS").build();
                    pipeline.addLast(sslContext.newHandler(ch.alloc(), host, port));
                }

                pipeline.addLast(new HttpClientCodec());
                pipeline.addLast(new HttpObjectAggregator(1048576));
                pipeline.addLast(new CoreHandler(null, writeTimeoutMillis, responseTimeoutMillis, readTimeoutMillis));
            }
        });

        AtomicReference<Response<BinaryData>> responseReference = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Channel channel = bootstrap.connect(host, port).sync().channel();

            FullHttpRequest nettyRequest = toNettyRequest(request);
            channel.writeAndFlush(nettyRequest).addListener(future -> {
                if (!future.isSuccess()) {
                    LOGGER.atLevel(LogLevel.ERROR).log("Failed to send request", future.cause());
                    latch.countDown();
                }
            });

            channel.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
                    responseReference.set(processResponse(request, response));
                    latch.countDown();
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    LOGGER.atLevel(LogLevel.ERROR).log("Error processing response", cause);
                    latch.countDown();
                }
            });

            latch.await();
            channel.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw LOGGER.logThrowableAsError(new IOException("Request interrupted", e));
        }

        return responseReference.get();
    }

    private FullHttpRequest toNettyRequest(HttpRequest request) {
        HttpMethod nettyMethod = HttpMethod.valueOf(request.getHttpMethod().toString());
        String uri = request.getUri().toString();

        ByteBuf content = Unpooled.EMPTY_BUFFER;
        if (request.getBody() != null && request.getBody() != BinaryData.empty()) {
            content = Unpooled.wrappedBuffer(request.getBody().toBytes());
        }

        FullHttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, content);

        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(request.getHeaders());

        // TODO (alzimmer): This will mutate the underlying ClientCore HttpHeaders. Will need to think about this design
        //  more once it's closer to completion.
        wrappedHttpHeaders.set(HttpHeaderNames.HOST, request.getUri().getHost());
        if (content.readableBytes() > 0) {
            // TODO (alzimmer): Should we be setting Content-Length here again? Shouldn't this be handled externally
            //  by the creator of the HttpRequest?
            wrappedHttpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        }

        return nettyRequest;
    }

    private Response<BinaryData> processResponse(HttpRequest request, FullHttpResponse response) {
        HttpHeaders responseHeaders = response.headers();

        BinaryData body = BinaryData.empty();
        switch (getBodyHandling(request, responseHeaders)) {
            case IGNORE:
                if (response.content().isReadable()) {
                    response.content().release();
                }
                break;

            //            case STREAM:
            //                body = BinaryData.fromStream(response.content().nioBuffer().asInputStream());
            //                break;

            case BUFFER:
                body = BinaryData.fromBytes(response.content().nioBuffer().array());
                break;

            default:
                body = BinaryData.empty();
                break;
        }

        return new NettyHttpResponse(body, response.status().code(), request, response);
    }

    private BodyHandling getBodyHandling(HttpRequest request, HttpHeaders responseHeaders) {
        String contentType = responseHeaders.get(HttpHeaderNames.CONTENT_TYPE);

        if (request.getHttpMethod() == HEAD) {
            return BodyHandling.IGNORE;
        } else if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            return BodyHandling.STREAM;
        } else {
            return BodyHandling.BUFFER;
        }
    }

    private enum BodyHandling {
        IGNORE, STREAM, BUFFER
    }

    public void close() {
        EventLoopGroup group = bootstrap.config().group();
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
