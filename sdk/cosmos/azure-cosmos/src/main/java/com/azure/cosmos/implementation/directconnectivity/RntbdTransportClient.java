// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdServiceEndpoint;
import com.azure.cosmos.implementation.guava25.base.Strings;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.micrometer.core.instrument.Tag;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdReporter.reportIssue;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

@JsonSerialize(using = RntbdTransportClient.JsonSerializer.class)
public class RntbdTransportClient extends TransportClient {

    // region Fields

    private static final String TAG_NAME = RntbdTransportClient.class.getSimpleName();

    private static final AtomicLong instanceCount = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(RntbdTransportClient.class);

    /**
     * NOTE: This context key name has been copied from {link Hooks#KEY_ON_ERROR_DROPPED} which is
     * not exposed as public Api but package internal only
     *
     * A key that can be used to store a sequence-specific {@link Hooks#onErrorDropped(Consumer)}
     * hook in a {@link Context}, as a {@link Consumer Consumer&lt;Throwable&gt;}.
     */
    private static final String KEY_ON_ERROR_DROPPED = "reactor.onErrorDropped.local";

    /**
     * This lambda gets injected into the local Reactor Context to react tot he onErrorDropped event and
     * log the throwable with DEBUG level instead of the ERROR level used in the default hook.
     * This is safe here because we guarantee resource clean-up with the doFinally-lambda
     */
    private static final Consumer<? super Throwable> onErrorDropHookWithReduceLogLevel =
        throwable -> {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Extra error - on error dropped - operator called :",
                    throwable);
            }
        };

    private final AtomicBoolean closed = new AtomicBoolean();
    private final RntbdEndpoint.Provider endpointProvider;
    private final long id;
    private final Tag tag;

    // endregion

    // region Constructors

    /**
     * Initializes a newly created {@linkplain RntbdTransportClient} object.
     *
     * @param configs          A {@link Configs} instance containing the {@link SslContext} to be used.
     * @param connectionPolicy The {@linkplain ConnectionPolicy connection policy} to be applied.
     * @param userAgent        The {@linkplain UserAgentContainer user agent} identifying.
     * @param addressResolver  The address resolver to be used for connection endpoint rediscovery, if connection
     *                         endpoint rediscovery is enabled by {@code connectionPolicy}.
     */
    public RntbdTransportClient(
        final Configs configs,
        final ConnectionPolicy connectionPolicy,
        final UserAgentContainer userAgent,
        final IAddressResolver addressResolver) {

        this(
            new Options.Builder(connectionPolicy).userAgent(userAgent).build(),
            configs.getSslContext(),
            addressResolver);
    }

    RntbdTransportClient(final RntbdEndpoint.Provider endpointProvider) {
        this.endpointProvider = endpointProvider;
        this.id = instanceCount.incrementAndGet();
        this.tag = RntbdTransportClient.tag(this.id);
    }

    RntbdTransportClient(
        final Options options,
        final SslContext sslContext,
        final IAddressResolver addressResolver) {

        this.endpointProvider = new RntbdServiceEndpoint.Provider(
            this,
            options,
            checkNotNull(sslContext, "expected non-null sslContext"),
            addressResolver);

        this.id = instanceCount.incrementAndGet();
        this.tag = RntbdTransportClient.tag(this.id);
    }

    // endregion

    // region Methods

    /**
     * {@code true} if this {@linkplain RntbdTransportClient client} is closed.
     *
     * @return {@code true} if this {@linkplain RntbdTransportClient client} is closed; {@code false} otherwise.
     */
    public boolean isClosed() {
        return this.closed.get();
    }

    /**
     * Closes this {@linkplain RntbdTransportClient client} and releases all resources associated with it.
     */
    @Override
    public void close() {

        if (this.closed.compareAndSet(false, true)) {
            logger.debug("close {}", this);
            this.endpointProvider.close();
            return;
        }

        logger.debug("already closed {}", this);
    }

    /**
     * The number of {@linkplain RntbdEndpoint endpoints} allocated to this {@linkplain RntbdTransportClient client}.
     *
     * @return The number of {@linkplain RntbdEndpoint endpoints} associated with this {@linkplain RntbdTransportClient
     * client}.
     */
    public int endpointCount() {
        return this.endpointProvider.count();
    }

    public int endpointEvictionCount() {
        return this.endpointProvider.evictions();
    }

    /**
     * The integer identity of this {@linkplain RntbdTransportClient client}.
     * <p>
     * Clients are numbered sequentially based on the order in which they are initialized.
     *
     * @return The integer identity of this {@linkplain RntbdTransportClient client}.
     */
    public long id() {
        return this.id;
    }

    /**
     * Issues a Direct TCP request to the specified Cosmos service address asynchronously.
     *
     * @param addressUri A Cosmos service address.
     * @param request The {@linkplain RxDocumentServiceRequest request} to issue.
     *
     * @return A {@link Mono} of type {@link StoreResponse} that will complete when the Direct TCP request completes.
     * I shI
     * @throws TransportException if this {@linkplain RntbdTransportClient client} is closed.
     */
    @Override
    public Mono<StoreResponse> invokeStoreAsync(final Uri addressUri, final RxDocumentServiceRequest request) {

        checkNotNull(addressUri, "expected non-null addressUri");
        checkNotNull(request, "expected non-null request");
        this.throwIfClosed();

        final URI address = addressUri.getURI();

        final RntbdRequestArgs requestArgs = new RntbdRequestArgs(request, address);
        final RntbdEndpoint endpoint = this.endpointProvider.get(address);
        final RntbdRequestRecord record = endpoint.request(requestArgs);

        final Context reactorContext = Context.of(KEY_ON_ERROR_DROPPED, onErrorDropHookWithReduceLogLevel);

        final Mono<StoreResponse> result = Mono.fromFuture(record.whenComplete((response, throwable) -> {
            record.stage(RntbdRequestRecord.Stage.COMPLETED);

            if (request.requestContext.cosmosDiagnostics == null) {
                request.requestContext.cosmosDiagnostics = request.createCosmosDiagnostics();
            }

            if (response != null) {
                RequestTimeline timeline = record.takeTimelineSnapshot();
                response.setRequestTimeline(timeline);
                response.setEndpointStatistics(record.serviceEndpointStatistics());
                response.setRntbdResponseLength(record.responseLength());
                response.setRntbdRequestLength(record.requestLength());
                response.setRequestPayloadLength(request.getContentLength());
                response.setRntbdChannelTaskQueueSize(record.channelTaskQueueLength());
                response.setRntbdPendingRequestSize(record.pendingRequestQueueSize());
                response.setChannelAcquisitionTimeline(record.getChannelAcquisitionTimeline());
            }

        })).onErrorMap(throwable -> {

            Throwable error = throwable instanceof CompletionException ? throwable.getCause() : throwable;

            if (!(error instanceof CosmosException)) {

                String unexpectedError = RntbdObjectMapper.toJson(error);

                reportIssue(logger, endpoint,
                    "request completed with an unexpected {}: \\{\"record\":{},\"error\":{}}",
                    error.getClass(),
                    record,
                    unexpectedError);

                error = new GoneException(
                    lenientFormat("an unexpected %s occurred: %s", unexpectedError),
                    address,
                    error instanceof Exception ? (Exception) error : new RuntimeException(error));
            }

            assert error instanceof CosmosException;
            CosmosException cosmosException = (CosmosException) error;
            BridgeInternal.setServiceEndpointStatistics(cosmosException, record.serviceEndpointStatistics());

            BridgeInternal.setRntbdRequestLength(cosmosException, record.requestLength());
            BridgeInternal.setRntbdResponseLength(cosmosException, record.responseLength());
            BridgeInternal.setRequestBodyLength(cosmosException, request.getContentLength());
            BridgeInternal.setRequestTimeline(cosmosException, record.takeTimelineSnapshot());
            BridgeInternal.setRntbdPendingRequestQueueSize(cosmosException, record.pendingRequestQueueSize());
            BridgeInternal.setChannelTaskQueueSize(cosmosException, record.channelTaskQueueLength());
            BridgeInternal.setSendingRequestStarted(cosmosException, record.hasSendingRequestStarted());
            BridgeInternal.setChannelAcquisitionTimeline(cosmosException, record.getChannelAcquisitionTimeline());

            return cosmosException;
        });

        return result.doFinally(signalType -> {

            // This lambda ensures that a pending Direct TCP request in a reactive stream dropped by an end user or the
            // HA layer completes without bubbling up to reactor.core.publisher.Hooks#onErrorDropped as a
            // CompletionException error. Pending requests may be left outstanding when, for example, an end user calls
            // CosmosAsyncClient#close or the HA layer detects that a partition split has occurred. This code guarantees
            // that each pending Mono<StoreResponse> in the stream will run to completion with a new subscriber.
            // Consequently the default Hooks#onErrorDropped method will not be called thus preventing distracting error
            // messages.
            //
            // This lambda does not prevent requests that complete exceptionally before the call to this lambda from
            // bubbling up to Hooks#onErrorDropped as CompletionException errors. We will still see some onErrorDropped
            // messages due to CompletionException errors. Anecdotal evidence shows that this is more likely to be seen
            // in low latency environments on Azure cloud. To avoid the onErrorDropped events to get logged in the
            // default hook (which logs with level ERROR) we inject a local hook in the Reactor Context to just log it
            // as DEBUG level for the lifecycle of this Mono (safe here because we know the onErrorDropped doesn't have
            // any functional issues.
            //
            // One might be tempted to complete a pending request here, but that is ill advised. Testing and
            // inspection of the reactor code shows that this does not prevent errors from bubbling up to
            // reactor.core.publisher.Hooks#onErrorDropped. Worse than this it has been seen to cause failures in
            // the HA layer:
            //
            // * Calling record.cancel or record.completeExceptionally causes failures in (low-latency) cloud
            //   environments and all errors bubble up Hooks#onErrorDropped.
            //
            // * Calling record.complete with a null value causes failures in all environments, depending on the
            //   operation being performed. In short: many of our tests fail.

            if (signalType != SignalType.CANCEL) {
                return;
            }

            result.subscribe(
                response -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "received response to cancelled request: {\"request\":{},\"response\":{\"type\":{},"
                                + "\"value\":{}}}}",
                            RntbdObjectMapper.toJson(record),
                            response.getClass().getSimpleName(),
                            RntbdObjectMapper.toJson(response));
                    }
                },
                throwable -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "received response to cancelled request: {\"request\":{},\"response\":{\"type\":{},"
                                + "\"value\":{}}}",
                            RntbdObjectMapper.toJson(record),
                            throwable.getClass().getSimpleName(),
                            RntbdObjectMapper.toJson(throwable));
                    }
                });
        }).subscriberContext(reactorContext);
    }

    /**
     * The key-value pair used to classify and drill into metrics produced by this {@linkplain RntbdTransportClient
     * client}.
     *
     * @return The key-value pair used to classify and drill into metrics collected by this {@linkplain
     * RntbdTransportClient client}.
     */
    public Tag tag() {
        return this.tag;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    // endregion

    // region Privates

    private static Tag tag(long id) {
        return Tag.of(TAG_NAME, Strings.padStart(Long.toHexString(id).toUpperCase(Locale.ROOT), 4, '0'));
    }

    private void throwIfClosed() {
        if (this.closed.get()) {
            throw new TransportException(lenientFormat("%s is closed", this), null);
        }
    }

    // endregion

    // region Types

    public static final class Options {

        private static final int DEFAULT_MIN_MAX_CONCURRENT_REQUESTS_PER_ENDPOINT = 10_000;

        // region Fields

        @JsonProperty()
        private final int bufferPageSize;

        @JsonProperty()
        private final Duration connectionAcquisitionTimeout;

        @JsonProperty()
        private final boolean connectionEndpointRediscoveryEnabled;

        @JsonProperty()
        private final Duration connectTimeout;

        @JsonProperty()
        private final Duration idleChannelTimeout;

        @JsonProperty()
        private final Duration idleChannelTimerResolution;

        @JsonProperty()
        private final Duration idleEndpointTimeout;

        @JsonProperty()
        private final int maxBufferCapacity;

        @JsonProperty()
        private final int maxChannelsPerEndpoint;

        @JsonProperty()
        private final int maxRequestsPerChannel;

        @JsonProperty()
        private final int maxConcurrentRequestsPerEndpointOverride;

        @JsonProperty()
        private final Duration receiveHangDetectionTime;

        @JsonProperty()
        private final Duration requestTimeout;

        @JsonProperty()
        private final Duration requestTimerResolution;

        @JsonProperty()
        private final Duration sendHangDetectionTime;

        @JsonProperty()
        private final Duration shutdownTimeout;

        @JsonProperty()
        private final int threadCount;

        @JsonIgnore()
        private final UserAgentContainer userAgent;

        // endregion

        // region Constructors

        @JsonCreator
        private Options() {
            this(ConnectionPolicy.getDefaultPolicy());
        }

        private Options(final Builder builder) {

            this.bufferPageSize = builder.bufferPageSize;
            this.connectionAcquisitionTimeout = builder.connectionAcquisitionTimeout;
            this.connectionEndpointRediscoveryEnabled = builder.connectionEndpointRediscoveryEnabled;
            this.idleChannelTimeout = builder.idleChannelTimeout;
            this.idleChannelTimerResolution = builder.idleChannelTimerResolution;
            this.idleEndpointTimeout = builder.idleEndpointTimeout;
            this.maxBufferCapacity = builder.maxBufferCapacity;
            this.maxChannelsPerEndpoint = builder.maxChannelsPerEndpoint;
            this.maxRequestsPerChannel = builder.maxRequestsPerChannel;
            this.maxConcurrentRequestsPerEndpointOverride = builder.maxConcurrentRequestsPerEndpointOverride;
            this.receiveHangDetectionTime = builder.receiveHangDetectionTime;
            this.requestTimeout = builder.requestTimeout;
            this.requestTimerResolution = builder.requestTimerResolution;
            this.sendHangDetectionTime = builder.sendHangDetectionTime;
            this.shutdownTimeout = builder.shutdownTimeout;
            this.threadCount = builder.threadCount;
            this.userAgent = builder.userAgent;

            this.connectTimeout = builder.connectTimeout == null
                ? builder.requestTimeout
                : builder.connectTimeout;
        }

        private Options(final ConnectionPolicy connectionPolicy) {
            this.bufferPageSize = 8192;
            this.connectionAcquisitionTimeout = Duration.ofSeconds(5L);
            this.connectionEndpointRediscoveryEnabled = connectionPolicy.isTcpConnectionEndpointRediscoveryEnabled();
            this.connectTimeout = connectionPolicy.getConnectTimeout();
            this.idleChannelTimeout = connectionPolicy.getIdleTcpConnectionTimeout();
            this.idleChannelTimerResolution = Duration.ofMillis(100);
            this.idleEndpointTimeout = connectionPolicy.getIdleTcpEndpointTimeout();
            this.maxBufferCapacity = 8192 << 10;
            this.maxChannelsPerEndpoint = connectionPolicy.getMaxConnectionsPerEndpoint();
            this.maxRequestsPerChannel = connectionPolicy.getMaxRequestsPerConnection();

            this.maxConcurrentRequestsPerEndpointOverride = -1;

            this.receiveHangDetectionTime = Duration.ofSeconds(65L);
            this.requestTimeout = connectionPolicy.getRequestTimeout();
            this.requestTimerResolution = Duration.ofMillis(100L);
            this.sendHangDetectionTime = Duration.ofSeconds(10L);
            this.shutdownTimeout = Duration.ofSeconds(15L);
            this.threadCount = 2 * Runtime.getRuntime().availableProcessors();
            this.userAgent = new UserAgentContainer();
        }

        // endregion

        // region Accessors

        public int bufferPageSize() {
            return this.bufferPageSize;
        }

        public Duration connectionAcquisitionTimeout() {
            return this.connectionAcquisitionTimeout;
        }

        public Duration connectTimeout() {
            return this.connectTimeout;
        }

        public Duration idleChannelTimeout() {
            return this.idleChannelTimeout;
        }

        public Duration idleChannelTimerResolution() { return this.idleChannelTimerResolution; }

        public Duration idleEndpointTimeout() {
            return this.idleEndpointTimeout;
        }

        public boolean isConnectionEndpointRediscoveryEnabled() {
            return this.connectionEndpointRediscoveryEnabled;
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

        public int maxConcurrentRequestsPerEndpoint() {
            if (this.maxConcurrentRequestsPerEndpointOverride > 0) {
                return maxConcurrentRequestsPerEndpointOverride;
            }

            return Math.max(
                DEFAULT_MIN_MAX_CONCURRENT_REQUESTS_PER_ENDPOINT,
                this.maxChannelsPerEndpoint * this.maxRequestsPerChannel);
        }

        public Duration receiveHangDetectionTime() {
            return this.receiveHangDetectionTime;
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

        public int threadCount() {
            return this.threadCount;
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

        /**
         * A builder for constructing {@link Options} instances.
         *
         * <h3>Using system properties to set the default {@link Options} used by an {@link Builder}</h3>
         * <p>
         * A default options instance is created when the {@link Builder} class is initialized. This instance specifies
         * the default options used by every {@link Builder} instance. In priority order the default options instance
         * is created from:
         * <ol>
         * <li>The JSON value of system property {@code azure.cosmos.directTcp.defaultOptions}.
         * <p>Example:
         * <pre>{@code -Dazure.cosmos.directTcp.defaultOptions={\"maxChannelsPerEndpoint\":5,\"maxRequestsPerChannel\":30}}</pre>
         * </li>
         * <li>The contents of the JSON file located by system property {@code azure.cosmos.directTcp
         * .defaultOptionsFile}.
         * <p>Example:
         * <pre>{@code -Dazure.cosmos.directTcp.defaultOptionsFile=/path/to/default/options/file}</pre>
         * </li>
         * <li>The contents of JSON resource file {@code azure.cosmos.directTcp.defaultOptions.json}.
         * <p>Specifically, the resource file is read from this stream:
         * <pre>{@code RntbdTransportClient.class.getClassLoader().getResourceAsStream("azure.cosmos.directTcp.defaultOptions.json")}</pre>
         * <p>Example: <pre>{@code {
         *   "bufferPageSize": 8192,
         *   "connectionEndpointRediscoveryEnabled": false,
         *   "connectTimeout": "PT5S",
         *   "idleChannelTimeout": "PT0S",
         *   "idleEndpointTimeout": "PT1H",
         *   "maxBufferCapacity": 8388608,
         *   "maxChannelsPerEndpoint": 130,
         *   "maxRequestsPerChannel": 30,
         *   "maxConcurrentRequestsPerEndpointOverride": -1,
         *   "receiveHangDetectionTime": "PT1M5S",
         *   "requestTimeout": "PT5S",
         *   "requestTimerResolution": "PT100MS",
         *   "sendHangDetectionTime": "PT10S",
         *   "shutdownTimeout": "PT15S",
         *   "threadCount": 16
         * }}</pre>
         * </li>
         * </ol>
         * <p>JSON value errors are logged and then ignored. If none of the above values are available or all available
         * values are in error, the default options instance is created from the private parameterless constructor for
         * {@link Options}.
         */
        @SuppressWarnings("UnusedReturnValue")
        public static class Builder {

            // region Fields

            private static final String DEFAULT_OPTIONS_PROPERTY_NAME = "azure.cosmos.directTcp.defaultOptions";
            private static final Options DEFAULT_OPTIONS;

            static {

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

                        try (InputStream stream = loader.getResourceAsStream(name)) {
                            if (stream != null) {
                                // Attempt to load default options from the JSON resource file "{propertyName}.json"
                                options = RntbdObjectMapper.readValue(stream, Options.class);
                            }
                        } catch (IOException error) {
                            logger.error("failed to load Direct TCP options from resource {} due to ", name, error);
                        }
                    }
                } finally {
                    if (options == null) {
                        logger.info("Using default Direct TCP options: {}", DEFAULT_OPTIONS_PROPERTY_NAME);
                        DEFAULT_OPTIONS = new Options(ConnectionPolicy.getDefaultPolicy());
                    } else {
                        logger.info("Updated default Direct TCP options from system property {}: {}",
                            DEFAULT_OPTIONS_PROPERTY_NAME,
                            options);
                        DEFAULT_OPTIONS = options;
                    }
                }
            }

            private int bufferPageSize;
            private Duration connectionAcquisitionTimeout;
            private boolean connectionEndpointRediscoveryEnabled;
            private Duration connectTimeout;
            private Duration idleChannelTimeout;
            private Duration idleChannelTimerResolution;
            private Duration idleEndpointTimeout;
            private int maxBufferCapacity;
            private int maxChannelsPerEndpoint;
            private int maxRequestsPerChannel;
            private int maxConcurrentRequestsPerEndpointOverride;
            private Duration receiveHangDetectionTime;
            private Duration requestTimeout;
            private Duration requestTimerResolution;
            private Duration sendHangDetectionTime;
            private Duration shutdownTimeout;
            private int threadCount;
            private UserAgentContainer userAgent;

            // endregion

            // region Constructors

            public Builder(ConnectionPolicy connectionPolicy) {

                this.bufferPageSize = DEFAULT_OPTIONS.bufferPageSize;
                this.connectionAcquisitionTimeout = DEFAULT_OPTIONS.connectionAcquisitionTimeout;
                this.connectionEndpointRediscoveryEnabled = connectionPolicy.isTcpConnectionEndpointRediscoveryEnabled();
                this.connectTimeout = connectionPolicy.getConnectTimeout();
                this.idleChannelTimeout = connectionPolicy.getIdleTcpConnectionTimeout();
                this.idleChannelTimerResolution = DEFAULT_OPTIONS.idleChannelTimerResolution;
                this.idleEndpointTimeout = connectionPolicy.getIdleTcpEndpointTimeout();
                this.maxBufferCapacity = DEFAULT_OPTIONS.maxBufferCapacity;
                this.maxChannelsPerEndpoint = connectionPolicy.getMaxConnectionsPerEndpoint();
                this.maxRequestsPerChannel = connectionPolicy.getMaxRequestsPerConnection();

                this.maxConcurrentRequestsPerEndpointOverride =
                    DEFAULT_OPTIONS.maxConcurrentRequestsPerEndpointOverride;

                this.receiveHangDetectionTime = DEFAULT_OPTIONS.receiveHangDetectionTime;
                this.requestTimeout = connectionPolicy.getRequestTimeout();
                this.requestTimerResolution = DEFAULT_OPTIONS.requestTimerResolution;
                this.sendHangDetectionTime = DEFAULT_OPTIONS.sendHangDetectionTime;
                this.shutdownTimeout = DEFAULT_OPTIONS.shutdownTimeout;
                this.threadCount = DEFAULT_OPTIONS.threadCount;
                this.userAgent = DEFAULT_OPTIONS.userAgent;
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

            public Builder connectionAcquisitionTimeout(final Duration value) {
                checkNotNull(value, "expected non-null value");
                this.connectionAcquisitionTimeout = value.compareTo(Duration.ZERO) < 0 ? Duration.ZERO : value;
                return this;
            }

            public Builder connectionEndpointRediscoveryEnabled(final boolean value) {
                this.connectionEndpointRediscoveryEnabled = value;
                return this;
            }

            public Builder connectionTimeout(final Duration value) {
                checkArgument(value == null || value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.connectTimeout = value;
                return this;
            }

            public Builder idleChannelTimeout(final Duration value) {
                checkNotNull(value, "expected non-null value");
                this.idleChannelTimeout = value;
                return this;
            }

            public Builder idleChannelTimerResolution(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) <= 0,
                    "expected positive value, not %s",
                    value);
                this.idleChannelTimerResolution = value;
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

            public Builder maxConcurrentRequestsPerEndpointOverride(final int value) {
                checkArgument(value > 0, "expected positive value, not %s", value);
                this.maxConcurrentRequestsPerEndpointOverride = value;
                return this;
            }

            public Builder receiveHangDetectionTime(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.receiveHangDetectionTime = value;
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

            public Builder threadCount(final int value) {
                checkArgument(value > 0, "expected positive value, not %s", value);
                this.threadCount = value;
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

        private static final long serialVersionUID = 1007663695768825670L;

        JsonSerializer() {
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
