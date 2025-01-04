// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TraceContextGetter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link Instrumentation} which implements correlation and context propagation
 * and records traces as logs.
 */
public class DefaultInstrumentation implements Instrumentation {
    public static final DefaultInstrumentation DEFAULT_INSTANCE = new DefaultInstrumentation(null, null);
    private static final String INVALID_TRACE_ID = "00000000000000000000000000000000";
    private static final String INVALID_SPAN_ID = "0000000000000000";

    private final InstrumentationOptions<?> instrumentationOptions;
    private final LibraryInstrumentationOptions libraryOptions;

    /**
     * Creates a new instance of {@link DefaultInstrumentation}.
     * @param instrumentationOptions the application instrumentation options
     * @param libraryOptions the library instrumentation options
     */
    public DefaultInstrumentation(InstrumentationOptions<?> instrumentationOptions,
        LibraryInstrumentationOptions libraryOptions) {
        this.instrumentationOptions = instrumentationOptions;
        this.libraryOptions = libraryOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer getTracer() {
        return new DefaultTracer(instrumentationOptions, libraryOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TraceContextPropagator getW3CTraceContextPropagator() {
        return DefaultContextPropagator.W3C_TRACE_CONTEXT_PROPAGATOR;
    }

    public <T> InstrumentationContext createInstrumentationContext(T context) {
        if (context instanceof DefaultSpanContext) {
            return (DefaultSpanContext) context;
        } else if (context instanceof DefaultSpan) {
            return ((DefaultSpan) context).spanContext;
        } else {
            return DefaultSpanContext.INVALID;
        }
    }

    static final class DefaultTracer implements Tracer {
        private final boolean isEnabled;
        private final ClientLogger logger;

        DefaultTracer(InstrumentationOptions<?> instrumentationOptions, LibraryInstrumentationOptions libraryOptions) {
            this.isEnabled = instrumentationOptions == null || instrumentationOptions.isTracingEnabled(); // TODO: probably need additional config for log-based tracing

            Object providedLogger = instrumentationOptions == null ? null : instrumentationOptions.getProvider();
            if (providedLogger instanceof ClientLogger) {
                this.logger = (ClientLogger) providedLogger;
            } else {
                Map<String, Object> libraryContext = new HashMap<>(2);
                libraryContext.put("library.version", libraryOptions.getLibraryVersion());
                libraryContext.put("library.instrumentation.schema_url", libraryOptions.getSchemaUrl());

                this.logger = new ClientLogger(libraryOptions.getLibraryName() + ".tracing", libraryContext);
            }
        }

        @Override
        public SpanBuilder spanBuilder(String spanName, SpanKind spanKind, InstrumentationContext instrumentationContext) {
            return new DefaultSpanBuilder(this.logger, spanName, spanKind, instrumentationContext);
        }

        @Override
        public boolean isEnabled() {
            return isEnabled;
        }
    }

    private static final class DefaultSpanBuilder implements SpanBuilder {
        private final ClientLogger.LoggingEvent log;
        private final boolean isRecording;
        private final DefaultSpanContext parentSpanContext;

        DefaultSpanBuilder(ClientLogger logger, String spanName, SpanKind spanKind, InstrumentationContext instrumentationContext) {
            isRecording = logger.canLogAtLevel(ClientLogger.LogLevel.INFORMATIONAL);
            DefaultSpanContext parentSpanContext = instrumentationContext instanceof DefaultSpanContext
                ? (DefaultSpanContext) instrumentationContext : DefaultSpanContext.INVALID;
            this.parentSpanContext = parentSpanContext;
            this.log = logger.atInfo()
                .addKeyValue("span.parent.id", parentSpanContext.getSpanId())
                .addKeyValue("span.name", spanName)
                .addKeyValue("span.kind", spanKind.name());
        }

        @Override
        public SpanBuilder setAttribute(String key, Object value) {
            this.log.addKeyValue(key, value);
            return this;
        }

        @Override
        public Span startSpan() {
            return new DefaultSpan(log, parentSpanContext, isRecording);
        }
    }

    private static final class DefaultSpan implements Span {
        private final ClientLogger.LoggingEvent log;
        private final long startTime;
        private final boolean isRecording;
        private final DefaultSpanContext spanContext;
        private String errorType;

        DefaultSpan(ClientLogger.LoggingEvent log, DefaultSpanContext parentSpanContext, boolean isRecording) {
            this.log = log;
            this.startTime = System.nanoTime();
            this.isRecording = isRecording;
            this.spanContext = DefaultSpanContext.create(parentSpanContext, isRecording, this);
            if (log != null) {
                this.log
                    .addKeyValue("trace.id", spanContext.getTraceId())
                    .addKeyValue("span.id", spanContext.getSpanId());
            }
        }

        DefaultSpan(DefaultSpanContext parentSpanContext) {
            this(null, parentSpanContext, false);
        }

        @Override
        public Span setAttribute(String key, Object value) {
            if (log != null) {
                log.addKeyValue(key, value);
            }
            return this;
        }

        @Override
        public Span setError(String errorType) {
            this.errorType = errorType;
            return this;
        }

        @Override
        public void end() {
            end(null);
        }

        @Override
        public void end(Throwable error) {
            if (log == null) {
                return;
            }

            if (isRecording) {
                double durationMs = (System.nanoTime() - startTime) / 1_000_000.0;
                log.addKeyValue("span.duration.ms", durationMs);
                if (error != null || errorType != null) {
                    setAttribute("error.type", errorType != null ? errorType : error.getClass().getCanonicalName());
                }
            }

            if (error != null) {
                log.log("span ended", error);
            } else {
                log.log("span ended");
            }
        }

        @Override
        public boolean isRecording() {
            return isRecording;
        }

        @Override
        public TracingScope makeCurrent() {
            return new DefaultScope(this);
        }

        @Override
        public InstrumentationContext getInstrumentationContext() {
            return spanContext;
        }
    }

    private static final class DefaultScope implements TracingScope {
        private static final ThreadLocal<DefaultSpan> CURRENT_SPAN = new ThreadLocal<>();
        private final DefaultSpan originalSpan;

        DefaultScope(DefaultSpan span) {
            this.originalSpan = CURRENT_SPAN.get();
            CURRENT_SPAN.set(span);
        }

        @Override
        public void close() {
            CURRENT_SPAN.set(originalSpan);
        }
    }

    private static final class DefaultContextPropagator implements TraceContextPropagator {
        static final TraceContextPropagator W3C_TRACE_CONTEXT_PROPAGATOR = new DefaultContextPropagator();

        private DefaultContextPropagator() {
        }

        @Override
        public <C> void inject(InstrumentationContext spanContext, C carrier, TraceContextSetter<C> setter) {
            if (spanContext.isValid()) {
                setter.set(carrier, "traceparent", "00-" + spanContext.getTraceId() + "-" + spanContext.getSpanId()
                    + "-" + spanContext.getTraceFlags());
            }
        }

        @Override
        public <C> InstrumentationContext extract(InstrumentationContext context, C carrier, TraceContextGetter<C> getter) {
            String traceparent = getter.get(carrier, "traceparent");
            if (traceparent != null) {
                if (isValidTraceparent(traceparent)) {
                    String traceId = traceparent.substring(3, 35);
                    String spanId = traceparent.substring(36, 52);
                    String traceFlags = traceparent.substring(53, 55);
                    return new DefaultSpanContext(traceId, spanId, traceFlags, Span.noop());
                } else {
                    // TODO log
                }
            }
            return context;
        }

        private static boolean isValidTraceparent(String traceparent) {
            // TODO: add more validation
            return traceparent.startsWith("00-") && traceparent.length() == 55;
        }
    }

    private static final class DefaultSpanContext implements InstrumentationContext {
        static final DefaultSpanContext INVALID = new DefaultSpanContext();
        private final String traceId;
        private final String spanId;
        private final String traceFlags;
        private final boolean isValid;
        private final Span span;

        @Override
        public String getTraceId() {
            return traceId;
        }

        @Override
        public String getSpanId() {
            return spanId;
        }

        @Override
        public boolean isValid() {
            return isValid;
        }

        @Override
        public Span getSpan() {
            return this.span;
        }

        @Override
        public String getTraceFlags() {
            return traceFlags;
        }

        DefaultSpanContext() {
            this.traceId = INVALID_TRACE_ID;
            this.spanId = INVALID_SPAN_ID;
            this.traceFlags = "00";
            this.isValid = false;
            this.span = Span.noop();
        }

        DefaultSpanContext(String traceId, String spanId, String traceFlags, Span span) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.traceFlags = traceFlags;
            this.isValid = true;
            this.span = span;
        }

        static DefaultSpanContext create(DefaultSpanContext parent, boolean isSampled, DefaultSpan span) {
            return parent.isValid()
                ? new DefaultSpanContext(parent.traceId, getRandomId(16), isSampled ? "01" : "00", span)
                : new DefaultSpanContext(getRandomId(32), getRandomId(16), isSampled ? "01" : "00", span);
        }

        /**
         * Generates random id with given length up to 32 chars.
         */
        private static String getRandomId(int length) {
            // TODO: copy impl from OTel
            UUID uuid = UUID.randomUUID();
            return uuid.toString().replace("-", "").substring(32 - length);
        }
    }
}
