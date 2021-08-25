// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code Context} offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
 * Most applications do not need to pass arbitrary data to the pipeline and can pass {@code Context.NONE} or
 * {@code null}.
 * <p>
 * Each context object is immutable. The {@link #addData(Object, Object)} method creates a new
 * {@code Context} object that refers to its parent, forming a linked list.
 */
@Immutable
public class Context {
    private final ClientLogger logger = new ClientLogger(Context.class);

    // All fields must be immutable.
    //
    /**
     * Signifies that no data needs to be passed to the pipeline.
     */
    public static final Context NONE = new Context(null, null, null);

    private final Context parent;
    private final Object key;
    private final Object value;

    private final int size;

    protected Context() {
        this.parent = null;
        this.key = null;
        this.value = null;
        this.size = 0;
    }

    /**
     * Constructs a new {@link Context} object.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.context#object-object}
     *
     * @param key The key with which the specified value should be associated.
     * @param value The value to be associated with the specified key.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context(Object key, Object value) {
        this.parent = null;
        this.key = Objects.requireNonNull(key, "'key' cannot be null.");
        this.value = value;
        this.size = 1;
    }

    private Context(Context parent, Object key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;

        // we need to check if this key is shadowing an existing key
        this.size = parent == null
                        ? 1
                        : parent.size + (parent.getData(key).isPresent() ? 0 : 1);
    }

    /**
     * Adds a new immutable {@link Context} object with the specified key-value pair to
     * the existing {@link Context} chain.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.context.addData#object-object}
     *
     * @param key The key with which the specified value should be associated.
     * @param value The value to be associated with the specified key.
     * @return the new {@link Context} object containing the specified pair added to the set of pairs.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context addData(Object key, Object value) {
        if (key == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key cannot be null"));
        }
        return new Context(this, key, value);
    }

    /**
     * Creates a new immutable {@link Context} object with all the keys and values provided by
     * the input {@link Map}.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.context.of#map}
     *
     * @param keyValues The input key value pairs that will be added to this context.
     * @return Context object containing all the key-value pairs in the input map.
     * @throws IllegalArgumentException If {@code keyValues} is {@code null} or empty
     */
    public static Context of(Map<Object, Object> keyValues) {
        if (CoreUtils.isNullOrEmpty(keyValues)) {
            throw new IllegalArgumentException("Key value map cannot be null or empty");
        }

        Context context = null;
        for (Map.Entry<Object, Object> entry : keyValues.entrySet()) {
            if (context == null) {
                context = new Context(entry.getKey(), entry.getValue());
            } else {
                context = context.addData(entry.getKey(), entry.getValue());
            }
        }
        return context;
    }

    /**
     * Scans the linked-list of {@link Context} objects looking for one with the specified key.
     * Note that the first key found, i.e. the most recently added, will be returned.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.context.getData#object}
     *
     * @param key The key to search for.
     * @return The value of the specified key if it exists.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Optional<Object> getData(Object key) {
        if (key == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key cannot be null"));
        }
        for (Context c = this; c != null; c = c.parent) {
            if (key.equals(c.key)) {
                return Optional.of(c.value);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the number of elements in this Context, measured from the given context to the root element. If there
     * are children of this context, they are not counted.
     *
     * @return The number of elements in this context.
     */
    public int size() {
        return size;
    }

    /**
     * Scans the linked-list of {@link Context} objects populating a {@link Map} with the values of the context.
     *
     * <p><strong>Code samples</strong></p>
     *
     * {@codesnippet com.azure.core.util.Context.getValues}
     *
     * @return A map containing all values of the context linked-list.
     */
    public Map<Object, Object> getValues() {
        return getValuesHelper(new HashMap<>());
    }

    private Map<Object, Object> getValuesHelper(Map<Object, Object> values) {
        if (key != null) {
            values.putIfAbsent(key, value);
        }

        return (parent == null) ? values : parent.getValuesHelper(values);
    }
}
