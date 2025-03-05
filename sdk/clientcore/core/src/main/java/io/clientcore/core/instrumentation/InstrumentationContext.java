// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.instrumentation.tracing.Span;

/**
 * The instrumentation context that is used to correlate telemetry data.
 * It's created along with the {@link Span} by
 * the client libraries and is propagated through SDK code.
 * <p>
 * It must provide access to <a href="https://www.w3.org/TR/trace-context/">W3C Trace Context</a>
 * properties. Implementations may use it to propagate additional information within the process.
 *
 * @see Instrumentation#createInstrumentationContext(Object)
 */
public interface InstrumentationContext {
    /**
     * Gets the trace id - 32-char long hex-encoded string that identifies end-to-end operation.
     * @return the trace id.
     */
    String getTraceId();

    /**
     * Gets the span id - 16-char hex-encoded string that identifies span - an individual
     * operation within a trace.
     * @return the span id.
     */
    String getSpanId();

    /**
     * Gets the trace flags - 2-char hex-encoded string that represents trace flags.
     * Flag with value "01" indicates that the span is sampled, "00" indicates that it is
     * not sampled.
    * @return the trace flags.
     */
    String getTraceFlags();

    /**
     * Checks if the context is valid - i.e. if it can be propagated.
     * Invalid contexts are ignored by the instrumentation.
     *
     * @return true if the context is valid, false otherwise.
     */
    boolean isValid();

    /**
     * Gets the span that is associated with this context. If there is no span associated with this context,
     * returns a no-op span.
     *
     * @return the span.
     */
    Span getSpan();
}
