package io.clientcore.core.observability;

import io.clientcore.core.observability.tracing.Span;
import io.clientcore.core.observability.tracing.SpanBuilder;
import io.clientcore.core.observability.tracing.SpanContext;
import io.clientcore.core.observability.tracing.SpanKind;
import io.clientcore.core.observability.tracing.Tracer;
import io.clientcore.core.util.Context;

import java.util.Objects;

class NoopObservabilityProvider implements ObservabilityProvider {
    static final ObservabilityProvider NOOP_PROVIDER = new NoopObservabilityProvider();

    @Override
    public Tracer getTracer(ObservabilityOptions<?> applicationOptions, LibraryObservabilityOptions libraryOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null");
        return NOOP_TRACER;
    }

    private static final Span NOOP_SPAN = new Span() {
        @Override
        public Span setAttribute(String key, Object value) {
            return this;
        }

        @Override
        public Span setError(Throwable error) {
            return this;
        }

        @Override
        public Span setError(String errorType) {
            return this;
        }

        @Override
        public void end() {

        }

        @Override
        public SpanContext getSpanContext() {
            return NOOP_SPAN_CONTEXT;
        }

        @Override
        public boolean isRecording() {
            return false;
        }

        @Override
        public Scope makeCurrent() {
            return NOOP_SCOPE;
        }
    };

    private static final SpanBuilder NOOP_SPAN_BUILDER = new SpanBuilder() {
        @Override
        public SpanBuilder setParent(Context context) {
            return this;
        }

        @Override
        public SpanBuilder setAttribute(String key, Object value) {
            return this;
        }

        @Override
        public SpanBuilder setSpanKind(SpanKind spanKind) {
            return this;
        }

        @Override
        public Span startSpan() {
            return NOOP_SPAN;
        }
    };

    private static final SpanContext NOOP_SPAN_CONTEXT = new SpanContext() {
        @Override
        public String getTraceId() {
            return "00000000000000000000000000000000";
        }

        @Override
        public String getSpanId() {
            return "0000000000000000";
        }

        @Override
        public Object getTraceFlags() {
            return null;
        }
    };

    private static final Scope NOOP_SCOPE = new Scope() {
    };
    private static final Tracer NOOP_TRACER = spanName -> NOOP_SPAN_BUILDER;
}
