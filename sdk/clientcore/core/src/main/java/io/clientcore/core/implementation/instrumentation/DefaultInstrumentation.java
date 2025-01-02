// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.Instrumentation;
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
import io.clientcore.core.util.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultInstrumentation implements Instrumentation {
    private static final String INVALID_TRACE_ID = "00000000000000000000000000000000";
    private static final String INVALID_SPAN_ID = "0000000000000000";

    private final InstrumentationOptions<?> instrumentationOptions;
    private final LibraryInstrumentationOptions libraryOptions;

    public DefaultInstrumentation(InstrumentationOptions<?> instrumentationOptions,
        LibraryInstrumentationOptions libraryOptions) {
        this.instrumentationOptions = instrumentationOptions;
        this.libraryOptions = libraryOptions;
    }

    @Override
    public Tracer getTracer() {
        return new DefaultTracer(instrumentationOptions, libraryOptions);
    }

    @Override
    public TraceContextPropagator getW3CTraceContextPropagator() {
        return DefaultContextPropagator.W3C_TRACE_CONTEXT_PROPAGATOR;
    }

    public static ClientLogger.LoggingEvent enrichLog(ClientLogger.LoggingEvent log, Context context) {
        if (OTelInitializer.isInitialized()) {
            return log;
        }

        DefaultSpan span = DefaultSpan.fromContextOrCurrent(context);
        if (span == null) {
            return log;
        }

        return log.addKeyValue("trace.id", span.getSpanContext().getTraceId())
            .addKeyValue("span.id", span.getSpanContext().getSpanId());
    }

    public static final class DefaultTracer implements Tracer {
        private final boolean isEnabled;
        private final ClientLogger logger;

        DefaultTracer(InstrumentationOptions<?> instrumentationOptions, LibraryInstrumentationOptions libraryOptions) {
            this.isEnabled = instrumentationOptions == null || instrumentationOptions.isTracingEnabled(); // TODO: probably need additional config for log-based tracing
            Map<String, Object> libraryContext = new HashMap<>(2);
            libraryContext.put("library.version", libraryOptions.getLibraryVersion());
            libraryContext.put("library.instrumentation.schema_url", libraryOptions.getSchemaUrl());

            Object providedLogger = instrumentationOptions == null ? null : instrumentationOptions.getProvider();
            if (providedLogger instanceof ClientLogger) {
                this.logger = (ClientLogger) providedLogger;
            } else {
                this.logger = new ClientLogger(libraryOptions.getLibraryName() + ".tracing", libraryContext);
            }
        }

        @Override
        public SpanBuilder spanBuilder(String spanName, SpanKind spanKind, RequestOptions requestOptions) {
            return new DefaultSpanBuilder(this.logger, spanName, spanKind, requestOptions);
        }

        @Override
        public boolean isEnabled() {
            return isEnabled;
        }
    }

    private static final class DefaultSpanBuilder implements SpanBuilder {
        private final ClientLogger.LoggingEvent log;
        private final boolean isRecording;
        private final DefaultSpanContext spanContext;

        DefaultSpanBuilder(ClientLogger logger, String spanName, SpanKind spanKind, RequestOptions requestOptions) {
            isRecording = logger.canLogAtLevel(ClientLogger.LogLevel.INFORMATIONAL);
            DefaultSpanContext parentSpanContext = requestOptions == null
                ? DefaultSpanContext.INVALID
                : DefaultSpanContext.fromContext(requestOptions.getContext());
            spanContext = DefaultSpanContext.fromParent(parentSpanContext, isRecording);
            this.log = logger.atInfo()
                .addKeyValue("span.trace_id", spanContext.getTraceId())
                .addKeyValue("span.id", spanContext.getSpanId())
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
            return new DefaultSpan(log, spanContext, isRecording);
        }
    }

    private static final class DefaultSpan implements Span {
        private final ClientLogger.LoggingEvent log;
        private final long startTime;
        private final boolean isRecording;
        private final DefaultSpanContext spanContext;
        private String errorType;

        DefaultSpan(ClientLogger.LoggingEvent log, DefaultSpanContext spanContext, boolean isRecording) {
            this.log = log;
            this.startTime = System.nanoTime();
            this.spanContext = spanContext;
            this.isRecording = isRecording;
        }

        DefaultSpan(DefaultSpanContext spanContext) {
            this.spanContext = spanContext;
            this.isRecording = false;
            this.log = null;
            this.startTime = 0;
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

        public DefaultSpanContext getSpanContext() {
            return spanContext;
        }

        public static DefaultSpan fromContextOrCurrent(Context context) {
            if (context != null) {
                Object span = context.get(TRACE_CONTEXT_KEY);
                if (span instanceof DefaultSpan) {
                    return (DefaultSpan) span;
                }

                if (span != null) {
                    return null;
                }
            }

            return DefaultScope.getCurrent();
        }
    };

    private static final class DefaultScope implements TracingScope {
        private final static ThreadLocal<DefaultSpan> CURRENT_SPAN = new ThreadLocal<>();
        private final DefaultSpan originalSpan;

        DefaultScope(DefaultSpan span) {
            this.originalSpan = CURRENT_SPAN.get();
            CURRENT_SPAN.set(span);
        }

        @Override
        public void close() {
            CURRENT_SPAN.set(originalSpan);
        }

        static DefaultSpan getCurrent() {
            return CURRENT_SPAN.get();
        }
    }

    private static final class DefaultContextPropagator implements TraceContextPropagator {
        static final TraceContextPropagator W3C_TRACE_CONTEXT_PROPAGATOR = new DefaultContextPropagator();

        private DefaultContextPropagator() {
        }

        @Override
        public <C> void inject(Context context, C carrier, TraceContextSetter<C> setter) {
            DefaultSpanContext spanContext = DefaultSpanContext.fromContext(context);
            if (spanContext.isValid()) {
                setter.set(carrier, "traceparent", "00-" + spanContext.getTraceId() + "-" + spanContext.getSpanId()
                    + "-" + spanContext.getTraceFlags());
            }
        }

        @Override
        public <C> Context extract(Context context, C carrier, TraceContextGetter<C> getter) {
            String traceparent = getter.get(carrier, "traceparent");
            if (traceparent != null) {
                if (isValidTraceparent(traceparent)) {
                    String traceId = traceparent.substring(3, 35);
                    String spanId = traceparent.substring(36, 52);
                    String traceFlags = traceparent.substring(53, 55);
                    DefaultSpanContext spanContext = new DefaultSpanContext(traceId, spanId, traceFlags);
                    return context.put(TRACE_CONTEXT_KEY, new DefaultSpan(spanContext));
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
    };

    private static final class DefaultSpanContext {
        static final DefaultSpanContext INVALID = new DefaultSpanContext();
        private final String traceId;
        private final String spanId;
        private final String traceFlags;
        private final boolean isValid;

        String getTraceId() {
            return traceId;
        }

        String getSpanId() {
            return spanId;
        }

        String getTraceFlags() {
            return traceFlags;
        }

        boolean isValid() {
            return isValid;
        }

        DefaultSpanContext() {
            this.traceId = INVALID_TRACE_ID;
            this.spanId = INVALID_SPAN_ID;
            this.traceFlags = "00";
            this.isValid = false;
        }

        DefaultSpanContext(String traceId, String spanId, String traceFlags) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.traceFlags = traceFlags;
            this.isValid = true;
        }

        static DefaultSpanContext fromParent(DefaultSpanContext parent, boolean isSampled) {
            return parent.isValid()
                ? new DefaultSpanContext(parent.traceId, getRandomId(16), isSampled ? "01" : "00")
                : new DefaultSpanContext(getRandomId(32), getRandomId(16), isSampled ? "01" : "00");
        }

        static DefaultSpanContext fromContext(Context context) {
            Object span = context.get(TRACE_CONTEXT_KEY);
            if (span instanceof DefaultSpan) {
                return ((DefaultSpan) span).getSpanContext();
            } else if (span != null) {
                // TODO log
            }

            return INVALID;
        }

        /**
         * Generates random id with given length up to 32 chars.
         */
        private static String getRandomId(int length) {
            // TODO: copy impl from OTel
            UUID uuid = UUID.randomUUID();
            return uuid.toString().replace("-", "").substring(32 - length);
        }
    };
}
