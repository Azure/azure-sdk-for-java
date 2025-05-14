// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.implementation.Configs;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.logging.LogLevel;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyOutbound;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.http.client.HttpClientState;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.ProxyProvider;
import reactor.util.context.Context;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * HttpClient that is implemented using reactor-netty.
 */
public class ReactorNettyClient implements HttpClient {

    private static final String REACTOR_NETTY_REQUEST_RECORD_KEY = "reactorNettyRequestRecordKey";

    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyClient.class.getSimpleName());

    private static final MethodHandle HTTP_CLIENT_WARMUP;

    static {
        MethodHandle httpClientWarmup = null;
        try {
            httpClientWarmup = MethodHandles.publicLookup()
                .findVirtual(reactor.netty.http.client.HttpClient.class, "warmup", MethodType.methodType(Mono.class));
        } catch (IllegalAccessException | NoSuchMethodException ex) {
            // Version of Reactor Netty doesn't have the warmup API on HttpClient.
            // So warmup won't be performed and this error is ignored.
        }

        HTTP_CLIENT_WARMUP = httpClientWarmup;
    }

    private HttpClientConfig httpClientConfig;
    private reactor.netty.http.client.HttpClient httpClient;
    private ConnectionProvider connectionProvider;
    private String reactorNetworkLogCategory;

    private ReactorNettyClient() {}

    /**
     * Creates ReactorNettyClient with un-pooled connection.
     */
    public static ReactorNettyClient create(HttpClientConfig httpClientConfig) {
        ReactorNettyClient reactorNettyClient = new ReactorNettyClient();
        reactorNettyClient.httpClientConfig = httpClientConfig;
        reactorNettyClient.reactorNetworkLogCategory = httpClientConfig.getReactorNetworkLogCategory();
        reactorNettyClient.httpClient = reactor.netty.http.client.HttpClient
            .newConnection()
            .observe(getConnectionObserver())
            .resolver(DefaultAddressResolverGroup.INSTANCE);
        reactorNettyClient.configureChannelPipelineHandlers();
        attemptToWarmupHttpClient(reactorNettyClient);
        return reactorNettyClient;
    }

    /**
     * Creates ReactorNettyClient with {@link ConnectionProvider}.
     */
    public static ReactorNettyClient createWithConnectionProvider(ConnectionProvider connectionProvider, HttpClientConfig httpClientConfig) {
        ReactorNettyClient reactorNettyClient = new ReactorNettyClient();
        reactorNettyClient.connectionProvider = connectionProvider;
        reactorNettyClient.httpClientConfig = httpClientConfig;
        reactorNettyClient.reactorNetworkLogCategory = httpClientConfig.getReactorNetworkLogCategory();
        reactorNettyClient.httpClient = reactor.netty.http.client.HttpClient
            .create(connectionProvider)
            .observe(getConnectionObserver())
            .resolver(DefaultAddressResolverGroup.INSTANCE);
        reactorNettyClient.configureChannelPipelineHandlers();
        attemptToWarmupHttpClient(reactorNettyClient);
        return reactorNettyClient;
    }

    /*
     * This enables fast warm up of HttpClient
     */
    private static void attemptToWarmupHttpClient(ReactorNettyClient reactorNettyClient) {
        // Warmup wasn't found, so don't attempt it.
        if (HTTP_CLIENT_WARMUP == null) {
            return;
        }

        try {
            ((Mono<?>) HTTP_CLIENT_WARMUP.invoke(reactorNettyClient.httpClient)).block();
        } catch (ClassCastException | WrongMethodTypeException throwable) {
            // Invocation failed.
            logger.debug("Invoking HttpClient.warmup failed.", throwable);
        } catch (Throwable throwable) {
            // Warmup failed.
            throw new RuntimeException(throwable);
        }
    }

    private void configureChannelPipelineHandlers() {
        Configs configs = this.httpClientConfig.getConfigs();

        if (this.httpClientConfig.getProxy() != null) {
            this.httpClient = this.httpClient.proxy(typeSpec -> typeSpec.type(ProxyProvider.Proxy.HTTP)
                .address(this.httpClientConfig.getProxy().getAddress())
                .username(this.httpClientConfig.getProxy().getUsername())
                .password(userName -> this.httpClientConfig.getProxy().getPassword())
            );
        }

        if (LoggerFactory.getLogger(reactorNetworkLogCategory).isTraceEnabled()) {
            this.httpClient = this.httpClient.wiretap(this.httpClientConfig.getReactorNetworkLogCategory(), LogLevel.INFO);
        }

        this.httpClient =
            this.httpClient
                .secure(sslContextSpec ->
                    sslContextSpec.sslContext(
                        configs.getSslContext(
                            httpClientConfig.isServerCertValidationDisabled(),
                            false)))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) this.httpClientConfig.getConnectionAcquireTimeout().toMillis())
                .httpResponseDecoder(httpResponseDecoderSpec ->
                    httpResponseDecoderSpec.maxInitialLineLength(this.httpClientConfig.getMaxInitialLineLength())
                        .maxHeaderSize(this.httpClientConfig.getMaxHeaderSize())
                        .maxChunkSize(this.httpClientConfig.getMaxChunkSize())
                        .validateHeaders(true));

        if (httpClientConfig.getHttp2ConnectionConfig().isEnabled()) {
            this.httpClient = this.httpClient
                .secure(sslContextSpec ->
                    sslContextSpec.sslContext(
                        configs.getSslContext(
                            httpClientConfig.isServerCertValidationDisabled(),
                            true
                        )))
                .protocol(HttpProtocol.H2, HttpProtocol.HTTP11)
                .doOnConnected((connection -> {
                    // The response header clean up pipeline is being added due to an error getting when calling gateway:
                    // java.lang.IllegalArgumentException: a header value contains prohibited character 0x20 at index 0 for 'x-ms-serviceversion', there is whitespace in the front of the value.
                    // validateHeaders(false) does not work for http2
                    ChannelPipeline channelPipeline = connection.channel().pipeline();
                    if (channelPipeline.get("reactor.left.httpCodec") != null) {
                        channelPipeline.addAfter(
                            "reactor.left.httpCodec",
                            "customHeaderCleaner",
                            new Http2ResponseHeaderCleanerHandler());
                    }
                }));
        }
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, this.httpClientConfig.getNetworkRequestTimeout());
    }

    @Override
    public Mono<HttpResponse> send(final HttpRequest request, Duration responseTimeout) {
        Objects.requireNonNull(request.httpMethod());
        Objects.requireNonNull(request.uri());
        Objects.requireNonNull(this.httpClientConfig);
        if(request.reactorNettyRequestRecord() == null) {
            ReactorNettyRequestRecord reactorNettyRequestRecord = new ReactorNettyRequestRecord();
            reactorNettyRequestRecord.setTimeCreated(Instant.now());
            request.withReactorNettyRequestRecord(reactorNettyRequestRecord);
        }

        final AtomicReference<ReactorNettyHttpResponse> responseReference = new AtomicReference<>();

        return this.httpClient
            .keepAlive(this.httpClientConfig.isConnectionKeepAlive())
            .port(request.port())
            .responseTimeout(responseTimeout)
            .request(HttpMethod.valueOf(request.httpMethod().toString()))
            .uri(request.uri().toASCIIString())
            .send(bodySendDelegate(request))
            .responseConnection((reactorNettyResponse, reactorNettyConnection) -> {
                HttpResponse httpResponse = new ReactorNettyHttpResponse(reactorNettyResponse,
                    reactorNettyConnection).withRequest(request);
                responseReference.set((ReactorNettyHttpResponse) httpResponse);
                return Mono.just(httpResponse);
            })
            .contextWrite(Context.of(REACTOR_NETTY_REQUEST_RECORD_KEY, request.reactorNettyRequestRecord()))
            .doOnCancel(() -> {
                ReactorNettyHttpResponse reactorNettyHttpResponse = responseReference.get();
                if (reactorNettyHttpResponse != null) {
                    reactorNettyHttpResponse.releaseOnNotSubscribedResponse(ReactorNettyResponseState.CANCELLED);
                }
            })
            .onErrorMap(throwable -> {
                ReactorNettyHttpResponse reactorNettyHttpResponse = responseReference.get();
                if (reactorNettyHttpResponse != null) {
                    reactorNettyHttpResponse.releaseOnNotSubscribedResponse(ReactorNettyResponseState.ERROR);
                }
                return throwable;
            })
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
                return reactorNettyOutbound.sendByteArray(restRequest.body());
            } else {
                return reactorNettyOutbound;
            }
        };
    }

    @Override
    public void shutdown() {
        if (this.connectionProvider != null) {
            this.connectionProvider.dispose();
        }
    }

    private static ConnectionObserver getConnectionObserver() {
        return (conn, state) -> {
            Instant time = Instant.now();

            if (state.equals(HttpClientState.CONNECTED)) {
                if (conn instanceof ConnectionObserver) {
                    ConnectionObserver observer = (ConnectionObserver) conn;
                    ReactorNettyRequestRecord requestRecord =
                        observer.currentContext().getOrDefault(REACTOR_NETTY_REQUEST_RECORD_KEY, null);
                    if (requestRecord == null) {
                        throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                    }
                    requestRecord.setTimeConnected(time);
                }
            }else if (state.equals(HttpClientState.ACQUIRED)) {
                if (conn instanceof ConnectionObserver) {
                    ConnectionObserver observer = (ConnectionObserver) conn;
                    ReactorNettyRequestRecord requestRecord =
                        observer.currentContext().getOrDefault(REACTOR_NETTY_REQUEST_RECORD_KEY, null);
                    if (requestRecord == null) {
                        throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                    }
                    requestRecord.setTimeAcquired(time);
                }
            } else if (state.equals(HttpClientState.CONFIGURED)) {
                if (conn instanceof HttpClientRequest) {
                    HttpClientRequest httpClientRequest = (HttpClientRequest) conn;
                    ReactorNettyRequestRecord requestRecord =
                        httpClientRequest.currentContextView().getOrDefault(REACTOR_NETTY_REQUEST_RECORD_KEY, null);
                    if (requestRecord == null) {
                        throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                    }
                    requestRecord.setTimeConfigured(time);
                }
            } else if (state.equals(HttpClientState.REQUEST_SENT)) {
                if (conn instanceof HttpClientRequest) {
                    HttpClientRequest httpClientRequest = (HttpClientRequest) conn;
                    ReactorNettyRequestRecord requestRecord =
                        httpClientRequest.currentContextView().getOrDefault(REACTOR_NETTY_REQUEST_RECORD_KEY, null);
                    if (requestRecord == null) {
                        throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                    }
                    requestRecord.setTimeSent(time);
                }
            } else if (state.equals(HttpClientState.RESPONSE_RECEIVED)) {
                if (conn instanceof HttpClientRequest) {
                    HttpClientRequest httpClientRequest = (HttpClientRequest) conn;
                    ReactorNettyRequestRecord requestRecord =
                        httpClientRequest.currentContextView().getOrDefault(REACTOR_NETTY_REQUEST_RECORD_KEY, null);
                    if (requestRecord == null) {
                        throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                    }
                    requestRecord.setTimeReceived(time);
                }
            }
        };
    }

    private static class ReactorNettyHttpResponse extends HttpResponse {

        private final AtomicReference<ReactorNettyResponseState> state = new AtomicReference<>(ReactorNettyResponseState.NOT_SUBSCRIBED);

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
        public Mono<ByteBuf> body() {
            return bodyIntern()
                .aggregate()
                .doOnSubscribe(this::updateSubscriptionState);
        }

        @Override
        public Mono<String> bodyAsString() {
            return bodyIntern().aggregate()
                .asString()
                .doOnSubscribe(this::updateSubscriptionState);
        }

        private ByteBufFlux bodyIntern() {
            return reactorNettyConnection.inbound().receive();
        }

        @Override
        Connection internConnection() {
            return reactorNettyConnection;
        }

        private void updateSubscriptionState(Subscription subscription) {
            if (this.state.compareAndSet(ReactorNettyResponseState.NOT_SUBSCRIBED, ReactorNettyResponseState.SUBSCRIBED)) {
                return;
            }
            // https://github.com/reactor/reactor-netty/issues/503
            // FluxReceive rejects multiple subscribers, but not after a cancel().
            // Subsequent subscribers after cancel() will not be rejected, but will hang instead.
            // So we need to reject ones in cancelled state.
            if (this.state.get() == ReactorNettyResponseState.CANCELLED) {
                throw new IllegalStateException(
                    "The client response body has been released already due to cancellation.");
            }
        }

        /**
         * Called by {@link ReactorNettyClient} when a cancellation or error is detected
         * but the content has not been subscribed to. If the subscription never
         * materializes then the content will remain not drained. Or it could still
         * materialize if the cancellation or error happened very early, or the response
         * reading was delayed for some reason.
         */
        private void releaseOnNotSubscribedResponse(ReactorNettyResponseState reactorNettyResponseState) {
            if (this.state.compareAndSet(ReactorNettyResponseState.NOT_SUBSCRIBED, reactorNettyResponseState)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Releasing body, not yet subscribed");
                }
                this.bodyIntern()
                    .doOnNext(byteBuf -> {})
                    .subscribe(byteBuf -> {}, ex -> {});
            }
        }
    }

    private enum ReactorNettyResponseState {
        // 0 - not subscribed, 1 - subscribed, 2 - cancelled via connector (before subscribe)
        // 3 - error occurred before subscribe
        NOT_SUBSCRIBED,
        SUBSCRIBED,
        CANCELLED,
        ERROR;
    }

    /**
     * DO NOT USE
     *
     * This API is only for testing purposes, don't use it anywhere else in the code.
     * This changes the logging level of Reactor Netty Http Client.
     */
    public void enableNetworkLogging() {
        Logger logger = LoggerFactory.getLogger(this.reactorNetworkLogCategory);
        if (logger.isTraceEnabled()) {
            this.httpClient = this.httpClient.wiretap(this.reactorNetworkLogCategory, LogLevel.TRACE);
        } else if (logger.isDebugEnabled()) {
            this.httpClient = this.httpClient.wiretap(this.reactorNetworkLogCategory, LogLevel.DEBUG);
        } else if (logger.isInfoEnabled()) {
            this.httpClient = this.httpClient.wiretap(this.reactorNetworkLogCategory, LogLevel.INFO);
        } else if (logger.isWarnEnabled()) {
            this.httpClient = this.httpClient.wiretap(this.reactorNetworkLogCategory, LogLevel.WARN);
        } else if (logger.isErrorEnabled()) {
            this.httpClient = this.httpClient.wiretap(this.reactorNetworkLogCategory, LogLevel.ERROR);
        }
    }
}
