// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.logging.LogLevel;
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
import reactor.netty.http.client.HttpClientState;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.azure.cosmos.implementation.http.HttpClientConfig.REACTOR_NETWORK_LOG_CATEGORY;

/**
 * HttpClient that is implemented using reactor-netty.
 */
class ReactorNettyClient implements HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyClient.class.getSimpleName());

    private HttpClientConfig httpClientConfig;
    private reactor.netty.http.client.HttpClient httpClient;
    private ConnectionProvider connectionProvider;

    private ReactorNettyClient() {}

    /**
     * Creates ReactorNettyClient with un-pooled connection.
     */
    public static ReactorNettyClient create(HttpClientConfig httpClientConfig) {
        ReactorNettyClient reactorNettyClient = new ReactorNettyClient();
        reactorNettyClient.httpClientConfig = httpClientConfig;
        reactorNettyClient.httpClient = reactor.netty.http.client.HttpClient.newConnection();
        reactorNettyClient.configureChannelPipelineHandlers();
        return reactorNettyClient;
    }

    /**
     * Creates ReactorNettyClient with {@link ConnectionProvider}.
     */
    public static ReactorNettyClient createWithConnectionProvider(ConnectionProvider connectionProvider, HttpClientConfig httpClientConfig) {
        ReactorNettyClient reactorNettyClient = new ReactorNettyClient();
        reactorNettyClient.connectionProvider = connectionProvider;
        reactorNettyClient.httpClientConfig = httpClientConfig;
        reactorNettyClient.httpClient = reactor.netty.http.client.HttpClient.create(connectionProvider);
        reactorNettyClient.configureChannelPipelineHandlers();
        return reactorNettyClient;
    }

    private void configureChannelPipelineHandlers() {
        Configs configs = this.httpClientConfig.getConfigs();
        this.httpClient = this.httpClient.tcpConfiguration(tcpClient -> {

            if (this.httpClientConfig.getProxy() != null) {
                tcpClient =
                    tcpClient.proxy(typeSpec -> typeSpec.type(ProxyProvider.Proxy.HTTP).address(this.httpClientConfig.getProxy().getAddress()));
            }
            tcpClient =
                tcpClient.secure(sslContextSpec -> sslContextSpec.sslContext(configs.getSslContext()));
            if (LoggerFactory.getLogger(REACTOR_NETWORK_LOG_CATEGORY).isTraceEnabled()) {
                tcpClient = tcpClient.wiretap(REACTOR_NETWORK_LOG_CATEGORY, LogLevel.INFO);
            }
            //  By default, keep alive is enabled on http client
            tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                (int) configs.getConnectionAcquireTimeout().toMillis());

            return tcpClient;
        }).httpResponseDecoder(httpResponseDecoderSpec -> {
            httpResponseDecoderSpec.maxInitialLineLength(configs.getMaxHttpInitialLineLength());
            httpResponseDecoderSpec.maxHeaderSize(configs.getMaxHttpHeaderSize());
            httpResponseDecoderSpec.maxChunkSize(configs.getMaxHttpChunkSize());
            httpResponseDecoderSpec.validateHeaders(true);
            return httpResponseDecoderSpec;
        });
    }

    @Override
    public Mono<HttpResponse> send(final HttpRequest request) {
        Objects.requireNonNull(request.httpMethod());
        Objects.requireNonNull(request.uri());
        Objects.requireNonNull(this.httpClientConfig);
        if(request.getReactorNettyRequestRecord() == null) {
            ReactorNettyRequestRecord reactorNettyRequestRecord = new ReactorNettyRequestRecord();
            reactorNettyRequestRecord.setTimeCreated(Instant.now());
            request.setReactorNettyRequestRecord(reactorNettyRequestRecord);
        }

        return this.httpClient
            .observe((connection, state) -> {
                Instant time = Instant.now();
                if(state.equals(HttpClientState.CONNECTED) || state.equals(HttpClientState.ACQUIRED)){
                    request.getReactorNettyRequestRecord().setTimeConnected(time);
                } else if(state.equals(HttpClientState.CONFIGURED)){
                    request.getReactorNettyRequestRecord().setTimeConfigured(time);
                } else if(state.equals(HttpClientState.REQUEST_SENT)){
                    request.getReactorNettyRequestRecord().setTimeSent(time);
                } else if(state.equals(HttpClientState.RESPONSE_RECEIVED)){
                    request.getReactorNettyRequestRecord().setTimeReceived(time);
                }
            })
            .keepAlive(this.httpClientConfig.isConnectionKeepAlive())
                .port(request.port())
                .request(HttpMethod.valueOf(request.httpMethod().toString()))
                .uri(request.uri().toString())
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
                reactorNettyRequest.header(header.name(), header.value());
            }
            if (restRequest.body() != null) {
                return reactorNettyOutbound.send(restRequest.body());
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
    public void shutdown() {
        if (this.connectionProvider != null) {
            this.connectionProvider.dispose();
        }
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
            HttpHeaders headers = new HttpHeaders(reactorNettyResponse.responseHeaders().size());
            reactorNettyResponse.responseHeaders().forEach(e -> headers.set(e.getKey(), e.getValue()));
            return headers;
        }

        @Override
        public Flux<ByteBuf> body() {
            return bodyIntern();
        }

        @Override
        public Mono<byte[]> bodyAsByteArray() {
            return bodyIntern().aggregate().asByteArray();
        }

        @Override
        public Mono<String> bodyAsString() {
            return bodyIntern().aggregate().asString();
        }

        @Override
        public Mono<String> bodyAsString(Charset charset) {
            return bodyIntern().aggregate().asString(charset);
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
