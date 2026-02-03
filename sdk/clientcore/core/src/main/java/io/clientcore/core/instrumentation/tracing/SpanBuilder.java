// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.tracing;

import io.clientcore.core.instrumentation.InstrumentationAttributes;

/**
 * Represents a span builder.
 * <p><strong>This interface is intended to be used by client libraries only. Application developers should use OpenTelemetry API directly</strong></p>
 */
public interface SpanBuilder {

    /**
     * Sets attribute value under provided key.
     * <p>
     * Attributes added on span builder are used to make sampling decisions,
     * and if the span is sampled, they are added to the resulting span.
     * <p>
     * <strong>When adding attributes, make sure to follow <a href="https://github.com/open-telemetry/semantic-conventions">OpenTelemetry semantic conventions</a>
     * </strong>
     *
     * @param key The attribute key.
     * @param value The value of the attribute. Only {@link Boolean}, {@link String}, {@link Long}, {@link Integer},
     *              and {@link Double} are supported.
     * @return Updated {@link SpanBuilder} object.
     */
    SpanBuilder setAttribute(String key, Object value);

    /**
     * Sets attributes on the span builder.
     * <p>
     * Attributes added on span builder are used to make sampling decisions,
     * and if the span is sampled, they are added to the resulting span.
     * <p>
     * <strong>When adding attributes, make sure to follow <a href="https://github.com/open-telemetry/semantic-conventions">OpenTelemetry semantic conventions</a></strong>
     * @param attributes The attributes to set.
     * @return Updated {@link SpanBuilder} object.
     */
    SpanBuilder setAllAttributes(InstrumentationAttributes attributes);

    /**
     * Starts the span.
     *
     * @return The started {@link Span} instance.
     */
    Span startSpan();
}
