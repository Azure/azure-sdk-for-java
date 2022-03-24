// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.implementation.simple.InMemoryBodyCollector;
import com.azure.core.http.netty.implementation.simple.SimpleChannelPoolMap;
import com.azure.core.http.netty.implementation.simple.SimpleRequestContext;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.azure.core.http.netty.implementation.simple.SimpleNettyConstants.REQUEST_CONTEXT_KEY;

/**
 * TODO (kasobol-msft) add docs.
 */
public class SimpleNettyHttpClient implements HttpClient {

    private static final ClientLogger LOGGER = new ClientLogger(SimpleNettyHttpClient.class);

    private final ChannelPoolMap<URI, ChannelPool> channelPoolMap;

    /**
     * TODO (kasobol-msft) add docs.
     */
    public SimpleNettyHttpClient() {
        // TODO (kasobol-msft) is there better way? Closeable? reactor-netty seems to default to daemons.
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(r -> {
            Thread t = new Thread(r);
            // TODO (kasobol-msft) is there better way? Closeable? reactor-netty seems to default to daemons.
            t.setDaemon(true);
            return t;
        });

        channelPoolMap = new SimpleChannelPoolMap(eventLoopGroup);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return this.send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return Mono.fromFuture(() -> sendInternal(request, context));
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

        // Configure the client.
        CompletableFuture<HttpResponse> responseFuture = new CompletableFuture<>();
        // Make the connection attempt.
        URI channelPoolKey;
        try {
            channelPoolKey = new URI(url.getProtocol(), null, url.getHost(),
                url.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
        ChannelPool channelPool = channelPoolMap.get(channelPoolKey);
        SimpleRequestContext requestContext = new SimpleRequestContext(
            channelPool, request, responseFuture, new InMemoryBodyCollector());
        channelPool.acquire()
            .addListener(new ConnectionAcquiredListener(requestContext));

        return responseFuture;
    }

    private static final class ConnectionAcquiredListener implements FutureListener<Channel> {
        private final SimpleRequestContext requestContext;

        private ConnectionAcquiredListener(SimpleRequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public void operationComplete(Future<Channel> future) throws Exception {
            if (future.isSuccess()) {
                Channel ch = future.getNow();
                try {
                    ch.attr(REQUEST_CONTEXT_KEY).set(requestContext);

                    HttpRequest request = requestContext.getRequest();
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
                        HttpVersion.HTTP_1_1, method, request.getUrl().toString(), requestBuffer);

                    for (HttpHeader header : request.getHeaders()) {
                        nettyRequest.headers().set(header.getName(), header.getValuesList());
                    }

                    nettyRequest.headers().set(HttpHeaderNames.HOST, request.getUrl().getHost());
                    //nettyRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                    //nettyRequest.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

                    if (requestBuffer.readableBytes() > 0) {
                        // TODO (kasobol-msft) what about chunked?
                        nettyRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, requestBuffer.readableBytes());
                    }

                    // Send the HTTP request.
                    ch.attr(REQUEST_CONTEXT_KEY).set(
                        requestContext);
                    ch.writeAndFlush(nettyRequest);
                } catch (RuntimeException e) {
                    requestContext.getChannelPool().release(ch);
                    requestContext.getResponseFuture().completeExceptionally(e);
                }
            } else {
                requestContext.getResponseFuture().completeExceptionally(future.cause());
            }
        }
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
}
