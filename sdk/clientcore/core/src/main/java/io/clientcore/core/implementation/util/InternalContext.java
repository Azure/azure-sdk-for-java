// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.util;

import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import java.util.Map;
import java.util.Objects;

/**
 * Internal representation of {@link Context}.
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

    /**
     * Creates an {@link InternalContext} from the given key-value pair.
     *
     * @param key The key for the internal context.
     * @param value The value for the internal context.
     * @return The created internal context.
     */
    public static InternalContext of(Object key, Object value) {
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
    public static InternalContext of(Object key1, Object value1, Object key2, Object value2) {
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
    public static InternalContext of(Object key1, Object value1, Object key2, Object value2, Object key3,
        Object value3) {
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
    public static InternalContext of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3,
        Object key4, Object value4) {
        return new InternalContext4(key1, value1, key2, value2, key3, value3, key4, value4);
    }

    /**
     * Creates a new context from the given map.
     *
     * @param map The map to create the context from.
     * @return A new context with the given map.
     * @throws NullPointerException If {@code map} is null or if any key in the map is null.
     */
    public static InternalContext of(Map<Object, Object> map, ClientLogger logger) {
        if (map == null) {
            throw logger.logThrowableAsError(new NullPointerException("map cannot be null"));
        }

        // Naive implementation that will create a new context for each key-value pair.
        // In the future this could be optimized to create contexts based on the size of the key-value pairs.
        // For example, if the key-values had 10 entries this could be optimized to create two InternalContext4 and
        // one InternalContext2 then combine them into a single InternalContextN.
        // But this method isn't called from anywhere within SDK code, so this won't be prioritized.
        InternalContext context = InternalContext.empty();
        int entryCount = 0;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            context = context.put(validateKey(entry.getKey(), "key" + entryCount, logger), entry.getValue());
            entryCount++;
        }

        return context;
    }

    private static Object validateKey(Object key, String keyName, ClientLogger logger) {
        if (key == null) {
            throw logger.logThrowableAsError(new NullPointerException(keyName + " cannot be null"));
        }

        return key;
    }
}
