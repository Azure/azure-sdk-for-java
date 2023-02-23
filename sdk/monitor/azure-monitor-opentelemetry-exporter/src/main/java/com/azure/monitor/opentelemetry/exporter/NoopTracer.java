package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;

import java.util.Objects;

class NoopTracer implements Tracer {

    static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };

    static final Tracer INSTANCE = new NoopTracer();

    NoopTracer() {
    }

    public Context start(String spanName, Context context) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null");
        return context;
    }

    public void end(String statusMessage, Throwable error, Context context) {
    }

    public void setAttribute(String key, String value, Context context) {
        Objects.requireNonNull(key, "'key' cannot be null");
        Objects.requireNonNull(value, "'value' cannot be null");
    }

    public boolean isEnabled() {
        return false;
    }

    public AutoCloseable makeSpanCurrent(Context context) {
        return NOOP_CLOSEABLE;
    }
}
