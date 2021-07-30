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
public class StartSpanOptions {

    /**
     * Type of Span. Only CLIENT and INTERNAL kinds are supported.
     */
    public enum Kind {
        /** Default value. Indicates that the span is used internally. */
        INTERNAL,

        /**
         * Indicates that the span covers the client-side wrapper around an RPC or other remote request.
         */
        CLIENT,
    }

    private boolean makeCurrent;
    private final Kind spanKind;
    private Map<String, Object> spanAttributes;

    /**
     * Describes span with given name and INTERNAL kind
     */
    public StartSpanOptions() {
        this(Kind.INTERNAL);
    }

    /**
     * Describes span with given name and kind
     *
     * @param kind The kind of the span to be created, only INTERNAL and CLIENT are supported.
     */
    public StartSpanOptions(Kind kind) {
        Objects.requireNonNull(kind, "'kind' cannot be null.");
        this.spanKind = kind;
        this.spanAttributes = null;
        this.makeCurrent = false;
    }

    /**
     * Sets flag that controls if span should be made current after it's started.
     * @param makeCurrent flag indicating if span should be made current after start.
     *
     * @return this instance for chaining.
     */
    public StartSpanOptions setMakeCurrent(boolean makeCurrent) {
        this.makeCurrent = makeCurrent;
        return this;
    }

    /**
     * Sets attribute on span before its started. Such attributes may affect sampling decision.
     *
     * @param key attribute key.
     * @param value attribute value. Note that underlying tracer implementations limit supported value types to
     * String, int, double, boolean, long and arrays of them.
     *
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
     * @return span kind.
     */
    public Kind getSpanKind() {
        return this.spanKind;
    }

    /**
     * Gets flag indicating if span should be made current after start.
     * @return true if span should be made current after start, false otherwise.
     */
    public boolean shouldMakeCurrent() {
        return this.makeCurrent;
    }

    /**
     * Gets all attributes on span that should be set before span is started.
     * @return attributes to be set on span and used for sampling.
     */
    public Map<String, Object> getAttributes() {
        return this.spanAttributes;
    }
}
