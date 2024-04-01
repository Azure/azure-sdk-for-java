// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.generic.core.implementation.util;

import com.generic.core.util.Context;

import java.util.Optional;

/**
 * Internal representation of {@link Context}.
 */
public interface InternalContext {
    /**
     * Get the key for the internal context.
     * <p>
     * For both {@code getKey} and {@code getValue} the value return is the last key-value pair added to the context.
     *
     * @return The key for the internal context.
     */
    Object getKey();

    /**
     * Get the value for the internal context.
     * <p>
     * For both {@code getKey} and {@code getValue} the value return is the last key-value pair added to the context.
     *
     * @return The value for the internal context.
     */
    Object getValue();

    /**
     * Get the number of key-value pairs in the internal context.
     *
     * @return The number of key-value pairs in the internal context.
     */
    int size();

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
    InternalContext put(Object key, Object value);

    /**
     * Get the value for the given key.
     * <p>
     * If the key is not found in the internal context an empty {@link Optional} will be returned.
     *
     * @param key The key to get the value for.
     * @return The value for the given key, or an empty {@link Optional} if the key is not found.
     */
    Object get(Object key);

    /**
     * Creates an empty {@link InternalContext}.
     *
     * @return An empty {@link InternalContext}.
     */
    static InternalContext empty() {
        return InternalContext0.INSTANCE;
    }

    /**
     * Creates an {@link InternalContext} from the given key-value pair.
     *
     * @param key The key for the internal context.
     * @param value The value for the internal context.
     * @return The created internal context.
     */
    static InternalContext of(Object key, Object value) {
        return new InternalContext1(key, value);
    }

    /**
     * Creates an {@link InternalContext} from the given key-value pairs.
     *
     * @param key1 The key for the first key-value pair.
     * @param value1 The value for the first key-value pair.
     * @param key2 The key for the second key-value pair.
     * @param value2 The value for the second key-value pair.
     * @return The created internal context.
     */
    static InternalContext of(Object key1, Object value1, Object key2, Object value2) {
        return new InternalContext2(key1, value1, key2, value2);
    }

    /**
     * Creates an {@link InternalContext} from the given key-value pairs.
     *
     * @param key1 The key for the first key-value pair.
     * @param value1 The value for the first key-value pair.
     * @param key2 The key for the second key-value pair.
     * @param value2 The value for the second key-value pair.
     * @param key3 The key for the third key-value pair.
     * @param value3 The value for the third key-value pair.
     * @return The created internal context.
     */
    static InternalContext of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3) {
        return new InternalContext3(key1, value1, key2, value2, key3, value3);
    }

    /**
     * Creates an {@link InternalContext} from the given key-value pairs.
     *
     * @param key1 The key for the first key-value pair.
     * @param value1 The value for the first key-value pair.
     * @param key2 The key for the second key-value pair.
     * @param value2 The value for the second key-value pair.
     * @param key3 The key for the third key-value pair.
     * @param value3 The value for the third key-value pair.
     * @param key4 The key for the fourth key-value pair.
     * @param value4 The value for the fourth key-value pair.
     * @return The created internal context.
     */
    static InternalContext of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3,
        Object key4, Object value4) {
        return new InternalContext4(key1, value1, key2, value2, key3, value3, key4, value4);
    }
}
