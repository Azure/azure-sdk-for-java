package io.clientcore.core.observability.tracing;

public interface Tracer {
    SpanBuilder spanBuilder(String spanName);

    default boolean isEnabled() {
        return false;
    }
}
