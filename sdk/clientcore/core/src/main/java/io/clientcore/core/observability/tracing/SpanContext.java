package io.clientcore.core.observability.tracing;

public interface SpanContext {
    String getTraceId();
    String getSpanId();
    Object getTraceFlags();
}
