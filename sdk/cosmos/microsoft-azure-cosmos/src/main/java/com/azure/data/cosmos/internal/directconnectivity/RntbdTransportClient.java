// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdServiceEndpoint;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Strings;
import io.micrometer.core.instrument.Tag;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@JsonSerialize(using = RntbdTransportClient.JsonSerializer.class)
public final class RntbdTransportClient extends TransportClient {

    // region Fields

    private static final String TAG_NAME = RntbdTransportClient.class.getSimpleName();

    private static final AtomicLong instanceCount = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(RntbdTransportClient.class);

    private final AtomicBoolean closed = new AtomicBoolean();
    private final RntbdEndpoint.Provider endpointProvider;
    private final long id;
    private final Tag tag;

    // endregion

    // region Constructors

    RntbdTransportClient(final RntbdEndpoint.Provider endpointProvider) {
        this.endpointProvider = endpointProvider;
        this.id = instanceCount.incrementAndGet();
        this.tag = RntbdTransportClient.tag(this.id);
    }

    RntbdTransportClient(final Options options, final SslContext sslContext) {
        this.endpointProvider = new RntbdServiceEndpoint.Provider(this, options, sslContext);
        this.id = instanceCount.incrementAndGet();
        this.tag = RntbdTransportClient.tag(this.id);
    }

    RntbdTransportClient(final Configs configs, final int requestTimeoutInSeconds, final UserAgentContainer userAgent) {
        this(new Options.Builder(requestTimeoutInSeconds).userAgent(userAgent).build(), configs.getSslContext());
    }

    // endregion

    // region Methods

    public boolean isClosed() {
        return this.closed.get();
    }

    @Override
    public void close() {

        if (this.closed.compareAndSet(false, true)) {
            logger.debug("close {}", this);
            this.endpointProvider.close();
            return;
        }

        logger.debug("already closed {}", this);
    }

    public int endpointCount() {
        return this.endpointProvider.count();
    }

    public int endpointEvictionCount() {
        return this.endpointProvider.evictions();
    }

    public long id() {
        return this.id;
    }

    @Override
    public Mono<StoreResponse> invokeStoreAsync(final URI address, final RxDocumentServiceRequest request) {

        logger.debug("RntbdTransportClient.invokeStoreAsync({}, {})", address, request);

        checkNotNull(address, "expected non-null address");
        checkNotNull(request, "expected non-null request");
        this.throwIfClosed();

        final RntbdRequestArgs requestArgs = new RntbdRequestArgs(request, address);
        requestArgs.traceOperation(logger, null, "invokeStoreAsync");

        final RntbdEndpoint endpoint = this.endpointProvider.get(address);
        final RntbdRequestRecord record = endpoint.request(requestArgs);

        logger.debug("RntbdTransportClient.invokeStoreAsync({}, {}): {}", address, request, record);

        return Mono.fromFuture(record).doFinally(signalType -> {
            logger.debug("SignalType.{} received from reactor: {\n  endpoint: {},\n  record: {}\n}",
                signalType.name(),
                endpoint,
                record);
            if (signalType == SignalType.CANCEL) {
                record.stage(RntbdRequestRecord.Stage.CANCELLED_BY_CLIENT);
            }
        });
    }

    public Tag tag() {
        return this.tag;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    private static Tag tag(long id) {
        return Tag.of(TAG_NAME, Strings.padStart(Long.toHexString(id).toUpperCase(), 4, '0'));
    }

    // endregion

    // region Privates

    private void throwIfClosed() {
        checkState(!this.closed.get(), "%s is closed", this);
    }

    // endregion

    // region Types

    public static final class Options {

        // region Fields

        private final int bufferPageSize;
        private final String certificateHostNameOverride;
        private final Duration connectionTimeout;
        private final Duration idleChannelTimeout;
        private final Duration idleEndpointTimeout;
        private final int maxBufferCapacity;
        private final int maxChannelsPerEndpoint;
        private final int maxRequestsPerChannel;
        private final int partitionCount;
        private final Duration receiveHangDetectionTime;
        private final Duration requestExpiryInterval;
        private final Duration requestTimeout;
        private final Duration sendHangDetectionTime;
        private final Duration shutdownTimeout;
        private final UserAgentContainer userAgent;

        // endregion

        // region Constructors

        private Options(Builder builder) {
            this.bufferPageSize = builder.bufferPageSize;
            this.certificateHostNameOverride = builder.certificateHostNameOverride;
            this.connectionTimeout = builder.connectionTimeout == null ? builder.requestTimeout : builder.connectionTimeout;
            this.idleChannelTimeout = builder.idleChannelTimeout;
            this.idleEndpointTimeout = builder.idleEndpointTimeout;
            this.maxBufferCapacity = builder.maxBufferCapacity;
            this.maxChannelsPerEndpoint = builder.maxChannelsPerEndpoint;
            this.maxRequestsPerChannel = builder.maxRequestsPerChannel;
            this.partitionCount = builder.partitionCount;
            this.receiveHangDetectionTime = builder.receiveHangDetectionTime;
            this.requestExpiryInterval = builder.requestExpiryInterval;
            this.requestTimeout = builder.requestTimeout;
            this.sendHangDetectionTime = builder.sendHangDetectionTime;
            this.shutdownTimeout = builder.shutdownTimeout;
            this.userAgent = builder.userAgent;
        }

        // endregion

        // region Accessors

        public int bufferPageSize() {
            return this.bufferPageSize;
        }

        public String certificateHostNameOverride() {
            return this.certificateHostNameOverride;
        }

        public Duration connectionTimeout() {
            return this.connectionTimeout;
        }

        public Duration idleChannelTimeout() {
            return this.idleChannelTimeout;
        }

        public Duration idleEndpointTimeout() {
            return this.idleEndpointTimeout;
        }

        public int maxBufferCapacity() {
            return this.maxBufferCapacity;
        }

        public int maxChannelsPerEndpoint() {
            return this.maxChannelsPerEndpoint;
        }

        public int maxRequestsPerChannel() {
            return this.maxRequestsPerChannel;
        }

        public int partitionCount() {
            return this.partitionCount;
        }

        public Duration receiveHangDetectionTime() {
            return this.receiveHangDetectionTime;
        }

        public Duration requestExpiryInterval() {
            return this.requestExpiryInterval;
        }

        public Duration requestTimeout() {
            return this.requestTimeout;
        }

        public Duration sendHangDetectionTime() {
            return this.sendHangDetectionTime;
        }

        public Duration shutdownTimeout() {
            return this.shutdownTimeout;
        }

        public UserAgentContainer userAgent() {
            return this.userAgent;
        }

        // endregion

        // region Methods

        @Override
        public String toString() {
            return RntbdObjectMapper.toJson(this);
        }

        // endregion

        // region Types

        public static class Builder {

            // region Fields

            private static final UserAgentContainer DEFAULT_USER_AGENT_CONTAINER = new UserAgentContainer();
            private static final Duration FIFTEEN_SECONDS = Duration.ofSeconds(15L);
            private static final Duration FIVE_SECONDS =Duration.ofSeconds(5L);
            private static final Duration SEVENTY_SECONDS = Duration.ofSeconds(70L);
            private static final Duration SIXTY_FIVE_SECONDS = Duration.ofSeconds(65L);
            private static final Duration TEN_SECONDS = Duration.ofSeconds(10L);

            private int bufferPageSize = 8192;
            private String certificateHostNameOverride = null;
            private Duration connectionTimeout = null;
            private Duration idleChannelTimeout = Duration.ZERO;
            private Duration idleEndpointTimeout = SEVENTY_SECONDS;
            private int maxBufferCapacity = 8192 << 10;
            private int maxChannelsPerEndpoint = 10;
            private int maxRequestsPerChannel = 30;
            private int partitionCount = 1;
            private Duration receiveHangDetectionTime = SIXTY_FIVE_SECONDS;
            private Duration requestExpiryInterval = FIVE_SECONDS;
            private Duration requestTimeout;
            private Duration sendHangDetectionTime = TEN_SECONDS;
            private Duration shutdownTimeout = FIFTEEN_SECONDS;
            private UserAgentContainer userAgent = DEFAULT_USER_AGENT_CONTAINER;

            // endregion

            // region Constructors

            public Builder(Duration requestTimeout) {
                this.requestTimeout(requestTimeout);
            }

            public Builder(int requestTimeoutInSeconds) {
                this(Duration.ofSeconds(requestTimeoutInSeconds));
            }

            // endregion

            // region Methods

            public Builder bufferPageSize(final int value) {
                checkArgument(value >= 4096 && (value & (value - 1)) == 0,
                    "expected value to be a power of 2 >= 4096, not %s",
                    value);
                this.bufferPageSize = value;
                return this;
            }

            public Options build() {
                checkState(this.bufferPageSize <= this.maxBufferCapacity,
                    "expected bufferPageSize (%s) <= maxBufferCapacity (%s)",
                    this.bufferPageSize,
                    this.maxBufferCapacity);
                return new Options(this);
            }

            public Builder certificateHostNameOverride(final String value) {
                this.certificateHostNameOverride = value;
                return this;
            }

            public Builder connectionTimeout(final Duration value) {
                checkArgument(value == null || value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.connectionTimeout = value;
                return this;
            }

            public Builder idleChannelTimeout(final Duration value) {
                checkNotNull(value, "expected non-null value");
                this.idleChannelTimeout = value;
                return this;
            }

            public Builder idleEndpointTimeout(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.idleEndpointTimeout = value;
                return this;
            }

            public Builder maxBufferCapacity(final int value) {
                checkArgument(value > 0 && (value & (value - 1)) == 0,
                    "expected positive value, not %s",
                    value);
                this.maxBufferCapacity = value;
                return this;
            }

            public Builder maxChannelsPerEndpoint(final int value) {
                checkArgument(value > 0, "expected positive value, not %s", value);
                this.maxChannelsPerEndpoint = value;
                return this;
            }

            public Builder maxRequestsPerChannel(final int value) {
                checkArgument(value > 0, "expected positive value, not %s", value);
                this.maxRequestsPerChannel = value;
                return this;
            }

            public Builder partitionCount(final int value) {
                checkArgument(value > 0, "expected positive value, not %s", value);
                this.partitionCount = value;
                return this;
            }

            public Builder receiveHangDetectionTime(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.receiveHangDetectionTime = value;
                return this;
            }

            public Builder requestExpiryInterval(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.requestExpiryInterval = value;
                return this;
            }

            public Builder requestTimeout(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.requestTimeout = value;
                return this;
            }

            public Builder sendHangDetectionTime(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.sendHangDetectionTime = value;
                return this;
            }

            public Builder shutdownTimeout(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.shutdownTimeout = value;
                return this;
            }

            public Builder userAgent(final UserAgentContainer value) {
                checkNotNull(value, "expected non-null value");
                this.userAgent = value;
                return this;
            }

            // endregion
        }

        // endregion
    }

    static final class JsonSerializer extends StdSerializer<RntbdTransportClient> {

        public JsonSerializer() {
            super(RntbdTransportClient.class);
        }

        @Override
        public void serialize(

            final RntbdTransportClient value,
            final JsonGenerator generator,
            final SerializerProvider provider

        ) throws IOException {

            generator.writeStartObject();
            generator.writeNumberField("id", value.id());
            generator.writeBooleanField("isClosed", value.isClosed());
            generator.writeObjectField("configuration", value.endpointProvider.config());
            generator.writeObjectFieldStart("serviceEndpoints");
            generator.writeNumberField("count", value.endpointCount());
            generator.writeArrayFieldStart("items");

            for (final Iterator<RntbdEndpoint> iterator = value.endpointProvider.list().iterator(); iterator.hasNext(); ) {
                generator.writeObject(iterator.next());
            }

            generator.writeEndArray();
            generator.writeEndObject();
            generator.writeEndObject();
        }
    }

    // endregion
}
