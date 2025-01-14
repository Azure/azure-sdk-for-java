// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.v2.util.tracing;

import com.azure.core.v2.annotation.Fluent;
import io.clientcore.core.util.Context;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents span options that are available before span starts and describe it.
 */
@Fluent
public final class StartSpanOptions {
    private final SpanKind spanKind;
    private Map<String, Object> spanAttributes;
    private List<TracingLink> links;
    private Instant startTimeStamp;
    private Context parentContext;

    /**
     * Create start options with given kind
     *
     * @param kind The kind of the span to be created.
     */
    public StartSpanOptions(SpanKind kind) {
        Objects.requireNonNull(kind, "'kind' cannot be null.");
        this.spanKind = kind;
        this.spanAttributes = null;
        this.startTimeStamp = null;
        this.parentContext = null;
    }

    /**
     * Sets attribute on span before its started. Such attributes may affect sampling decision.
     * Adding duplicate attributes, update, or removal is discouraged, since underlying implementations
     * behavior can vary.
     *
     * @param key attribute key.
     * @param value attribute value. Note that underlying tracer implementations limit supported value types.
     *              OpenTelemetry implementation supports following types:
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
     * Sets span start timestamp. This is optional and used to record past spans.
     * If not set, uses current time.
     *
     * @param timestamp span start time.
     * @return this instance for chaining.
     */
    public StartSpanOptions setStartTimestamp(Instant timestamp) {
        this.startTimeStamp = timestamp;
        return this;
    }

    /**
     * Gets span start time.
     * @return start timestamp.
     */
    public Instant getStartTimestamp() {
        return this.startTimeStamp;
    }

    /**
     * Sets remote parent context.
     *
     * @param parent context with remote span context.
     * @return this instance for chaining.
     */
    public StartSpanOptions setRemoteParent(Context parent) {
        this.parentContext = parent;
        return this;
    }

    /**
     * Gets remote parent.
     * @return context with remote parent span context on it.
     */
    public Context getRemoteParent() {
        return this.parentContext;
    }

    /**
     * Add link to span.
     *
     * @param link link.
     * @return this instance for chaining.
     */
    public StartSpanOptions addLink(TracingLink link) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }

        this.links.add(link);
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

    /**
     * Gets links to be set on span.
     *
     * @return list of links.
     */
    public List<TracingLink> getLinks() {
        return links;
    }
}
