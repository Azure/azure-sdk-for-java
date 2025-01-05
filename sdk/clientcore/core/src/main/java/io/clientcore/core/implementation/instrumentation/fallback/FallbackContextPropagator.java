// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.TraceContextGetter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;

final class FallbackContextPropagator implements TraceContextPropagator {
    private static final ClientLogger LOGGER = new ClientLogger(FallbackContextPropagator.class);
    static final TraceContextPropagator W3C_TRACE_CONTEXT_PROPAGATOR = new FallbackContextPropagator();

    private FallbackContextPropagator() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> void inject(InstrumentationContext spanContext, C carrier, TraceContextSetter<C> setter) {
        if (spanContext.isValid()) {
            setter.set(carrier, "traceparent",
                "00-" + spanContext.getTraceId() + "-" + spanContext.getSpanId() + "-" + spanContext.getTraceFlags());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> InstrumentationContext extract(InstrumentationContext context, C carrier, TraceContextGetter<C> getter) {
        String traceparent = getter.get(carrier, "traceparent");
        if (traceparent != null) {
            if (isValidTraceparent(traceparent)) {
                String traceId = traceparent.substring(3, 35);
                String spanId = traceparent.substring(36, 52);
                String traceFlags = traceparent.substring(53, 55);
                return new FallbackSpanContext(traceId, spanId, traceFlags, true, Span.noop());
            } else {
                LOGGER.atVerbose().addKeyValue("traceparent", traceparent).log("Invalid traceparent header");
            }
        }
        return context == null ? FallbackSpanContext.INVALID : context;
    }

    private static boolean isValidTraceparent(String traceparent) {
        if (traceparent == null || traceparent.length() != 55) {
            return false;
        }

        // version
        for (int i = 0; i < 2; i++) {
            if (traceparent.charAt(i) != '0') {
                return false;
            }
        }

        if (traceparent.charAt(2) != '-') {
            return false;
        }

        // trace-id
        boolean isAllZero = true;
        for (int i = 3; i < 35; i++) {
            char c = traceparent.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
            if (c != '0') {
                isAllZero = false;
            }
        }
        if (isAllZero) {
            return false;
        }

        if (traceparent.charAt(35) != '-') {
            return false;
        }

        // span-id
        isAllZero = true;
        for (int i = 36; i < 52; i++) {
            char c = traceparent.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
            if (c != '0') {
                isAllZero = false;
            }
        }

        if (isAllZero) {
            return false;
        }

        if (traceparent.charAt(52) != '-') {
            return false;
        }

        // trace-flags
        for (int i = 53; i < 55; i++) {
            char c = traceparent.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }

        return true;
    }
}
