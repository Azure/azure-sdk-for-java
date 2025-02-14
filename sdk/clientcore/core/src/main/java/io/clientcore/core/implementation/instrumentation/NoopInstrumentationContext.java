// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.tracing.Span;

/**
 * No-op implementation of {@link InstrumentationContext}.
 */
public final class NoopInstrumentationContext implements InstrumentationContext {
    public static final NoopInstrumentationContext INSTANCE = new NoopInstrumentationContext();

    private NoopInstrumentationContext() {
    }

    @Override
    public String getTraceId() {
        return null;
    }

    @Override
    public String getSpanId() {
        return null;
    }

    @Override
    public String getTraceFlags() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Span getSpan() {
        return Span.noop();
    }
}
