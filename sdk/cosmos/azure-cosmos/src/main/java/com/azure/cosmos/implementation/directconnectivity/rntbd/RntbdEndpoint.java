// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.directconnectivity.IAddressResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.instrument.Tag;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;

import java.net.SocketAddress;
import java.net.URI;
import java.time.Instant;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient.Options;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public interface RntbdEndpoint extends AutoCloseable {

    // region Accessors

    /**
     * @return approximate number of acquired channels.
     */
    int channelsAcquiredMetric();

    /**
     * @return approximate number of available channels.
     */
    int channelsAvailableMetric();

    int concurrentRequests();

    /**
     * @return returns approximate number of connections in the connecting mode.
     */
    int gettingEstablishedConnectionsMetrics();

    Instant getCreatedTime();

    long lastRequestNanoTime();

    long lastSuccessfulRequestNanoTime();

    int channelsMetrics();

    int executorTaskQueueMetrics();

    long id();

    boolean isClosed();

    int maxChannels();

    SocketAddress remoteAddress();

    URI serverKey();

    int requestQueueLength();

    Tag tag();

    long usedDirectMemory();

    long usedHeapMemory();

    // endregion

    // region Methods

    @Override
    void close();

    RntbdRequestRecord request(RntbdRequestArgs requestArgs);

    // endregion

    // region Types

    interface Provider extends AutoCloseable {

        @Override
        void close();

        Config config();

        int count();

        int evictions();

        RntbdEndpoint get(URI physicalAddress);

        IAddressResolver getAddressResolver();

        Stream<RntbdEndpoint> list();
    }

    final class Config {

        private final PooledByteBufAllocator allocator;
        private final Options options;
        private final SslContext sslContext;
        private final LogLevel wireLogLevel;

        public Config(final Options options, final SslContext sslContext, final LogLevel wireLogLevel) {

            checkNotNull(options, "options");
            checkNotNull(sslContext, "sslContext");

            int directArenaCount = PooledByteBufAllocator.defaultNumDirectArena();
            int heapArenaCount = PooledByteBufAllocator.defaultNumHeapArena();
            int pageSize = options.bufferPageSize();
            int maxOrder = Integer.numberOfTrailingZeros(options.maxBufferCapacity()) - Integer.numberOfTrailingZeros(pageSize);

            this.allocator = new PooledByteBufAllocator(heapArenaCount, directArenaCount, pageSize, maxOrder);
            this.options = options;
            this.sslContext = sslContext;
            this.wireLogLevel = wireLogLevel;
        }

        @JsonIgnore
        public PooledByteBufAllocator allocator() {
            return this.allocator;
        }

        @JsonProperty
        public int bufferPageSize() {
            return this.options.bufferPageSize();
        }

        @JsonProperty
        public long connectionAcquisitionTimeoutInNanos() {
            return this.options.connectionAcquisitionTimeout().toNanos();
        }

        @JsonProperty
        public int connectTimeoutInMillis() {
            final long value = this.options.connectTimeout().toMillis();
            assert value <= Integer.MAX_VALUE;
            return (int)value;
        }

        @JsonProperty
        public long idleConnectionTimeoutInNanos() {
            return this.options.idleChannelTimeout().toNanos();
        }

        @JsonProperty
        public long idleConnectionTimerResolutionInNanos() {
            return this.options.idleChannelTimerResolution().toNanos();
        }

        @JsonProperty
        public long idleEndpointTimeoutInNanos() {
            return this.options.idleEndpointTimeout().toNanos();
        }

        @JsonProperty()
        public boolean isConnectionEndpointRediscoveryEnabled() {
            return this.options.isConnectionEndpointRediscoveryEnabled();
        }

        @JsonProperty
        public int maxBufferCapacity() {
            return this.options.maxBufferCapacity();
        }

        @JsonProperty
        public int maxChannelsPerEndpoint() {
            return this.options.maxChannelsPerEndpoint();
        }

        @JsonProperty
        public int maxRequestsPerChannel() {
            return this.options.maxRequestsPerChannel();
        }

        @JsonProperty
        public int maxConcurrentRequestsPerEndpoint() {
            return this.options.maxConcurrentRequestsPerEndpoint();
        }

        @JsonProperty
        public long receiveHangDetectionTimeInNanos() {
            return this.options.receiveHangDetectionTime().toNanos();
        }

        @JsonProperty
        public long requestTimeoutInNanos() {
            return this.options.requestTimeout().toNanos();
        }

        @JsonProperty
        public long requestTimerResolutionInNanos() {
            return this.options.requestTimerResolution().toNanos();
        }

        @JsonProperty
        public long sendHangDetectionTimeInNanos() {
            return this.options.sendHangDetectionTime().toNanos();
        }

        @JsonProperty
        public long shutdownTimeoutInNanos() {
            return this.options.shutdownTimeout().toNanos();
        }

        @JsonIgnore
        public SslContext sslContext() {
            return this.sslContext;
        }

        @JsonProperty
        public UserAgentContainer userAgent() {
            return this.options.userAgent();
        }

        @JsonProperty
        public LogLevel wireLogLevel() {
            return this.wireLogLevel;
        }

        @JsonProperty
        public boolean isChannelAcquisitionContextEnabled() { return this.options.isChannelAcquisitionContextEnabled(); }

        @Override
        public String toString() {
            return RntbdObjectMapper.toString(this);
        }
    }

    // endregion
}
