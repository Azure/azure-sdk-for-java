// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.observability.tracing;

/**
 * Represents a tracer - a component that creates spans.
 * <p>
 *
 * This interface should only be used by client libraries. It is not intended to be used directly by the end users.
 */
public interface Tracer {
    /**
     * Creates a new span builder.
     *
     * @param spanName The name of the span.
     * @return The span builder.
     */
    SpanBuilder spanBuilder(String spanName);

    /**
     * Checks if the tracer is enabled.
     *
     * @return true if the tracer is enabled, false otherwise.
     */
    default boolean isEnabled() {
        return false;
    }
}
