// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.internal.directconnectivity.RntbdTransportClient;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.micrometer.core.instrument.Tag;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.azure.data.cosmos.internal.HttpConstants.HttpHeaders;
import static com.azure.data.cosmos.internal.directconnectivity.RntbdTransportClient.Options;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
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
    private final AtomicLong lastRequestTime;
    private final RntbdMetrics metrics;
    private final Provider provider;
    private final SocketAddress remoteAddress;
    private final RntbdRequestTimer requestTimer;
    private final Tag tag;

    // endregion

    // region Constructors

    private RntbdServiceEndpoint(
        final Provider provider, final Config config, final NioEventLoopGroup group, final RntbdRequestTimer timer,
        final URI physicalAddress
    ) {

        final Bootstrap bootstrap = new Bootstrap()
            .channel(NioSocketChannel.class)
            .group(group)
            .option(ChannelOption.ALLOCATOR, config.allocator())
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.connectionTimeoutInMillis())
            .option(ChannelOption.RCVBUF_ALLOCATOR, receiveBufferAllocator)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .remoteAddress(physicalAddress.getHost(), physicalAddress.getPort());

        this.channelPool = new RntbdClientChannelPool(this, bootstrap, config);
        this.remoteAddress = bootstrap.config().remoteAddress();
        this.concurrentRequests = new AtomicInteger();
        this.lastRequestTime = new AtomicLong();
        this.closed = new AtomicBoolean();
        this.requestTimer = timer;

        this.tag = Tag.of(TAG_NAME, RntbdMetrics.escape(this.remoteAddress.toString()));
        this.id = instanceCount.incrementAndGet();
        this.provider = provider;

        this.metrics = new RntbdMetrics(provider.transportClient, this);
    }

    // endregion

    // region Accessors

    @Override
    public int channelsAcquired() {
        return this.channelPool.channelsAcquired();
    }

    @Override
    public int channelsAvailable() {
        return this.channelPool.channelsAvailable();
    }

    @Override
    public int concurrentRequests() {
        return this.concurrentRequests.get();
    }

    @Override
    public long id() {
        return this.id;
    }

    @Override
    public boolean isClosed() {
        return this.closed.get();
    }

    public long lastRequestTime() {
        return this.lastRequestTime.get();
    }

    @Override
    public SocketAddress remoteAddress() {
        return this.remoteAddress;
    }

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

        this.concurrentRequests.incrementAndGet();
        this.lastRequestTime.set(args.creationTime());

        if (logger.isDebugEnabled()) {
            args.traceOperation(logger, null, "request");
            logger.debug("\n  {}\n  {}\n  REQUEST", this, args);
        }

        final RntbdRequestRecord record = this.write(args);

        record.whenComplete((response, error) -> {

            args.traceOperation(logger, null, "requestComplete", response, error);

            if (error == null) {
                logger.debug("\n  [{}]\n  {}\n  request succeeded with response status: {}", this, args, response.getStatus());
            } else {
                logger.debug("\n  [{}]\n  {}\n  request failed due to ", this, args, error);
            }

            this.concurrentRequests.decrementAndGet();
            this.metrics.markComplete(record);
        });

        return record;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    // endregion

    // region Privates

    private void releaseToPool(final Channel channel) {

        logger.debug("\n  [{}]\n  {}\n  RELEASE", this, channel);

        this.channelPool.release(channel).addListener(future -> {
            if (logger.isDebugEnabled()) {
                if (future.isSuccess()) {
                    logger.debug("\n  [{}]\n  {}\n  release succeeded", this, channel);
                } else {
                    logger.debug("\n  [{}]\n  {}\n  release failed due to {}", this, channel, future.cause());
                }
            }
        });
    }

    private void throwIfClosed() {
        checkState(!this.closed.get(), "%s is closed", this);
    }

    private RntbdRequestRecord write(final RntbdRequestArgs requestArgs) {

        final RntbdRequestRecord requestRecord = new RntbdRequestRecord(requestArgs, this.requestTimer);
        logger.debug("\n  [{}]\n  {}\n  WRITE", this, requestArgs);

        this.channelPool.acquire().addListener(connected -> {

            if (connected.isSuccess()) {

                requestArgs.traceOperation(logger, null, "write");
                final Channel channel = (Channel)connected.get();
                this.releaseToPool(channel);

                channel.write(requestRecord).addListener((ChannelFuture future) -> {
                    requestArgs.traceOperation(logger, null, "writeComplete", channel);
                });

                return;
            }

            final UUID activityId = requestArgs.activityId();
            final Throwable cause = connected.cause();

            if (connected.isCancelled()) {

                logger.debug("\n  [{}]\n  {}\n  write cancelled: {}", this, requestArgs, cause);
                requestRecord.cancel(true);

            } else {

                logger.debug("\n  [{}]\n  {}\n  write failed due to {} ", this, requestArgs, cause);
                final String reason = cause.getMessage();

                final GoneException goneException = new GoneException(
                    Strings.lenientFormat("failed to establish connection to %s: %s", this.remoteAddress, reason),
                    cause instanceof Exception ? (Exception)cause : new IOException(reason, cause),
                    ImmutableMap.of(HttpHeaders.ACTIVITY_ID, activityId.toString()),
                    requestArgs.replicaPath()
                );

                BridgeInternal.setRequestHeaders(goneException, requestArgs.serviceRequest().getHeaders());
                requestRecord.completeExceptionally(goneException);
            }
        });

        return requestRecord;
    }

    // endregion

    // region Types

    static final class JsonSerializer extends StdSerializer<RntbdServiceEndpoint> {

        public JsonSerializer() {
            super(RntbdServiceEndpoint.class);
        }

        @Override
        public void serialize(RntbdServiceEndpoint value, JsonGenerator generator, SerializerProvider provider)
            throws IOException {
            generator.writeStartObject();
            generator.writeNumberField("id", value.id);
            generator.writeBooleanField("isClosed", value.isClosed());
            generator.writeNumberField("concurrentRequests", value.concurrentRequests());
            generator.writeStringField("remoteAddress", value.remoteAddress.toString());
            generator.writeObjectField("channelPool", value.channelPool);
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
        private final RntbdRequestTimer requestTimer;
        private final RntbdTransportClient transportClient;

        public Provider(final RntbdTransportClient transportClient, final Options options, final SslContext sslContext) {

            checkNotNull(transportClient, "expected non-null provider");
            checkNotNull(options, "expected non-null options");
            checkNotNull(sslContext, "expected non-null sslContext");

            final DefaultThreadFactory threadFactory = new DefaultThreadFactory("cosmos-rntbd-nio", true);
            final int threadCount = 2 * Runtime.getRuntime().availableProcessors();
            final LogLevel wireLogLevel;

            if (logger.isTraceEnabled()) {
                wireLogLevel = LogLevel.TRACE;
            } else if (logger.isDebugEnabled()) {
                wireLogLevel = LogLevel.DEBUG;
            } else {
                wireLogLevel = null;
            }

            this.transportClient = transportClient;
            this.config = new Config(options, sslContext, wireLogLevel);
            this.requestTimer = new RntbdRequestTimer(config.requestTimeoutInNanos());
            this.eventLoopGroup = new NioEventLoopGroup(threadCount, threadFactory);

            this.endpoints = new ConcurrentHashMap<>();
            this.evictions = new AtomicInteger();
            this.closed = new AtomicBoolean();
        }

        @Override
        public void close() {

            if (this.closed.compareAndSet(false, true)) {

                this.requestTimer.close();

                for (final RntbdEndpoint endpoint : this.endpoints.values()) {
                    endpoint.close();
                }

                this.eventLoopGroup.shutdownGracefully(QUIET_PERIOD, this.config.shutdownTimeoutInNanos(), NANOSECONDS)
                    .addListener(future -> {
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
        public RntbdEndpoint get(URI physicalAddress) {
            return endpoints.computeIfAbsent(physicalAddress.getAuthority(), authority ->
                new RntbdServiceEndpoint(this, config, eventLoopGroup, requestTimer, physicalAddress)
            );
        }

        @Override
        public Stream<RntbdEndpoint> list() {
            return this.endpoints.values().stream();
        }

        private void evict(RntbdEndpoint endpoint) {
            if (this.endpoints.remove(endpoint.remoteAddress().toString()) != null) {
                this.evictions.incrementAndGet();
            }
        }
    }

    // endregion
}
