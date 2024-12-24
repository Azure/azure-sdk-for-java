// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.observability.tracing;

import io.clientcore.core.util.Context;

/**
 * Represents a span builder.
 * <p>
 *
 * This interface should only be used by client libraries. It is not intended to be used directly by the end users.
 */
public interface SpanBuilder {
    /**
     * Sets the parent span context.
     *
     * @param context The parent span context.
     * @return The span builder.
     */
    SpanBuilder setParent(Context context);

    /**
     * Sets the attribute.
     *
     * @param key The attribute key.
     * @param value The attribute value.
     * @return The span builder.
     */
    SpanBuilder setAttribute(String key, Object value);

    /**
     * Sets the span kind.
     *
     * @param spanKind The span kind.
     * @return The span builder.
     */
    SpanBuilder setSpanKind(SpanKind spanKind);

    /**
     * Starts the span.
     *
     * @return The span.
     */
    Span startSpan();
}
