package io.clientcore.core.observability.tracing;

public interface Tracer {
    String DISABLE_TRACING_KEY = "disable-tracing";

    SpanBuilder spanBuilder(String spanName);

    default boolean isEnabled() {
        return false;
    }
}
