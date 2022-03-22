// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import reactor.core.publisher.Mono;

import java.net.URL;

/**
 * TODO (kasobol-msft) add docs.
 */
public class SimpleNettyHttpClient implements HttpClient {

    private static ClientLogger LOGGER = new ClientLogger(SimpleNettyHttpClient.class);

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

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new MyClientInitializer());

            // Make the connection attempt.
            Channel ch = b.connect(host, port).sync().channel();

            // Prepare the HTTP request.
            io.netty.handler.codec.http.HttpRequest nettyRequest = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, url.toString());
            nettyRequest.headers().set(HttpHeaders.Names.HOST, host);
            nettyRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            nettyRequest.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            // Set some example cookies.
            nettyRequest.headers().set(
                HttpHeaders.Names.COOKIE,
                ClientCookieEncoder.encode(
                    new DefaultCookie("my-cookie", "foo"),
                    new DefaultCookie("another-cookie", "bar")));

            // Send the HTTP request.

            ch.writeAndFlush(nettyRequest);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }

        return Mono.empty();
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
            if (msg instanceof io.netty.handler.codec.http.HttpResponse) {
                io.netty.handler.codec.http.HttpResponse response = (io.netty.handler.codec.http.HttpResponse) msg;

                System.err.println("STATUS: " + response.getStatus());
                System.err.println("VERSION: " + response.getProtocolVersion());
                System.err.println();

                if (!response.headers().isEmpty()) {
                    for (String name: response.headers().names()) {
                        for (String value: response.headers().getAll(name)) {
                            System.err.println("HEADER: " + name + " = " + value);
                        }
                    }
                    System.err.println();
                }

                if (HttpHeaders.isTransferEncodingChunked(response)) {
                    System.err.println("CHUNKED CONTENT {");
                } else {
                    System.err.println("CONTENT {");
                }
            }
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                System.err.print(content.content().toString(CharsetUtil.UTF_8));
                System.err.flush();

                if (content instanceof LastHttpContent) {
                    System.err.println("} END OF CONTENT");
                    ctx.close();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
