// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.generic.core.implementation.util;

import com.generic.core.util.Context;

import java.util.LinkedHashMap;
import java.util.Map;
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
    int count();

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
    InternalContext addData(Object key, Object value);

    /**
     * Get the value for the given key.
     * <p>
     * If the key is not found in the internal context an empty {@link Optional} will be returned.
     *
     * @param key The key to get the value for.
     * @return The value for the given key, or an empty {@link Optional} if the key is not found.
     */
    Optional<Object> getData(Object key);

    /**
     * Get all the key-value pairs in the internal context.
     *
     * @param map The map to populate with the key-value pairs.
     */
    void getValues(LinkedHashMap<Object, Object> map);

    /**
     * Merge the given internal context with this internal context.
     *
     * @param other The internal context to merge with this internal context.
     * @return The merged internal context.
     */
    InternalContext merge(InternalContext other);

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

    /**
     * Creates an {@link InternalContext} from the given key-value pairs.
     *
     * @param keyValues The key-value pairs to create the internal context from.
     * @return The created internal context.
     */
    static InternalContext of(Map<Object, Object> keyValues) {
        if (CoreUtils.isNullOrEmpty(keyValues)) {
            throw new IllegalArgumentException("Key value map cannot be null or empty");
        }

        // Naive implementation that will create a new context for each key-value pair.
        // In the future this could be optimized to create contexts based on the size of the key-value pairs.
        // For example, if the key-values had 10 entries this could be optimized to create two InternalContext4 and
        // one InternalContext2 then combine them into a single InternalContextN.
        // But this method isn't called from anywhere within SDK code, so this won't be prioritized.
        InternalContext context = InternalContext0.INSTANCE;
        for (Map.Entry<Object, Object> entry : keyValues.entrySet()) {
            context = context.addData(entry.getKey(), entry.getValue());
        }
        return context;
    }
}
