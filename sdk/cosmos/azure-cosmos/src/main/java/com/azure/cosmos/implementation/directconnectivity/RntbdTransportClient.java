// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.CosmosMeterOptions;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdObjectMapper;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdServiceEndpoint;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.faultinjection.RntbdServerErrorInjector;
import com.azure.cosmos.implementation.guava25.base.Strings;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosMetricName;
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
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.CancellationException;
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
    private boolean channelAcquisitionContextEnabled;
    private final GlobalEndpointManager globalEndpointManager;
    private final CosmosClientTelemetryConfig metricConfig;
    private final RntbdServerErrorInjector serverErrorInjector;

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
        final IAddressResolver addressResolver,
        final ClientTelemetry clientTelemetry,
        final GlobalEndpointManager globalEndpointManager) {

        this(
            new Options.Builder(connectionPolicy).userAgent(userAgent).build(),
            configs.getSslContext(),
            addressResolver,
            clientTelemetry,
            globalEndpointManager);
    }

    //  TODO:(kuthapar) This constructor sets the globalEndpointmManager to null, which is not ideal.
    //  Figure out why we need this constructor, and if it can be avoided or can be fixed.
    RntbdTransportClient(final RntbdEndpoint.Provider endpointProvider) {
        this.endpointProvider = endpointProvider;
        this.id = instanceCount.incrementAndGet();
        this.tag = RntbdTransportClient.tag(this.id);
        this.globalEndpointManager = null;
        this.metricConfig = null;
        this.serverErrorInjector = new RntbdServerErrorInjector();
    }

    RntbdTransportClient(
        final Options options,
        final SslContext sslContext,
        final IAddressResolver addressResolver,
        final ClientTelemetry clientTelemetry,
        final GlobalEndpointManager globalEndpointManager) {

        this.serverErrorInjector = new RntbdServerErrorInjector();
        this.endpointProvider = new RntbdServiceEndpoint.Provider(
            this,
            options,
            checkNotNull(sslContext, "expected non-null sslContext"),
            addressResolver,
            clientTelemetry,
            this.serverErrorInjector);

        this.id = instanceCount.incrementAndGet();
        this.tag = RntbdTransportClient.tag(this.id);
        this.channelAcquisitionContextEnabled = options.channelAcquisitionContextEnabled;
        this.globalEndpointManager = globalEndpointManager;
        if (clientTelemetry != null &&
            clientTelemetry.getClientTelemetryConfig() != null) {

            this.metricConfig = clientTelemetry.getClientTelemetryConfig();
        } else {
            this.metricConfig = null;
        }
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

    @Override
    protected GlobalEndpointManager getGlobalEndpointManager() {
        return this.globalEndpointManager;
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

        final RntbdRequestArgs requestArgs = new RntbdRequestArgs(request, addressUri);

        final RntbdEndpoint endpoint = this.endpointProvider.createIfAbsent(request.requestContext.locationEndpointToRoute, addressUri.getURI());
        final RntbdRequestRecord record = endpoint.request(requestArgs);

        final Context reactorContext = Context.of(KEY_ON_ERROR_DROPPED, onErrorDropHookWithReduceLogLevel);

        // Since reactor-core 3.4.23, if the Mono.fromCompletionStage is cancelled, then it will also cancel the internal future
        // If SDK has not sent the request to server, then SDK will not send the request to server
        // If the request has been sent to server, then SDK will discard the response once get from server
        return Mono.fromFuture(record).map(storeResponse -> {
            record.stage(RntbdRequestRecord.Stage.COMPLETED);

            if (request.requestContext.cosmosDiagnostics == null) {
                request.requestContext.cosmosDiagnostics = request.createCosmosDiagnostics();
            }

            RequestTimeline timeline = record.takeTimelineSnapshot();
            storeResponse.setRequestTimeline(timeline);
            storeResponse.setEndpointStatistics(record.serviceEndpointStatistics());
            storeResponse.setChannelStatistics(record.channelStatistics());
            storeResponse.setRntbdResponseLength(record.responseLength());
            storeResponse.setRntbdRequestLength(record.requestLength());
            storeResponse.setRequestPayloadLength(request.getContentLength());
            storeResponse.setFaultInjectionRuleId(
                request.faultInjectionRequestContext.getFaultInjectionRuleId(record.transportRequestId()));
            if (this.channelAcquisitionContextEnabled) {
                storeResponse.setChannelAcquisitionTimeline(record.getChannelAcquisitionTimeline());
            }

            return storeResponse;

        }).onErrorMap(throwable -> {

            record.stage(RntbdRequestRecord.Stage.COMPLETED);

            if (request.requestContext.cosmosDiagnostics == null) {
                request.requestContext.cosmosDiagnostics = request.createCosmosDiagnostics();
            }

            Throwable error = throwable instanceof CompletionException ? throwable.getCause() : throwable;

            if (!(error instanceof CosmosException)) {

                String unexpectedError = RntbdObjectMapper.toJson(error);

                if (!(error instanceof CancellationException)) {
                    reportIssue(logger, endpoint,
                        "request completed with an unexpected {}: \\{\"record\":{},\"error\":{}}",
                        error.getClass(),
                        record,
                        unexpectedError);
                }

                error = new GoneException(
                    lenientFormat("an unexpected %s occurred: %s", unexpectedError),
                    address,
                    error instanceof Exception ? (Exception) error : new RuntimeException(error),
                    HttpConstants.SubStatusCodes.TRANSPORT_GENERATED_410);
            }

            assert error instanceof CosmosException;
            CosmosException cosmosException = (CosmosException) error;
            BridgeInternal.setServiceEndpointStatistics(cosmosException, record.serviceEndpointStatistics());
            ImplementationBridgeHelpers
                .CosmosExceptionHelper
                .getCosmosExceptionAccessor()
                .setRntbdChannelStatistics(cosmosException, record.channelStatistics());
            BridgeInternal.setRntbdRequestLength(cosmosException, record.requestLength());
            BridgeInternal.setRntbdResponseLength(cosmosException, record.responseLength());
            BridgeInternal.setRequestBodyLength(cosmosException, request.getContentLength());
            BridgeInternal.setRequestTimeline(cosmosException, record.takeTimelineSnapshot());
            BridgeInternal.setSendingRequestStarted(cosmosException, record.hasSendingRequestStarted());
            ImplementationBridgeHelpers
                .CosmosExceptionHelper
                .getCosmosExceptionAccessor()
                .setFaultInjectionRuleId(
                    cosmosException,
                    request.faultInjectionRequestContext.getFaultInjectionRuleId(record.transportRequestId()));
            if (this.channelAcquisitionContextEnabled) {
                BridgeInternal.setChannelAcquisitionTimeline(cosmosException, record.getChannelAcquisitionTimeline());
            }

            return cosmosException;
        }).doFinally(signalType -> {
            if (signalType != SignalType.CANCEL) {
                return;
            }

            // Since reactor-core 3.4.23, if the Mono.fromCompletionStage is cancelled, then it will also cancel the internal future
            // But the stated behavior may change in later versions (https://github.com/reactor/reactor-core/issues/3235).
            // In order to keep consistent behavior, we internally will always cancel the future.
            record.cancel(true);

        }).contextWrite(reactorContext);
    }

    @Override
    public Mono<OpenConnectionResponse> openConnection(Uri addressUri, RxDocumentServiceRequest openConnectionRequest) {
        checkNotNull(openConnectionRequest, "Argument 'openConnectionRequest' should not be null");
        checkNotNull(addressUri, "Argument 'addressUri' should not be null");

        this.throwIfClosed();

        final RntbdRequestArgs requestArgs = new RntbdRequestArgs(openConnectionRequest, addressUri);
        final RntbdEndpoint endpoint =
            this.endpointProvider.createIfAbsent(
                openConnectionRequest.requestContext.locationEndpointToRoute,
                addressUri.getURI());

        return Mono.fromFuture(endpoint.openConnection(requestArgs));
    }

    public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider) {
        injectorProvider.registerConnectionErrorInjector(this.endpointProvider);
        if (this.serverErrorInjector != null) {
            this.serverErrorInjector
                .registerServerErrorInjector(injectorProvider.getRntbdServerErrorInjector());
        }
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

    public EnumSet<MetricCategory> getMetricCategories() {
        return this.metricConfig != null ?
            ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor()
                .getMetricCategories(this.metricConfig) : MetricCategory.DEFAULT_CATEGORIES;
    }

    public CosmosMeterOptions getMeterOptions(CosmosMetricName name) {
        return this.metricConfig != null ?
            ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor()
                .getMeterOptions(this.metricConfig, name) :
            ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor()
                .createDisabledMeterOptions(name);
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
        private final Duration tcpNetworkRequestTimeout;

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

        @JsonProperty()
        private final boolean channelAcquisitionContextEnabled;

        @JsonProperty()
        private final int ioThreadPriority;

        @JsonProperty()
        private final int tcpKeepIntvl;

        @JsonProperty()
        private final int tcpKeepIdle;

        @JsonProperty()
        private final boolean preferTcpNative;

        @JsonProperty()
        private final Duration sslHandshakeTimeoutMinDuration;

        /**
         * Used during Rntbd health check flow.
         * This property will be used to indicate whether timeout related stats will be used.
         *
         * By default, it is enabled.
         */
        @JsonProperty()
        private final boolean timeoutDetectionEnabled;


        /**
         * Used during Rntbd health check flow.
         * Transit timeout can be a normal symptom under high CPU load.
         * When request timeout due to high CPU, close the existing the connection and re-establish a new one will not help the issue but rather make it worse.
         * This property indicate when the CPU load is beyond the threshold, the timeout detection flow will be disabled.
         *
         * By default, it is 90.0.
         */
        @JsonProperty()
        private final double timeoutDetectionDisableCPUThreshold;

        /**
         * Used during Rntbd health check flow.
         * When transitTimeoutHealthCheckEnabled is enabled,
         * the channel will be closed if all requests are failed due to transit timeout within the time limit.
         */
        @JsonProperty()
        private final Duration timeoutDetectionTimeLimit;

        /**
         * Used during Rntbd health check flow.
         * Will be used together with timeoutHighFrequencyTimeLimitInNanos. If both conditions are met, the channel will be closed.
         * This will control how fast the channel will be closed when high number consecutive timeout being observed.
         */
        @JsonProperty()
        private final int timeoutDetectionHighFrequencyThreshold;

        /**
         * Used during Rntbd health check flow.
         * Will be used together with timeoutHighFrequencyThreshold. If both conditions are met, the channel will be closed.
         * This will control how fast the channel will be closed when high number consecutive timeout being observed.
         */
        @JsonProperty()
        private final Duration timeoutDetectionHighFrequencyTimeLimit;

        /**
         * Used during Rntbd health check flow.
         * Will be used together with timeoutOnWriteTimeLimitInNanos. If both conditions are met, the channel will be closed.
         * This will control how fast the channel will be closed when write operation timeout being observed.
         */
        @JsonProperty()
        private final int timeoutDetectionOnWriteThreshold;

        /**
         * Used during Rntbd health check flow.
         * Will be used together with timeoutOnWriteThreshold. If both conditions are met, the channel will be closed.
         * This will control how fast the channel will be closed when write operation timeout being observed.
         */
        @JsonProperty()
        private final Duration timeoutDetectionOnWriteTimeLimit;


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
            this.tcpNetworkRequestTimeout = builder.tcpNetworkRequestTimeout;
            this.requestTimerResolution = builder.requestTimerResolution;
            this.sendHangDetectionTime = builder.sendHangDetectionTime;
            this.shutdownTimeout = builder.shutdownTimeout;
            this.threadCount = builder.threadCount;
            this.userAgent = builder.userAgent;
            this.channelAcquisitionContextEnabled = builder.channelAcquisitionContextEnabled;
            this.ioThreadPriority = builder.ioThreadPriority;
            this.tcpKeepIntvl = builder.tcpKeepIntvl;
            this.tcpKeepIdle = builder.tcpKeepIdle;
            this.preferTcpNative = builder.preferTcpNative;
            this.sslHandshakeTimeoutMinDuration = builder.sslHandshakeTimeoutMinDuration;
            this.timeoutDetectionEnabled = builder.timeoutDetectionEnabled;
            this.timeoutDetectionDisableCPUThreshold = builder.timeoutDetectionDisableCPUThreshold;
            this.timeoutDetectionTimeLimit = builder.timeoutDetectionTimeLimit;
            this.timeoutDetectionHighFrequencyThreshold = builder.timeoutDetectionHighFrequencyThreshold;
            this.timeoutDetectionHighFrequencyTimeLimit = builder.timeoutDetectionHighFrequencyTimeLimit;
            this.timeoutDetectionOnWriteThreshold = builder.timeoutDetectionOnWriteThreshold;
            this.timeoutDetectionOnWriteTimeLimit = builder.timeoutDetectionOnWriteTimeLimit;

            this.connectTimeout = builder.connectTimeout == null
                ? builder.tcpNetworkRequestTimeout
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
            this.tcpNetworkRequestTimeout = connectionPolicy.getTcpNetworkRequestTimeout();
            this.requestTimerResolution = Duration.ofMillis(100L);
            this.sendHangDetectionTime = Duration.ofSeconds(10L);
            this.shutdownTimeout = Duration.ofSeconds(15L);
            this.threadCount = connectionPolicy.getIoThreadCountPerCoreFactor() *
                Runtime.getRuntime().availableProcessors();
            this.userAgent = new UserAgentContainer();
            this.channelAcquisitionContextEnabled = false;
            this.ioThreadPriority = connectionPolicy.getIoThreadPriority();
            this.tcpKeepIntvl = 1; // Configuration for EpollChannelOption.TCP_KEEPINTVL
            this.tcpKeepIdle = 1; // Configuration for EpollChannelOption.TCP_KEEPIDLE
            this.sslHandshakeTimeoutMinDuration = Duration.ofSeconds(5);
            this.timeoutDetectionEnabled = connectionPolicy.isTcpHealthCheckTimeoutDetectionEnabled();
            this.timeoutDetectionDisableCPUThreshold = 90.0;
            this.timeoutDetectionTimeLimit = Duration.ofSeconds(60L);
            this.timeoutDetectionHighFrequencyThreshold = 3;
            this.timeoutDetectionHighFrequencyTimeLimit = Duration.ofSeconds(10L);
            this.timeoutDetectionOnWriteThreshold = 1;
            this.timeoutDetectionOnWriteTimeLimit = Duration.ofSeconds(6L);
            this.preferTcpNative = true;
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

        public Duration tcpNetworkRequestTimeout() {
            return this.tcpNetworkRequestTimeout;
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

        public boolean isChannelAcquisitionContextEnabled() { return this.channelAcquisitionContextEnabled; }

        public int ioThreadPriority() {
            checkArgument(
                this.ioThreadPriority >= Thread.MIN_PRIORITY && this.ioThreadPriority <= Thread.MAX_PRIORITY,
                "Expect ioThread priority between [%s, %s]",
                Thread.MIN_PRIORITY,
                Thread.MAX_PRIORITY);

            return this.ioThreadPriority;
        }

        public int tcpKeepIntvl() { return this.tcpKeepIntvl; }

        public int tcpKeepIdle() { return this.tcpKeepIdle; }

        public boolean preferTcpNative() { return this.preferTcpNative; }

        public long sslHandshakeTimeoutInMillis() {
            return Math.max(this.sslHandshakeTimeoutMinDuration.toMillis(), this.connectTimeout.toMillis());
        }

        public boolean timeoutDetectionEnabled() {
            return this.timeoutDetectionEnabled;
        }

        public double timeoutDetectionDisableCPUThreshold() {
            return this.timeoutDetectionDisableCPUThreshold;
        }

        public Duration timeoutDetectionTimeLimit() {
            return this.timeoutDetectionTimeLimit;
        }

        public int timeoutDetectionHighFrequencyThreshold() {
            return this.timeoutDetectionHighFrequencyThreshold;
        }

        public Duration timeoutDetectionHighFrequencyTimeLimit() {
            return this.timeoutDetectionHighFrequencyTimeLimit;
        }

        public int timeoutDetectionOnWriteThreshold() {
            return this.timeoutDetectionOnWriteThreshold;
        }

        public Duration timeoutDetectionOnWriteTimeLimit() {
            return this.timeoutDetectionOnWriteTimeLimit;
        }

        // endregion

        // region Methods

        @Override
        public String toString() {
            return RntbdObjectMapper.toJson(this);
        }

        public String toDiagnosticsString() {
            return lenientFormat("(cto:%s, nrto:%s, icto:%s, ieto:%s, mcpe:%s, mrpc:%s, cer:%s)",
                connectTimeout,
                tcpNetworkRequestTimeout,
                idleChannelTimeout,
                idleEndpointTimeout,
                maxChannelsPerEndpoint,
                maxRequestsPerChannel,
                connectionEndpointRediscoveryEnabled);
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
         *   "threadCount": 16,
         *   "timeoutDetectionEnabled": true,
         *   "timeoutDetectionDisableCPUThreshold": 90.0,
         *   "timeoutDetectionTimeLimit": "PT60S",
         *   "timeoutDetectionHighFrequencyThreshold": 3,
         *   "timeoutDetectionHighFrequencyTimeLimit": "PT10S",
         *   "timeoutDetectionOnWriteThreshold": 1,
         *   "timeoutDetectionOnWriteTimeLimit": "PT6s"
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
            private Duration tcpNetworkRequestTimeout;
            private Duration requestTimerResolution;
            private Duration sendHangDetectionTime;
            private Duration shutdownTimeout;
            private int threadCount;
            private UserAgentContainer userAgent;
            private boolean channelAcquisitionContextEnabled;
            private int ioThreadPriority;
            private int tcpKeepIntvl;
            private int tcpKeepIdle;
            private boolean preferTcpNative;
            private Duration sslHandshakeTimeoutMinDuration;
            private boolean timeoutDetectionEnabled;
            private double timeoutDetectionDisableCPUThreshold;
            private Duration timeoutDetectionTimeLimit;
            private int timeoutDetectionHighFrequencyThreshold;
            private Duration timeoutDetectionHighFrequencyTimeLimit;
            private int timeoutDetectionOnWriteThreshold;
            private Duration timeoutDetectionOnWriteTimeLimit;


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
                this.tcpNetworkRequestTimeout = connectionPolicy.getTcpNetworkRequestTimeout();
                this.requestTimerResolution = DEFAULT_OPTIONS.requestTimerResolution;
                this.sendHangDetectionTime = DEFAULT_OPTIONS.sendHangDetectionTime;
                this.shutdownTimeout = DEFAULT_OPTIONS.shutdownTimeout;
                this.threadCount = DEFAULT_OPTIONS.threadCount;
                this.userAgent = DEFAULT_OPTIONS.userAgent;
                this.channelAcquisitionContextEnabled = DEFAULT_OPTIONS.channelAcquisitionContextEnabled;
                this.ioThreadPriority = DEFAULT_OPTIONS.ioThreadPriority;
                this.tcpKeepIntvl = DEFAULT_OPTIONS.tcpKeepIntvl;
                this.tcpKeepIdle = DEFAULT_OPTIONS.tcpKeepIdle;
                this.preferTcpNative = DEFAULT_OPTIONS.preferTcpNative;
                this.sslHandshakeTimeoutMinDuration = DEFAULT_OPTIONS.sslHandshakeTimeoutMinDuration;
                this.timeoutDetectionEnabled = DEFAULT_OPTIONS.timeoutDetectionEnabled;
                this.timeoutDetectionDisableCPUThreshold = DEFAULT_OPTIONS.timeoutDetectionDisableCPUThreshold;
                this.timeoutDetectionTimeLimit = DEFAULT_OPTIONS.timeoutDetectionTimeLimit;
                this.timeoutDetectionHighFrequencyThreshold = DEFAULT_OPTIONS.timeoutDetectionHighFrequencyThreshold;
                this.timeoutDetectionHighFrequencyTimeLimit = DEFAULT_OPTIONS.timeoutDetectionHighFrequencyTimeLimit;
                this.timeoutDetectionOnWriteThreshold = DEFAULT_OPTIONS.timeoutDetectionOnWriteThreshold;
                this.timeoutDetectionOnWriteTimeLimit = DEFAULT_OPTIONS.timeoutDetectionOnWriteTimeLimit;
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

            public Builder tcpNetworkRequestTimeout(final Duration value) {
                checkArgument(value != null && value.compareTo(Duration.ZERO) > 0,
                    "expected positive value, not %s",
                    value);
                this.tcpNetworkRequestTimeout = value;
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
