// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.utils;

import java.util.Objects;

/**
 * Immutable property bag optimized for small number of key-value pairs.
 */
public abstract class InternalContext {
    /**
     * Sentinel object representing that the context didn't find a value for the given key.
     */
    static final Object SENTINEL = new Object();

    /**
     * Get the key for the internal context.
     * <p>
     * For both {@code getKey} and {@code getValue} the value return is the last key-value pair added to the context.
     *
     * @return The key for the internal context.
     */
    public abstract Object getKey();

    /**
     * Get the value for the internal context.
     * <p>
     * For both {@code getKey} and {@code getValue} the value return is the last key-value pair added to the context.
     *
     * @return The value for the internal context.
     */
    public abstract Object getValue();

    /**
     * Get the number of key-value pairs in the internal context.
     *
     * @return The number of key-value pairs in the internal context.
     */
    public abstract int size();

    /**
     * Adds a new key-value pair to the internal context.
     * <p>
     * Internal context is immutable (shallow only), so this method will return a new instance of the internal context
     * with the new key-value pair added.
     *
     * @param key The key to add.
     * @param value The value to add.
     * @return A new instance of the internal context with the new key-value pair added.
     */
    public abstract InternalContext put(Object key, Object value);

    /**
     * Get the value for the given key.
     *
     * @param key The key to get the value for.
     * @return The value for the given key, or null if the key is not found.
     */
    public final Object get(Object key) {
        Object value = getInternal(key);

        return Objects.equals(SENTINEL, value) ? null : value;
    }

    /**
     * Get the value for the given key.
     *
     * @param key The key to get the value for.
     * @return The value for the given key, or {@link #SENTINEL} if the key is not found.
     */
    abstract Object getInternal(Object key);

    /**
     * Creates an empty {@link InternalContext}.
     *
     * @return An empty {@link InternalContext}.
     */
    public static InternalContext empty() {
        return InternalContext0.INSTANCE;
    }
}
