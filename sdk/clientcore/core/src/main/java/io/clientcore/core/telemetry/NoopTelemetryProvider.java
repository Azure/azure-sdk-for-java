// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry;

import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanBuilder;
import io.clientcore.core.telemetry.tracing.SpanContext;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.telemetry.tracing.TracingScope;

import java.util.Objects;

class NoopTelemetryProvider implements TelemetryProvider {
    static final TelemetryProvider NOOP_PROVIDER = new NoopTelemetryProvider();

    @Override
    public Tracer getTracer(TelemetryOptions<?> applicationOptions, LibraryTelemetryOptions libraryOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null");
        return NOOP_TRACER;
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
        public SpanContext getSpanContext() {
            return NOOP_SPAN_CONTEXT;
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
        public String getTraceFlags() {
            return "00";
        }
    };

    private static final TracingScope NOOP_SCOPE = () -> {
    };
    private static final Tracer NOOP_TRACER = (name, kind, ctx) -> NOOP_SPAN_BUILDER;
}
