// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.directconnectivity.RntbdTransportClient.Options;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.ImmutableMap;
import io.netty.bootstrap.Bootstrap;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@JsonSerialize(using = RntbdServiceEndpoint.JsonSerializer.class)
public final class RntbdServiceEndpoint implements RntbdEndpoint {

    private static final AtomicLong instanceCount = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(RntbdServiceEndpoint.class);
    private static final String namePrefix = RntbdServiceEndpoint.class.getSimpleName() + '-';

    private final RntbdClientChannelPool channelPool;
    private final AtomicBoolean closed;
    private final RntbdMetrics metrics;
    private final String name;
    private final SocketAddress remoteAddress;
    private final RntbdRequestTimer requestTimer;

    // region Constructors

    private RntbdServiceEndpoint(
        final Config config, final NioEventLoopGroup group, final RntbdRequestTimer timer, final URI physicalAddress
    ) {

        final Bootstrap bootstrap = new Bootstrap()
            .channel(NioSocketChannel.class)
            .group(group)
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeout())
            .option(ChannelOption.SO_KEEPALIVE, true)
            .remoteAddress(physicalAddress.getHost(), physicalAddress.getPort());

        this.name = RntbdServiceEndpoint.namePrefix + instanceCount.incrementAndGet();
        this.channelPool = new RntbdClientChannelPool(bootstrap, config);
        this.remoteAddress = bootstrap.config().remoteAddress();
        this.metrics = new RntbdMetrics(this.name);
        this.closed = new AtomicBoolean();
        this.requestTimer = timer;
    }

    // endregion

    // region Accessors

    @Override
    public String getName() {
        return this.name;
    }

    // endregion

    // region Methods

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            this.channelPool.close();
            this.metrics.close();
        }
    }

    public RntbdRequestRecord request(final RntbdRequestArgs args) {

        this.throwIfClosed();

        if (logger.isDebugEnabled()) {
            args.traceOperation(logger, null, "request");
            logger.debug("\n  {}\n  {}\n  REQUEST", this, args);
        }

        final RntbdRequestRecord requestRecord = this.write(args);
        this.metrics.incrementRequestCount();

        requestRecord.whenComplete((response, error) -> {

            args.traceOperation(logger, null, "requestComplete", response, error);
            this.metrics.incrementResponseCount();

            if (error != null) {
                this.metrics.incrementErrorResponseCount();
            }

            if (logger.isDebugEnabled()) {
                if (error == null) {
                    final int status = response.getStatus();
                    logger.debug("\n  [{}]\n  {}\n  request succeeded with response status: {}", this, args, status);
                } else {
                    logger.debug("\n  [{}]\n  {}\n  request failed due to ", this, args, error);
                }
            }
        });

        return requestRecord;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toJson(this);
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
                    if (!future.isSuccess()) {
                        this.metrics.incrementErrorResponseCount();
                    }
                });

                return;
            }

            final UUID activityId = requestArgs.getActivityId();
            final Throwable cause = connected.cause();

            if (connected.isCancelled()) {

                logger.debug("\n  [{}]\n  {}\n  write cancelled: {}", this, requestArgs, cause);
                requestRecord.cancel(true);

            } else {

                logger.debug("\n  [{}]\n  {}\n  write failed due to {} ", this, requestArgs, cause);
                final String reason = cause.getMessage();

                final GoneException goneException = new GoneException(
                    String.format("failed to establish connection to %s: %s", this.remoteAddress, reason),
                    cause instanceof Exception ? (Exception)cause : new IOException(reason, cause),
                    ImmutableMap.of(HttpConstants.HttpHeaders.ACTIVITY_ID, activityId.toString()),
                    requestArgs.getReplicaPath()
                );

                BridgeInternal.setRequestHeaders(goneException, requestArgs.getServiceRequest().getHeaders());
                requestRecord.completeExceptionally(goneException);
            }
        });

        return requestRecord;
    }

    // endregion

    // region Types

    static final class JsonSerializer extends StdSerializer<RntbdServiceEndpoint> {

        public JsonSerializer() {
            this(null);
        }

        public JsonSerializer(Class<RntbdServiceEndpoint> type) {
            super(type);
        }

        @Override
        public void serialize(RntbdServiceEndpoint value, JsonGenerator generator, SerializerProvider provider)
            throws IOException {

            generator.writeStartObject();
            generator.writeStringField(value.name, value.remoteAddress.toString());
            generator.writeObjectField("channelPool", value.channelPool);
            generator.writeEndObject();
        }
    }

    public static final class Provider implements RntbdEndpoint.Provider {

        private static final Logger logger = LoggerFactory.getLogger(Provider.class);

        private final AtomicBoolean closed = new AtomicBoolean();
        private final Config config;
        private final ConcurrentHashMap<String, RntbdEndpoint> endpoints = new ConcurrentHashMap<>();
        private final NioEventLoopGroup eventLoopGroup;
        private final RntbdRequestTimer requestTimer;

        public Provider(final Options options, final SslContext sslContext) {

            checkNotNull(options, "options");
            checkNotNull(sslContext, "sslContext");

            final DefaultThreadFactory threadFactory = new DefaultThreadFactory("CosmosEventLoop", true);
            final int threadCount = Runtime.getRuntime().availableProcessors();
            final LogLevel wireLogLevel;

            if (logger.isTraceEnabled()) {
                wireLogLevel = LogLevel.TRACE;
            } else if (logger.isDebugEnabled()) {
                wireLogLevel = LogLevel.DEBUG;
            } else {
                wireLogLevel = null;
            }

            this.config = new Config(options, sslContext, wireLogLevel);
            this.requestTimer = new RntbdRequestTimer(config.getRequestTimeout());
            this.eventLoopGroup = new NioEventLoopGroup(threadCount, threadFactory);
        }

        @Override
        public void close() throws RuntimeException {

            if (this.closed.compareAndSet(false, true)) {

                this.requestTimer.close();

                for (final RntbdEndpoint endpoint : this.endpoints.values()) {
                    endpoint.close();
                }

                this.eventLoopGroup.shutdownGracefully().addListener(future -> {
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
        public RntbdEndpoint get(URI physicalAddress) {
            return endpoints.computeIfAbsent(physicalAddress.getAuthority(), authority ->
                new RntbdServiceEndpoint(config, eventLoopGroup, requestTimer, physicalAddress)
            );
        }

        @Override
        public Stream<RntbdEndpoint> list() {
            return this.endpoints.values().stream();
        }

        private void deleteEndpoint(final URI physicalAddress) {

            // TODO: DANOBLE: Utilize this method of tearing down unhealthy endpoints
            //  Specifically, ensure that this method is called when a Read/WriteTimeoutException occurs or a health
            //  check request fails. This perhaps likely requires a change to RntbdClientChannelPool.
            //  Links:
            //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/331552
            //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/331593

            checkNotNull(physicalAddress, "physicalAddress: %s", physicalAddress);

            final String authority = physicalAddress.getAuthority();
            final RntbdEndpoint endpoint = this.endpoints.remove(authority);

            if (endpoint != null) {
                endpoint.close();
            }
        }
    }

    // endregion
}
