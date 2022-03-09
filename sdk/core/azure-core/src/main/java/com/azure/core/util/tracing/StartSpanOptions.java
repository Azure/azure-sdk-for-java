// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.tracing;

import com.azure.core.annotation.Fluent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents span options that are available before span starts and describe it.
 */
@Fluent
public final class StartSpanOptions {
    private final SpanKind spanKind;
    private Map<String, Object> spanAttributes;

    /**
     * Describes span with given name and kind
     *
     * @param kind The kind of the span to be created.
     */
    public StartSpanOptions(SpanKind kind) {
        Objects.requireNonNull(kind, "'kind' cannot be null.");
        this.spanKind = kind;
        this.spanAttributes = null;
    }

    /**
     * Sets attribute on span before its started. Such attributes may affect sampling decision.
     *
     * @param key attribute key.
     * @param value attribute value. Note that underlying tracer implementations limit supported value types:
     * <ul>
     *     <li>{@link String}</li>
     *     <li>{@code int}</li>
     *     <li>{@code double}</li>
     *     <li>{@code boolean}</li>
     *     <li>{@code long}</li>
     *     <li>Arrays of the above</li>
     * </ul>
     * @return this instance for chaining.
     */
    public StartSpanOptions setAttribute(String key, Object value) {
        if (this.spanAttributes == null) {
            this.spanAttributes = new HashMap<>();
        }

        this.spanAttributes.put(key, value);
        return this;
    }

    /**
     * Gets span kind.
     *
     * @return span kind.
     */
    public SpanKind getSpanKind() {
        return this.spanKind;
    }

    /**
     * Gets all attributes on span that should be set before span is started.
     *
     * @return attributes to be set on span and used for sampling.
     */
    public Map<String, Object> getAttributes() {
        return this.spanAttributes;
    }
}
