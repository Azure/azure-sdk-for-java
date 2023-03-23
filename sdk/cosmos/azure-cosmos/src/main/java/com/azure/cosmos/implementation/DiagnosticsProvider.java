// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class DiagnosticsProvider {
    private static final ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor clientTelemetryConfigAccessor =
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.getCosmosClientTelemetryConfigAccessor();
    private static final ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.CosmosDiagnosticsContextAccessor ctxAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor();
    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor clientAccessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();
    private static final
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
            ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticsProvider.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String COSMOS_CALL_DEPTH = "cosmosCallDepth";
    public static final String COSMOS_CALL_DEPTH_VAL = "nested";
    public static final int ERROR_CODE = 0;
    public static final String RESOURCE_PROVIDER_NAME = "Microsoft.DocumentDB";
    public final static String DB_TYPE_VALUE = "Cosmos";
    public final static String DB_TYPE = "db.type";
    public final static String LEGACY_DB_URL = "db.url";
    public static final String LEGACY_DB_STATEMENT = "db.statement";
    public final static String LEGACY_DB_INSTANCE = "db.instance";
    private final static Duration FEED_RESPONSE_CONSUMER_LATENCY_THRESHOLD = Duration.ofMillis(5);

    private static final String REACTOR_TRACING_CONTEXT_KEY = "tracing-context";
    private static final String COSMOS_DIAGNOSTICS_CONTEXT_KEY = "azure-cosmos-context";
    private static final Object DUMMY_VALUE = new Object();
    private final Mono<Object> propagatingMono;
    private final Flux<Object> propagatingFlux;
    private final ArrayList<CosmosDiagnosticsHandler> diagnosticHandlers;
    private final Tracer tracer;
    private final CosmosTracer cosmosTracer;

    public DiagnosticsProvider(
        CosmosClientTelemetryConfig clientTelemetryConfig) {

        checkNotNull(clientTelemetryConfig, "Argument 'clientTelemetryConfig' must not be null.");

        this.diagnosticHandlers = new ArrayList<>(
            clientTelemetryConfigAccessor.getDiagnosticHandlers(clientTelemetryConfig));
        Tracer tracerCandidate = clientTelemetryConfigAccessor.getOrCreateTracer(clientTelemetryConfig);

        if (tracerCandidate.isEnabled()) {
            this.tracer = tracerCandidate;
        } else {
            if (!this.diagnosticHandlers.isEmpty()) {
                this.tracer = EnabledNoOpTracer.INSTANCE;
            } else {
                this.tracer = tracerCandidate;
            }
        }

        if (this.tracer.isEnabled()) {
            if (clientTelemetryConfigAccessor.isLegacyTracingEnabled(clientTelemetryConfig)) {
                this.cosmosTracer = new LegacyCosmosTracer(this.tracer);
            } else {
                this.cosmosTracer = new OpenTelemetryCosmosTracer(
                    this.tracer,
                    clientTelemetryConfig);
            }
        } else {
            this.cosmosTracer = null;
        }

        this.propagatingMono = new PropagatingMono();
        this.propagatingFlux = new PropagatingFlux();
    }

    public boolean isEnabled() {
        return this.tracer.isEnabled();
    }

    public boolean isRealTracer() {
        return this.tracer.isEnabled() && this.tracer != EnabledNoOpTracer.INSTANCE;
    }

    /**
     * Gets {@link Context} from Reactor {@link ContextView}.
     *
     * @param reactorContext Reactor context instance.
     * @return {@link Context} from reactor context or null if not present.
     */
    public static Context getContextFromReactorOrNull(ContextView reactorContext) {
        Object context = reactorContext.getOrDefault(REACTOR_TRACING_CONTEXT_KEY, null);

        if (context instanceof Context) {
            return (Context) context;
        }

        return null;
    }

    private static CosmosDiagnosticsContext getCosmosDiagnosticsContextFromTraceContextOrNull(Context traceContext) {
        Object cosmosCtx = traceContext.getData(COSMOS_DIAGNOSTICS_CONTEXT_KEY).orElse(null);

        if (cosmosCtx instanceof CosmosDiagnosticsContext) {
            return (CosmosDiagnosticsContext) cosmosCtx;
        }

        return null;
    }

    public static CosmosDiagnosticsContext getCosmosDiagnosticsContextFromTraceContextOrThrow(Context traceContext) {
        Object cosmosCtx = traceContext.getData(COSMOS_DIAGNOSTICS_CONTEXT_KEY).orElse(null);

        if (cosmosCtx instanceof CosmosDiagnosticsContext) {
            return (CosmosDiagnosticsContext) cosmosCtx;
        }

        throw new IllegalStateException("CosmosDiagnosticsContext not present.");
    }

    /**
     * Stores {@link Context} in Reactor {@link reactor.util.context.Context}.
     *
     * @param traceContext {@link Context} context with trace context to store.
     * @return {@link reactor.util.context.Context} Reactor context with trace context.
     */
    public static reactor.util.context.Context setContextInReactor(Context traceContext) {
        return reactor.util.context.Context.of(REACTOR_TRACING_CONTEXT_KEY, traceContext);
    }

    /**
     * For each tracer plugged into the SDK a new tracing span is created.
     * <p>
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    public Context startSpan(
        String spanName,
        CosmosDiagnosticsContext cosmosCtx,
        Context context) {

        checkNotNull(spanName, "Argument 'spanName' must not be null.");
        checkNotNull(cosmosCtx, "Argument 'cosmosCtx' must not be null.");

        ctxAccessor.startOperation(cosmosCtx);
        Context local = Objects
            .requireNonNull(context, "'context' cannot be null.")
            .addData(COSMOS_DIAGNOSTICS_CONTEXT_KEY, cosmosCtx);

        if (this.cosmosTracer == null) {
            return local;
        }

        return this.cosmosTracer.startSpan(spanName, cosmosCtx, local);
    }

    /**
     * Given a context containing the current tracing span the span is marked completed with status info from
     * {@link Signal}.  For each tracer plugged into the SDK the current tracing span is marked as completed.
     *
     * @param signal  The signal indicates the status and contains the metadata we need to end the tracing span.
     */

    public <T> void endSpan(
        Signal<T> signal,
        int statusCode,
        Integer actualItemCount,
        Double requestCharge,
        CosmosDiagnostics diagnostics
    ) {
        // called in PagedFlux - needs to be exception less - otherwise will result in hanging Flux.
        try {
            this.endSpanCore(signal, statusCode, actualItemCount, requestCharge, diagnostics);
        } catch (Throwable error) {
            LOGGER.error("Unexpected exception in DiagnosticsProvider.endSpan. ", error);
            System.exit(9901);
        }
    }

    private <T> void endSpanCore(
        Signal<T> signal,
        int statusCode,
        Integer actualItemCount,
        Double requestCharge,
        CosmosDiagnostics diagnostics
    ) {
        Objects.requireNonNull(signal, "'signal' cannot be null.");

        Context context = getContextFromReactorOrNull(signal.getContextView());
        if (context == null) {
            return;
        }

        switch (signal.getType()) {
            case ON_COMPLETE:
            case ON_NEXT:
                end(statusCode, 0, actualItemCount, requestCharge, diagnostics,null, context);
                break;
            case ON_ERROR:
                Throwable throwable = null;
                int subStatusCode = 0;
                Double effectiveRequestCharge = requestCharge;
                CosmosDiagnostics effectiveDiagnostics = diagnostics;
                if (signal.hasError()) {
                    // The last status available is on error, this contains the thrown error.
                    throwable = signal.getThrowable();

                    if (throwable instanceof CosmosException) {
                        CosmosException exception = (CosmosException) throwable;
                        statusCode = exception.getStatusCode();
                        subStatusCode = exception.getSubStatusCode();
                        if (effectiveRequestCharge != null) {
                            effectiveRequestCharge += exception.getRequestCharge();
                        } else {
                            effectiveRequestCharge = exception.getRequestCharge();
                        }
                        effectiveDiagnostics = exception.getDiagnostics();
                    }
                }
                end(statusCode, subStatusCode, actualItemCount, effectiveRequestCharge, effectiveDiagnostics, throwable, context);
                break;
            default:
                // ON_SUBSCRIBE isn't the right state to end span
                break;
        }
    }

    public void endSpan(Context context, Throwable throwable) {
        // called in PagedFlux - needs to be exception less - otherwise will result in hanging Flux.
        try {
            int statusCode = DiagnosticsProvider.ERROR_CODE;
            int subStatusCode = 0;
            Double effectiveRequestCharge = null;
            CosmosDiagnostics effectiveDiagnostics = null;

            if (throwable instanceof CosmosException) {
                CosmosException exception = (CosmosException) throwable;
                statusCode = exception.getStatusCode();
                subStatusCode = exception.getSubStatusCode();
                effectiveRequestCharge = exception.getRequestCharge();
                effectiveDiagnostics = exception.getDiagnostics();
            }
            end(statusCode, subStatusCode, null, effectiveRequestCharge, effectiveDiagnostics, throwable, context);
        } catch (Throwable error) {
            LOGGER.error("Unexpected exception in DiagnosticsProvider.endSpan. ", error);
            System.exit(9905);
        }
    }

    public void endSpan(Context context) {
        // called in PagedFlux - needs to be exception less - otherwise will result in hanging Flux.
        try {
            end(200, 0, null, null, null,null, context);
        } catch (Throwable error) {
            LOGGER.error("Unexpected exception in DiagnosticsProvider.endSpan. ", error);
            System.exit(9904);
        }
    }

    public void recordPage(
        Context context,
        CosmosDiagnostics diagnostics,
        Integer actualItemCount,
        Double requestCharge
    ) {
        // called in PagedFlux - needs to be exception less - otherwise will result in hanging Flux.
        try {
            this.recordPageCore(context, diagnostics, actualItemCount, requestCharge);
        } catch (Throwable error) {
            LOGGER.error("Unexpected exception in DiagnosticsProvider.recordPage. ", error);
            System.exit(9902);
        }
    }

    private void recordPageCore(
        Context context,
        CosmosDiagnostics diagnostics,
        Integer actualItemCount,
        Double requestCharge
    ) {
        if (context == null) {
            return;
        }

        CosmosDiagnosticsContext cosmosCtx = getCosmosDiagnosticsContextFromTraceContextOrThrow(context);
        ctxAccessor.recordOperation(
            cosmosCtx, 200, 0, actualItemCount, requestCharge, diagnostics, null);
    }

    public <T> void recordFeedResponseConsumerLatency(
        Signal<T> signal,
        Duration feedResponseConsumerLatency
    ) {
        // called in PagedFlux - needs to be exception less - otherwise will result in hanging Flux.
        try {
            Objects.requireNonNull(signal, "'signal' cannot be null.");
            Objects.requireNonNull(feedResponseConsumerLatency, "'feedResponseConsumerLatency' cannot be null.");
            checkArgument(
                signal.getType() == SignalType.ON_COMPLETE || signal.getType() == SignalType.ON_ERROR,
                "recordFeedResponseConsumerLatency should only be used for terminal signal");

            Context context = getContextFromReactorOrNull(signal.getContextView());
            if (context == null) {
                return;
            }

            this.recordFeedResponseConsumerLatencyCore(
                context, getCosmosDiagnosticsContextFromTraceContextOrNull(context), feedResponseConsumerLatency);
        } catch (Throwable error) {
            LOGGER.error("Unexpected exception in DiagnosticsProvider.recordFeedResponseConsumerLatency. ", error);
            System.exit(9902);
        }
    }

    private void recordFeedResponseConsumerLatencyCore(
        Context context,
        CosmosDiagnosticsContext cosmosCtx,
        Duration feedResponseConsumerLatency
    ) {
        Objects.requireNonNull(cosmosCtx, "'cosmosCtx' cannot be null.");
        Objects.requireNonNull(feedResponseConsumerLatency, "'feedResponseConsumerLatency' cannot be null.");

        if (feedResponseConsumerLatency.compareTo(FEED_RESPONSE_CONSUMER_LATENCY_THRESHOLD) <= 0 &&
            !LOGGER.isDebugEnabled()) {

            return;
        }

        if (feedResponseConsumerLatency.compareTo(FEED_RESPONSE_CONSUMER_LATENCY_THRESHOLD) <= 0 &&
            LOGGER.isDebugEnabled()) {

            LOGGER.debug(
                "Total duration spent in FeedResponseConsumer is {} but does not exceed threshold of {}, Diagnostics: {}",
                feedResponseConsumerLatency,
                FEED_RESPONSE_CONSUMER_LATENCY_THRESHOLD,
                cosmosCtx);

            return;
        }

        if (context != null && this.isRealTracer()) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("Diagnostics", cosmosCtx.toString());

            this.tracer.addEvent("SlowFeedResponseConsumer", attributes, OffsetDateTime.now(), context);
            return;
        }

        LOGGER.warn(
            "Total duration spent in FeedResponseConsumer is {} and exceeds threshold of {}, Diagnostics: {}",
            feedResponseConsumerLatency,
            FEED_RESPONSE_CONSUMER_LATENCY_THRESHOLD,
            cosmosCtx);
    }

    private void handleDiagnostics(Context context, CosmosDiagnosticsContext cosmosCtx) {
        // @TODO - investigate whether we should push the handling of diagnostics out of the hot path
        // currently diagnostics are handled by the same thread on the hot path - which is intentional
        // because any async queueing/throttling/sampling can best be done by diagnostic handlers
        // but there is some risk given that diagnostic handlers are custom code of course
        if (this.diagnosticHandlers != null && this.diagnosticHandlers.size() > 0) {
            for (CosmosDiagnosticsHandler handler: this.diagnosticHandlers) {
                handler.handleDiagnostics(context, cosmosCtx);
            }
        }
    }

    public <T extends CosmosResponse<?>> Mono<T> traceEnabledCosmosResponsePublisher(
        Mono<T> resultPublisher,
        Context context,
        String spanName,
        String databaseId,
        String containerId,
        CosmosAsyncClient client,
        ConsistencyLevel consistencyLevel,
        OperationType operationType,
        ResourceType resourceType,
        CosmosDiagnosticsThresholds thresholds) {

        checkNotNull(client, "Argument 'client' must not be null.");

        String accountName = clientAccessor.getAccountTagValue(client);

        return publisherWithDiagnostics(
            resultPublisher,
            context,
            spanName,
            containerId,
            databaseId,
            accountName,
            client,
            consistencyLevel,
            operationType,
            resourceType,
            null,
            (r) -> r.getStatusCode(),
            (r) -> null,
            (r) -> r.getRequestCharge(),
            (r) -> r.getDiagnostics(),
            thresholds);
    }

    public <T extends CosmosBatchResponse> Mono<T> traceEnabledBatchResponsePublisher(
        Mono<T> resultPublisher,
        Context context,
        String spanName,
        String databaseId,
        String containerId,
        CosmosAsyncClient client,
        ConsistencyLevel consistencyLevel,
        OperationType operationType,
        ResourceType resourceType,
        CosmosDiagnosticsThresholds thresholds) {

        checkNotNull(client, "Argument 'client' must not be null.");

        String accountName = clientAccessor.getAccountTagValue(client);

        return publisherWithDiagnostics(
            resultPublisher,
            context,
            spanName,
            containerId,
            databaseId,
            accountName,
            client,
            consistencyLevel,
            operationType,
            resourceType,
            null,
            CosmosBatchResponse::getStatusCode,
            (r) -> null,
            CosmosBatchResponse::getRequestCharge,
            CosmosBatchResponse::getDiagnostics,
            thresholds);
    }

    public <T> Mono<CosmosItemResponse<T>> traceEnabledCosmosItemResponsePublisher(
       Mono<CosmosItemResponse<T>> resultPublisher,
       Context context,
       String spanName,
       String containerId,
       String databaseId,
       CosmosAsyncClient client,
       ConsistencyLevel consistencyLevel,
       OperationType operationType,
       ResourceType resourceType,
       CosmosDiagnosticsThresholds thresholds) {

        checkNotNull(client, "Argument 'client' must not be null.");

        String accountName = clientAccessor.getAccountTagValue(client);

        return publisherWithDiagnostics(
            resultPublisher,
            context,
            spanName,
            containerId,
            databaseId,
            accountName,
            client,
            consistencyLevel,
            operationType,
            resourceType,
            null,
            CosmosItemResponse::getStatusCode,
            (r) -> null,
            CosmosItemResponse::getRequestCharge,
            CosmosItemResponse::getDiagnostics,
            thresholds);
    }

    /**
     * Runs given {@code Flux<T>} publisher in the scope of trace context passed in using
     * {@link DiagnosticsProvider#setContextInReactor(Context)} in {@code contextWrite}
     * Populates active trace context on Reactor's hot path. Reactor's instrumentation for OpenTelemetry
     * (or other hypothetical solution) will take care of the cold path.
     *
     * @param publisher publisher to run.
     * @return wrapped publisher.
     */
    public <T> Flux<T> runUnderSpanInContext(Flux<T> publisher) {
        return propagatingFlux
            .flatMap(ignored -> publisher);
    }

    private <T> Mono<T> diagnosticsEnabledPublisher(
      CosmosDiagnosticsContext cosmosCtx,
      Mono<T> resultPublisher,
      Context context,
      String spanName,
      Function<T, Integer> statusCodeFunc,
      Function<T, Integer> actualItemCountFunc,
      Function<T, Double> requestChargeFunc,
      Function<T, CosmosDiagnostics> diagnosticsFunc
    ) {

        if (!isEnabled()) {
            return resultPublisher;
        }

        Optional<Object> callDepth = context.getData(COSMOS_CALL_DEPTH);
        final boolean isNestedCall = callDepth.isPresent();
        if (isNestedCall) {
            return resultPublisher;
        }

        // propagatingMono ensures active span is propagated to the `resultPublisher`
        // subscription and hot path. OpenTelemetry reactor's instrumentation will
        // propagate it on the cold path.
        return propagatingMono
            .flatMap(ignored -> resultPublisher)
            .doOnEach(signal -> {
                switch (signal.getType()) {
                    case ON_NEXT:
                        T response = signal.get();

                        this.endSpan(
                            signal,
                            statusCodeFunc.apply(response),
                            actualItemCountFunc.apply(response),
                            requestChargeFunc.apply(response),
                            diagnosticsFunc.apply(response));
                        break;
                    case ON_ERROR:
                        // not adding diagnostics on trace event for exception as this information is already there as
                        // part of exception message
                        this.endSpan(
                            signal,
                            ERROR_CODE,
                            null,
                            null,
                            null);
                        break;
                    default:
                        break;
                }})
            .contextWrite(setContextInReactor(this.startSpan(spanName, cosmosCtx, context)));
    }

    private <T> Mono<T> publisherWithDiagnostics(Mono<T> resultPublisher,
                                                     Context context,
                                                     String spanName,
                                                     String containerId,
                                                     String databaseId,
                                                     String accountName,
                                                     CosmosAsyncClient client,
                                                     ConsistencyLevel consistencyLevel,
                                                     OperationType operationType,
                                                     ResourceType resourceType,
                                                     Integer maxItemCount,
                                                     Function<T, Integer> statusCodeFunc,
                                                     Function<T, Integer> actualItemCountFunc,
                                                     Function<T, Double> requestChargeFunc,
                                                     Function<T, CosmosDiagnostics> diagnosticFunc,
                                                     CosmosDiagnosticsThresholds thresholds) {

        CosmosDiagnosticsContext cosmosCtx = ctxAccessor.create(
            spanName,
            accountName,
            BridgeInternal.getServiceEndpoint(client),
            databaseId,
            containerId,
            resourceType,
            operationType,
            null,
            clientAccessor.getEffectiveConsistencyLevel(client, operationType, consistencyLevel),
            maxItemCount,
            thresholds);

        return diagnosticsEnabledPublisher(
            cosmosCtx,
            resultPublisher,
            context,
            spanName,
            statusCodeFunc,
            actualItemCountFunc,
            requestChargeFunc,
            diagnosticFunc);
    }

    private void end(
        int statusCode,
        int subStatusCode,
        Integer actualItemCount,
        Double requestCharge,
        CosmosDiagnostics diagnostics,
        Throwable throwable,
        Context context) {

        CosmosDiagnosticsContext cosmosCtx = getCosmosDiagnosticsContextFromTraceContextOrThrow(context);
        ctxAccessor.endOperation(
            cosmosCtx,
            statusCode,
            subStatusCode,
            actualItemCount,
            requestCharge,
            diagnostics,
            throwable);

        this.handleDiagnostics(context, cosmosCtx);

        if (this.cosmosTracer != null) {
            this.cosmosTracer.endSpan(cosmosCtx, context);
        }
    }

    private static void subscribe(Tracer tracer, CoreSubscriber<? super Object> actual) {
        Context context = getContextFromReactorOrNull(actual.currentContext());
        if (context != null) {
            AutoCloseable scope = tracer.makeSpanCurrent(context);
            try {
                actual.onSubscribe(Operators.scalarSubscription(actual, DUMMY_VALUE));
            } finally {
                try {
                    scope.close();
                } catch (Exception e) {
                    // can't happen
                    assert false : "Exception should never occur here.";
                }
            }
        } else {
            actual.onSubscribe(Operators.scalarSubscription(actual, DUMMY_VALUE));
        }
    }

    /**
     * Helper class allowing running Mono subscription (and anything on the hot path)
     * in scope of trace context. This enables OpenTelemetry auto-collection
     * to pick it up and correlate lower levels of instrumentation and logs
     * to logical Cosmos spans.
     * <p>
     * OpenTelemetry reactor auto-instrumentation will take care of the cold path.
     */
    private final class PropagatingMono extends Mono<Object> {
        @Override
        public void subscribe(CoreSubscriber<? super Object> actual) {
            DiagnosticsProvider.subscribe(tracer, actual);
        }
    }

    /**
     * Helper class allowing running Flux subscription (and anything on the hot path)
     * in scope of trace context. This enables OpenTelemetry auto-collection
     * to pick it up and correlate lower levels of instrumentation and logs
     * to logical Cosmos spans.
     * <p>
     * OpenTelemetry reactor auto-instrumentation will take care of the cold path.
     */
    private final class PropagatingFlux extends Flux<Object> {
        @Override
        public void subscribe(CoreSubscriber<? super Object> actual) {
            DiagnosticsProvider.subscribe(tracer, actual);
        }
    }

    private interface CosmosTracer {
        Context startSpan(String spanName, CosmosDiagnosticsContext cosmosCtx, Context context);
        void endSpan(CosmosDiagnosticsContext cosmosCtx, Context context);
    }

    private static final class LegacyCosmosTracer implements CosmosTracer {

        private final static String JSON_STRING = "JSON";
        private final Tracer tracer;

        public LegacyCosmosTracer(Tracer tracer) {
            checkNotNull(tracer, "Argument 'tracer' must not be null.");
            this.tracer = tracer;
        }

        @Override
        public Context startSpan(String spanName, CosmosDiagnosticsContext cosmosCtx, Context context) {
            checkNotNull(spanName, "Argument 'spanName' must not be null.");
            checkNotNull(cosmosCtx, "Argument 'cosmosCtx' must not be null.");


            // @TODO implement non-legacy
            StartSpanOptions spanOptions = this.startSpanOptions(
                spanName,
                cosmosCtx.getDatabaseName(),
                ctxAccessor.getEndpoint(cosmosCtx));

            // start the span and return the started span
            return tracer.start(spanName, spanOptions, context);
        }

        private StartSpanOptions startSpanOptions(String methodName, String databaseId, String endpoint) {
            StartSpanOptions spanOptions = new StartSpanOptions(SpanKind.CLIENT)
                .setAttribute(DB_TYPE, DB_TYPE_VALUE)
                .setAttribute(LEGACY_DB_URL, endpoint)
                .setAttribute(LEGACY_DB_STATEMENT, methodName);
            if (databaseId != null) {
                spanOptions.setAttribute(LEGACY_DB_INSTANCE, databaseId);
            }

            return spanOptions;
        }

        @Override
        public void endSpan(CosmosDiagnosticsContext cosmosCtx, Context context) {
            try {
                if (cosmosCtx != null && cosmosCtx.isThresholdViolated()) {
                    Collection<CosmosDiagnostics> diagnostics = cosmosCtx.getDiagnostics();
                    if (diagnostics != null && diagnostics.size() > 0) {
                        for (CosmosDiagnostics d: diagnostics) {
                            addDiagnosticsOnTracerEvent(d, context);
                        }
                    }
                }
            } catch (JsonProcessingException ex) {
                LOGGER.warn("Error while serializing diagnostics for tracer.", ex);
            }

            if (cosmosCtx != null) {
                tracer.end(cosmosCtx.getStatusCode(), cosmosCtx.getFinalError(), context);
            }
        }

        private void addClientSideRequestStatisticsOnTracerEvent(
            ClientSideRequestStatistics clientSideRequestStatistics,
            Context context) throws JsonProcessingException {

            if (clientSideRequestStatistics == null || context == null) {
                return;
            }

            Map<String, Object> attributes;

            //adding storeResponse
            int diagnosticsCounter = 1;
            for (ClientSideRequestStatistics.StoreResponseStatistics storeResponseStatistics :
                clientSideRequestStatistics.getResponseStatisticsList()) {
                attributes = new HashMap<>();
                attributes.put(JSON_STRING, mapper.writeValueAsString(storeResponseStatistics));
                Iterator<RequestTimeline.Event> eventIterator = null;
                try {
                    if (storeResponseStatistics.getStoreResult() != null) {
                        eventIterator = storeResponseStatistics.getStoreResult().getStoreResponseDiagnostics().getRequestTimeline().iterator();
                    }
                } catch (CosmosException ex) {
                    eventIterator = BridgeInternal.getRequestTimeline(ex).iterator();
                }

                OffsetDateTime requestStartTime = OffsetDateTime.ofInstant(storeResponseStatistics.getRequestResponseTimeUTC()
                    , ZoneOffset.UTC);
                if (eventIterator != null) {
                    while (eventIterator.hasNext()) {
                        RequestTimeline.Event event = eventIterator.next();
                        if (event.getName().equals("created")) {
                            requestStartTime = OffsetDateTime.ofInstant(event.getStartTime(), ZoneOffset.UTC);
                            break;
                        }
                    }
                }

                this.addEvent("StoreResponse" + diagnosticsCounter++, attributes, requestStartTime, context);
            }

            //adding supplemental storeResponse
            diagnosticsCounter = 1;
            for (ClientSideRequestStatistics.StoreResponseStatistics statistics :
                ClientSideRequestStatistics.getCappedSupplementalResponseStatisticsList(clientSideRequestStatistics.getSupplementalResponseStatisticsList())) {
                attributes = new HashMap<>();
                attributes.put(JSON_STRING, mapper.writeValueAsString(statistics));
                OffsetDateTime requestStartTime = OffsetDateTime.ofInstant(statistics.getRequestResponseTimeUTC(),
                    ZoneOffset.UTC);
                if (statistics.getStoreResult() != null) {
                    for (RequestTimeline.Event event :
                        statistics.getStoreResult().getStoreResponseDiagnostics().getRequestTimeline()) {
                        if (event.getName().equals("created")) {
                            requestStartTime = OffsetDateTime.ofInstant(event.getStartTime(), ZoneOffset.UTC);
                            break;
                        }
                    }
                }
                this.addEvent("Supplemental StoreResponse" + diagnosticsCounter++, attributes, requestStartTime, context);
            }

            //adding gateway statistics
            if (clientSideRequestStatistics.getGatewayStatistics() != null) {
                attributes = new HashMap<>();
                attributes.put(JSON_STRING,
                    mapper.writeValueAsString(clientSideRequestStatistics.getGatewayStatistics()));
                OffsetDateTime requestStartTime =
                    OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC);
                if (clientSideRequestStatistics.getGatewayStatistics().getRequestTimeline() != null) {
                    for (RequestTimeline.Event event :
                        clientSideRequestStatistics.getGatewayStatistics().getRequestTimeline()) {
                        if (event.getName().equals("created")) {
                            requestStartTime = OffsetDateTime.ofInstant(event.getStartTime(), ZoneOffset.UTC);
                            break;
                        }
                    }
                }
                this.addEvent("GatewayStatistics", attributes, requestStartTime, context);
            }

            //adding retry context
            if (clientSideRequestStatistics.getRetryContext().getRetryStartTime() != null) {
                attributes = new HashMap<>();
                attributes.put(JSON_STRING,
                    mapper.writeValueAsString(clientSideRequestStatistics.getRetryContext()));
                this.addEvent("Retry Context", attributes,
                    OffsetDateTime.ofInstant(clientSideRequestStatistics.getRetryContext().getRetryStartTime(),
                        ZoneOffset.UTC), context);
            }

            //adding addressResolutionStatistics
            diagnosticsCounter = 1;
            for (ClientSideRequestStatistics.AddressResolutionStatistics addressResolutionStatistics :
                clientSideRequestStatistics.getAddressResolutionStatistics().values()) {
                attributes = new HashMap<>();
                attributes.put(JSON_STRING, mapper.writeValueAsString(addressResolutionStatistics));
                this.addEvent("AddressResolutionStatistics" + diagnosticsCounter++, attributes,
                    OffsetDateTime.ofInstant(addressResolutionStatistics.getStartTimeUTC(), ZoneOffset.UTC), context);
            }

            //adding serializationDiagnosticsContext
            if (clientSideRequestStatistics.getSerializationDiagnosticsContext().serializationDiagnosticsList != null) {
                for (SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics :
                    clientSideRequestStatistics.getSerializationDiagnosticsContext().serializationDiagnosticsList) {
                    attributes = new HashMap<>();
                    attributes.put(JSON_STRING, mapper.writeValueAsString(serializationDiagnostics));
                    this.addEvent("SerializationDiagnostics " + serializationDiagnostics.serializationType, attributes,
                        OffsetDateTime.ofInstant(serializationDiagnostics.startTimeUTC, ZoneOffset.UTC), context);
                }
            }

            //adding systemInformation
            attributes = new HashMap<>();
            attributes.put(JSON_STRING,
                mapper.writeValueAsString(clientSideRequestStatistics.getContactedRegionNames()));
            this.addEvent("RegionContacted", attributes,
                OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), context);


            //adding systemInformation
            attributes = new HashMap<>();
            attributes.put(JSON_STRING,
                mapper.writeValueAsString(ClientSideRequestStatistics.fetchSystemInformation()));
            this.addEvent("SystemInformation", attributes,
                OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), context);

            //adding clientCfgs
            attributes = new HashMap<>();
            attributes.put(JSON_STRING,
                mapper.writeValueAsString(clientSideRequestStatistics.getDiagnosticsClientConfig()));
            this.addEvent("ClientCfgs", attributes,
                OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), context);

            if (clientSideRequestStatistics.getResponseStatisticsList() != null && clientSideRequestStatistics.getResponseStatisticsList().size() > 0
                && clientSideRequestStatistics.getResponseStatisticsList().get(0).getStoreResult() != null) {
                String eventName =
                    "Diagnostics for PKRange "
                        + clientSideRequestStatistics.getResponseStatisticsList().get(0).getStoreResult().getStoreResponseDiagnostics().getPartitionKeyRangeId();
                this.addEvent(eventName, attributes,
                    OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), context);
            } else if (clientSideRequestStatistics.getGatewayStatistics() != null) {
                String eventName =
                    "Diagnostics for PKRange " + clientSideRequestStatistics.getGatewayStatistics().getPartitionKeyRangeId();
                this.addEvent(eventName, attributes,
                    OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), context);

            } else {
                String eventName = "Diagnostics ";
                this.addEvent(eventName, attributes,
                    OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), context);
            }
        }

        private void addDiagnosticsOnTracerEvent(CosmosDiagnostics cosmosDiagnostics, Context context) throws JsonProcessingException {
            if (cosmosDiagnostics == null || context == null) {
                return;
            }

            Map<String, Object> attributes;
            FeedResponseDiagnostics feedResponseDiagnostics =
                diagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics);
            if (feedResponseDiagnostics != null) {
                QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnostics = feedResponseDiagnostics
                    .getQueryPlanDiagnosticsContext();
                if (queryPlanDiagnostics != null) {
                    attributes = new HashMap<>();
                    attributes.put("JSON",
                        mapper.writeValueAsString(queryPlanDiagnostics));
                    this.addEvent(
                        "Query Plan Statistics",
                        attributes,
                        OffsetDateTime.ofInstant(queryPlanDiagnostics.getStartTimeUTC(), ZoneOffset.UTC),
                        context);
                }

                Map<String, QueryMetrics> queryMetrics = feedResponseDiagnostics.getQueryMetricsMap();
                if (queryMetrics != null && queryMetrics.size() > 0) {
                    for(Map.Entry<String, QueryMetrics> entry : queryMetrics.entrySet()) {
                        attributes = new HashMap<>();
                        attributes.put("Query Metrics", entry.getValue().toString());
                        this.addEvent("Query Metrics for PKRange " + entry.getKey(), attributes,
                            OffsetDateTime.now(), context);
                    }
                }

                for (ClientSideRequestStatistics c: feedResponseDiagnostics.getClientSideRequestStatistics()) {
                    addClientSideRequestStatisticsOnTracerEvent(c, context);
                }
            }

            addClientSideRequestStatisticsOnTracerEvent(
                BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics),
                context);
        }

        void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp, Context context) {
            tracer.addEvent(name, attributes, timestamp, context);
        }
    }

    private final static class OpenTelemetryCosmosTracer implements CosmosTracer {
        private final Tracer tracer;
        private final CosmosClientTelemetryConfig config;

        public OpenTelemetryCosmosTracer(Tracer tracer, CosmosClientTelemetryConfig config) {
            checkNotNull(tracer, "Argument 'tracer' must not be null.");
            checkNotNull(config, "Argument 'config' must not be null.");
            this.tracer = tracer;
            this.config = config;
        }

        private boolean isTransportLevelTracingEnabled() {
            return clientTelemetryConfigAccessor.isTransportLevelTracingEnabled(this.config);
        }

        @Override
        public Context startSpan(String spanName, CosmosDiagnosticsContext cosmosCtx, Context context) {

            checkNotNull(spanName, "Argument 'spanName' must not be null.");
            checkNotNull(cosmosCtx, "Argument 'cosmosCtx' must not be null.");
            Context local = Objects
                .requireNonNull(context, "'context' cannot be null.")
                .addData(COSMOS_DIAGNOSTICS_CONTEXT_KEY, cosmosCtx);

            StartSpanOptions spanOptions = this.startSpanOptions(
                spanName,
                cosmosCtx);

            // start the span and return the started span
            return tracer.start(spanName, spanOptions, local);
        }

        private StartSpanOptions startSpanOptions(String spanName, CosmosDiagnosticsContext cosmosCtx) {
            StartSpanOptions spanOptions;

            if (tracer instanceof EnabledNoOpTracer) {
                spanOptions = new StartSpanOptions(SpanKind.CLIENT);
            } else {
                spanOptions = new StartSpanOptions(SpanKind.CLIENT)
                    .setAttribute("db.system", "cosmosdb")
                    .setAttribute("db.operation", spanName)
                    .setAttribute("net.peer.name", cosmosCtx.getAccountName())
                    .setAttribute("db.cosmosdb.operation_type",cosmosCtx.getOperationType())
                    .setAttribute("db.cosmosdb.resource_type",cosmosCtx.getResourceType())
                    .setAttribute("db.name", cosmosCtx.getDatabaseName());

                if (!cosmosCtx.getOperationId().isEmpty()) {
                    spanOptions.setAttribute("db.cosmosdb.operation_id", cosmosCtx.getOperationId());
                }

                String containerName = cosmosCtx.getContainerName();
                if (containerName != null) {
                    spanOptions.setAttribute("db.cosmosdb.container", containerName);
                }
            }

            return spanOptions;
        }

        @Override
        public void endSpan(CosmosDiagnosticsContext cosmosCtx, Context context) {

            if (cosmosCtx == null) {
                return;
            }

            if (!cosmosCtx.isCompleted()) {
                tracer.end("CosmosCtx not completed yet.", null, context);
                return;
            }

            String errorMessage = null;
            Throwable finalError = cosmosCtx.getFinalError();
            if (finalError != null && cosmosCtx.isFailure()) {

                if (finalError instanceof CosmosException) {
                    CosmosException cosmosException = (CosmosException) finalError;
                    errorMessage = cosmosException.getMessageWithoutDiagnostics();
                } else {
                    errorMessage = finalError.getMessage();
                }
            }

            if (tracer instanceof EnabledNoOpTracer) {
                tracer.end(errorMessage, finalError, context);
                return;
            }

            if (cosmosCtx.isFailure() || cosmosCtx.isThresholdViolated()) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("Diagnostics", cosmosCtx.toString());

                if (cosmosCtx.isFailure()) {
                    tracer.addEvent("failure", attributes, OffsetDateTime.now(), context);
                } else {
                    tracer.addEvent("threshold_violation", attributes, OffsetDateTime.now(), context);
                }
            }

            if (finalError != null) {

                String exceptionType;
                if (finalError instanceof  CosmosException) {
                    exceptionType = CosmosException.class.getCanonicalName();
                } else {
                    exceptionType = finalError.getClass().getCanonicalName();
                }

                tracer.setAttribute("exception.escaped", Boolean.toString(cosmosCtx.isFailure()), context);
                tracer.setAttribute("exception.type", exceptionType, context);
                tracer.setAttribute("exception.message", errorMessage, context);
                tracer.setAttribute("exception.stacktrace", prettifyCallstack(finalError), context);
            }

            if (this.isTransportLevelTracingEnabled()) {
                traceTransportLevel(cosmosCtx, context);
            }

            tracer.setAttribute(
                "db.cosmosdb.status_code",
                Integer.toString(cosmosCtx.getStatusCode()),
                context);
            tracer.setAttribute(
                "db.cosmosdb.sub_status_code",
                Integer.toString(cosmosCtx.getSubStatusCode()),
                context);
            tracer.setAttribute(
                "db.cosmosdb.request_charge",
                Float.toString(cosmosCtx.getTotalRequestCharge()),
                context);
            tracer.setAttribute("db.cosmosdb.max_request_content_length",cosmosCtx.getMaxRequestPayloadSizeInBytes(), context);
            tracer.setAttribute("db.cosmosdb.max_response_content_length_bytes",cosmosCtx.getMaxResponsePayloadSizeInBytes(), context);
            tracer.setAttribute("db.cosmosdb.retry_count",cosmosCtx.getRetryCount() , context);

            Set<String> regionsContacted = cosmosCtx.getContactedRegionNames();
            if (!regionsContacted.isEmpty()) {
                tracer.setAttribute(
                    "db.cosmosdb.regions_contacted",
                    String.join(", ", regionsContacted),
                    context);
            }

            tracer.end(errorMessage, finalError, context);
        }

        private void recordStoreResponseStatistics(
            List<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseStatistics,
            Context context) {

            for (ClientSideRequestStatistics.StoreResponseStatistics responseStatistics: storeResponseStatistics) {
                StoreResultDiagnostics storeResultDiagnostics = responseStatistics.getStoreResult();
                StoreResponseDiagnostics storeResponseDiagnostics =
                    storeResultDiagnostics.getStoreResponseDiagnostics();

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("rntbd.url", storeResultDiagnostics.getStorePhysicalAddressAsString());
                attributes.put("rntbd.resource_type", responseStatistics.getRequestResourceType().toString());
                attributes.put("rntbd.operation_type", responseStatistics.getRequestOperationType().toString());
                attributes.put("rntbd.region", responseStatistics.getRegionName());

                if (storeResultDiagnostics.getLsn() > 0) {
                    attributes.put("rntbd.lsn", Long.toString(storeResultDiagnostics.getLsn()));
                }

                if (storeResultDiagnostics.getGlobalCommittedLSN() > 0) {
                    attributes.put("rntbd.gclsn", Long.toString(storeResultDiagnostics.getGlobalCommittedLSN()));
                }

                String responseSessionToken = responseStatistics.getRequestSessionToken();
                if (responseSessionToken != null && !responseSessionToken.isEmpty()) {
                    attributes.put("rntbd.session_token", responseSessionToken);
                }

                String requestSessionToken = responseStatistics.getRequestSessionToken();
                if (requestSessionToken != null && !requestSessionToken.isEmpty()) {
                    attributes.put("rntbd.request_session_token", requestSessionToken);
                }

                String activityId = storeResponseDiagnostics.getActivityId();
                if (requestSessionToken != null && !requestSessionToken.isEmpty()) {
                    attributes.put("rntbd.activity_id", activityId);
                }

                String pkRangeId = storeResponseDiagnostics.getPartitionKeyRangeId();
                if (pkRangeId != null && !pkRangeId.isEmpty()) {
                    attributes.put("rntbd.partition_key_range_id", pkRangeId);
                }

                attributes.put("rntbd.status_code", Integer.toString(storeResponseDiagnostics.getStatusCode()));
                if (storeResponseDiagnostics.getSubStatusCode() != 0) {
                    attributes.put("rntbd.sub_status_code", Integer.toString(storeResponseDiagnostics.getSubStatusCode()));
                }

                Double backendLatency = storeResultDiagnostics.getBackendLatencyInMs();
                if (backendLatency != null) {
                    attributes.put("rntbd.backend_latency", Double.toString(backendLatency));
                }

                double requestCharge = storeResponseDiagnostics.getRequestCharge();
                attributes.put("rntbd.request_charge", Double.toString(requestCharge));

                Duration latency = responseStatistics.getDuration();
                if (latency != null) {
                    attributes.put("rntbd.latency", latency.toString());
                }

                if (storeResponseDiagnostics.getRntbdChannelStatistics() != null) {
                    attributes.put(
                        "rntbd.is_new_channel",
                        storeResponseDiagnostics.getRntbdChannelStatistics().isWaitForConnectionInit());
                }

                OffsetDateTime startTime = null;
                for (RequestTimeline.Event event : storeResponseDiagnostics.getRequestTimeline()) {
                    OffsetDateTime eventTime = event.getStartTime() != null ?
                        event.getStartTime().atOffset(ZoneOffset.UTC) : null;

                    if (eventTime != null &&
                        (startTime == null || startTime.isBefore(eventTime))) {
                        startTime = eventTime;
                    }

                    Duration duration = event.getDuration();
                    if (duration == null || duration == Duration.ZERO) {
                        continue;
                    }

                    attributes.put("rntbd.latency_" + event.getName().toLowerCase(Locale.ROOT), duration.toString());
                }

                attributes.put("rntbd.request_size_bytes",storeResponseDiagnostics.getRequestPayloadLength());
                attributes.put("rntbd.response_size_bytes",storeResponseDiagnostics.getResponsePayloadLength());

                this.tracer.addEvent(
                    "rntbd.request",
                    attributes,
                    startTime != null ? startTime : OffsetDateTime.now(),
                    context);
             }
        }

        private void traceTransportLevelRequests(
            Collection<ClientSideRequestStatistics> clientSideRequestStatistics,
            Context context) {

            if (clientSideRequestStatistics != null) {
                for (ClientSideRequestStatistics requestStatistics : clientSideRequestStatistics) {

                    recordStoreResponseStatistics(
                        requestStatistics.getResponseStatisticsList(),
                        context);
                    recordStoreResponseStatistics(
                        requestStatistics.getSupplementalResponseStatisticsList(),
                        context);
                }
            }
        }

        private void traceTransportLevel(CosmosDiagnosticsContext diagnosticsContext, Context context) {
            // HTTP calls are automatically captured as well

            for (CosmosDiagnostics diagnostics: diagnosticsContext.getDiagnostics()) {
                traceTransportLevelRequests(
                    diagnosticsAccessor.getClientSideRequestStatistics(diagnostics),
                    context);

                FeedResponseDiagnostics feedResponseDiagnostics =
                    diagnosticsAccessor.getFeedResponseDiagnostics(diagnostics);
                if (feedResponseDiagnostics != null) {
                    traceTransportLevelRequests(
                        feedResponseDiagnostics.getClientSideRequestStatistics(),
                        context);
                }
            }
        }
    }

    public static String prettifyCallstack(Throwable e) {
        StringWriter stackWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stackWriter);
        e.printStackTrace(printWriter);
        printWriter.flush();
        stackWriter.flush();
        String prettifiedCallstack = stackWriter.toString();
        String message = e.toString();
        if (prettifiedCallstack.length() > message.length()) {
            prettifiedCallstack = prettifiedCallstack.substring(message.length());
        }
        printWriter.close();

        try {
            stackWriter.close();
        } catch (IOException closeError) {
            LOGGER.warn("Error trying to close StringWriter.", closeError);
        }

        return prettifiedCallstack;
    }

    private static final class EnabledNoOpTracer implements Tracer {
        public static final Tracer INSTANCE = new EnabledNoOpTracer();

        private EnabledNoOpTracer() {
        }

        @Override
        public Context start(String methodName, Context context) {
            return context;
        }

        @Override
        public Context start(String methodName, Context context, ProcessKind processKind) {
            return context;
        }

        @Override
        public void end(int responseCode, Throwable error, Context context) {
        }

        @Override
        public void end(String errorCondition, Throwable error, Context context) {
        }

        @Override
        public void setAttribute(String key, String value, Context context) {
        }

        @Override
        public Context setSpanName(String spanName, Context context) {
            return Context.NONE;
        }

        @Override
        public void addLink(Context context) {
        }

        @Override
        public Context extractContext(String diagnosticId, Context context) {
            return Context.NONE;
        }

        @Override
        public Context getSharedSpanBuilder(String spanName, Context context) {
            return Context.NONE;
        }
    }
}
