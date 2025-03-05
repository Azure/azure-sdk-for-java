// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.TraceContextGetter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.HTTP_REQUEST_HEADER_TRACEPARENT_KEY;

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
                LOGGER.atVerbose()
                    .addKeyValue(HTTP_REQUEST_HEADER_TRACEPARENT_KEY, traceparent)
                    .log("Invalid traceparent header");
            }
        }
        return context == null ? FallbackSpanContext.INVALID : context;
    }

    /**
     * Validates the traceparent header according to <a href="https://www.w3.org/TR/trace-context/#traceparent-header-field-values">W3C Trace Context</a>
     *
     * @param traceparent the traceparent header value
     * @return true if the traceparent header is valid, false otherwise
     */
    private static boolean isValidTraceparent(String traceparent) {
        if (traceparent == null || traceparent.length() != 55) {
            return false;
        }

        // valid traceparent format: <version>-<trace-id>-<span-id>-<trace-flags>
        // version - only 00 is supported
        if (traceparent.charAt(0) != '0'
            || traceparent.charAt(1) != '0'
            || traceparent.charAt(2) != '-'
            || traceparent.charAt(35) != '-'
            || traceparent.charAt(52) != '-') {
            return false;
        }

        // trace-id - 32 lower case hex characters, all 0 is invalid
        boolean isAllZero = true;
        for (int i = 3; i < 35; i++) {
            char c = traceparent.charAt(i);
            if (c < '0' || c > 'f' || (c > '9' && c < 'a')) {
                return false;
            }
            if (c != '0') {
                isAllZero = false;
            }
        }
        if (isAllZero) {
            return false;
        }

        // span-id - 16 lower case hex characters, all 0 is invalid
        isAllZero = true;
        for (int i = 36; i < 52; i++) {
            char c = traceparent.charAt(i);
            if (c < '0' || c > 'f' || (c > '9' && c < 'a')) {
                return false;
            }
            if (c != '0') {
                isAllZero = false;
            }
        }

        if (isAllZero) {
            return false;
        }

        // trace-flags - 2 lower case hex characters
        for (int i = 53; i < 55; i++) {
            char c = traceparent.charAt(i);
            if (c < '0' || c > 'f' || (c > '9' && c < 'a')) {
                return false;
            }
        }

        return true;
    }
}
