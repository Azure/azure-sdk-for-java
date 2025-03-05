// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.TracingScope;

final class FallbackScope implements TracingScope {
    private static final ClientLogger LOGGER = new ClientLogger(FallbackScope.class);
    private static final ThreadLocal<FallbackSpan> CURRENT_SPAN = new ThreadLocal<>();
    private final FallbackSpan originalSpan;
    private final FallbackSpan span;

    FallbackScope(FallbackSpan span) {
        this.originalSpan = CURRENT_SPAN.get();
        this.span = span;
        CURRENT_SPAN.set(span);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (CURRENT_SPAN.get() == span) {
            CURRENT_SPAN.set(originalSpan);
        } else {
            LOGGER.atVerbose().log("Attempting to close scope that is not the current. Ignoring.");
        }
    }

    static Span getCurrentSpan() {
        FallbackSpan span = CURRENT_SPAN.get();
        return span == null ? Span.noop() : span;
    }
}
