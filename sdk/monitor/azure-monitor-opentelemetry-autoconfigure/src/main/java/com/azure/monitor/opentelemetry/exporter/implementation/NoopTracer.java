// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;

import java.util.Objects;

public final class NoopTracer implements Tracer {

    static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };

    static final Tracer INSTANCE = new NoopTracer();

    public NoopTracer() {
    }

    @Override
    public Context start(String spanName, Context context) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null");
        return context;
    }

    @Override
    public void end(String statusMessage, Throwable error, Context context) {
    }

    @Override
    public void setAttribute(String key, String value, Context context) {
        Objects.requireNonNull(key, "'key' cannot be null");
        Objects.requireNonNull(value, "'value' cannot be null");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public AutoCloseable makeSpanCurrent(Context context) {
        return NOOP_CLOSEABLE;
    }
}
