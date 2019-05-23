// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyOutbound;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * HttpClient that is implemented using reactor-netty.
 */
class ReactorNettyClient implements HttpClient {
    private reactor.netty.http.client.HttpClient httpClient;

    /**
     * Creates default ReactorNettyClient.
     */
    ReactorNettyClient() {
        this(reactor.netty.http.client.HttpClient.create());
    }

    /**
     * Creates ReactorNettyClient with provided http client.
     *
     * @param httpClient the reactor http client
     */
    private ReactorNettyClient(reactor.netty.http.client.HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     *  Creates ReactorNettyClient with provided http client with configuration applied.
     *
     * @param httpClient the reactor http client
     * @param config the configuration to apply on the http client
     */
    private ReactorNettyClient(reactor.netty.http.client.HttpClient httpClient, Function<reactor.netty.http.client.HttpClient, reactor.netty.http.client.HttpClient> config) {
        this.httpClient = config.apply(httpClient);
    }

    @Override
    public Mono<HttpResponse> send(final HttpRequest request) {
        Objects.requireNonNull(request.httpMethod());
        Objects.requireNonNull(request.url());
        Objects.requireNonNull(request.url().getProtocol());
        //
        return httpClient
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
            Mono.just(new ReactorNettyHttpResponse(reactorNettyResponse, reactorNettyConnection).withRequest(restRequest));
    }

    @Override
    public final HttpClient proxy(Supplier<ProxyOptions> proxyOptionsSupplier) {
        return new ReactorNettyClient(this.httpClient, client -> client.tcpConfiguration(c -> {
            ProxyOptions options = proxyOptionsSupplier.get();
            return c.proxy(ts -> ts.type(options.type().value()).address(options.address()));
        }));
    }

    @Override
    public final HttpClient wiretap(boolean enableWiretap) {
        return new ReactorNettyClient(this.httpClient, client -> client.wiretap(enableWiretap));
    }

    @Override
    public final HttpClient port(int port) {
        return new ReactorNettyClient(this.httpClient, client -> client.port(port));
    }

    private static class ReactorNettyHttpResponse extends HttpResponse {
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
        public Flux<ByteBuf> body() {
            return bodyIntern().doFinally(s -> {
                if (!reactorNettyConnection.isDisposed()) {
                    reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
                }
            });
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

        @Override
        Connection internConnection() {
            return reactorNettyConnection;
        }
    }
}
