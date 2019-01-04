/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyOutbound;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An HttpClient that is implemented using Netty.
 */
public final class NettyClient extends HttpClient {
    private final HttpClientConfiguration configuration;
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    /**
     * Creates NettyClient.
     *
     * @param configuration
     *            the HTTP client configuration.
     */
    private NettyClient(HttpClientConfiguration configuration) {
        this.configuration = configuration != null ? configuration : new HttpClientConfiguration(null);
    }

    @Override
    public Mono<HttpResponse> sendRequestAsync(final HttpRequest request) {
        Objects.requireNonNull(request.httpMethod());
        Objects.requireNonNull(request.url());
        Objects.requireNonNull(request.url().getProtocol());
        //
        Mono<HttpResponse> response = reactor.netty.http.client.HttpClient.create()
                .wiretap(true)
                .headersWhen(headersDelegate(request))
                .request(HttpMethod.valueOf(request.httpMethod().toString()))
                .uri(request.url().toString())
                .send(bodySendDelegate(request))
                .responseConnection(responseDelegate(request))
                .last();
        return response;
    }

    /**
     * @param restRequest the Rest request contains the header to be sent
     * @return a delegate upon invocation sets the request headers in reactor-netty request object
     */
    private static Function<HttpHeaders, Mono<? extends HttpHeaders>> headersDelegate(final HttpRequest restRequest) {
        Function<HttpHeaders, Mono<? extends HttpHeaders>> headersDelegate = (HttpHeaders reactorNettyHeaders) -> {
            for (HttpHeader header : restRequest.headers()) {
                reactorNettyHeaders.set(header.name(), header.value());
            }
            return Mono.just(reactorNettyHeaders);
        };
        return headersDelegate;
    }

    /**
     * @param restRequest the Rest request contains the body to be sent
     * @return a delegate upon invocation sets the request body in reactor-netty outbound object
     */
    private static BiFunction<HttpClientRequest, NettyOutbound, Publisher<Void>> bodySendDelegate(final HttpRequest restRequest) {
        BiFunction<HttpClientRequest, NettyOutbound, Publisher<Void>> sendDelegate = (reactorNettyRequest, reactorNettyOutbound) -> {
            if (restRequest.body() != null) {
                Flux<ByteBuf> nettyByteBufFlux = restRequest.body().map(Unpooled::wrappedBuffer);
                return reactorNettyOutbound.send(nettyByteBufFlux);
            } else {
                return reactorNettyOutbound;
            }
        };
        return sendDelegate;
    }

    /**
     * @param restRequest the Rest request whose response this delegate handles
     * @return a delegate upon invocation setup Rest response object
     */
    private static BiFunction<HttpClientResponse, Connection, Publisher<HttpResponse>> responseDelegate(final HttpRequest restRequest) {
        BiFunction<HttpClientResponse, Connection, Publisher<HttpResponse>> responseDelegate = (reactorNettyResponse, reactorNettyConnection) -> {
            HttpResponse httpResponse = new HttpResponse() {
                @Override
                public int statusCode() {
                    return reactorNettyResponse.status().code();
                }

                @Override
                public String headerValue(String headerName) {
                    return reactorNettyResponse.responseHeaders().get(headerName);
                }

                @Override
                public com.microsoft.rest.v2.http.HttpHeaders headers() {
                    Map<String, String> map = new HashMap<>();
                    reactorNettyResponse.responseHeaders().forEach(e -> map.put(e.getKey(), e.getValue()));
                    return new com.microsoft.rest.v2.http.HttpHeaders(map);
                }

                @Override
                public Flux<ByteBuffer> body() {
                    final ByteBufFlux body = bodyIntern();
                    //
                    Flux<ByteBuffer> javaNioByteBufferFlux = body.map((ByteBuf nettyByteBuf) -> {
                        ByteBuffer dst = ByteBuffer.allocate(nettyByteBuf.readableBytes());
                        nettyByteBuf.readBytes(dst);
                        dst.flip();
                        return dst;
                    }).doFinally(s -> {
                        if (!reactorNettyConnection.isDisposed()) {
                            reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                        }
                    });
                    //
                    return javaNioByteBufferFlux;
                }

                @Override
                public Mono<byte[]> bodyAsByteArray() {
                    return bodyIntern().aggregate().asByteArray().doFinally(s -> {
                        if (!reactorNettyConnection.isDisposed()) {
                            reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                        }
                    });
                }

                @Override
                public Mono<String> bodyAsString() {
                    return bodyIntern().aggregate().asString().doFinally(s -> {
                        if (!reactorNettyConnection.isDisposed()) {
                            reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                        }
                    });
                }

                @Override
                public void close() {
                    if (!reactorNettyConnection.isDisposed()) {
                        reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                    }
                }

                private ByteBufFlux bodyIntern() {
                    return reactorNettyConnection.inbound().receive();
                }

                @Override
                Connection internConnection() {
                    return reactorNettyConnection;
                }
            };
            return Mono.just(httpResponse);
        };
        return responseDelegate;
    }

    /**
     * The factory for creating a NettyClient.
     */
    public static class Factory implements HttpClientFactory {

        /**
         * Create a Netty client factory with default settings.
         */
        public Factory() {
        }

        @Override
        public HttpClient create(final HttpClientConfiguration configuration) {
            return new NettyClient(configuration);
        }

        @Override
        public void close() {
        }
    }
}
