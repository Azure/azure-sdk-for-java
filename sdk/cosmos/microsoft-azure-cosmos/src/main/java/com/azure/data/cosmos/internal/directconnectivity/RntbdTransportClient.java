// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdMetrics;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdServiceEndpoint;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@JsonSerialize(using = RntbdTransportClient.JsonSerializer.class)
public final class RntbdTransportClient extends TransportClient {

    // region Fields

    private static final AtomicLong instanceCount = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(RntbdTransportClient.class);
    private static final String namePrefix = RntbdTransportClient.class.getSimpleName() + '-';

    private final AtomicBoolean closed = new AtomicBoolean();
    private final RntbdEndpoint.Provider endpointProvider;
    private final RntbdMetrics metrics;
    private final String name;

    // endregion

    // region Constructors

    RntbdTransportClient(final RntbdEndpoint.Provider endpointProvider) {
        this.name = RntbdTransportClient.namePrefix + RntbdTransportClient.instanceCount.incrementAndGet();
        this.endpointProvider = endpointProvider;
        this.metrics = new RntbdMetrics(this.name);
    }

    RntbdTransportClient(final Options options, final SslContext sslContext) {
        this(new RntbdServiceEndpoint.Provider(options, sslContext));
    }

    RntbdTransportClient(final Configs configs, final int requestTimeoutInSeconds, final UserAgentContainer userAgent) {
        this(new Options.Builder(requestTimeoutInSeconds).userAgent(userAgent).build(), configs.getSslContext());
    }

    // endregion

    // region Methods

    @Override
    public void close() {

        logger.debug("\n  [{}] CLOSE", this);

        if (this.closed.compareAndSet(false, true)) {
            this.endpointProvider.close();
            this.metrics.close();
            return;
        }

        logger.debug("\n  [{}]\n  already closed", this);
    }

    @Override
    public Mono<StoreResponse> invokeStoreAsync(final URI physicalAddress, final RxDocumentServiceRequest request) {

        checkNotNull(physicalAddress, "physicalAddress");
        checkNotNull(request, "request");
        this.throwIfClosed();

        final RntbdRequestArgs requestArgs = new RntbdRequestArgs(request, physicalAddress);

        if (logger.isDebugEnabled()) {
            requestArgs.traceOperation(logger, null, "invokeStoreAsync");
            logger.debug("\n  [{}]\n  {}\n  INVOKE_STORE_ASYNC", this, requestArgs);
        }

        final RntbdEndpoint endpoint = this.endpointProvider.get(physicalAddress);
        this.metrics.incrementRequestCount();

        final RntbdRequestRecord requestRecord = endpoint.request(requestArgs);

        requestRecord.whenComplete((response, error) -> {
            this.metrics.incrementResponseCount();
            if (error != null) {
                this.metrics.incrementErrorResponseCount();
            }
        });

        return Mono.fromFuture(requestRecord).doFinally(signal -> {
            if (signal == SignalType.CANCEL) {
                requestRecord.cancel(false);
            }
        });
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toJson(this);
    }

    private void throwIfClosed() {
        checkState(!this.closed.get(), "%s is closed", this);
    }

    // endregion

    // region Types

    static final class JsonSerializer extends StdSerializer<RntbdTransportClient> {

        public JsonSerializer() {
            this(null);
        }

        public JsonSerializer(Class<RntbdTransportClient> type) {
            super(type);
        }

        @Override
        public void serialize(RntbdTransportClient value, JsonGenerator generator, SerializerProvider provider) throws IOException {

            generator.writeStartObject();

            generator.writeArrayFieldStart(value.name);

            value.endpointProvider.list().forEach(endpoint -> {
                try {
                    generator.writeObject(endpoint);
                } catch (IOException error) {
                    logger.error("failed to serialize {} due to ", endpoint.getName(), error);
                }
            });

            generator.writeEndArray();

            generator.writeObjectField("config", value.endpointProvider.config());
            generator.writeObjectField("metrics", value.metrics);
            generator.writeEndObject();
        }
    }

    public static final class Options {

        // region Fields

        private final String certificateHostNameOverride;
        private final int maxChannelsPerEndpoint;
        private final int maxRequestsPerChannel;
        private final Duration connectionTimeout;
        private final int partitionCount;
        private final Duration receiveHangDetectionTime;
        private final Duration requestTimeout;
        private final Duration sendHangDetectionTime;
        private final UserAgentContainer userAgent;

        // endregion

        // region Constructors

        private Options(Builder builder) {

            this.certificateHostNameOverride = builder.certificateHostNameOverride;
            this.maxChannelsPerEndpoint = builder.maxChannelsPerEndpoint;
            this.maxRequestsPerChannel = builder.maxRequestsPerChannel;
            this.connectionTimeout = builder.connectionTimeout == null ? builder.requestTimeout : builder.connectionTimeout;
            this.partitionCount = builder.partitionCount;
            this.requestTimeout = builder.requestTimeout;
            this.receiveHangDetectionTime = builder.receiveHangDetectionTime;
            this.sendHangDetectionTime = builder.sendHangDetectionTime;
            this.userAgent = builder.userAgent;
        }

        // endregion

        // region Accessors

        public String getCertificateHostNameOverride() {
            return this.certificateHostNameOverride;
        }

        public int getMaxChannelsPerEndpoint() {
            return this.maxChannelsPerEndpoint;
        }

        public int getMaxRequestsPerChannel() {
            return this.maxRequestsPerChannel;
        }

        public Duration getConnectionTimeout() {
            return this.connectionTimeout;
        }

        public int getPartitionCount() {
            return this.partitionCount;
        }

        public Duration getReceiveHangDetectionTime() {
            return this.receiveHangDetectionTime;
        }

        public Duration getRequestTimeout() {
            return this.requestTimeout;
        }

        public Duration getSendHangDetectionTime() {
            return this.sendHangDetectionTime;
        }

        public UserAgentContainer getUserAgent() {
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
            private static final Duration SIXTY_FIVE_SECONDS = Duration.ofSeconds(65L);
            private static final Duration TEN_SECONDS = Duration.ofSeconds(10L);

            // Required parameters

            private String certificateHostNameOverride = null;

            // Optional parameters

            private int maxChannelsPerEndpoint = 10;
            private int maxRequestsPerChannel = 30;
            private Duration connectionTimeout = null;
            private int partitionCount = 1;
            private Duration receiveHangDetectionTime = SIXTY_FIVE_SECONDS;
            private Duration requestTimeout;
            private Duration sendHangDetectionTime = TEN_SECONDS;
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

            public Options build() {
                return new Options(this);
            }

            public Builder certificateHostNameOverride(final String value) {
                this.certificateHostNameOverride = value;
                return this;
            }

            public Builder connectionTimeout(final Duration value) {
                checkArgument(value == null || value.compareTo(Duration.ZERO) > 0, "value: %s", value);
                this.connectionTimeout = value;
                return this;
            }

            public Builder maxRequestsPerChannel(final int value) {
                checkArgument(value > 0, "value: %s", value);
                this.maxRequestsPerChannel = value;
                return this;
            }

            public Builder maxChannelsPerEndpoint(final int value) {
                checkArgument(value > 0, "value: %s", value);
                this.maxChannelsPerEndpoint = value;
                return this;
            }

            public Builder partitionCount(final int value) {
                checkArgument(value > 0, "value: %s", value);
                this.partitionCount = value;
                return this;
            }

            public Builder receiveHangDetectionTime(final Duration value) {

                checkNotNull(value, "value: null");
                checkArgument(value.compareTo(Duration.ZERO) > 0, "value: %s", value);

                this.receiveHangDetectionTime = value;
                return this;
            }

            public Builder requestTimeout(final Duration value) {

                checkNotNull(value, "value: null");
                checkArgument(value.compareTo(Duration.ZERO) > 0, "value: %s", value);

                this.requestTimeout = value;
                return this;
            }

            public Builder sendHangDetectionTime(final Duration value) {

                checkNotNull(value, "value: null");
                checkArgument(value.compareTo(Duration.ZERO) > 0, "value: %s", value);

                this.sendHangDetectionTime = value;
                return this;
            }

            public Builder userAgent(final UserAgentContainer value) {
                checkNotNull(value, "value: null");
                this.userAgent = value;
                return this;
            }

            // endregion
        }

        // endregion
    }

    // endregion
}
