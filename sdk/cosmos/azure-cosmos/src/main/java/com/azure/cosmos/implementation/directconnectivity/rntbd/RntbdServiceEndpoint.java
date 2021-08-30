// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.directconnectivity.IAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.TransportException;
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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
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
    private final RntbdMetrics metrics;
    private final Provider provider;
    private final URI serverKey;
    private final SocketAddress remoteAddress;
    private final RntbdRequestTimer requestTimer;
    private final Tag tag;
    private final int maxConcurrentRequests;
    private final boolean channelAcquisitionContextEnabled;

    private final RntbdConnectionStateListener connectionStateListener;

    // endregion

    // region Constructors

    private RntbdServiceEndpoint(
        final Provider provider,
        final Config config,
        final NioEventLoopGroup group,
        final RntbdRequestTimer timer,
        final URI physicalAddress) {

        this.serverKey = RntbdUtils.getServerKey(physicalAddress);

        final Bootstrap bootstrap = new Bootstrap()
            .channel(NioSocketChannel.class)
            .group(group)
            .option(ChannelOption.ALLOCATOR, config.allocator())
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.connectTimeoutInMillis())
            .option(ChannelOption.RCVBUF_ALLOCATOR, receiveBufferAllocator)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .remoteAddress(this.serverKey.getHost(), this.serverKey.getPort());

        this.createdTime = Instant.now();
        this.channelPool = new RntbdClientChannelPool(this, bootstrap, config);
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
        this.id = instanceCount.incrementAndGet();
        this.provider = provider;

        this.metrics = new RntbdMetrics(provider.transportClient, this);
        this.maxConcurrentRequests = config.maxConcurrentRequestsPerEndpoint();

        this.connectionStateListener = this.provider.addressResolver != null && config.isConnectionEndpointRediscoveryEnabled()
            ? new RntbdConnectionStateListener(this.provider.addressResolver, this)
            : null;

        this.channelAcquisitionContextEnabled = config.isChannelAcquisitionContextEnabled();
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
    public long usedDirectMemory() {
        return this.channelPool.usedDirectMemory();
    }

    @Override
    public long usedHeapMemory() {
        return this.channelPool.usedHeapMemory();
    }

    // endregion

    // region Methods

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            this.provider.evict(this);
            this.channelPool.close();
        }
    }

    public RntbdRequestRecord request(final RntbdRequestArgs args) {

        this.throwIfClosed();

        int concurrentRequestSnapshot = this.concurrentRequests.incrementAndGet();

        RntbdEndpointStatistics stat = endpointMetricsSnapshot(concurrentRequestSnapshot);

        if (concurrentRequestSnapshot > this.maxConcurrentRequests) {
            try {
                FailFastRntbdRequestRecord requestRecord = FailFastRntbdRequestRecord.createAndFailFast(
                    args,
                    concurrentRequestSnapshot,
                    metrics,
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
            this.metrics.markComplete(record);
            onResponse(error, record);
        });

        return record;
    }

    private void onResponse(Throwable exception, RntbdRequestRecord record) {
        if (exception == null) {
            this.lastSuccessfulRequestNanoTime.set(System.nanoTime());
            return;
        }

        if (this.connectionStateListener != null) {
            this.connectionStateListener.onException(record.args().serviceRequest(), exception);
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
            .inflightRequests(concurrentRequestSnapshot);
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
        requestRecord.channelAcquisitionContextEnabled(this.channelAcquisitionContextEnabled);
        requestRecord.stage(RntbdRequestRecord.Stage.CHANNEL_ACQUISITION_STARTED);
        final Future<Channel> connectedChannel = this.channelPool.acquire(requestRecord.getChannelAcquisitionTimeline());

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
            requestRecord.channelTaskQueueLength(RntbdUtils.tryGetExecutorTaskQueueSize(channel.eventLoop()));
            channel.write(requestRecord.stage(RntbdRequestRecord.Stage.PIPELINED))
                .addListener(ignore -> this.releaseToPool(channel));
            return requestRecord;
        }

        final RntbdRequestArgs requestArgs = requestRecord.args();
        final UUID activityId = requestArgs.activityId();
        final Throwable cause = connected.cause();

        if (connected.isCancelled()) {

            logger.debug("\n  [{}]\n  {}\n  write cancelled: {}", this, requestArgs, cause);
            requestRecord.cancel(true);

        } else {

            logger.debug("\n  [{}]\n  {}\n  write failed due to {} ", this, requestArgs, cause);
            final String reason = cause.toString();

            final GoneException goneException = new GoneException(
                lenientFormat("failed to establish connection to %s due to %s", this.remoteAddress, reason),
                cause instanceof Exception ? (Exception) cause : new IOException(reason, cause),
                ImmutableMap.of(HttpHeaders.ACTIVITY_ID, activityId.toString()),
                requestArgs.replicaPath()
            );

            BridgeInternal.setRequestHeaders(goneException, requestArgs.serviceRequest().getHeaders());
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
        private final NioEventLoopGroup eventLoopGroup;
        private final AtomicInteger evictions;
        private final RntbdEndpointMonitoringProvider monitoring;
        private final RntbdRequestTimer requestTimer;
        private final RntbdTransportClient transportClient;
        private final IAddressResolver addressResolver;

        public Provider(
            final RntbdTransportClient transportClient,
            final Options options,
            final SslContext sslContext,
            final IAddressResolver addressResolver) {

            checkNotNull(transportClient, "expected non-null provider");
            checkNotNull(options, "expected non-null options");
            checkNotNull(sslContext, "expected non-null sslContext");

            final DefaultThreadFactory threadFactory = new DefaultThreadFactory("cosmos-rntbd-nio", true);
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
                config.requestTimeoutInNanos(),
                config.requestTimerResolutionInNanos());

            this.eventLoopGroup = new NioEventLoopGroup(options.threadCount(), threadFactory);
            this.endpoints = new ConcurrentHashMap<>();
            this.evictions = new AtomicInteger();
            this.closed = new AtomicBoolean();
            this.monitoring = new RntbdEndpointMonitoringProvider(this);
            this.monitoring.init();
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
        public RntbdEndpoint get(final URI physicalAddress) {
            return endpoints.computeIfAbsent(physicalAddress.getAuthority(), authority -> new RntbdServiceEndpoint(
                this,
                this.config,
                this.eventLoopGroup,
                this.requestTimer,
                physicalAddress));
        }

        @Override
        public IAddressResolver getAddressResolver() {
            return this.addressResolver;
        }

        @Override
        public Stream<RntbdEndpoint> list() {
            return this.endpoints.values().stream();
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
            this.future = RntbdEndpointMonitoringProvider.monitoringRntbdChannelPool.scheduleAtFixedRate(() -> {
                logAllPools();
            }, 0, MONITORING_PERIOD.toMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public synchronized void close() {
            logger.info("Shutting down RntbdClientChannelPoolMonitoringProvider ...");
            this.future.cancel(false);
            this.future = null;
        }

        synchronized void logAllPools() {
            try {
                logger.debug("Total number of RntbdClientChannelPool [{}].", provider.endpoints.size());
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
