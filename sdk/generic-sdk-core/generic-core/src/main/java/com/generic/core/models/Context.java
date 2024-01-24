// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.annotation.Metadata;
import com.generic.core.util.ClientLogger;

import java.util.Objects;
import java.util.Optional;

import static com.generic.core.annotation.TypeConditions.IMMUTABLE;

/**
 * {@code Context} offers a means of passing arbitrary data (key-value pairs) to pipeline policies. Most applications do
 * not need to pass arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}.
 *
 * <p>Each context object is immutable. The {@link #addData(Object, Object)} method creates a new {@code Context} object
 * that refers to its parent, forming a linked list.</p>
 */
@Metadata(conditions = IMMUTABLE)
public class Context {
    private static final ClientLogger LOGGER = new ClientLogger(Context.class);

    private static final Context[] EMPTY_CHAIN = new Context[0];

    // All fields must be immutable.
    /**
     * Signifies that no data needs to be passed to the pipeline.
     */
    public static final Context NONE = new Context(null, new Object(), null, 0) {
        @Override
        public Optional<Object> getData(Object key) {
            if (key == null) {
                throw LOGGER.logThrowableAsError(new IllegalArgumentException("key cannot be null"));
            }

            return Optional.empty();
        }

        @Override
        Context[] getContextChain() {
            return EMPTY_CHAIN;
        }
    };

    private final Context parent;
    private final Object key;
    private final Object value;
    private final int contextCount;

    /**
     * Constructs a new {@link Context} object.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <!-- src_embed com.generic.core.util.context#object-object -->
     * <pre>
     * &#47;&#47; Create an empty context having no data
     * Context emptyContext = Context.NONE;
     *
     * &#47;&#47; OpenTelemetry context can be optionally passed using PARENT_TRACE_CONTEXT_KEY
     * &#47;&#47; when OpenTelemetry context is not provided explicitly, ambient
     * &#47;&#47; io.opentelemetry.context.Context.current&#40;&#41; is used
     *
     * &#47;&#47; Context contextWithSpan = new Context&#40;PARENT_TRACE_CONTEXT_KEY, openTelemetryContext&#41;;
     * </pre>
     * <!-- end com.generic.core.util.context#object-object -->
     *
     * @param key The key with which the specified value should be associated.
     * @param value The value to be associated with the specified key.
     *
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context(Object key, Object value) {
        this.parent = null;
        this.key = Objects.requireNonNull(key, "'key' cannot be null.");
        this.value = value;
        this.contextCount = 1;
    }

    private Context(Context parent, Object key, Object value, int contextCount) {
        this.parent = parent;
        this.key = key;
        this.value = value;
        this.contextCount = contextCount;
    }

    /**
     * Gets the parent {@link Context} object.
     * <p>
     * Returns null is this is the root {@link Context}.
     *
     * @return The parent {@link Context} object, or null if this is the root {@link Context}.
     */
    public Context getParent() {
        return parent;
    }

    /**
     * Gets the key associated with this {@link Context} object.
     * <p>
     * The key will never be null.
     *
     * @return The key associated with this {@link Context} object.
     */
    public Object getKey() {
        return key;
    }

    /**
     * Gets the value associated with this {@link Context} object.
     * <p>
     * The value may be null.
     *
     * @return The value associated with this {@link Context} object.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Adds a new immutable {@link Context} object with the specified key-value pair to the existing {@link Context}
     * chain.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <!-- src_embed com.generic.core.util.context.addData#object-object -->
     * <pre>
     * &#47;&#47; Users can pass parent trace context information and additional metadata to attach to spans created by SDKs
     * &#47;&#47; using the com.generic.core.util.Context object.
     * final String hostNameValue = &quot;host-name-value&quot;;
     * final String entityPathValue = &quot;entity-path-value&quot;;
     *
     * &#47;&#47; TraceContext represents a tracing solution context type - io.opentelemetry.context.Context for OpenTelemetry.
     * final TraceContext parentContext = TraceContext.root&#40;&#41;;
     * Context parentSpanContext = new Context&#40;&quot;PARENT_TRACE_CONTEXT_KEY&quot;, parentContext&#41;;
     *
     * &#47;&#47; Add a new key value pair to the existing context object.
     * Context updatedContext = parentSpanContext.addData&#40;&quot;HOST_NAME_KEY&quot;, hostNameValue&#41;
     *     .addData&#40;&quot;ENTITY_PATH_KEY&quot;, entityPathValue&#41;;
     *
     * &#47;&#47; Both key values found on the same updated context object
     * System.out.printf&#40;&quot;Hostname value: %s%n&quot;, updatedContext.getData&#40;&quot;HOST_NAME_KEY&quot;&#41;.get&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Entity Path value: %s%n&quot;, updatedContext.getData&#40;&quot;ENTITY_PATH_KEY&quot;&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.generic.core.util.context.addData#object-object -->
     *
     * @param key The key with which the specified value should be associated.
     * @param value The value to be associated with the specified key.
     *
     * @return the new {@link Context} object containing the specified pair added to the set of pairs.
     *
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context addData(Object key, Object value) {
        if (key == null) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("key cannot be null"));
        }

        return new Context(this, key, value, contextCount + 1);
    }

    /**
     * Scans the linked-list of {@link Context} objects looking for one with the specified key. Note that the first key
     * found, i.e. the most recently added, will be returned.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <!-- src_embed com.generic.core.util.context.getData#object -->
     * <pre>
     * final String key1 = &quot;Key1&quot;;
     * final String value1 = &quot;first-value&quot;;
     *
     * &#47;&#47; Create a context object with given key and value
     * Context context = new Context&#40;key1, value1&#41;;
     *
     * &#47;&#47; Look for the specified key in the returned context object
     * Optional&lt;Object&gt; optionalObject = context.getData&#40;key1&#41;;
     * if &#40;optionalObject.isPresent&#40;&#41;&#41; &#123;
     *     System.out.printf&#40;&quot;Key1 value: %s%n&quot;, optionalObject.get&#40;&#41;&#41;;
     * &#125; else &#123;
     *     System.out.println&#40;&quot;Key1 does not exist or have data.&quot;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.generic.core.util.context.getData#object -->
     *
     * @param key The key to search for.
     *
     * @return The value of the specified key if it exists.
     *
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Optional<Object> getData(Object key) {
        if (key == null) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("key cannot be null"));
        }

        for (Context c = this; c != null; c = c.parent) {
            if (key.equals(c.key)) {
                return Optional.ofNullable(c.value);
            }

            // If the contextCount is 1 that means the next parent Context is the NONE Context.
            // Return Optional.empty now to prevent a meaningless check.
            if (c.contextCount == 1) {
                return Optional.empty();
            }
        }

        // This should never be reached but is required by the compiler.
        return Optional.empty();
    }

    /**
     * Gets the {@link Context Contexts} in the chain of Contexts that this Context is the tail.
     *
     * @return The Contexts, in oldest-to-newest order, in the chain of Contexts that this Context is the tail.
     */
    Context[] getContextChain() {
        Context[] chain = new Context[contextCount];

        int chainPosition = contextCount - 1;

        for (Context pointer = this; pointer != null; pointer = pointer.parent) {
            chain[chainPosition--] = pointer;

            // If the contextCount is 1 that means the next parent Context is the NONE Context.
            // Break out of the loop to prevent a meaningless check.
            if (pointer.contextCount == 1) {
                break;
            }
        }

        return chain;
    }

    /**
     * Merges two {@link Context Contexts} into a new {@link Context}.
     *
     * @param into Context being merged into.
     * @param from Context being merged.
     *
     * @return A new Context that is the merged Contexts.
     *
     * @throws NullPointerException If either {@code into} or {@code from} is {@code null}.
     */
    public static Context mergeContexts(Context into, Context from) {
        Objects.requireNonNull(into, "'into' cannot be null.");
        Objects.requireNonNull(from, "'from' cannot be null.");

        // If the 'into' Context is the NONE Context just return the 'from' Context.
        // This is safe as Context is immutable and prevents needing to create any new Contexts and temporary arrays.
        if (into == Context.NONE) {
            return from;
        }

        // Same goes the other way, where if the 'from' Context is the NONE Context just return the 'into' Context.
        if (from == Context.NONE) {
            return into;
        }

        Context[] contextChain = from.getContextChain();

        Context returnContext = into;

        for (Context toAdd : contextChain) {
            if (toAdd != null) {
                returnContext = returnContext.addData(toAdd.getKey(), toAdd.getValue());
            }
        }

        return returnContext;
    }
}
