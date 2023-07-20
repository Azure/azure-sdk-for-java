// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryMetrics;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.IAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.TransportException;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.faultinjection.RntbdServerErrorInjector;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.micrometer.core.instrument.Tag;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders;
import static com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient.Options;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@JsonSerialize(using = RntbdServiceEndpoint.JsonSerializer.class)
public final class RntbdServiceEndpoint implements RntbdEndpoint {

    // region Fields

    private static final String TAG_NAME = RntbdServiceEndpoint.class.getSimpleName();
    private static final long QUIET_PERIOD = 2_000_000_000L; // 2 seconds

    private static final AtomicLong instanceCount = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(RntbdServiceEndpoint.class);
    private static final AdaptiveRecvByteBufAllocator receiveBufferAllocator = new AdaptiveRecvByteBufAllocator();

    private final RntbdClientChannelPool channelPool;
    private final AtomicBoolean closed;
    private final AtomicInteger concurrentRequests;
    private final long id;
    private final AtomicLong lastRequestNanoTime;
    private final AtomicLong lastSuccessfulRequestNanoTime;

    private final Instant createdTime;
    private final RntbdMetricsCompletionRecorder metricsComplectionRecorder;
    private final Provider provider;
    private final URI serverKey;
    private final SocketAddress remoteAddress;
    private final RntbdRequestTimer requestTimer;
    private final Tag tag;
    private final Tag clientMetricTag;
    private final int maxConcurrentRequests;

    private final RntbdConnectionStateListener connectionStateListener;
    private final URI serviceEndpoint;
    private final RntbdDurableEndpointMetrics durableMetrics;
    private String lastFaultInjectionRuleId;
    private Instant lastFaultInjectionTimestamp;
    private int minChannelsRequired;
    private final Uri addressUri;

    // endregion

    // region Constructors

    private RntbdServiceEndpoint(
        final Provider provider,
        final Config config,
        final EventLoopGroup group,
        final RntbdRequestTimer timer,
        final Uri addressUri,
        final ClientTelemetry clientTelemetry,
        final RntbdServerErrorInjector faultInjectionInterceptors,
        final URI serviceEndpoint,
        final RntbdDurableEndpointMetrics durableMetrics,
        final ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor,
        final int minChannelsRequired,
        final AddressSelector addressSelector) {

        this.durableMetrics = durableMetrics;
        this.addressUri = addressUri;
        this.serverKey = RntbdUtils.getServerKey(addressUri.getURI());
        this.serviceEndpoint = serviceEndpoint;
        this.minChannelsRequired = minChannelsRequired;

        final Bootstrap bootstrap = this.getBootStrap(group, config);

        this.createdTime = Instant.now();
        this.remoteAddress = bootstrap.config().remoteAddress();
        this.concurrentRequests = new AtomicInteger();
        // if no request has been sent over this endpoint we want to make sure we don't trigger a connection close
        // due to elapsedTimeInNanos being negative.
        // if no request has been sent initially over this endpoint, the below calculation can result in a very big difference
        // long elapsedTimeInNanos = System.nanoTime() - endpoint.lastRequestNanoTime()
        // which can cause endpoint to close unnecessary.
        this.lastRequestNanoTime = new AtomicLong(System.nanoTime());
        this.lastSuccessfulRequestNanoTime = new AtomicLong(System.nanoTime());
        this.closed = new AtomicBoolean();
        this.requestTimer = timer;

        this.tag = Tag.of(TAG_NAME, RntbdMetrics.escape(this.remoteAddress.toString()));
        this.clientMetricTag = Tag.of(
            TagName.ServiceEndpoint.toString(),
            String.format("%s_%d", this.serverKey.getHost(), this.serverKey.getPort()));
        this.id = instanceCount.incrementAndGet();
        this.provider = provider;

        this.maxConcurrentRequests = config.maxConcurrentRequestsPerEndpoint();

        this.connectionStateListener = this.provider.addressResolver != null && config.isConnectionEndpointRediscoveryEnabled()
            ? new RntbdConnectionStateListener(this, proactiveOpenConnectionsProcessor, addressSelector) : null;

        this.channelPool =
            new RntbdClientChannelPool(
                this,
                bootstrap,
                config,
                clientTelemetry,
                this.connectionStateListener,
                faultInjectionInterceptors,
                durableMetrics);

        if (clientTelemetry != null &&
            clientTelemetry.isClientMetricsEnabled() &&
            (
                provider.transportClient.getMetricCategories().contains(MetricCategory.DirectEndpoints)
                    || provider.transportClient.getMetricCategories().contains(MetricCategory.DirectChannels)
                    || provider.transportClient.getMetricCategories().contains(MetricCategory.DirectRequests)
            )
        ) {

            RntbdMetricsCompletionRecorder rntbdMetricsV2 =
                ClientTelemetryMetrics.createRntbdMetrics(provider.transportClient, this);
            if (RntbdMetrics.isEmpty()) {
                this.metricsComplectionRecorder = rntbdMetricsV2;
            } else {
                List<RntbdMetricsCompletionRecorder> metricCompletionRecorders = new ArrayList<>();
                metricCompletionRecorders.add(RntbdMetrics.create(provider.transportClient, this));
                metricCompletionRecorders.add(rntbdMetricsV2);
                this.metricsComplectionRecorder = new RntbdMetricsDelegatingCompletionRecorder(
                    metricCompletionRecorders
                );
            }

        } else {
            if (RntbdMetrics.isEmpty()) {
                this.metricsComplectionRecorder = RntbdMetricsCompletionRecorder.NoOpSingletonInstance;
            } else {
                this.metricsComplectionRecorder = RntbdMetrics.create(provider.transportClient, this);
            }
        }
    }

    private Bootstrap getBootStrap(EventLoopGroup eventLoopGroup, Config config) {
        checkNotNull(eventLoopGroup, "expected non-null eventLoopGroup");
        checkNotNull(config, "expected non-null config");

        RntbdLoop rntbdLoop = RntbdLoopNativeDetector.getRntbdLoop(config.preferTcpNative());
        Bootstrap bootstrap = new Bootstrap()
            .group(eventLoopGroup)
            .channel(rntbdLoop.getChannelClass())
            .option(ChannelOption.ALLOCATOR, config.allocator())
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.connectTimeoutInMillis())
            .option(ChannelOption.RCVBUF_ALLOCATOR, receiveBufferAllocator)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .remoteAddress(this.serverKey.getHost(), this.serverKey.getPort());

        if (rntbdLoop instanceof RntbdLoopEpoll) {
            // Override the default Linux os config for tcp keep-alive, so a broken connection can be detected faster.
            // see man 7 tcp for more details.
            bootstrap
                .option(EpollChannelOption.TCP_KEEPINTVL, config.tcpKeepIntvl()) // default value 75s
                .option(EpollChannelOption.TCP_KEEPIDLE, config.tcpKeepIdle()); // default value 2hrs
        }

        return bootstrap;
    }

    // endregion

    // region Accessors

    /**
     * @return approximate number of acquired channels.
     */
    @Override
    public int channelsAcquiredMetric() {
        return this.channelPool.channelsAcquiredMetrics();
    }

    @Override
    public RntbdDurableEndpointMetrics durableEndpointMetrics() {
        return this.durableMetrics;
    }

    /**
     * @return approximate number of available channels.
     */
    @Override
    public int channelsAvailableMetric() {
        return this.channelPool.channelsAvailableMetrics();
    }

    @Override
    public int concurrentRequests() {
        return this.concurrentRequests.get();
    }

    @Override
    public int gettingEstablishedConnectionsMetrics() {
        return this.channelPool.attemptingToConnectMetrics();
    }

    @Override
    public long id() {
        return this.id;
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

    @Override
    public int maxChannels() {
        return this.channelPool.channels(true);
    }

    public long lastRequestNanoTime() {
        return this.lastRequestNanoTime.get();
    }

    @Override
    public long lastSuccessfulRequestNanoTime() {
        return this.lastSuccessfulRequestNanoTime.get();
    }

    @Override
    public int channelsMetrics() {
        return this.channelPool.channels(true);
    }

    @Override
    public int executorTaskQueueMetrics() {
        return this.channelPool.executorTaskQueueMetrics();
    }

    public Instant getCreatedTime() {
        return this.createdTime;
    }

    @Override
    public SocketAddress remoteAddress() {
        return this.remoteAddress;
    }

    @Override
    public URI serverKey() { return this.serverKey; }

    @Override
    public int requestQueueLength() {
        return this.channelPool.requestQueueLength();
    }

    @Override
    public Tag tag() {
        return this.tag;
    }

    @Override
    public Tag clientMetricTag() { return this.clientMetricTag;}

    @Override
    public long usedDirectMemory() {
        return this.channelPool.usedDirectMemory();
    }

    @Override
    public long usedHeapMemory() {
        return this.channelPool.usedHeapMemory();
    }

    @Override
    public URI serviceEndpoint() {
        return this.serviceEndpoint;
    }

    @Override
    public void injectConnectionErrors(
        String faultInjectionRuleId,
        double threshold,
        Class<?> eventType) {

        this.lastFaultInjectionRuleId = faultInjectionRuleId;
        this.lastFaultInjectionTimestamp = Instant.now();

        this.channelPool.injectConnectionErrors(faultInjectionRuleId, threshold, eventType);
    }

    @Override
    public int getMinChannelsRequired() {
        return this.minChannelsRequired;
    }

    @Override
    public void setMinChannelsRequired(int minChannelsRequired) {
        this.minChannelsRequired = minChannelsRequired;
    }

    @Override
    public Uri getAddressUri() {
        return this.addressUri;
    }

    // endregion

    // region Methods

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            this.provider.evict(this);
            this.durableMetrics.clearEndpoint(this);
            this.channelPool.close();
        }
    }

    public RntbdRequestRecord request(final RntbdRequestArgs args) {

        this.throwIfClosed();

        int concurrentRequestSnapshot = this.concurrentRequests.incrementAndGet();

        if (this.connectionStateListener != null) {
            this.connectionStateListener.onBeforeSendRequest(args.physicalAddressUri());
        }

        RntbdEndpointStatistics stat = endpointMetricsSnapshot(concurrentRequestSnapshot);

        if (concurrentRequestSnapshot > this.maxConcurrentRequests) {
            try {
                FailFastRntbdRequestRecord requestRecord = FailFastRntbdRequestRecord.createAndFailFast(
                    args,
                    concurrentRequestSnapshot,
                    metricsComplectionRecorder,
                    remoteAddress);
                requestRecord.serviceEndpointStatistics(stat);
                return requestRecord;
            }
            finally {
                concurrentRequests.decrementAndGet();
            }
        }

        this.lastRequestNanoTime.set(args.nanoTimeCreated());

        final RntbdRequestRecord record = this.write(args);
        record.serviceEndpointStatistics(stat);

        record.whenComplete((response, error) -> {
            this.concurrentRequests.decrementAndGet();
            this.metricsComplectionRecorder.markComplete(record);
            onResponse(error);
        });

        return record;
    }

    @Override
    public OpenConnectionRntbdRequestRecord openConnection(final RntbdRequestArgs args) {
        checkNotNull(args, "Argument 'args' should not be null");

        this.throwIfClosed();

        if (this.connectionStateListener != null) {
            this.connectionStateListener.onBeforeSendRequest(args.physicalAddressUri());
        }

        OpenConnectionRntbdRequestRecord requestRecord = new OpenConnectionRntbdRequestRecord(args);
        final Future<Channel> openChannelFuture = this.channelPool.acquire(requestRecord);

        if (openChannelFuture.isDone()) {
            return processWhenConnectionOpened(requestRecord, openChannelFuture);
        } else {
            openChannelFuture.addListener(ignored -> processWhenConnectionOpened(requestRecord, openChannelFuture));
        }

        return requestRecord;
    }

    private OpenConnectionRntbdRequestRecord processWhenConnectionOpened(
            final OpenConnectionRntbdRequestRecord requestRecord,
            final Future<Channel> openChannelFuture) {

        OpenConnectionResponse openConnectionResponse;

        if (openChannelFuture.isSuccess()) {
            final Channel channel = openChannelFuture.getNow();
            assert channel != null : "impossible";

            // This is a very important step
            // Releasing the channel back to the pool so other requests can use it
            this.releaseToPool(channel);

            requestRecord.args().physicalAddressUri().setConnected();

            openConnectionResponse =
                new OpenConnectionResponse(
                        requestRecord.args().physicalAddressUri(),
                        true,
                        null,
                        this.channelsMetrics());
        } else {
            openConnectionResponse =
                new OpenConnectionResponse(
                    requestRecord.args().physicalAddressUri(),
                    false,
                    openChannelFuture.cause(),
                    this.channelsMetrics());
        }

        requestRecord.complete(openConnectionResponse);
        return requestRecord;
    }

    private void onResponse(Throwable exception) {
        if (exception == null) {
            this.lastSuccessfulRequestNanoTime.set(System.nanoTime());
            return;
        }

        // exception != null
        if (exception instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) exception;
            switch (cosmosException.getStatusCode()) {
                // non 200 status codes representing business logic success
                case HttpConstants.StatusCodes.CONFLICT:
                case HttpConstants.StatusCodes.NOTFOUND:
                    this.lastSuccessfulRequestNanoTime.set(System.nanoTime());
                    return;
                default:
                    return;
            }
        }
    }

    private RntbdEndpointStatistics endpointMetricsSnapshot(int concurrentRequestSnapshot) {
        RntbdEndpointStatistics stats = new RntbdEndpointStatistics()
            .availableChannels(this.channelsAvailableMetric())
            .acquiredChannels(this.channelsAcquiredMetric())
            .executorTaskQueueSize(this.executorTaskQueueMetrics())
            .lastSuccessfulRequestNanoTime(this.lastSuccessfulRequestNanoTime())
            .createdTime(this.createdTime)
            .lastRequestNanoTime(this.lastRequestNanoTime())
            .closed(this.closed.get())
            .inflightRequests(concurrentRequestSnapshot)
            .lastFaultInjectionId(this.lastFaultInjectionRuleId)
            .lastFaultInjectionTimestamp(this.lastFaultInjectionTimestamp);

        if (this.connectionStateListener != null) {
            stats.connectionStateListenerMetrics(this.connectionStateListener.getMetrics());
        }

        return stats;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    // endregion

    // region Privates

    private void ensureSuccessWhenReleasedToPool(Channel channel, Future<Void> released) {
        if (released.isSuccess()) {
            logger.debug("\n  [{}]\n  {}\n  release succeeded", this, channel);
        } else {
            logger.debug("\n  [{}]\n  {}\n  release failed due to {}", this, channel, released.cause());
        }
    }

    private void releaseToPool(final Channel channel) {

        logger.debug("\n  [{}]\n  {}\n  RELEASE", this, channel);
        final Future<Void> released = this.channelPool.release(channel);

        if (logger.isDebugEnabled()) {
            if (released.isDone()) {
                ensureSuccessWhenReleasedToPool(channel, released);
            } else {
                released.addListener(ignored -> ensureSuccessWhenReleasedToPool(channel, released));
            }
        }
    }

    private void throwIfClosed() {
        if (this.closed.get()) {
            throw new TransportException(lenientFormat("%s is closed", this), new IllegalStateException());
        }
    }

    private RntbdRequestRecord write(final RntbdRequestArgs requestArgs) {

        final RntbdRequestRecord requestRecord = new AsyncRntbdRequestRecord(requestArgs, this.requestTimer);
        requestRecord.stage(RntbdRequestRecord.Stage.CHANNEL_ACQUISITION_STARTED);
        final Future<Channel> connectedChannel = this.channelPool.acquire(requestRecord);

        logger.debug("\n  [{}]\n  {}\n  WRITE WHEN CONNECTED {}", this, requestArgs, connectedChannel);

        if (connectedChannel.isDone()) {
            return writeWhenConnected(requestRecord, connectedChannel);
        } else {
            connectedChannel.addListener(ignored -> writeWhenConnected(requestRecord, connectedChannel));
        }

        return requestRecord;
    }

    private RntbdRequestRecord writeWhenConnected(
        final RntbdRequestRecord requestRecord, final Future<? super Channel> connected) {

        if (connected.isSuccess()) {
            final Channel channel = (Channel) connected.getNow();
            assert channel != null : "impossible";
            this.releaseToPool(channel);

            // record channel statistics
            requestRecord.channelStatistics(channel, requestRecord.getChannelAcquisitionTimeline());

            channel.write(requestRecord.stage(RntbdRequestRecord.Stage.PIPELINED));

            // mark address connected
            requestRecord.args().physicalAddressUri().setConnected();
            return requestRecord;
        }

        final RntbdRequestArgs requestArgs = requestRecord.args();
        final UUID activityId = requestArgs.activityId();
        final Throwable cause = connected.cause();

        if (connected.isCancelled()) {
            if(logger.isDebugEnabled()) {
                logger.debug("\n  [{}]\n  {}\n  write cancelled: {}", this, requestArgs, cause);
            }
            requestRecord.cancel(true);

        } else {

            if (logger.isDebugEnabled()) {
                logger.debug("\n  [{}]\n  {}\n  write failed due to {} ", this, requestArgs, cause);
            }

            final String reason = cause.toString();

            final GoneException goneException = new GoneException(
                lenientFormat("failed to establish connection to %s due to %s", this.remoteAddress, reason),
                cause instanceof Exception ? (Exception) cause : new IOException(reason, cause),
                ImmutableMap.of(HttpHeaders.ACTIVITY_ID, activityId.toString()),
                requestArgs.replicaPath(),
                HttpConstants.SubStatusCodes.TRANSPORT_GENERATED_410
            );

            RxDocumentServiceRequest serviceRequest = requestArgs.serviceRequest();
            BridgeInternal.setRequestHeaders(goneException, serviceRequest.getHeaders());

            if (serviceRequest.requestContext != null) {
                serviceRequest.requestContext.forceRefreshAddressCache = true;
            }

            connectionStateListener.attemptBackgroundAddressRefresh(serviceRequest, goneException);
            requestRecord.completeExceptionally(goneException);
        }

        return requestRecord;
    }

    // endregion

    // region Types

    static final class JsonSerializer extends StdSerializer<RntbdServiceEndpoint> {

        private static final long serialVersionUID = -5764954918168771152L;

        public JsonSerializer() {
            super(RntbdServiceEndpoint.class);
        }

        @Override
        public void serialize(RntbdServiceEndpoint value, JsonGenerator generator, SerializerProvider provider)
            throws IOException {

            final RntbdTransportClient transportClient = value.provider.transportClient;

            generator.writeStartObject();
            generator.writeNumberField("id", value.id);
            generator.writeBooleanField("closed", value.isClosed());
            generator.writeNumberField("concurrentRequests", value.concurrentRequests());
            generator.writeStringField("remoteAddress", value.remoteAddress.toString());
            generator.writeObjectField("channelPool", value.channelPool);
            generator.writeObjectFieldStart("transportClient");
            generator.writeNumberField("id", transportClient.id());
            generator.writeBooleanField("closed", transportClient.isClosed());
            generator.writeNumberField("endpointCount", transportClient.endpointCount());
            generator.writeNumberField("endpointEvictionCount", transportClient.endpointEvictionCount());
            generator.writeEndObject();
            generator.writeEndObject();
        }
    }

    public static final class Provider implements RntbdEndpoint.Provider {

        private static final Logger logger = LoggerFactory.getLogger(Provider.class);
        private final AtomicBoolean closed;
        private final Config config;
        private final ConcurrentHashMap<String, RntbdEndpoint> endpoints;
        private final ConcurrentHashMap<String, RntbdDurableEndpointMetrics> durableMetrics;
        private final EventLoopGroup eventLoopGroup;
        private final AtomicInteger evictions;
        private final RntbdEndpointMonitoringProvider monitoring;
        private final RntbdRequestTimer requestTimer;
        private final RntbdTransportClient transportClient;
        private final IAddressResolver addressResolver;
        private final ClientTelemetry clientTelemetry;
        private final RntbdServerErrorInjector serverErrorInjector;

        public Provider(
            final RntbdTransportClient transportClient,
            final Options options,
            final SslContext sslContext,
            final IAddressResolver addressResolver,
            final ClientTelemetry clientTelemetry,
            final RntbdServerErrorInjector serverErrorInjector) {

            checkNotNull(transportClient, "expected non-null provider");
            checkNotNull(options, "expected non-null options");
            checkNotNull(sslContext, "expected non-null sslContext");

            final LogLevel wireLogLevel;

            if (logger.isDebugEnabled()) {
                wireLogLevel = LogLevel.TRACE;
            } else {
                wireLogLevel = null;
            }

            this.addressResolver = addressResolver;
            this.transportClient = transportClient;
            this.config = new Config(options, sslContext, wireLogLevel);

            this.requestTimer = new RntbdRequestTimer(
                config.tcpNetworkRequestTimeoutInNanos(),
                config.requestTimerResolutionInNanos());

            this.eventLoopGroup = this.getEventLoopGroup(options);
            this.endpoints = new ConcurrentHashMap<>();
            this.durableMetrics = new ConcurrentHashMap<>();
            this.evictions = new AtomicInteger();
            this.closed = new AtomicBoolean();
            this.clientTelemetry = clientTelemetry;
            this.serverErrorInjector = serverErrorInjector;
            this.monitoring = new RntbdEndpointMonitoringProvider(this);
            this.monitoring.init();
        }

        private EventLoopGroup getEventLoopGroup(Options options) {
            checkNotNull(options, "expected non-null options");

            RntbdLoop rntbdEventLoop = RntbdLoopNativeDetector.getRntbdLoop(options.preferTcpNative());
            DefaultThreadFactory threadFactory =
                new DefaultThreadFactory(
                    "cosmos-rntbd-" + rntbdEventLoop.getName(),
                    true,
                    options.ioThreadPriority());

            return rntbdEventLoop.newEventLoopGroup(options.threadCount(), threadFactory);
        }

        @Override
        public void close() {

            if (this.closed.compareAndSet(false, true)) {
                this.monitoring.close();

                for (final RntbdEndpoint endpoint : this.endpoints.values()) {
                    endpoint.close();
                }

                this.eventLoopGroup.shutdownGracefully(QUIET_PERIOD, this.config.shutdownTimeoutInNanos(), NANOSECONDS)
                    .addListener(future -> {

                        this.requestTimer.close();

                        if (future.isSuccess()) {
                            logger.debug("\n  [{}]\n  closed endpoints", this);
                            return;
                        }

                        logger.error("\n  [{}]\n  failed to close endpoints due to ", this, future.cause());
                    });

                return;
            }

            logger.debug("\n  [{}]\n  already closed", this);
        }

        @Override
        public Config config() {
            return this.config;
        }

        @Override
        public int count() {
            return this.endpoints.size();
        }

        @Override
        public int evictions() {
            return this.evictions.get();
        }

        @Override
        public RntbdEndpoint createIfAbsent(
            final URI serviceEndpoint,
            final Uri addressUri,
            ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor,
            int minRequiredChannelsForEndpoint,
            AddressSelector addressSelector) {
            return endpoints.computeIfAbsent(
                addressUri.getURI().getAuthority(),
                authority -> {
                    RntbdDurableEndpointMetrics durableEndpointMetrics = durableMetrics.computeIfAbsent(
                        authority,
                        ignoreMe -> new RntbdDurableEndpointMetrics()
                    );

                    RntbdServiceEndpoint endpoint = new RntbdServiceEndpoint(
                        this,
                        this.config,
                        this.eventLoopGroup,
                        this.requestTimer,
                        addressUri,
                        this.clientTelemetry,
                        this.serverErrorInjector,
                        serviceEndpoint,
                        durableEndpointMetrics,
                        proactiveOpenConnectionsProcessor,
                        minRequiredChannelsForEndpoint,
                        addressSelector);

                    durableEndpointMetrics.setEndpoint(endpoint);

                    return endpoint;
                });
        }

        @Override
        public RntbdEndpoint get(URI physicalAddress) {
            return endpoints.get(physicalAddress.getAuthority());
        }

        @Override
        public IAddressResolver getAddressResolver() {
            return this.addressResolver;
        }

        @Override
        public Stream<RntbdEndpoint> list() {
            return this.endpoints.values().stream();
        }

        @Override
        public boolean isClosed() {
            return this.closed.get();
        }

        private void evict(final RntbdEndpoint endpoint) {
            if (this.endpoints.remove(endpoint.serverKey().getAuthority()) != null) {
                this.evictions.incrementAndGet();
            }
        }
    }

    public static class RntbdEndpointMonitoringProvider implements AutoCloseable {
        private final Logger logger = LoggerFactory.getLogger(RntbdEndpointMonitoringProvider.class);
        // this is only for debugging monitoring of the health of RNTBD
        // TODO: once we are certain no task gets stuck in the rntbd queue remove this
        private static final EventExecutor monitoringRntbdChannelPool = new DefaultEventExecutor(new RntbdThreadFactory(
            "monitoring-rntbd-endpoints",
            true,
            Thread.MIN_PRIORITY));
        private static final Duration MONITORING_PERIOD = Duration.ofSeconds(60);
        private final Provider provider;
        private final static int MAX_TASK_LIMIT = 5_000;

        private ScheduledFuture<?> future;

        RntbdEndpointMonitoringProvider(Provider provider) {
            this.provider = provider;
        }

        synchronized void init() {
            logger.info("Starting RntbdClientChannelPoolMonitoringProvider ...");
            this.future = RntbdEndpointMonitoringProvider
                .monitoringRntbdChannelPool
                .scheduleAtFixedRate(this::logAllPools, 0, MONITORING_PERIOD.toMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public synchronized void close() {
            logger.info("Shutting down RntbdClientChannelPoolMonitoringProvider ...");
            this.future.cancel(false);
            this.future = null;
        }

        synchronized void logAllPools() {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Total number of RntbdClientChannelPool [{}].", provider.endpoints.size());
                }

                for (RntbdEndpoint endpoint : provider.endpoints.values()) {
                    logEndpoint(endpoint);
                }
            } catch (Exception e) {
                logger.error("monitoring unexpected failure", e);
            }
        }

        private void logEndpoint(RntbdEndpoint endpoint) {
            if (this.logger.isWarnEnabled() &&
                (endpoint.executorTaskQueueMetrics() > MAX_TASK_LIMIT ||
                endpoint.requestQueueLength() > MAX_TASK_LIMIT ||
                endpoint.gettingEstablishedConnectionsMetrics() > 0 ||
                endpoint.channelsMetrics() > endpoint.maxChannels())) {
                logger.warn("RntbdEndpoint Identifier {}, Stat {}", getPoolId(endpoint), getPoolStat(endpoint));
            } else if (this.logger.isDebugEnabled()) {
                logger.debug("RntbdEndpoint Identifier {}, Stat {}", getPoolId(endpoint), getPoolStat(endpoint));
            }
        }

        private String getPoolStat(RntbdEndpoint endpoint) {
            return "[ "
                + "poolTaskExecutorSize " + endpoint.executorTaskQueueMetrics()
                + ", lastRequestNanoTime " + Instant.now().minusNanos(
                System.nanoTime() - endpoint.lastRequestNanoTime())
                + ", connecting " + endpoint.gettingEstablishedConnectionsMetrics()
                + ", acquiredChannel " + endpoint.channelsAcquiredMetric()
                + ", availableChannel " + endpoint.channelsAvailableMetric()
                + ", pendingAcquisitionSize " + endpoint.requestQueueLength()
                + ", closed " + endpoint.isClosed()
                + " ]";
        }

        private String getPoolId(RntbdEndpoint endpoint) {
            if (endpoint == null) {
                return "null";
            }

            return "[RntbdEndpoint" +
                ", id " + endpoint.id() +
                ", remoteAddress " + endpoint.remoteAddress() +
                ", creationTime " + endpoint.getCreatedTime() +
                ", hashCode " + endpoint.hashCode() +
                "]";
        }
    }

    // endregion
}
