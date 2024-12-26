// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry.tracing;

/**
 * Represents a span builder.
 * <p>
 *
 * This interface should only be used by client libraries. It is not intended to be used directly by the end users.
 */
public interface SpanBuilder {
    /**
     * Sets the attribute.
     *
     * @param key The attribute key.
     * @param value The attribute value.
     * @return Updated {@link SpanBuilder} object.
     */
    SpanBuilder setAttribute(String key, Object value);

    /**
     * Starts the span.
     *
     * @return The started {@link Span} instance.
     */
    Span startSpan();
}
