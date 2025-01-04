package io.clientcore.core.instrumentation;

import io.clientcore.core.instrumentation.tracing.Span;

public interface InstrumentationContext {
    String getTraceId();
    String getSpanId();
    String getTraceFlags();
    boolean isValid();
    Span getSpan();
}
