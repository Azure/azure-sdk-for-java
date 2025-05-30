// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2SettingsFrame;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.util.AttributeKey;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.ChannelPipelineConfigurer;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyOutbound;
import reactor.netty.NettyPipeline;
import reactor.netty.channel.ChannelMetricsRecorder;
import reactor.netty.channel.ChannelOperations;
import reactor.netty.http.Http2SettingsSpec;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpClientResponse;
import reactor.netty.http.client.HttpClientState;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.ProxyProvider;
import reactor.util.context.Context;

import java.io.IOException;
import java.lang.invoke.WrongMethodTypeException;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_ISSUED;
import static io.netty.handler.codec.http.HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_REJECTED;
import static io.netty.handler.codec.http.HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_SUCCESSFUL;
import static reactor.netty.ReactorNetty.format;

/**
 * HttpClient that is implemented using reactor-netty.
 */
public class ReactorNettyClient implements HttpClient {

    private static final String REACTOR_NETTY_REQUEST_RECORD_KEY = "reactorNettyRequestRecordKey";

    private static final Logger logger = LoggerFactory.getLogger(ReactorNettyClient.class.getSimpleName());

    private HttpClientConfig httpClientConfig;
    private reactor.netty.http.client.HttpClient httpClient;
    private ConnectionProvider connectionProvider;
    private String reactorNetworkLogCategory;
    private Logger wireTapLogger;

    private ReactorNettyClient() {}

    /**
     * Creates ReactorNettyClient with un-pooled connection.
     */
    public static ReactorNettyClient create(HttpClientConfig httpClientConfig) {
        ReactorNettyClient reactorNettyClient = new ReactorNettyClient();
        reactorNettyClient.httpClientConfig = httpClientConfig;
        reactorNettyClient.reactorNetworkLogCategory = httpClientConfig.getReactorNetworkLogCategory();
        reactorNettyClient.wireTapLogger = LoggerFactory.getLogger(reactorNettyClient.reactorNetworkLogCategory);
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
        reactorNettyClient.wireTapLogger = LoggerFactory.getLogger(reactorNettyClient.reactorNetworkLogCategory);
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
        try {
            reactorNettyClient.httpClient.warmup().block();
        } catch (ClassCastException | WrongMethodTypeException throwable) {
            // Invocation failed.
            logger.debug("Invoking HttpClient.warmup failed.", throwable);
        } catch (Throwable throwable) {
            // Warmup failed.
            throw new RuntimeException(throwable);
        }
    }

    private String getChannelPipelineLog(String name, ChannelPipeline channelPipeline) {
        Map<String, ChannelHandler> handlerMap = channelPipeline.toMap();
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================================");
        sb.append(System.lineSeparator());
        sb.append(name + ": " + this);
        sb.append(System.lineSeparator());
        sb.append("====================================================================================");
        sb.append(System.lineSeparator());
        handlerMap.entrySet().forEach(
            entry -> {
                sb.append("Http2Handler " + entry.getKey() + ": " + entry.getValue());
                sb.append(System.lineSeparator());
            });
        sb.append("====================================================================================");
        sb.append(System.lineSeparator());
        return sb.toString();
    }
    private void logChannelPipelineAsTrace(String name, ChannelPipeline channelPipeline) {
        if (logger.isTraceEnabled()) {
            logger.trace(getChannelPipelineLog(name, channelPipeline));
        }
    }

    private void logChannelPipelineAsDebug(String name, ChannelPipeline channelPipeline) {
        if (logger.isTraceEnabled()) {
            logger.trace(getChannelPipelineLog(name, channelPipeline));
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

        if (this.wireTapLogger != null && this.wireTapLogger.isTraceEnabled()) {
            this.httpClient = this.httpClient.wiretap(reactorNetworkLogCategory, LogLevel.INFO);
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

        ImplementationBridgeHelpers.Http2ConnectionConfigHelper.Http2ConnectionConfigAccessor http2CfgAccessor =
            ImplementationBridgeHelpers.Http2ConnectionConfigHelper.getHttp2ConnectionConfigAccessor();
        Http2ConnectionConfig http2Cfg = httpClientConfig.getHttp2ConnectionConfig();
        if (http2CfgAccessor.isEffectivelyEnabled(http2Cfg)) {
            final AtomicReference<Http2SettingsSpec.Builder> settingsBuilderRef = new AtomicReference<>(null);
            this.httpClient = this.httpClient
                .secure(sslContextSpec ->
                    sslContextSpec.sslContext(
                        configs.getSslContext(
                            httpClientConfig.isServerCertValidationDisabled(),
                            true
                        )))
                .protocol(HttpProtocol.H2, HttpProtocol.HTTP11)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(EpollChannelOption.TCP_KEEPINTVL, 1)
                .option(EpollChannelOption.TCP_KEEPIDLE, 1)
                .http2Settings(settings -> settingsBuilderRef.set(
                    settings
                        .initialWindowSize(1024 * 1024) // 1MB initial window size
                        .maxFrameSize(64 * 1024)        // 64KB max frame size
                        .maxConcurrentStreams(http2CfgAccessor.getEffectiveMaxConcurrentStreams(http2Cfg))  // Increased from default 30
                    )
                )
                .doOnChannelInit(new ChannelPipelineConfigurer() {
                    @Override
                    public void onChannelInit(ConnectionObserver connectionObserver,
                                              Channel channel,
                                              SocketAddress remoteAddress) {

                        ChannelPipeline channelPipeline = channel.pipeline();
                        ChannelHandler codecCandidate = channelPipeline.get(NettyPipeline.H2OrHttp11Codec);
                        if (codecCandidate == null) {
                            logChannelPipelineAsDebug("NO CODEC FOUND - doOnChannelInit (Before)", channelPipeline);
                        } else {
                            logChannelPipelineAsTrace("doOnChannelInit (Before)", channelPipeline);
                            // Reactor netty http does not allow changing certain configs in the underlying
                            // netty http/2 stack
                            // we need to allow custom http/2 settings, inject the broken header cleanup and allow
                            // custom http/2 frame logger (using different log levels depending on the frame type)
                            channelPipeline.replace(
                                NettyPipeline.H2OrHttp11Codec,
                                CosmosNettyPipeline.H2_OR_HTTP11_CODEC,
                                new CosmosH2OrHttp11Codec(settingsBuilderRef.get().build(), connectionObserver, remoteAddress)
                            );
                            logChannelPipelineAsTrace("doOnChannelInit (After)", channelPipeline);
                        }
                    }
                });
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

            logger.trace("STATE {}, Connection {}, Time {}", state, conn, time);

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
            } else if (state.equals(HttpClientState.ACQUIRED)) {
                if (conn instanceof ConnectionObserver) {
                    ConnectionObserver observer = (ConnectionObserver) conn;
                    ReactorNettyRequestRecord requestRecord =
                        observer.currentContext().getOrDefault(REACTOR_NETTY_REQUEST_RECORD_KEY, null);
                    if (requestRecord == null) {
                        throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                    }
                    requestRecord.setTimeAcquired(time);
                }
            } else if (state.equals(HttpClientState.CONFIGURED) || state.equals(HttpClientState.STREAM_CONFIGURED)) {
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
            } else if (state.equals(HttpClientState.DISCONNECTING)
                || state.equals(HttpClientState.REQUEST_PREPARED)
                || state.equals(HttpClientState.RESPONSE_COMPLETED)
                || state.equals(HttpClientState.RESPONSE_INCOMPLETE)
                || state.equals(HttpClientState.RELEASED)) {

                // No-op
            } else {
                logger.debug("IGNORED STATE {}, Connection {}, Time {}", state, conn, time);
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

    private static class CosmosH2OrHttp11Codec extends ChannelInboundHandlerAdapter {
        final Http2Settings http2Settings;
        final ChannelMetricsRecorder metricsRecorder;
        final ConnectionObserver observer;
        final SocketAddress proxyAddress;
        final SocketAddress remoteAddress;
        final Function<String, String> uriTagValue;

        CosmosH2OrHttp11Codec(Http2SettingsSpec settings, ConnectionObserver observer, SocketAddress remoteAddress) {
            this.http2Settings = toNettySettings(settings);

            // TODO @fabianm - maybe worth evaluating whether we want to hook in a custom metrics recorder here
            this.metricsRecorder = null;

            this.observer = observer;

            // TODO @fabianm - do we need to support proxy for http/2 (thin client)
            this.proxyAddress = null;

            this.remoteAddress = remoteAddress;

            // TODO @fabianm - this would be some nomrmalized Uri for metrics with reasonable low cardinality
            // for example removing id-values - possibly also database/container
            this.uriTagValue = null;
        }

        private static Http2Settings toNettySettings(Http2SettingsSpec spec) {
            Http2Settings nettyHttp2Settings = Http2Settings.defaultSettings();
            if (spec.initialWindowSize() != null) {
                nettyHttp2Settings = nettyHttp2Settings.initialWindowSize(spec.initialWindowSize());
            }

            if (spec.maxConcurrentStreams() != null) {
                nettyHttp2Settings = nettyHttp2Settings.maxConcurrentStreams(spec.maxConcurrentStreams());
            }

            if (spec.maxFrameSize() != null) {
                nettyHttp2Settings = nettyHttp2Settings.maxFrameSize(spec.maxFrameSize());
            }

            if (spec.maxHeaderListSize() != null) {
                nettyHttp2Settings = nettyHttp2Settings.maxHeaderListSize(spec.maxHeaderListSize());
            }

            if (spec.headerTableSize() != null) {
                nettyHttp2Settings = nettyHttp2Settings.headerTableSize(spec.headerTableSize());
            }

            if (spec.pushEnabled() != null) {
                nettyHttp2Settings = nettyHttp2Settings.pushEnabled(spec.pushEnabled());
            }

            return nettyHttp2Settings;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ChannelHandler handler = ctx.pipeline().get(NettyPipeline.SslHandler);
            if (handler instanceof SslHandler) {
                SslHandler sslHandler = (SslHandler) handler;

                String protocol = sslHandler.applicationProtocol() != null ? sslHandler.applicationProtocol() : ApplicationProtocolNames.HTTP_1_1;
                if (logger.isDebugEnabled()) {
                    logger.debug(format(ctx.channel(), "Negotiated application-level protocol [" + protocol + "]"));
                }
                if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                    configureHttp2Pipeline(ctx.channel().pipeline(), http2Settings, observer);
                } else {
                    throw new IllegalStateException("unknown protocol: " + protocol);
                }

                ctx.fireChannelActive();

                ctx.channel().pipeline().remove(this);
            } else {
                throw new IllegalStateException("Cannot determine negotiated application-level protocol.");
            }
        }

        static void configureHttp2Pipeline(ChannelPipeline p,
                                           Http2Settings http2Settings,
                                           ConnectionObserver observer) {
            Http2FrameCodecBuilder http2FrameCodecBuilder =
                Http2FrameCodecBuilder.forClient()
                                      .validateHeaders(false)
                                      .initialSettings(http2Settings);

            if (p.get(NettyPipeline.LoggingHandler) != null) {
                http2FrameCodecBuilder.frameLogger(new Http2FrameLogger(LogLevel.DEBUG,
                    "reactor.netty.http.client.h2"));
            }

            p.addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.H2Flush, new FlushConsolidationHandler(1024, true))
             .addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.HttpCodec, http2FrameCodecBuilder.build())
             .addBefore(NettyPipeline.ReactiveBridge, CosmosNettyPipeline.HEADER_CLEANER, new Http2ResponseHeaderCleanerHandler())
             .addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.H2MultiplexHandler, new Http2MultiplexHandler(H2InboundStreamHandler.INSTANCE))
             .addBefore(NettyPipeline.ReactiveBridge, NettyPipeline.HttpTrafficHandler, new HttpTrafficHandler(observer));
        }
    }

    /**
     * Handle inbound streams (server pushes).
     * This feature is not supported and disabled.
     */
    static final class H2InboundStreamHandler extends ChannelHandlerAdapter {
        static final ChannelHandler INSTANCE = new H2InboundStreamHandler();

        @Override
        public boolean isSharable() {
            return true;
        }
    }

    static final class HttpTrafficHandler extends ChannelInboundHandlerAdapter {
        static final AttributeKey<Long> ENABLE_CONNECT_PROTOCOL = AttributeKey.valueOf("$ENABLE_CONNECT_PROTOCOL");
        final ConnectionObserver listener;

        HttpTrafficHandler(ConnectionObserver listener) {
            this.listener = listener;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            if (channel.isActive()) {
                if (ctx.pipeline().get(NettyPipeline.H2MultiplexHandler) == null) {
                    // Proceed with HTTP/1.x as per configuration
                    ctx.fireChannelActive();
                }
                else if (ctx.pipeline().get(NettyPipeline.SslHandler) == null) {
                    // Proceed with H2C as per configuration
                    sendNewState(Connection.from(channel), ConnectionObserver.State.CONNECTED);
                    ctx.flush();
                    ctx.read();
                }
                else {
                    // Proceed with H2 as per configuration
                    sendNewState(Connection.from(channel), ConnectionObserver.State.CONNECTED);
                }
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof Http2SettingsFrame) {
                ctx.channel().attr(ENABLE_CONNECT_PROTOCOL).set(((Http2SettingsFrame) msg).settings().get(SETTINGS_ENABLE_CONNECT_PROTOCOL));
                sendNewState(Connection.from(ctx.channel()), ConnectionObserver.State.CONFIGURED);
                ctx.pipeline().remove(NettyPipeline.ReactiveBridge);
                ctx.pipeline().remove(this);
                return;
            }

            ctx.fireChannelRead(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            ctx.fireExceptionCaught(new PrematureCloseException("Connection prematurely closed BEFORE response"));
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            Channel channel = ctx.channel();
            if (evt == UPGRADE_ISSUED) {
                if (logger.isDebugEnabled()) {
                    logger.debug(format(channel, "An upgrade request was sent to the server."));
                }
            }
            else if (evt == UPGRADE_SUCCESSFUL) {
                if (logger.isDebugEnabled()) {
                    logger.debug(format(channel, "The upgrade to H2C protocol was successful."));
                }
                sendNewState(Connection.from(channel), HttpClientState.UPGRADE_SUCCESSFUL);
                ctx.pipeline().remove(this);
            }
            else if (evt == UPGRADE_REJECTED) {
                if (logger.isDebugEnabled()) {
                    logger.debug(format(channel, "The upgrade to H2C protocol was rejected, continue using HTTP/1.x protocol."));
                }
                sendNewState(Connection.from(channel), HttpClientState.UPGRADE_REJECTED);
                ctx.pipeline().remove(this);
            }
            ctx.fireUserEventTriggered(evt);
        }

        void sendNewState(Connection connection, ConnectionObserver.State state) {
            ChannelOperations<?, ?> ops = connection.as(ChannelOperations.class);
            if (ops != null) {
                listener.onStateChange(ops, state);
            }
            else {
                listener.onStateChange(connection, state);
            }
        }

        // https://datatracker.ietf.org/doc/html/rfc8441#section-9.1
        static final char SETTINGS_ENABLE_CONNECT_PROTOCOL = 8;
    }

    static final class PrematureCloseException extends IOException {

        public static final PrematureCloseException TEST_EXCEPTION =
            new PrematureCloseException("Simulated prematurely closed connection");

        PrematureCloseException(String message) {
            super(message);
        }

        PrematureCloseException(Throwable throwable) {
            super(throwable);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            // omit stacktrace for this exception
            return this;
        }

        private static final long serialVersionUID = -3569621032752341973L;
    }

    static final class CosmosNettyPipeline {
        public static final String H2_OR_HTTP11_CODEC = "cosmos.left.h2OrHttp11Codec";
        public static final String HEADER_CLEANER = "cosmos.left.headerCleaner";
    }
}
