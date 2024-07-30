// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.TracingLink;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Basic tracing implementation class for use with REST and AMQP Service Clients to create {@link Span} and in-process
 * context propagation. Singleton OpenTelemetry tracer capable of starting and exporting spans.
 * <p>
 * This helper class supports W3C distributed tracing protocol and injects SpanContext into the outgoing HTTP and AMQP
 * requests.
 */
public class OpenTelemetryTracer implements com.azure.core.util.tracing.Tracer {
    private static final StartSpanOptions DEFAULT_SPAN_START_OPTIONS
        = new StartSpanOptions(com.azure.core.util.tracing.SpanKind.INTERNAL);
    private static final TextMapPropagator TRACE_CONTEXT_FORMAT = W3CTraceContextPropagator.getInstance();
    private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.23.1";
    private static final ClientLogger LOGGER = new ClientLogger(OpenTelemetryTracer.class);
    private static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };
    private static final String SUPPRESSED_SPAN_FLAG = "suppressed-span-flag";
    private static final String CLIENT_METHOD_CALL_FLAG = "client-method-call-flag";
    private static final String AZ_TRACING_NAMESPACE_KEY = "az.namespace";
    private final Tracer tracer;
    private final boolean isEnabled;

    private final String azNamespace;
    private static Method convertAppToAgentContextMethod = null;
    private static Class<?> applicationContextClass = null;

    static {
        // OTel and ApplicationInsights agents include this plugin out-of-the-box.
        // but when running in agent, the io.opentelemetry.context.Context is relocated.
        // OTel agent bridges two representations of the io.opentelemetry.context.Context,
        // but it has a hard time with our explicit context propagation.
        //
        // As a result, we sometimes get parent context in the form of io.opentelemetry.context.Context
        // and sometimes in the form of io.opentelemetry.javaagent.shaded.io.opentelemetry.context.Context.
        //
        // io.opentelemetry.context.Context comes from the application - this is explicit context provided by user
        // io.opentelemetry.javaagent.shaded.io.opentelemetry.context.Context comes from higher layers of our own instrumentations.
        // So we need to be prepared to handle both.
        //
        // Let's detect if we're running in agent and get the method to convert application context to agent context.
        // See https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/11042 for the full context (pun intended)
        try {
            Class<?> agentCtxStorageClass = Class
                .forName("io.opentelemetry.javaagent.instrumentation.opentelemetryapi.context.AgentContextStorage");

            // Shading plugins rewrite string literals that look like package name, so we have to trick it
            // to resolve the actual io.opentelemetry.context.Context.
            applicationContextClass = Class.forName("#io.opentelemetry.context.Context".substring(1));
            convertAppToAgentContextMethod = agentCtxStorageClass.getMethod("getAgentContext", applicationContextClass);
        } catch (ReflectiveOperationException t) {
            // it's expected if we're not running in the agent
            LOGGER.verbose("Failed to resolve AgentContextStorage.getAgentContext or one of its dependencies", t);
        } catch (RuntimeException t) {
            throw LOGGER.logExceptionAsError(t);
        }
    }

    /**
     * Creates new {@link OpenTelemetryTracer} using default global tracer -
     * {@link GlobalOpenTelemetry#getTracer(String)}
     *
     */
    public OpenTelemetryTracer() {
        this("azure-core", null, null, null);
    }

    /**
     * Creates new {@link OpenTelemetryTracer} using default global tracer -
     * {@link GlobalOpenTelemetry#getTracer(String)}
     *
     */
    OpenTelemetryTracer(String libraryName, String libraryVersion, String azNamespace, TracingOptions options) {

        TracerProvider otelProvider = getTracerProvider(options);
        this.isEnabled = (options == null || options.isEnabled()) && otelProvider != TracerProvider.noop();
        this.azNamespace = azNamespace;
        this.tracer = otelProvider.tracerBuilder(libraryName)
            .setInstrumentationVersion(libraryVersion)
            .setSchemaUrl(SCHEMA_URL)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, Context context) {
        return start(spanName, DEFAULT_SPAN_START_OPTIONS, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, StartSpanOptions options, Context context) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        if (!isEnabled) {
            return context;
        }

        SpanKind spanKind = convertToOtelKind(options.getSpanKind());
        if (shouldSuppress(spanKind, context)) {
            return startSuppressedSpan(context);
        }
        context = unsuppress(context);
        if (isInternalOrClientSpan(spanKind) && !context.getData(CLIENT_METHOD_CALL_FLAG).isPresent()) {
            context = context.addData(CLIENT_METHOD_CALL_FLAG, true);
        }

        io.opentelemetry.context.Context traceContext = getTraceContextOrDefault(context, null);

        SpanBuilder spanBuilder = createSpanBuilder(spanName, options, traceContext);

        if (options.getStartTimestamp() != null) {
            spanBuilder.setStartTimestamp(options.getStartTimestamp());
        }

        Span span = spanBuilder.startSpan();
        if (span.isRecording()) {
            // If span is sampled in, add additional attributes

            String tracingNamespace = getAzNamespace(context);
            if (tracingNamespace != null) {
                OpenTelemetryUtils.addAttribute(span, AZ_TRACING_NAMESPACE_KEY, tracingNamespace);
            }
        }

        return context.addData(PARENT_TRACE_CONTEXT_KEY,
            (traceContext != null ? traceContext : io.opentelemetry.context.Context.current()).with(span));
    }

    /*
     * Converts our SpanKind to OpenTelemetry SpanKind.
     */
    private SpanKind convertToOtelKind(com.azure.core.util.tracing.SpanKind kind) {
        switch (kind) {
            case CLIENT:
                return SpanKind.CLIENT;

            case SERVER:
                return SpanKind.SERVER;

            case CONSUMER:
                return SpanKind.CONSUMER;

            case PRODUCER:
                return SpanKind.PRODUCER;

            default:
                return SpanKind.INTERNAL;
        }
    }

    private SpanBuilder createSpanBuilder(String spanName, StartSpanOptions options,
        io.opentelemetry.context.Context parentContext) {
        SpanBuilder spanBuilder = tracer.spanBuilder(spanName).setSpanKind(convertToOtelKind(options.getSpanKind()));

        // if remote parent is provided, it has higher priority
        Context remoteParentContext = options.getRemoteParent();
        SpanContext remoteSpanContext
            = remoteParentContext == null ? null : getOrNull(remoteParentContext, SPAN_CONTEXT_KEY, SpanContext.class);

        if (remoteSpanContext != null) {
            if (parentContext == null) {
                parentContext = io.opentelemetry.context.Context.root();
            }
            spanBuilder.setParent(parentContext.with(Span.wrap(remoteSpanContext)));
        } else if (parentContext != null) {
            spanBuilder.setParent(parentContext);
        }

        if (options.getAttributes() != null) {
            Attributes beforeSamplingAttributes = OpenTelemetryUtils.convert(options.getAttributes());
            // if some attributes are provided, set them
            spanBuilder.setAllAttributes(beforeSamplingAttributes);
        }

        if (!CoreUtils.isNullOrEmpty(options.getLinks())) {
            for (TracingLink link : options.getLinks()) {
                SpanContext spanContext = getOrNull(link.getContext(), SPAN_CONTEXT_KEY, SpanContext.class);
                spanBuilder.addLink(spanContext != null ? spanContext : SpanContext.getInvalid(),
                    OpenTelemetryUtils.convert(link.getAttributes()));
            }
        }
        return spanBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void injectContext(BiConsumer<String, String> headerSetter, Context context) {
        io.opentelemetry.context.Context otelContext = getTraceContextOrDefault(context, null);
        if (otelContext != null) {
            TRACE_CONTEXT_FORMAT.inject(otelContext, null, (ignored, key, value) -> headerSetter.accept(key, value));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String key, long value, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null");
        if (!isEnabled) {
            return;
        }

        final Span span = getSpanOrNull(context);
        if (span == null) {
            return;
        }

        if (span.isRecording()) {
            OpenTelemetryUtils.addAttribute(span, key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String key, String value, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null");

        if (CoreUtils.isNullOrEmpty(value)) {
            LOGGER.verbose("Failed to set span attribute since value is null or empty.");
            return;
        }

        if (!isEnabled) {
            return;
        }

        final Span span = getSpanOrNull(context);
        if (span == null) {
            return;
        }

        if (span.isRecording()) {
            OpenTelemetryUtils.addAttribute(span, key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String key, Object value, Context context) {
        Objects.requireNonNull(value, "'value' cannot be null");
        Objects.requireNonNull(context, "'context' cannot be null");

        if (!isEnabled) {
            return;
        }

        final Span span = getSpanOrNull(context);
        if (span == null) {
            return;
        }

        if (span.isRecording()) {
            OpenTelemetryUtils.addAttribute(span, key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(String errorMessage, Throwable throwable, Context context) {
        if (!isEnabled) {
            return;
        }

        Span span = getSpanOrNull(context);
        if (span != null) {
            span = OpenTelemetryUtils.setError(span, errorMessage, throwable);
            span.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context extractContext(Function<String, String> headerGetter) {
        Objects.requireNonNull(headerGetter, "'headerGetter' cannot be null");
        if (!isEnabled) {
            return Context.NONE;
        }

        io.opentelemetry.context.Context traceContext
            = TRACE_CONTEXT_FORMAT.extract(io.opentelemetry.context.Context.root(), headerGetter, Getter.INSTANCE);

        return new Context(SPAN_CONTEXT_KEY, Span.fromContext(traceContext).getSpanContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecording(Context context) {
        Objects.requireNonNull(context, "'context' cannot be null");
        if (!isEnabled) {
            return false;
        }

        Span span = getSpanOrNull(context);
        if (span != null) {
            return span.isRecording();
        }

        return false;
    }

    private static class Getter implements TextMapGetter<Function<String, String>> {

        public static final TextMapGetter<Function<String, String>> INSTANCE = new Getter();
        private static final Iterable<String> KEYS = Arrays.asList("traceparent", "tracestate");

        @Override
        public Iterable<String> keys(Function<String, String> headerGetter) {
            return KEYS;
        }

        @Override
        @SuppressWarnings("deprecation")
        public String get(Function<String, String> headerGetter, String headerName) {
            if (headerGetter == null) {
                // headerGetter is annotated with Nullable, guard.
                return null;
            }

            String value = headerGetter.apply(headerName);
            if ("traceparent".equals(headerName) && value == null) {
                value = headerGetter.apply(DIAGNOSTIC_ID_KEY);
            }
            return value;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AutoCloseable makeSpanCurrent(Context context) {
        if (!isEnabled || getBoolean(SUPPRESSED_SPAN_FLAG, context)) {
            return NOOP_CLOSEABLE;
        }

        io.opentelemetry.context.Context traceContext = getTraceContextOrDefault(context, null);
        if (traceContext == null) {
            return NOOP_CLOSEABLE;
        }
        return traceContext.makeCurrent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEvent(String eventName, Map<String, Object> traceEventAttributes, OffsetDateTime timestamp,
        Context context) {
        Objects.requireNonNull(eventName, "'eventName' cannot be null.");
        if (!isEnabled) {
            return;
        }

        Span currentSpan = getSpanOrNull(context);

        if (currentSpan == null) {
            LOGGER.verbose("There is no OpenTelemetry Span or Context on the context, cannot add event");
            return;
        }

        Attributes otelAttributes = OpenTelemetryUtils.convert(traceEventAttributes);
        if (timestamp == null) {
            currentSpan.addEvent(eventName, otelAttributes);
        } else {
            currentSpan.addEvent(eventName, otelAttributes, timestamp.toInstant());
        }
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    private String getAzNamespace(Context context) {
        return azNamespace != null ? azNamespace : getOrNull(context, AZ_TRACING_NAMESPACE_KEY, String.class);
    }

    /**
     * Returns the value of the specified key from the context.
     *
     * @param key The name of the attribute that needs to be extracted from the {@link Context}.
     * @param clazz clazz the type of raw class to find data for.
     * @param context The context containing the specified key.
     * @return The T type of raw class object
     */
    @SuppressWarnings("unchecked")
    private static <T> T getOrNull(Context context, String key, Class<T> clazz) {
        final Object data = context.getData(key).orElse(null);
        if (data != null && clazz.isAssignableFrom(data.getClass())) {
            return (T) data;
        }

        return null;
    }

    /**
     * Returns OpenTelemetry trace context from given com.azure.core.Context under PARENT_TRACE_CONTEXT_KEY
     * or default value.
     */
    private static io.opentelemetry.context.Context getTraceContextOrDefault(Context azContext,
        io.opentelemetry.context.Context defaultContext) {
        final Object data = azContext.getData(PARENT_TRACE_CONTEXT_KEY).orElse(null);
        if (data == null) {
            return defaultContext;
        }

        if (data instanceof io.opentelemetry.context.Context) {
            return (io.opentelemetry.context.Context) data;
        }

        // If tracing plugin is running in the agent, we would get a relocated shaded context or the original context here
        // this code below converts the original context coming from user application to the shaded context
        // using stable public API that's always present in the agent
        // (see note on the static block initializing convertAppToAgentContextMethod)
        if (convertAppToAgentContextMethod != null
            && applicationContextClass != null
            && applicationContextClass.isAssignableFrom(data.getClass())) {
            try {
                return (io.opentelemetry.context.Context) convertAppToAgentContextMethod.invoke(null, data);
            } catch (Throwable t) {
                convertAppToAgentContextMethod = null;
                LOGGER.warning(
                    "Failed to convert application context to agent context. Will not attempt to convert again.", t);
            }
        }

        return defaultContext;
    }

    /**
     * Returns OpenTelemetry trace context from given com.azure.core.Context under PARENT_TRACE_CONTEXT_KEY.
     */
    private Span getSpanOrNull(Context azContext) {
        if (getBoolean(SUPPRESSED_SPAN_FLAG, azContext)) {
            return null;
        }

        io.opentelemetry.context.Context traceContext = getTraceContextOrDefault(azContext, null);

        return traceContext == null ? null : Span.fromContext(traceContext);
    }

    private Context startSuppressedSpan(Context context) {
        return context.addData(SUPPRESSED_SPAN_FLAG, true);
    }

    private static boolean shouldSuppress(SpanKind kind, Context context) {
        return kind == SpanKind.INTERNAL && getBoolean(CLIENT_METHOD_CALL_FLAG, context);
    }

    private static Context unsuppress(Context context) {
        if (getBoolean(SUPPRESSED_SPAN_FLAG, context)) {
            return context.addData(SUPPRESSED_SPAN_FLAG, false);
        }

        return context;
    }

    private static boolean getBoolean(String key, Context context) {
        Optional<Object> flag = context.getData(key);
        return flag.isPresent() && Boolean.TRUE.equals(flag.get());
    }

    private static TracerProvider getTracerProvider(TracingOptions options) {
        if (options != null
            && options.isEnabled()
            && options instanceof OpenTelemetryTracingOptions
            && ((OpenTelemetryTracingOptions) options).getOpenTelemetryProvider() != null) {
            return ((OpenTelemetryTracingOptions) options).getOpenTelemetryProvider();
        }

        return GlobalOpenTelemetry.getTracerProvider();
    }

    private static boolean isInternalOrClientSpan(SpanKind kind) {
        return kind == SpanKind.INTERNAL || kind == SpanKind.CLIENT;
    }
}
