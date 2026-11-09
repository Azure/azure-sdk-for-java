// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.implementation.instrumentation.NoopAttributes;
import io.clientcore.core.implementation.instrumentation.NoopMeter;
import io.clientcore.core.implementation.instrumentation.SdkInstrumentationOptionsAccessHelper;
import io.clientcore.core.implementation.instrumentation.otel.metrics.OTelMeter;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpan;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpanContext;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelTraceContextPropagator;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelTracer;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.ERROR_TYPE_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.OPERATION_NAME_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_ADDRESS_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_PORT_KEY;
import static io.clientcore.core.implementation.instrumentation.InstrumentationUtils.UNKNOWN_LIBRARY_OPTIONS;
import static io.clientcore.core.implementation.instrumentation.InstrumentationUtils.createOperationDurationHistogram;

/**
 * A {@link Instrumentation} implementation that uses OpenTelemetry.
 */
public class OTelInstrumentation implements Instrumentation {

    private static final Object NOOP_PROVIDER;
    private static final OTelTraceContextPropagator W3C_PROPAGATOR_INSTANCE;
    private static final ClientLogger LOGGER = new ClientLogger(OTelInstrumentation.class);

    static {
        Object noopProvider = null;
        Object w3cPropagatorInstance = null;

        if (OTelInitializer.isInitialized()) {
            try {
                noopProvider = TracerProvider.noop();

                w3cPropagatorInstance = W3CTraceContextPropagator.getInstance();
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        NOOP_PROVIDER = noopProvider;

        W3C_PROPAGATOR_INSTANCE = new OTelTraceContextPropagator(w3cPropagatorInstance);
    }

    public static final OTelInstrumentation DEFAULT_INSTANCE
        = new OTelInstrumentation(null, UNKNOWN_LIBRARY_OPTIONS, null, -1);

    private final boolean isTracingEnabled;
    private final boolean isMetricsEnabled;
    private final boolean allowNestedSpans;
    private final DoubleHistogram callDurationMetric;
    private final Tracer tracer;
    private final Meter meter;
    private final String host;
    private final int port;
    private final Map<String, InstrumentationAttributes> commonAttributesCache = new ConcurrentHashMap<>();

    /**
     * Creates a new instance of {@link OTelInstrumentation}.
     *
     * @param applicationOptions the application options
     * @param sdkOptions the sdk instrumentation options
     * @param host the service host
     * @param port the service port
     */
    public OTelInstrumentation(InstrumentationOptions applicationOptions, SdkInstrumentationOptions sdkOptions,
        String host, int port) {
        Object explicitOTel = applicationOptions == null ? null : applicationOptions.getTelemetryProvider();
        if (explicitOTel != null && !(explicitOTel instanceof OpenTelemetry)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("providerType", explicitOTel.getClass().getCanonicalName())
                .addKeyValue("expectedType", OpenTelemetry.class.getCanonicalName())
                .log("Unexpected telemetry provider type.", IllegalArgumentException::new);
        }

        Object otelInstance = explicitOTel;
        if (otelInstance == null && OTelInitializer.isInitialized()) {
            otelInstance = FallbackInvoker.call(LOGGER, null, () -> GlobalOpenTelemetry.get());
        }
        this.isTracingEnabled = applicationOptions == null || applicationOptions.isTracingEnabled();
        this.isMetricsEnabled = applicationOptions == null || applicationOptions.isMetricsEnabled();
        this.allowNestedSpans
            = sdkOptions != null && SdkInstrumentationOptionsAccessHelper.isSpanSuppressionDisabled(sdkOptions);

        this.tracer = createTracer(isTracingEnabled, sdkOptions, otelInstance);
        this.meter = createMeter(isMetricsEnabled, sdkOptions, otelInstance);

        boolean isExperimentalFeaturesEnabled
            = applicationOptions != null && applicationOptions.isExperimentalFeaturesEnabled();
        // operation duration metric is experimental and enabled only if the feature flag is set
        this.callDurationMetric = createOperationDurationHistogram(isExperimentalFeaturesEnabled,
            sdkOptions == null ? null : sdkOptions.getSdkName(), meter);

        this.host = host;
        this.port = port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer getTracer() {
        return tracer;
    }

    @Override
    public Meter getMeter() {
        return meter;
    }

    private static Tracer createTracer(boolean isTracingEnabled, SdkInstrumentationOptions sdkOptions,
        Object otelInstance) {
        if (isTracingEnabled && OTelInitializer.isInitialized()) {
            Object otelTracerProvider
                = FallbackInvoker.call(LOGGER, null, () -> ((OpenTelemetry) otelInstance).getTracerProvider());

            if (otelTracerProvider != null && otelTracerProvider != NOOP_PROVIDER) {
                return new OTelTracer(otelTracerProvider, sdkOptions);
            }
        }

        return OTelTracer.NOOP;
    }

    private static Meter createMeter(boolean isMetricsEnabled, SdkInstrumentationOptions sdkOptions,
        Object otelInstance) {
        if (isMetricsEnabled && OTelInitializer.isInitialized()) {
            Object otelMeterProvider
                = FallbackInvoker.call(LOGGER, null, () -> ((OpenTelemetry) otelInstance).getMeterProvider());

            if (otelMeterProvider != null && otelMeterProvider != NOOP_PROVIDER) {
                return new OTelMeter(otelMeterProvider, sdkOptions);
            }
        }

        return NoopMeter.INSTANCE;
    }

    @Override
    public InstrumentationAttributes createAttributes(Map<String, Object> attributes) {
        return OTelInitializer.isInitialized() ? OTelAttributes.create(attributes) : NoopAttributes.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TraceContextPropagator getW3CTraceContextPropagator() {
        return OTelInitializer.isInitialized() ? W3C_PROPAGATOR_INSTANCE : OTelTraceContextPropagator.NOOP;
    }

    /**
     * Creates a new instance of {@link InstrumentationContext} from the given object.
     * It recognizes {@code io.opentelemetry.api.trace.Span}, {@code io.opentelemetry.api.trace.SpanContext},
     * {@code io.opentelemetry.context.Context} and generic {@link InstrumentationContext}
     * as a source and converts them to {@link InstrumentationContext}.
     * @param context the context object to convert
     * @return the instance of {@link InstrumentationContext} which is invalid if the context is not recognized
     * @param <T> the type of the context object
     */
    public <T> InstrumentationContext createInstrumentationContext(T context) {
        if (context instanceof InstrumentationContext) {
            return (InstrumentationContext) context;
        } else if (context instanceof OTelSpan) {
            return ((OTelSpan) context).getInstrumentationContext();
        } else if (context instanceof io.opentelemetry.api.trace.Span) {
            return OTelSpanContext.fromOTelSpan(context);
        } else if (context instanceof Context) {
            return OTelSpanContext.fromOTelContext(context);
        } else if (context instanceof SpanContext) {
            return new OTelSpanContext(context, null);
        }

        return OTelSpanContext.getInvalid();
    }

    @Override
    public <TResponse> TResponse instrumentWithResponse(String operationName, RequestContext requestContext,
        Function<RequestContext, TResponse> operation) {
        Objects.requireNonNull(operationName, "'operationName' cannot be null");
        Objects.requireNonNull(operation, "'operation' cannot be null");

        requestContext = requestContext == null ? RequestContext.none() : requestContext;

        InstrumentationContext context = requestContext.getInstrumentationContext();
        if (!shouldInstrument(SpanKind.INTERNAL, context)) {
            return operation.apply(requestContext);
        }

        long startTimeNs = callDurationMetric.isEnabled() ? System.nanoTime() : 0;
        InstrumentationAttributes commonAttributes = getOrCreateCommonAttributes(operationName);
        Span span = tracer.spanBuilder(operationName, SpanKind.INTERNAL, context)
            .setAllAttributes(commonAttributes)
            .startSpan();

        TracingScope scope = span.makeCurrent();
        RuntimeException error = null;

        RequestContext childContext
            = requestContext.toBuilder().setInstrumentationContext(span.getInstrumentationContext()).build();

        try {
            return operation.apply(childContext);
        } catch (RuntimeException t) {
            error = t;
            throw t;
        } finally {
            if (callDurationMetric.isEnabled()) {
                InstrumentationAttributes attributes = error == null
                    ? commonAttributes
                    : commonAttributes.put(ERROR_TYPE_KEY, error.getClass().getCanonicalName());
                callDurationMetric.record((System.nanoTime() - startTimeNs) / 1e9, attributes,
                    childContext.getInstrumentationContext());
            }
            span.end(error);
            scope.close();
        }
    }

    private InstrumentationAttributes getOrCreateCommonAttributes(String operationName) {
        return commonAttributesCache.computeIfAbsent(operationName, name -> {
            Map<String, Object> attributeMap = new HashMap<>(4);
            attributeMap.put(OPERATION_NAME_KEY, operationName);
            if (host != null) {
                attributeMap.put(SERVER_ADDRESS_KEY, host);
                if (port > 0) {
                    attributeMap.put(SERVER_PORT_KEY, port);
                }
            }

            return createAttributes(attributeMap);
        });
    }

    private boolean shouldInstrument(SpanKind spanKind, InstrumentationContext context) {
        if (!isTracingEnabled && !isMetricsEnabled) {
            return false;
        }

        if (allowNestedSpans) {
            return true;
        }

        return spanKind != tryGetParentSpanKind(context);
    }

    /**
     * Retrieves the span kind from the given context if and only if the context is a {@link OTelSpanContext}
     * i.e. was created by this instrumentation.
     * @param context the context to get the span kind from
     * @return the span kind or {@code null} if the context is not recognized
     */
    private static SpanKind tryGetParentSpanKind(InstrumentationContext context) {
        if (context instanceof OTelSpanContext) {
            Span span = context.getSpan();
            if (span instanceof OTelSpan) {
                return ((OTelSpan) span).getSpanKind();
            }
        }
        return null;
    }
}
