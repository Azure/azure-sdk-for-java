// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.TextMapGetter;
import io.clientcore.core.instrumentation.tracing.TextMapPropagator;
import io.clientcore.core.instrumentation.tracing.TextMapSetter;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.util.Context;

class NoopInstrumentationProvider implements InstrumentationProvider {
    static final InstrumentationProvider NOOP_PROVIDER = new NoopInstrumentationProvider();

    @Override
    public Tracer getTracer() {
        return NOOP_TRACER;
    }

    @Override
    public TextMapPropagator getW3CTraceContextPropagator() {
        return NOOP_CONTEXT_PROPAGATOR;
    }

    private static final Span NOOP_SPAN = new Span() {
        @Override
        public Span setAttribute(String key, Object value) {
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
        public void end(Throwable error) {
        }

        @Override
        public boolean isRecording() {
            return false;
        }

        @Override
        public InstrumentationScope makeCurrent() {
            return NOOP_SCOPE;
        }
    };

    private static final SpanBuilder NOOP_SPAN_BUILDER = new SpanBuilder() {
        @Override
        public SpanBuilder setAttribute(String key, Object value) {
            return this;
        }

        @Override
        public Span startSpan() {
            return NOOP_SPAN;
        }
    };

    private static final InstrumentationScope NOOP_SCOPE = () -> {
    };
    private static final Tracer NOOP_TRACER = (name, kind, ctx) -> NOOP_SPAN_BUILDER;

    private static final TextMapPropagator NOOP_CONTEXT_PROPAGATOR = new TextMapPropagator() {

        @Override
        public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {

        }

        @Override
        public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
            return context;
        }
    };
}
