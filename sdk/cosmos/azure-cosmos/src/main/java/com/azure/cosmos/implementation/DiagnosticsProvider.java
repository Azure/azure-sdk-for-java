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
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Signal;
import reactor.util.context.ContextView;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class DiagnosticsProvider {
    private static final ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor clientTelemetryConfigAccessor =
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.getCosmosClientTelemetryConfigAccessor();
    private static final ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.CosmosDiagnosticsContextAccessor ctxAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor();
    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor clientAccessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticsProvider.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String COSMOS_CALL_DEPTH = "cosmosCallDepth";
    public static final int ERROR_CODE = 0;
    public static final String RESOURCE_PROVIDER_NAME = "Microsoft.DocumentDB";
    public final static String DB_TYPE_VALUE = "Cosmos";
    public final static String DB_TYPE = "db.type";

    private static final String REACTOR_TRACING_CONTEXT_KEY = "tracing-context";
    private static final String COSMOS_DIAGNOSTICS_CONTEXT_KEY = "azure-cosmos-context";
    private static final Object DUMMY_VALUE = new Object();
    private final Mono<Object> propagatingMono;
    private final Flux<Object> propagatingFlux;
    private final ArrayList<CosmosDiagnosticsHandler> diagnosticHandlers;
    private final Tracer tracer;
    private final CosmosTracer cosmosTracer;

    public DiagnosticsProvider(
        Tracer tracer,
        CosmosClientTelemetryConfig clientTelemetryConfig) {

        checkNotNull(clientTelemetryConfig, "Argument 'clientTelemetryConfig' must not be null.");

        this.diagnosticHandlers = new ArrayList<>(
            clientTelemetryConfigAccessor.getDiagnosticHandlers(clientTelemetryConfig));

        if (tracer != null) {
            checkNotNull(diagnosticHandlers, "Argument 'diagnosticHandlers' must not be null.");
            this.tracer = tracer;
        } else {
            if (!this.diagnosticHandlers.isEmpty()) {
                this.tracer = NoOpTracer.INSTANCE;
            } else {
                this.tracer = null;
            }
        }

        if (this.tracer != null) {
            this.cosmosTracer = new LegacyCosmosTracer(this.tracer);
        } else {
            this.cosmosTracer = null;
        }

        this.propagatingMono = new PropagatingMono();
        this.propagatingFlux = new PropagatingFlux();
    }

    public boolean isEnabled() {
        return this.tracer != null;
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

    private static CosmosDiagnosticsContext getCosmosDiagnosticsContextFromTraceContextOrThrow(Context traceContext) {
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

        if (this.cosmosTracer == null) {
            return context;
        }

        return this.cosmosTracer.startSpan(spanName, cosmosCtx, context);
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
                        effectiveRequestCharge += exception.getRequestCharge();
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

    public <T> Mono<CosmosItemResponse<T>> traceEnabledCosmosItemResponsePublisher(
       Mono<CosmosItemResponse<T>> resultPublisher,
       Context context,
       String spanName,
       String containerId,
       String databaseId,
       String accountName,
       CosmosAsyncClient client,
       ConsistencyLevel consistencyLevel,
       OperationType operationType,
       ResourceType resourceType,
       Duration thresholdForDiagnosticsOnTracer) {

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
            thresholdForDiagnosticsOnTracer);
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
                                                     Duration thresholdForDiagnosticsOnTracer) {

        Duration effectiveLatencyThreshold = thresholdForDiagnosticsOnTracer != null ?
            thresholdForDiagnosticsOnTracer :
            operationType.isPointOperation() ? Duration.ofSeconds(1) : Duration.ofSeconds(3);

        CosmosDiagnosticsContext cosmosCtx = ctxAccessor.create(
            spanName,
            accountName,
            databaseId,
            containerId,
            resourceType,
            operationType,
            clientAccessor.getEffectiveConsistencyLevel(client, operationType, consistencyLevel),
            maxItemCount,
            effectiveLatencyThreshold);

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

    private boolean isNoneExceptional(int statusCode, CosmosException cosmosException) {
        // @TODO implement other non-exceptional cases to short-circuit relatively expensive
        // exception propagation
        if (statusCode == HttpConstants.StatusCodes.NOTFOUND && cosmosException.getSubStatusCode() == 0) {
            return true;
        }

        return false;
    }

    private void end(
        int statusCode,
        int subStatusCode,
        Integer actualItemCount,
        Double requestCharge,
        CosmosDiagnostics diagnostics,
        Throwable throwable,
        Context context) {

        Throwable throwableForDiagnostics = throwable;
        if (throwable instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) throwable;
            if (isNoneExceptional(statusCode, cosmosException)) {
                throwableForDiagnostics = null;
            }
        }

        CosmosDiagnosticsContext cosmosCtx = getCosmosDiagnosticsContextFromTraceContextOrThrow(context);
        ctxAccessor.endOperation(
            cosmosCtx,
            statusCode,
            subStatusCode,
            actualItemCount,
            requestCharge,
            diagnostics,
            throwableForDiagnostics);

        // @TODO - investigate whether we should push the handling of diagnostics out of the hot path
        // currently diagnostics are handled by the same thread on the hot path - which is intentional
        // because any async queueing/throttling/sampling can best be done by diagnostic handlers
        // but there is some risk given that diagnostic handlers are custom code of course
        if (this.diagnosticHandlers != null && this.diagnosticHandlers.size() > 0) {
            for (CosmosDiagnosticsHandler handler: this.diagnosticHandlers) {
                handler.handleDiagnostics(context, cosmosCtx);
            }
        }

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
     *
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
     *
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
            Context local = Objects
                .requireNonNull(context, "'context' cannot be null.")
                .addData(COSMOS_DIAGNOSTICS_CONTEXT_KEY, cosmosCtx);

            // @TODO implement non-legacy
            StartSpanOptions spanOptions = this.startSpanOptions(
                spanName,
                cosmosCtx.getDatabaseName(),
                cosmosCtx.getAccountName());

            // start the span and return the started span
            return tracer.start(spanName, spanOptions, local);
        }

        private StartSpanOptions startSpanOptions(String methodName, String databaseId, String endpoint) {
            StartSpanOptions spanOptions = new StartSpanOptions(SpanKind.CLIENT)
                .setAttribute(AZ_TRACING_NAMESPACE_KEY, RESOURCE_PROVIDER_NAME)
                .setAttribute(DB_TYPE, DB_TYPE_VALUE)
                .setAttribute(TracerProvider.DB_URL, endpoint)
                .setAttribute(TracerProvider.DB_STATEMENT, methodName);
            if (databaseId != null) {
                spanOptions.setAttribute(TracerProvider.DB_INSTANCE, databaseId);
            }

            return spanOptions;
        }

        @Override
        public void endSpan(CosmosDiagnosticsContext cosmosCtx, Context context) {
            try {
                if (cosmosCtx != null && cosmosCtx.isLatencyThresholdViolated()) {
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

        private void addDiagnosticsOnTracerEvent(CosmosDiagnostics cosmosDiagnostics, Context context) throws JsonProcessingException {
            if (cosmosDiagnostics == null || context == null) {
                return;
            }

            ClientSideRequestStatistics clientSideRequestStatistics =
                BridgeInternal.getClientSideRequestStatics(cosmosDiagnostics);

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
        }

        void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp, Context context) {
            tracer.addEvent(name, attributes, timestamp, context);
        }
    }

    private final static class OpenTelemetryCosmosTracer implements CosmosTracer {

        @Override
        public Context startSpan(String spanName, CosmosDiagnosticsContext cosmosCtx, Context context) {
            return null;
        }

        @Override
        public void endSpan(CosmosDiagnosticsContext cosmosCtx, Context context) {

        }
    }

    private static final class NoOpTracer implements Tracer {
        public static final Tracer INSTANCE = new NoOpTracer();

        private NoOpTracer() {
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
