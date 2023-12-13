// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.annotation.Metadata;
import com.generic.core.util.logging.ClientLogger;

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
