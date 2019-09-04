// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyOutbound;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * This class provides a Netty-based implementation for the {@link HttpClient} interface. Creating an instance of
 * this class can be achieved by using the {@link NettyAsyncHttpClientBuilder} class, which offers Netty-specific API
 * for features such as {@link NettyAsyncHttpClientBuilder#setNioEventLoopGroup(NioEventLoopGroup) thread pooling},
 * {@link NettyAsyncHttpClientBuilder#setWiretap(boolean) wiretapping},
 * {@link NettyAsyncHttpClientBuilder#setProxy(ProxyOptions) setProxy configuration}, and much more.
 *
 * @see HttpClient
 * @see NettyAsyncHttpClientBuilder
 */
public class NettyAsyncHttpClient implements HttpClient {
    final reactor.netty.http.client.HttpClient nettyClient;

    /**
     * Creates default NettyAsyncHttpClient.
     */
    NettyAsyncHttpClient() {
        this(reactor.netty.http.client.HttpClient.create());
    }

    /**
     * Creates NettyAsyncHttpClient with provided http client.
     *
     * @param nettyClient the reactor-netty http client
     */
    NettyAsyncHttpClient(reactor.netty.http.client.HttpClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    /** {@inheritDoc} */
    @Override
    public Mono<HttpResponse> send(final HttpRequest request) {
        Objects.requireNonNull(request.httpMethod());
        Objects.requireNonNull(request.url());
        Objects.requireNonNull(request.url().getProtocol());

        return nettyClient
            .request(HttpMethod.valueOf(request.httpMethod().toString()))
            .uri(request.url().toString())
            .send(bodySendDelegate(request))
            .responseConnection(responseDelegate(request))
            .single();
    }

    /**
     * Delegate to send the request content.
     *
     * @param restRequest the Rest request contains the body to be sent
     * @return a delegate upon invocation sets the request body in reactor-netty outbound object
     */
    private static BiFunction<HttpClientRequest, NettyOutbound, Publisher<Void>> bodySendDelegate(final HttpRequest restRequest) {
        return (reactorNettyRequest, reactorNettyOutbound) -> {
            for (HttpHeader header : restRequest.headers()) {
                if (header.value() != null) {
                    reactorNettyRequest.header(header.name(), header.value());
                }
            }
            if (restRequest.body() != null) {
                Flux<ByteBuf> nettyByteBufFlux = restRequest.body().map(Unpooled::wrappedBuffer);
                return reactorNettyOutbound.send(nettyByteBufFlux);
            } else {
                return reactorNettyOutbound;
            }
        };
    }

    /**
     * Delegate to receive response.
     *
     * @param restRequest the Rest request whose response this delegate handles
     * @return a delegate upon invocation setup Rest response object
     */
    private static BiFunction<HttpClientResponse, Connection, Publisher<HttpResponse>> responseDelegate(final HttpRequest restRequest) {
        return (reactorNettyResponse, reactorNettyConnection) ->
            Mono.just(new ReactorNettyHttpResponse(reactorNettyResponse, reactorNettyConnection).request(restRequest));
    }

    static class ReactorNettyHttpResponse extends HttpResponse {
        private final HttpClientResponse reactorNettyResponse;
        private final Connection reactorNettyConnection;

        ReactorNettyHttpResponse(HttpClientResponse reactorNettyResponse, Connection reactorNettyConnection) {
            this.reactorNettyResponse = reactorNettyResponse;
            this.reactorNettyConnection = reactorNettyConnection;
        }

        @Override
        public int statusCode() {
            return reactorNettyResponse.status().code();
        }

        @Override
        public String headerValue(String name) {
            return reactorNettyResponse.responseHeaders().get(name);
        }

        @Override
        public HttpHeaders headers() {
            HttpHeaders headers = new HttpHeaders();
            reactorNettyResponse.responseHeaders().forEach(e -> headers.put(e.getKey(), e.getValue()));
            return headers;
        }

        @Override
        public Flux<ByteBuffer> body() {
            return bodyIntern().doFinally(s -> {
                if (!reactorNettyConnection.isDisposed()) {
                    reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                }
            }).map(ByteBuf::nioBuffer);
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
        public Mono<String> bodyAsString(Charset charset) {
            return bodyIntern().aggregate().asString(charset).doFinally(s -> {
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

        // used for testing only
        Connection internConnection() {
            return reactorNettyConnection;
        }
    }
}
