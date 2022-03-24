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
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.azure.core.http.netty.implementation.simple.SimpleNettyConstants.REQUEST_CONTEXT_KEY;

/**
 * TODO (kasobol-msft) add docs.
 */
public class SimpleNettyHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(SimpleNettyHttpClient.class);

    private final EventLoopGroup eventLoopGroup;

    private volatile Channel channelPool;

    /**
     * TODO (kasobol-msft) add docs.
     */
    public SimpleNettyHttpClient() {
        eventLoopGroup = new NioEventLoopGroup(r -> {
            Thread t = new Thread(r);
            // TODO (kasobol-msft) is there better way? Closeable? reactor-netty seems to default to daemons.
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return this.send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return Mono.fromFuture(sendInternal(request, context));
    }

    @Override
    public HttpResponse sendSynchronously(HttpRequest request, Context context) {
        try {
            return sendInternal(request, context).get();
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private CompletableFuture<HttpResponse> sendInternal(HttpRequest request, Context context) {
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
        CompletableFuture<HttpResponse> responseFuture = new CompletableFuture<>();
        try {
            // Make the connection attempt.
            Channel ch = channelPool;
            if (ch == null || !ch.isActive()) {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new MyClientInitializer());
                ch = b.connect(host, port).sync().channel();
                channelPool = ch;
            }

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
            //nettyRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            //nettyRequest.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            if (requestBuffer.readableBytes() > 0) {
                // TODO (kasobol-msft) what about chunked?
                nettyRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, requestBuffer.readableBytes());
            }

            // Send the HTTP request.
            ch.attr(REQUEST_CONTEXT_KEY).set(
                new SimpleRequestContext(request, responseFuture, new InMemoryBodyCollector()));
            ch.writeAndFlush(nettyRequest);

            // TODO (kasobol-msft) where do we close conn?
            // Wait for the server to close the connection.
            //ch.closeFuture().sync();
        } catch (InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
        // Shut down executor threads to exit.
        // TODO (kasobol-msft) should we do this somehow ?
        // group.shutdownGracefully()

        return responseFuture;
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
                    CompletableFuture<HttpResponse> responseFuture = requestContext.getResponseFuture();
                    ctx.channel().attr(REQUEST_CONTEXT_KEY).set(null);
                    responseFuture.complete(new SimpleNettyResponse(requestContext));
                    // TODO (kasobol-msft) do not close conn. remove this ?
                    // ctx.close();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            SimpleRequestContext requestContext = ctx.channel().attr(REQUEST_CONTEXT_KEY).get();
            ctx.channel().attr(REQUEST_CONTEXT_KEY).set(null);
            requestContext.getResponseFuture().completeExceptionally(cause);
            ctx.close();
        }
    }
}
