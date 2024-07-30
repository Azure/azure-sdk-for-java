// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import io.clientcore.core.annotation.Metadata;
import io.clientcore.core.implementation.util.InternalContext;

import java.util.Map;

import static io.clientcore.core.annotation.TypeConditions.IMMUTABLE;

/**
 * {@code Context} offers a means of passing arbitrary data (key-value pairs).
 * <p>
 * Context is an immutable construct, meaning that any time {@link #put(Object, Object)} is called a new instance of
 * Context is created. If new data is put with the same key as an existing key, the new value will hide the old value.
 * The old value will still exist within the Context but if you don't have a reference to a previous state it won't be
 * accessible.
 */
@Metadata(conditions = IMMUTABLE)
public final class Context {
    // Context is a highly used, short-lived class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(Context.class);
    private static final Context NONE = new Context(InternalContext.empty());

    private final InternalContext internal;

    private Context(InternalContext internal) {
        this.internal = internal;
    }

    /**
     * Puts a new key-value pair into the context and returns the new instance.
     * <p>
     * Context is immutable, so this returns the new instance with the key-value pair added.
     *
     * @param key The key to add.
     * @param value The value to add.
     * @return A new instance of the context with the new key-value pair added.
     * @throws NullPointerException If {@code key}  is null.
     */
    public Context put(Object key, Object value) {
        return new Context(internal.put(validateKey(key, "key"), value));
    }

    /**
     * Gets the value of the specified key.
     * <p>
     * If the key is not found in the context null will be returned.
     *
     * @param key The key to search for.
     * @return The value of the specified key if it exists, otherwise null.
     * @throws NullPointerException If {@code key} is null.
     */
    public Object get(Object key) {
        return internal.get(validateKey(key, "key"));
    }

    /**
     * An empty {@link Context} that is immutable, used in situations where there is no context-specific content to pass
     * into the request.
     *
     * @return The singleton instance of an empty {@link Context}.
     */
    public static Context none() {
        return NONE;
    }

    /**
     * Creates a new context with the given key and value.
     *
     * @param key The key to add.
     * @param value The value to add.
     * @return A new context with the given key and value.
     * @throws NullPointerException If {@code key} is null.
     */
    public static Context of(Object key, Object value) {
        return new Context(InternalContext.of(validateKey(key, "key"), value));
    }

    /**
     * Creates a new context with the given keys and values.
     *
     * @param key1 The first key to add.
     * @param value1 The first value to add.
     * @param key2 The second key to add.
     * @param value2 The second value to add.
     * @return A new context with the given keys and values.
     * @throws NullPointerException If {@code key1} or {@code key2} is null.
     */
    public static Context of(Object key1, Object value1, Object key2, Object value2) {
        return new Context(InternalContext.of(validateKey(key1, "key1"), value1, validateKey(key2, "key2"), value2));
    }

    /**
     * Creates a new context with the given keys and values.
     *
     * @param key1 The first key to add.
     * @param value1 The first value to add.
     * @param key2 The second key to add.
     * @param value2 The second value to add.
     * @param key3 The third key to add.
     * @param value3 The third value to add.
     * @return A new context with the given keys and values.
     * @throws NullPointerException If {@code key1}, {@code key2}, or {@code key3} is null.
     */
    public static Context of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3) {
        return new Context(InternalContext.of(validateKey(key1, "key1"), value1, validateKey(key2, "key2"), value2,
            validateKey(key3, "key3"), value3));
    }

    /**
     * Creates a new context with the given keys and values.
     *
     * @param key1 The first key to add.
     * @param value1 The first value to add.
     * @param key2 The second key to add.
     * @param value2 The second value to add.
     * @param key3 The third key to add.
     * @param value3 The third value to add.
     * @param key4 The fourth key to add.
     * @param value4 The fourth value to add.
     * @return A new context with the given keys and values.
     * @throws NullPointerException If {@code key1}, {@code key2}, {@code key3}, or {@code key4} is null.
     */
    public static Context of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3,
        Object key4, Object value4) {
        return new Context(InternalContext.of(validateKey(key1, "key1"), value1, validateKey(key2, "key2"), value2,
            validateKey(key3, "key3"), value3, validateKey(key4, "key4"), value4));
    }

    /**
     * Creates a new context from the given map.
     *
     * @param map The map to create the context from.
     * @return A new context with the given map.
     * @throws NullPointerException If {@code map} is null or if any key in the map is null.
     */
    public static Context of(Map<Object, Object> map) {
        return new Context(InternalContext.of(map, LOGGER));
    }

    private static Object validateKey(Object key, String keyName) {
        if (key == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException(keyName + " cannot be null"));
        }

        return key;
    }
}
