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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

        @JsonProperty()
        private final int bufferPageSize;

        @JsonProperty()
        private final Duration connectionTimeout;

        @JsonProperty()
        private final Duration idleChannelTimeout;

        @JsonProperty()
        private final Duration idleEndpointTimeout;

        @JsonProperty()
        private final int maxBufferCapacity;

        @JsonProperty()
        private final int maxChannelsPerEndpoint;

        @JsonProperty()
        private final int maxRequestsPerChannel;

        @JsonProperty()
        private final Duration receiveHangDetectionTime;

        @JsonProperty()
        private final Duration requestExpiryInterval;

        @JsonProperty()
        private final Duration requestTimeout;

        @JsonProperty()
        private final Duration requestTimerResolution;

        @JsonProperty()
        private final Duration sendHangDetectionTime;

        @JsonProperty()
        private final Duration shutdownTimeout;

        @JsonIgnore()
        private final UserAgentContainer userAgent;

        // endregion

        // region Constructors

        private Options() {
            this.bufferPageSize = 8192;
            this.connectionTimeout = null;
            this.idleChannelTimeout = Duration.ZERO;
            this.idleEndpointTimeout = Duration.ofSeconds(70L);
            this.maxBufferCapacity = 8192 << 10;
            this.maxChannelsPerEndpoint = 10;
            this.maxRequestsPerChannel = 30;
            this.receiveHangDetectionTime = Duration.ofSeconds(65L);
            this.requestExpiryInterval = Duration.ofSeconds(5L);
            this.requestTimeout = null;
            this.requestTimerResolution = Duration.ofMillis(5L);
            this.sendHangDetectionTime = Duration.ofSeconds(10L);
            this.shutdownTimeout = Duration.ofSeconds(15L);
            this.userAgent = new UserAgentContainer();
        }

        private Options(Builder builder) {

            this.bufferPageSize = builder.bufferPageSize;
            this.idleChannelTimeout = builder.idleChannelTimeout;
            this.idleEndpointTimeout = builder.idleEndpointTimeout;
            this.maxBufferCapacity = builder.maxBufferCapacity;
            this.maxChannelsPerEndpoint = builder.maxChannelsPerEndpoint;
            this.maxRequestsPerChannel = builder.maxRequestsPerChannel;
            this.receiveHangDetectionTime = builder.receiveHangDetectionTime;
            this.requestExpiryInterval = builder.requestExpiryInterval;
            this.requestTimeout = builder.requestTimeout;
            this.requestTimerResolution = builder.requestTimerResolution;
            this.sendHangDetectionTime = builder.sendHangDetectionTime;
            this.shutdownTimeout = builder.shutdownTimeout;
            this.userAgent = builder.userAgent;

            this.connectionTimeout = builder.connectionTimeout == null
                ? builder.requestTimeout
                : builder.connectionTimeout;
        }

        // endregion

        // region Accessors

        public int bufferPageSize() {
            return this.bufferPageSize;
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

        public Duration receiveHangDetectionTime() {
            return this.receiveHangDetectionTime;
        }

        public Duration requestExpiryInterval() {
            return this.requestExpiryInterval;
        }

        public Duration requestTimeout() {
            return this.requestTimeout;
        }

        public Duration requestTimerResolution() {
            return this.requestTimerResolution;
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

        @SuppressWarnings("UnusedReturnValue")
        public static class Builder {

            // region Fields

            private static final String DEFAULT_OPTIONS_PROPERTY_NAME = "azure.cosmos.directTcp.defaultOptions";
            private static final Options DEFAULT_OPTIONS;

            static {

                // In priority order we take default Direct TCP options from:
                //
                // 1. the string value of system property "azure.cosmos.directTcp.options", or
                // 2. the contents of the file located by the system property "azure.cosmos.directTcp.optionsFile", or
                // 3. the contents of the resource file named "azure.cosmos.directTcp.options.json"
                //
                // Otherwise, if none of these values are set or an error occurs we create default options based on a
                // set of hard-wired values defined in the default private parameterless constructor for
                // RntbdTransportClient.Options.

                Options options = null;

                try {
                    final String string = System.getProperty(DEFAULT_OPTIONS_PROPERTY_NAME);

                    if (string != null) {
                        // Attempt to set default options based on the JSON string value of "{propertyName}"
                        try {
                            options = RntbdObjectMapper.readValue(string, Options.class);
                        } catch (IOException error) {
                            logger.error("failed to parse default Direct TCP options {} due to ", string, error);
                        }
                    }

                    if (options == null) {

                        final String path = System.getProperty(DEFAULT_OPTIONS_PROPERTY_NAME + "File");

                        if (path != null) {
                            // Attempt to load default options from the JSON file on the path specified by
                            // "{propertyName}File"
                            try {
                                options = RntbdObjectMapper.readValue(new File(path), Options.class);
                            } catch (IOException error) {
                                logger.error("failed to load default Direct TCP options from {} due to ", path, error);
                            }
                        }
                    }

                    if (options == null) {

                        final ClassLoader loader = RntbdTransportClient.class.getClassLoader();
                        final String name = DEFAULT_OPTIONS_PROPERTY_NAME + ".json";

                        try (final InputStream stream = loader.getResourceAsStream(name)) {
                            if (stream != null) {
                                // Attempt to load default options from the JSON resource file "{propertyName}.json"
                                options = RntbdObjectMapper.readValue(stream, Options.class);
                            }
                        } catch (IOException error) {
                            logger.error("failed to load Direct TCP options from resource {} due to ", name, error);
                        }
                    }
                } finally {
                    DEFAULT_OPTIONS = options != null ? options : new Options();
                }
            }

            private int bufferPageSize;
            private Duration connectionTimeout;
            private Duration idleChannelTimeout;
            private Duration idleEndpointTimeout;
            private int maxBufferCapacity;
            private int maxChannelsPerEndpoint;
            private int maxRequestsPerChannel;
            private Duration receiveHangDetectionTime;
            private Duration requestExpiryInterval;
            private Duration requestTimeout;
            private Duration requestTimerResolution;
            private Duration sendHangDetectionTime;
            private Duration shutdownTimeout;
            private UserAgentContainer userAgent;

            // endregion

            // region Constructors

            public Builder(Duration requestTimeout) {

                this.requestTimeout(requestTimeout);

                this.bufferPageSize = DEFAULT_OPTIONS.bufferPageSize;
                this.connectionTimeout = DEFAULT_OPTIONS.connectionTimeout;
                this.idleChannelTimeout = DEFAULT_OPTIONS.idleChannelTimeout;
                this.idleEndpointTimeout = DEFAULT_OPTIONS.idleEndpointTimeout;
                this.maxBufferCapacity = DEFAULT_OPTIONS.maxBufferCapacity;
                this.maxChannelsPerEndpoint = DEFAULT_OPTIONS.maxChannelsPerEndpoint;
                this.maxRequestsPerChannel = DEFAULT_OPTIONS.maxRequestsPerChannel;
                this.receiveHangDetectionTime = DEFAULT_OPTIONS.receiveHangDetectionTime;
                this.requestExpiryInterval = DEFAULT_OPTIONS.requestExpiryInterval;
                this.requestTimerResolution = DEFAULT_OPTIONS.requestTimerResolution;
                this.sendHangDetectionTime = DEFAULT_OPTIONS.sendHangDetectionTime;
                this.shutdownTimeout = DEFAULT_OPTIONS.shutdownTimeout;
                this.userAgent = DEFAULT_OPTIONS.userAgent;
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

            public Builder requestTimerResolution(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.requestTimerResolution = value;
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
