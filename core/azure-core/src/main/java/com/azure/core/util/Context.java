// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.annotation.Immutable;
import com.azure.core.implementation.util.ImplUtils;
import java.util.Map;
import java.util.Optional;

/**
 * {@code Context} offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
 * Most applications do not need to pass arbitrary data to the pipeline and can pass {@code Context.NONE} or
 * {@code null}. Each context object is immutable. The {@code addData(Object, Object)} method creates a new
 * {@code Context} object that refers to its parent, forming a linked list.
 */
@Immutable
public class Context {
    // All fields must be immutable.
    //
    /**
     * Signifies that no data need be passed to the pipeline.
     */
    public static final Context NONE = new Context(null, null, null);

    private final Context parent;
    private final Object key;
    private final Object value;

    /**
     * Constructs a new {@link Context} object.
     *
     * @param key the key
     * @param value the value
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        this.parent = null;
        this.key = key;
        this.value = value;
    }

    private Context(Context parent, Object key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    /**
     * Adds a new immutable {@link Context} object with the specified key-value pair to
     * the existing {@link Context} chain.
     *
     * @param key the key
     * @param value the value
     * @return the new {@link Context} object containing the specified pair added to the set of pairs
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Context addData(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return new Context(this, key, value);
    }

    public static Context of(Map<Object, Object> keyValues) {
        if (ImplUtils.isNullOrEmpty(keyValues)) {
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
     * @param key the key to search for
     * @return the value of the key if it exists
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     */
    public Optional<Object> getData(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        for (Context c = this; c != null; c = c.parent) {
            if (key.equals(c.key)) {
                return Optional.of(c.value);
            }
        }
        return Optional.empty();
    }
}
