// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry.tracing;

import io.clientcore.core.telemetry.Scope;

/**
 * A {@code Span} represents a single operation within a trace. Spans can be nested to form a trace tree.
 * <p>
 *
 * This span is a wrapper over OpenTelemetry's span,
 * This interface should only be used by client libraries. It is not intended to be used directly by the end users.
 */
public interface Span {
    /**
     * Sets an attribute on the span.
     *
     * @param key The key of the attribute.
     * @param value The value of the attribute. Only Boolean, String, Long, Integer, Double are supported.
     * @return The updated {@link Span} object.
     */
    Span setAttribute(String key, Object value);

    /**
     * Sets an error on the span.
     *
     * @param error The error to set on the span.
     * @return The updated {@link Span} object.
     */
    Span setError(Throwable error);

    /**
     * Sets an error on the span.
     *
     * @param errorType The error type to set on the span.
     * @return The updated {@link Span} object.
     */
    Span setError(String errorType);

    /**
     * Ends the span.
     */
    void end();

    /**
     * Gets the span context.
     *
     * @return The span context.
     */
    SpanContext getSpanContext();

    /**
     * Checks if the span is recording.
     *
     * @return true if the span is recording, false otherwise.
     */
    boolean isRecording();

    /**
     * Makes the span the current span.
     *
     * @return The {@link Scope} object.
     */
    Scope makeCurrent();
}
