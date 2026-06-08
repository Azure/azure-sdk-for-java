// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ResourceLeakDetector;
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
    private static ImplementationBridgeHelpers.Http2ConnectionConfigHelper.Http2ConnectionConfigAccessor http2CfgAccessor() {
        return ImplementationBridgeHelpers.Http2ConnectionConfigHelper.getHttp2ConnectionConfigAccessor();
    }

    private static final boolean leakDetectionDebuggingEnabled = ResourceLeakDetector.getLevel().ordinal() >=
        ResourceLeakDetector.Level.ADVANCED.ordinal();
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

        Http2ConnectionConfig http2Cfg = httpClientConfig.getHttp2ConnectionConfig();

        boolean isH2Enabled = http2CfgAccessor().isEffectivelyEnabled(http2Cfg);

        if (isH2Enabled) {
            this.httpClient = this.httpClient.doOnConnected(connection -> {
                // Manual HTTP/2 PING keepalive -- sends PING frames when the connection is idle
                // to prevent L7 middleboxes (NAT, firewalls, LBs) from reaping the connection.
                // For H2, doOnConnected fires on the parent TCP channel when the connection
                // is first established (State.CONFIGURED). Child stream channels emit
                // STREAM_CONFIGURED, which does not trigger this callback. The parent-
                // resolution and installIfAbsent guard below are defensive -- they correctly
                // handle both parent and child channels if the reactor-netty contract
                // ever changes.
                //
                // Gating is consolidated in Http2PingHandler.isPingHealthEffectivelyEnabled so
                // the transport install site and the user-agent feature flag cannot drift.
                // The handler also re-checks the kill-switch per tick so toggling it at
                // runtime immediately stops/resumes PINGing on already-installed connections.
                if (Http2PingHandler.isPingHealthEffectivelyEnabled(http2Cfg)) {
                    // Resolve to the parent (TCP) channel -- defensive in case reactor-netty
                    // ever invokes this callback for a child stream channel.
                    Channel ch = connection.channel();
                    Channel parent = ch.parent() != null ? ch.parent() : ch;
                    if (parent.pipeline().get(Http2MultiplexHandler.class) != null) {
                        Http2PingHandler.installIfAbsent(parent,
                            Configs.getHttp2PingIntervalInSeconds(),
                            Configs.getHttp2PingTimeoutInSeconds(),
                            Configs.getHttp2PingFailureThreshold());
                    }
                }
            });

            this.httpClient = this.httpClient
                .secure(sslContextSpec ->
                    sslContextSpec.sslContext(
                        configs.getSslContext(
                            httpClientConfig.isServerCertValidationDisabled(),
                            true
                        )))
                .protocol(HttpProtocol.H2, HttpProtocol.HTTP11)
                .http2Settings(settings -> settings
                    .initialWindowSize(1024 * 1024) // 1MB initial window size
                    .maxFrameSize(Configs.getHttp2MaxFrameSizeInBytes())   // 64KB default; overridable via COSMOS.HTTP2_MAX_FRAME_SIZE_IN_KB / COSMOS_HTTP2_MAX_FRAME_SIZE_IN_KB (clamped to [64KB, 16383KB])
                    .maxConcurrentStreams(http2CfgAccessor().getEffectiveMaxConcurrentStreams(http2Cfg))  // Increased from default 30
                )
                .doOnConnected((connection -> {
                    // The response header clean up pipeline is being added due to an error getting when calling gateway:
                    // java.lang.IllegalArgumentException: a header value contains prohibited character 0x20 at index 0 for 'x-ms-serviceversion', there is whitespace in the front of the value.
                    // validateHeaders(false) does not work for http2
                    ChannelPipeline channelPipeline = connection.channel().pipeline();
                    if (channelPipeline.get("reactor.left.httpCodec") != null
                        && channelPipeline.get("customHeaderCleaner") == null) {
                        try {
                            channelPipeline.addAfter(
                                "reactor.left.httpCodec",
                                "customHeaderCleaner",
                                new Http2ResponseHeaderCleanerHandler());
                        } catch (IllegalArgumentException ignored) {
                            // TOCTOU race: between the get()==null check above and addAfter(),
                            // a concurrent doOnConnected may have installed the handler.
                            // Duplicate handler name is the only possible cause.
                        }
                    }

                    // Install exception handler at the tail of the HTTP/2 parent (TCP)
                    // channel pipeline. This pipeline has no ChannelOperationsHandler
                    // (unlike H1.1), so TCP-level exceptions (RST, broken pipe) propagate
                    // to Netty's TailContext. This handler consumes them with
                    // connection-state-based log levels: DEBUG when idle, WARN when
                    // active streams exist.
                    if (channelPipeline.get(Http2ParentChannelExceptionHandler.HANDLER_NAME) == null) {
                        try {
                            channelPipeline.addLast(
                                Http2ParentChannelExceptionHandler.HANDLER_NAME,
                                Http2ParentChannelExceptionHandler.INSTANCE);
                        } catch (IllegalArgumentException ignored) {
                            // TOCTOU race: between the get()==null check above and addLast(),
                            // a concurrent doOnConnected may have installed the handler.
                            // Duplicate handler name is the only possible cause.
                        }
                    }

                }))
                // Install a @Sharable head-of-pipeline rewrap handler on each H2
                // child-stream pipeline. STREAM_CONFIGURED is the per-child-stream
                // lifecycle event reactor-netty fires once when a stream is opened, so
                // connection.channel() here is the child stream (its parent is the
                // parent TCP channel). doOnConnected cannot be used for this install:
                // it fires only on the parent TCP channel (State.CONFIGURED) and never
                // on a child stream, so a ch.parent() != null gate inside doOnConnected
                // would never be satisfied and the handler would never install.
                //
                // The rewrap MUST live on the child stream pipeline, not the parent.
                // When Http2PingHandler closes the parent (TCP) channel after
                // consecutive PING-ACK timeouts or PING-send failures, the H2 multiplex
                // codec propagates channelInactive to each child stream independently.
                // Child streams are separate Netty Channels, so that close does not
                // surface on the parent pipeline -- reactor-netty's per-stream
                // HttpClientOperations turns the child channelInactive into a bare
                // PrematureCloseException. This head-of-child-pipeline handler
                // intercepts channelInactive first and fires exceptionCaught with a
                // typed Http2PingTimeoutChannelClosedException before HttpClientOperations
                // observes the close, so the response Mono fails with the typed
                // exception. The rest of the stack maps that to
                // GATEWAY_HTTP2_PING_TIMEOUT_CHANNEL_CLOSED so ClientRetryPolicy can
                // suppress region mark-down.
                //
                // The install/skip decision (state gate, PING-health gate, parent and
                // idempotency guards) is extracted into
                // installHttp2PingCloseRewrapHandlerIfNeeded so it can be unit-tested
                // without a live HTTP/2 connection.
                .observe((connection, state) ->
                    installHttp2PingCloseRewrapHandlerIfNeeded(connection.channel(), state, http2Cfg));
        }
    }

    /**
     * Installs the {@link Http2PingCloseRewrapHandler} at the head of an HTTP/2
     * child-stream pipeline when, and only when, PING-health is effectively enabled.
     * Invoked from the {@code .observe(...)} hook in
     * {@link #configureChannelPipelineHandlers()} for every connection-lifecycle event;
     * see that hook for why the install must happen on the child stream at
     * {@link HttpClientState#STREAM_CONFIGURED} rather than via {@code doOnConnected} on
     * the parent TCP channel.
     *
     * <p>This is a no-op unless all of the following hold:
     * <ul>
     *   <li>{@code state} is {@link HttpClientState#STREAM_CONFIGURED} (a child stream was
     *       just opened);</li>
     *   <li>{@link Http2PingHandler#isPingHealthEffectivelyEnabled(Http2ConnectionConfig)}
     *       is {@code true} -- the rewrap handler only has a PING-timeout close signal to
     *       translate while the PING sender is active, so when PING-health is disabled (via
     *       {@code COSMOS.HTTP2_PING_HEALTH_ENABLED=false}, a non-positive PING interval, or
     *       HTTP/2 itself being disabled) the install is skipped and the kill-switch is a
     *       true revert-to-baseline with no extra per-stream pipeline hop;</li>
     *   <li>{@code channel.parent()} is non-null (defensive: STREAM_CONFIGURED already
     *       implies a child stream) and the handler is not already installed.</li>
     * </ul>
     * The PING-health predicate is evaluated only after the state check so the common
     * non-stream lifecycle events stay off this path. The gate is evaluated once per stream
     * at install time; toggling the kill-switch on at runtime only affects streams
     * configured afterwards, and since streams are single-use behavior converges within one
     * stream lifetime.
     *
     * @param channel  the channel reactor-netty associated with the lifecycle event (the
     *                 child stream when {@code state} is STREAM_CONFIGURED)
     * @param state    the connection-lifecycle state being observed
     * @param http2Cfg the per-client HTTP/2 configuration backing the PING-health gate
     */
    static void installHttp2PingCloseRewrapHandlerIfNeeded(
        Channel channel,
        ConnectionObserver.State state,
        Http2ConnectionConfig http2Cfg) {

        if (state != HttpClientState.STREAM_CONFIGURED
            || !Http2PingHandler.isPingHealthEffectivelyEnabled(http2Cfg)) {
            return;
        }

        ChannelPipeline childPipeline = channel.pipeline();
        // STREAM_CONFIGURED implies a child stream, so channel.parent() is the parent TCP
        // channel; the null-check is defensive.
        if (channel.parent() != null
            && childPipeline.get(Http2PingCloseRewrapHandler.HANDLER_NAME) == null) {
            try {
                childPipeline.addFirst(
                    Http2PingCloseRewrapHandler.HANDLER_NAME,
                    Http2PingCloseRewrapHandler.INSTANCE);
            } catch (IllegalArgumentException ignored) {
                // Benign duplicate install: another install path may have added the handler
                // between the get()==null check above and addFirst(). A duplicate handler
                // name is the only cause.
            }
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

        // Per-request CONNECT_TIMEOUT_MILLIS via reactor-netty's immutable HttpClient.
        // .option() returns a new config snapshot — does NOT mutate the shared httpClient.
        // Thin client requests (isThinClientRequest=true): connect timeout is configured via
        // HttpClientConfig.getThinClientConnectTimeoutMs() (default 5s) to fail fast.
        // Standard gateway requests: 45s (default).
        // Note: CONNECT_TIMEOUT_MILLIS controls TCP SYN→SYN-ACK timeout for NEW connections.
        // For H2, once a TCP connection exists, stream acquisition is near-instant (~sub-ms)
        // so pendingAcquireTimeout (pool-level 45s) is effectively never hit.
        int connectTimeoutMs = this.resolveConnectTimeoutMs(request);

        return this.httpClient
            .keepAlive(this.httpClientConfig.isConnectionKeepAlive())
            .port(request.port())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
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
    private static BiFunction<HttpClientRequest, NettyOutbound, Publisher<Void>> bodySendDelegate(
        final HttpRequest restRequest) {
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

    /**
     * Resolves the TCP connect timeout (CONNECT_TIMEOUT_MILLIS) based on the request type.
     *
     * Thin client requests (identified by {@link HttpRequest#isThinClientRequest()}) use the thin
     * client connection timeout configured via {@link Configs#getThinClientConnectionTimeoutInMs()}
     * (default 5s) to fail fast when the thin client proxy is unreachable.
     * Standard gateway requests use the configured connection acquire timeout (default 45s).
     *
     * The thin client timeout is eagerly cached in {@link HttpClientConfig} at construction time
     * to avoid per-request System.getProperty/getenv overhead.
     *
     * @param request the HTTP request
     * @return the connect timeout in milliseconds
     */
    private int resolveConnectTimeoutMs(HttpRequest request) {
        if (request.isThinClientRequest()) {
            return this.httpClientConfig.getThinClientConnectTimeoutMs();
        }
        return (int) this.httpClientConfig.getConnectionAcquireTimeout().toMillis();
    }

    private static ConnectionObserver getConnectionObserver() {
        return (conn, state) -> {
            Instant time = Instant.now();

            logger.trace("STATE {}, Connection {}, Time {}", state, conn, time);

            if (state.equals(HttpClientState.CONNECTED)) {
                ReactorNettyRequestRecord requestRecord = getRequestRecordFromConnection(conn);
                if (requestRecord == null) {
                    throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                }
                requestRecord.setTimeConnected(time);
                captureChannelIds(conn.channel(), requestRecord, true);
            } else if (state.equals(HttpClientState.ACQUIRED)) {
                ReactorNettyRequestRecord requestRecord = getRequestRecordFromConnection(conn);
                if (requestRecord == null) {
                    throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                }
                requestRecord.setTimeAcquired(time);
                captureChannelIds(conn.channel(), requestRecord, false);
            } else if (state.equals(HttpClientState.STREAM_CONFIGURED)) {
                // STREAM_CONFIGURED fires for HTTP/2 streams on every request (unlike CONNECTED
                // which only fires once when the TCP connection is established).
                // For H2, conn.channel() here is the stream channel; conn.channel().parent() is the TCP connection.
                if (conn instanceof HttpClientRequest) {
                    HttpClientRequest httpClientRequest = (HttpClientRequest) conn;
                    ReactorNettyRequestRecord requestRecord =
                        httpClientRequest.currentContextView().getOrDefault(REACTOR_NETTY_REQUEST_RECORD_KEY, null);
                    if (requestRecord == null) {
                        throw new IllegalStateException("ReactorNettyRequestRecord not found in context");
                    }
                    requestRecord.setTimeAcquired(time);
                    requestRecord.setHttp2(true);
                    captureChannelIds(conn.channel(), requestRecord, true);
                }
            } else if (state.equals(HttpClientState.CONFIGURED) || state.equals(HttpClientState.REQUEST_PREPARED)) {
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
                || state.equals(HttpClientState.RESPONSE_COMPLETED)
                || state.equals(HttpClientState.RESPONSE_INCOMPLETE)
                || state.equals(HttpClientState.RELEASED)) {

                // No-op
            } else {
                logger.debug("IGNORED STATE {}, Connection {}, Time {}", state, conn, time);
            }
        };
    }

    /**
     * Extracts the ReactorNettyRequestRecord from the connection's context.
     * Returns null if the connection is not a ConnectionObserver or if the record is not in context.
     */
    private static ReactorNettyRequestRecord getRequestRecordFromConnection(Connection conn) {
        if (conn instanceof ConnectionObserver) {
            return ((ConnectionObserver) conn)
                .currentContext().getOrDefault(REACTOR_NETTY_REQUEST_RECORD_KEY, null);
        }
        return null;
    }

    /**
     * Captures channelId and parentChannelId from the given channel onto the request record.
     * For HTTP/2, channel.parent() is the TCP connection; for HTTP/1.1, parent is null.
     *
     * @param channel the netty channel (stream channel for H2, connection channel for H1)
     * @param requestRecord the record to populate
     * @param overwrite if true, always writes channel IDs; if false, only writes when not already set
     */
    private static void captureChannelIds(Channel channel, ReactorNettyRequestRecord requestRecord, boolean overwrite) {
        ChannelId id = channel.id();
        String channelId = id.asShortText();
        Channel parent = channel.parent();
        String parentChannelId = parent != null
            ? parent.id().asShortText()
            : channelId;

        if (overwrite || requestRecord.getParentChannelId() == null) {
            requestRecord.setChannelId(channelId);
            requestRecord.setParentChannelId(parentChannelId);
        }
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
            return ByteBufFlux
                .fromInbound(
                    bodyIntern().doOnDiscard(
                        ByteBuf.class,
                        buf -> {
                            if (buf.refCnt() > 0) {
                                if (leakDetectionDebuggingEnabled) {
                                    buf.touch("ReactorNettyHttpResponse.body - onDiscard - refCnt: " + buf.refCnt());
                                    logger.debug("ReactorNettyHttpResponse.body - onDiscard - refCnt: {}", buf.refCnt());
                                }
                                ReferenceCountUtil.safeRelease(buf);
                            }
                        })
                )
                .aggregate()
                .doOnSubscribe(this::updateSubscriptionState);
        }

        @Override
        public Mono<String> bodyAsString() {
            return  ByteBufFlux
                .fromInbound(
                   bodyIntern().doOnDiscard(ByteBuf.class, io.netty.util.ReferenceCountUtil::safeRelease)
                )
                .aggregate()
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

                body()
                    .map(buf -> {
                        if (buf.refCnt() > 0) {
                            if (leakDetectionDebuggingEnabled) {
                                buf.touch("ReactorNettyHttpResponse.releaseOnNotSubscribedResponse - refCnt: " + buf.refCnt());
                                logger.debug("ReactorNettyHttpResponse.releaseOnNotSubscribedResponse - refCnt: {}", buf.refCnt());
                            }
                            ReferenceCountUtil.safeRelease(buf);
                        }

                        return buf;
                    })
                    .subscribe(v -> {}, ex -> {}, () -> {});
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
