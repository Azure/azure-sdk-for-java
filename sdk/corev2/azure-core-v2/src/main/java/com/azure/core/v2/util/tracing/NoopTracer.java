// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.tracing;

import io.clientcore.core.util.Context;

import java.util.Objects;

class NoopTracer implements Tracer {
    static final Tracer INSTANCE = new NoopTracer();

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
        return Utils.NOOP_CLOSEABLE;
    }
}
