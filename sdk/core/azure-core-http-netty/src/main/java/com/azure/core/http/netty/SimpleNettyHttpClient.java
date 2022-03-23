// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.implementation.simple.InMemoryBodyCollector;
import com.azure.core.http.netty.implementation.simple.SimpleNettyResponse;
import com.azure.core.http.netty.implementation.simple.SimpleRequestContext;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * TODO (kasobol-msft) add docs.
 */
public class SimpleNettyHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(SimpleNettyHttpClient.class);

    private static final AttributeKey<SimpleRequestContext> REQUEST_CONTEXT_KEY =
        AttributeKey.newInstance("com.azure.core.simple.netty.request.context.key");

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return this.send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        URL url = request.getUrl();
        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(protocol)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(protocol)) {
                port = 443;
            }
        }

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        CompletableFuture<HttpResponse> responseFuture = new CompletableFuture<>();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new MyClientInitializer());

            // Make the connection attempt.
            Channel ch = b.connect(host, port).sync().channel();

            HttpMethod method = mapHttpMethod(request.getHttpMethod());

            // Prepare the HTTP request.
            ByteBuf requestBuffer;
            if (request.getContent() != null) {
                byte[] requestBytes = request.getContent().toBytes();
                requestBuffer = Unpooled.wrappedBuffer(requestBytes);
            } else {
                requestBuffer = Unpooled.buffer(0);
            }
            io.netty.handler.codec.http.HttpRequest nettyRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, method, url.toString(), requestBuffer);

            for (HttpHeader header : request.getHeaders()) {
                nettyRequest.headers().set(header.getName(), header.getValuesList());
            }

            nettyRequest.headers().set(HttpHeaderNames.HOST, host);
            nettyRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            //nettyRequest.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            // Send the HTTP request.
            ch.attr(REQUEST_CONTEXT_KEY).set(
                new SimpleRequestContext(request, responseFuture, new InMemoryBodyCollector()));
            ch.writeAndFlush(nettyRequest);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }

        return Mono.fromFuture(responseFuture);
    }

    private static HttpMethod mapHttpMethod(com.azure.core.http.HttpMethod httpMethod) {
        switch (httpMethod) {
            case GET:
                return HttpMethod.GET;
            case POST:
                return HttpMethod.POST;
            case PUT:
                return HttpMethod.PUT;
            case PATCH:
                return HttpMethod.PATCH;
            case DELETE:
                return HttpMethod.DELETE;
            case HEAD:
                return HttpMethod.HEAD;
            case OPTIONS:
                return HttpMethod.OPTIONS;
            case TRACE:
                return HttpMethod.TRACE;
            case CONNECT:
                return HttpMethod.CONNECT;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown http method"));
        }
    }

    private static class MyClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline p = socketChannel.pipeline();

            // Enable HTTPS if necessary.
            //if (sslCtx != null) {
            //    p.addLast(sslCtx.newHandler(ch.alloc()));
            //}

            p.addLast(new HttpClientCodec());

            // Remove the following line if you don't want automatic content decompression.
            p.addLast(new HttpContentDecompressor());

            // Uncomment the following line if you don't want to handle HttpContents.
            //p.addLast(new HttpObjectAggregator(1048576));

            p.addLast(new MyClientHandler());
        }
    }

    private static class MyClientHandler extends SimpleChannelInboundHandler<HttpObject> {

        @Override
        public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
            SimpleRequestContext requestContext = ctx.channel().attr(REQUEST_CONTEXT_KEY).get();
            if (msg instanceof io.netty.handler.codec.http.HttpResponse) {
                io.netty.handler.codec.http.HttpResponse response = (io.netty.handler.codec.http.HttpResponse) msg;

                requestContext.setStatusCode(response.status().code());

                com.azure.core.http.HttpHeaders coreHeaders = new com.azure.core.http.HttpHeaders();

                if (!response.headers().isEmpty()) {
                    for (String name: response.headers().names()) {
                        for (String value: response.headers().getAll(name)) {
                            coreHeaders.add(name, value);
                        }
                    }
                }

                requestContext.setHttpHeaders(coreHeaders);
            }
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                requestContext.getBodyCollector().collect(content.content());

                if (content instanceof LastHttpContent) {
                    requestContext.getResponseFuture().complete(new SimpleNettyResponse(requestContext));

                    ctx.close();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            SimpleRequestContext requestContext = ctx.channel().attr(REQUEST_CONTEXT_KEY).get();
            requestContext.getResponseFuture().completeExceptionally(cause);
            ctx.close();
        }
    }
}
