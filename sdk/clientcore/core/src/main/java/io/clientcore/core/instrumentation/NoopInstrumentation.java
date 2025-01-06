// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.TraceContextGetter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.util.Context;

class NoopInstrumentation implements Instrumentation {
    static final Instrumentation NOOP_PROVIDER = new NoopInstrumentation();

    @Override
    public Tracer getTracer() {
        return NOOP_TRACER;
    }

    @Override
    public TraceContextPropagator getW3CTraceContextPropagator() {
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
        public TracingScope makeCurrent() {
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

    private static final TracingScope NOOP_SCOPE = () -> {
    };
    private static final Tracer NOOP_TRACER = (name, kind, ctx) -> NOOP_SPAN_BUILDER;

    private static final TraceContextPropagator NOOP_CONTEXT_PROPAGATOR = new TraceContextPropagator() {

        @Override
        public <C> void inject(Context context, C carrier, TraceContextSetter<C> setter) {

        }

        @Override
        public <C> Context extract(Context context, C carrier, TraceContextGetter<C> getter) {
            return context;
        }
    };
}
