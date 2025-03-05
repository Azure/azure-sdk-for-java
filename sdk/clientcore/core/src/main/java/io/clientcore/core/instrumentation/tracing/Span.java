// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.tracing;

import io.clientcore.core.instrumentation.InstrumentationContext;

/**
 * A {@code Span} represents a single operation within a trace. Spans can be nested to form a trace tree.
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 */
public interface Span {
    /**
     * Sets an attribute on the span.
     * <p>
     * <strong>When adding attributes, make sure to follow <a href="https://github.com/open-telemetry/semantic-conventions">OpenTelemetry semantic conventions</a>
     * </strong>
     * <p>
     * You may optionally guard this call with {@link #isRecording()} to avoid unnecessary work
     * if the span is not sampled.
     *
     * @param key The key of the attribute.
     * @param value The value of the attribute. Only {@link Boolean}, {@link String}, {@link Long}, {@link Integer},
     *              and {@link Double} are supported.
     * @return The updated {@link Span} object.
     *
     * @see SpanBuilder#setAttribute(String, Object)
     */
    Span setAttribute(String key, Object value);

    /**
     * Sets an error on the span.
     *
     * @param errorType The error type to set on the span.
     * @return The updated {@link Span} object.
     *
     * @see #end(Throwable)
     */
    Span setError(String errorType);

    /**
     * Ends the span with exception. This should match the exception (or its cause)
     * that will be thrown to the application code.
     * <p>
     * Exceptions handled by the client library should not be passed to this method.
     * <p>
     *
     * <strong>It is important to record any exceptions that are about to be thrown
     * to the user code including unchecked ones.</strong>
     * @param throwable The exception to set on the span.
     */
    void end(Throwable throwable);

    /**
     * Ends the span.
     * <p>
     * This method may be called multiple times.
     */
    void end();

    /**
     * Checks if the span is recording.
     *
     * @return true if the span is recording, false otherwise.
     */
    boolean isRecording();

    /**
     * Makes the context representing this span current.
     * <p>
     * By making span current, we create a scope that's used to correlate all other telemetry reported under it
     * such as other spans, logs, or metrics exemplars.
     * <p>
     * The scope MUST be closed on the same thread that created it.
     * <p>
     * <strong>Closing the scope does not end the span.</strong>
     *
     * @return The {@link TracingScope} object.
     */
    TracingScope makeCurrent();

    /**
     * Gets the instrumentation context that is used to correlate telemetry data.
     *
     * @return The instrumentation context.
     */
    InstrumentationContext getInstrumentationContext();

    /**
     * Returns a no-op span.
     * @return A no-op span.
     */
    static Span noop() {
        return NoopSpan.INSTANCE;
    }
}
