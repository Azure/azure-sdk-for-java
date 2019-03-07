/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * HttpClient that is implemented using reactor-netty.
 */
class ReactorNettyClient extends HttpClient {
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

    @Override
    public Mono<HttpResponse> send(final HttpRequest request) {
        Objects.requireNonNull(request.httpMethod());
        Objects.requireNonNull(request.url());
        Objects.requireNonNull(request.url().getProtocol());
        //
        Mono<HttpResponse> response = httpClient
                .request(HttpMethod.valueOf(request.httpMethod().toString()))
                .uri(request.url().toString())
                .send(bodySendDelegate(request))
                .responseConnection(responseDelegate(request))
                .single();
        return response;
    }

    /**
     * Delegate to send the request content.
     *
     * @param restRequest the Rest request contains the body to be sent
     * @return a delegate upon invocation sets the request body in reactor-netty outbound object
     */
    private static BiFunction<HttpClientRequest, NettyOutbound, Publisher<Void>> bodySendDelegate(final HttpRequest restRequest) {
        BiFunction<HttpClientRequest, NettyOutbound, Publisher<Void>> sendDelegate = (reactorNettyRequest, reactorNettyOutbound) -> {
            for (HttpHeader header : restRequest.headers()) {
                reactorNettyRequest.header(header.name(), header.value());
            }
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
     * Delegate to receive response.
     *
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
                public String headerValue(String name) {
                    return reactorNettyResponse.responseHeaders().get(name);
                }

                @Override
                public com.microsoft.rest.v3.http.HttpHeaders headers() {
                    Map<String, String> map = new HashMap<>();
                    reactorNettyResponse.responseHeaders().forEach(e -> map.put(e.getKey(), e.getValue()));
                    return new com.microsoft.rest.v3.http.HttpHeaders(map);
                }

                @Override
                public Flux<ByteBuf> body() {
                    final ByteBufFlux body = bodyIntern();
                    //
                    return body.doFinally(s -> {
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
            };
            return Mono.just(httpResponse.withRequest(restRequest));
        };
        return responseDelegate;
    }

    @Override
    protected final HttpClient setProxy(Supplier<ProxyOptions> proxyOptionsSupplier) {
        return new ClientProxyOptions(this.httpClient, proxyOptionsSupplier);
    }

    @Override
    protected final HttpClient setWiretap(boolean enableWiretap) {
        return new ClientWiretap(this.httpClient, enableWiretap);
    }

    @Override
    protected final HttpClient setPort(int port) {
        return new ClientPort(this.httpClient, port);
    }

    private static class ClientProxyOptions extends ReactorNettyClient {
        ClientProxyOptions(reactor.netty.http.client.HttpClient httpClient, Supplier<ProxyOptions> proxyOptions) {
            super(httpClient.tcpConfiguration(tcpClient -> {
                ProxyOptions options = proxyOptions.get();
                return tcpClient.proxy(ts -> ts.type(options.type().value()).address(options.address()));
            }));
        }
    }

    private static class ClientWiretap extends ReactorNettyClient {
        ClientWiretap(reactor.netty.http.client.HttpClient httpClient, boolean enableWiretap) {
            super(httpClient.wiretap(enableWiretap));
        }
    }

    private static class ClientPort extends ReactorNettyClient {
        ClientPort(reactor.netty.http.client.HttpClient httpClient, int port) {
            super(httpClient.port(port));
        }
    }
}
