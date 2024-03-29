// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util;

import com.generic.core.annotation.Metadata;
import com.generic.core.implementation.util.InternalContext;

import java.util.Map;

import static com.generic.core.annotation.TypeConditions.IMMUTABLE;

/**
 * {@code Context} offers a means of passing arbitrary data (key-value pairs) to pipeline policies. Most applications do
 * not need to pass arbitrary data to the pipeline and can pass {@code Context.NONE} or {@code null}.
 *
 * <p>Each context object is immutable. The {@link #put(Object, Object)} method creates a new {@code Context}
 * object.</p>
 */
@Metadata(conditions = IMMUTABLE)
public final class Context {
    private static final ClientLogger LOGGER = new ClientLogger(Context.class);

    // All fields must be immutable.
    /**
     * Signifies that no data needs to be passed to the pipeline.
     */
    public static final Context NONE = new Context(InternalContext.empty());

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
     * @throws NullPointerException If {@code key} or {@code value} is null.
     */
    public Context put(Object key, Object value) {
        return new Context(internal.put(validateKey(key, "key"), validateValue(value, "value")));
    }

    /**
     * Puts a new key-value pair into the context if the value is not null and returns the new instance.
     * <p>
     * Context is immutable, so this returns the new instance with the key-value pair added.
     *
     * @param key The key to add.
     * @param value The value to add.
     * @return A new instance of the context with the new key-value pair added.
     * @throws NullPointerException If {@code key} is null.
     */
    public Context putNotNull(Object key, Object value) {
        if (value != null) {
            return new Context(internal.put(validateKey(key, "key"), value));
        }

        return this;
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
     * Gets the singleton instance of an empty context.
     *
     * @return The singleton instance of an empty context.
     */
    public static Context empty() {
        return NONE;
    }

    /**
     * Creates a new context with the given key and value.
     *
     * @param key The key to add.
     * @param value The value to add.
     * @return A new context with the given key and value.
     * @throws NullPointerException If {@code key} or {@code value} is null.
     */
    public static Context of(Object key, Object value) {
        return new Context(InternalContext.of(validateKey(key, "key"), validateValue(value, "value")));
    }

    /**
     * Creates a new context with the given keys and values.
     *
     * @param key1 The first key to add.
     * @param value1 The first value to add.
     * @param key2 The second key to add.
     * @param value2 The second value to add.
     * @return A new context with the given keys and values.
     * @throws NullPointerException If {@code key1}, {@code value1}, {@code key2}, or {@code value2} is null.
     */
    public static Context of(Object key1, Object value1, Object key2, Object value2) {
        return new Context(InternalContext.of(validateKey(key1, "key1"), validateValue(value1, "value1"),
            validateKey(key2, "key2"), validateValue(value2, "value2")));
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
     * @throws NullPointerException If {@code key1}, {@code value1}, {@code key2}, {@code value2}, {@code key3}, or
     * {@code value3} is null.
     */
    public static Context of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3) {
        return new Context(InternalContext.of(validateKey(key1, "key1"), validateValue(value1, "value1"),
            validateKey(key2, "key2"), validateValue(value2, "value2"),
            validateKey(key3, "key3"), validateValue(value3, "value3")));
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
     * @throws NullPointerException If {@code key1}, {@code value1}, {@code key2}, {@code value2}, {@code key3},
     * {@code value3}, {@code key4}, or {@code value4} is null.
     */
    public static Context of(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3,
        Object key4, Object value4) {
        return new Context(InternalContext.of(validateKey(key1, "key1"), validateValue(value1, "value1"),
            validateKey(key2, "key2"), validateValue(value2, "value2"),
            validateKey(key3, "key3"), validateValue(value3, "value3"),
            validateKey(key4, "key4"), validateValue(value4, "value4")));
    }

    /**
     * Creates a new context from the given map.
     *
     * @param map The map to create the context from.
     * @return A new context with the given map.
     * @throws NullPointerException If {@code map} is null or if any key or value in the map is null.
     */
    public static Context of(Map<Object, Object> map) {
        if (map == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException("map cannot be null"));
        }

        // Naive implementation that will create a new context for each key-value pair.
        // In the future this could be optimized to create contexts based on the size of the key-value pairs.
        // For example, if the key-values had 10 entries this could be optimized to create two InternalContext4 and
        // one InternalContext2 then combine them into a single InternalContextN.
        // But this method isn't called from anywhere within SDK code, so this won't be prioritized.
        InternalContext context = InternalContext.empty();
        int entryCount = 0;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            context = context.put(validateKey(entry.getKey(), "key" + entryCount),
                validateValue(entry.getValue(), "value" + entryCount));
            entryCount++;
        }

        return new Context(context);
    }

    private static Object validateKey(Object key, String keyName) {
        if (key == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException(keyName + " cannot be null"));
        }

        return key;
    }

    private static Object validateValue(Object value, String valueName) {
        if (value == null) {
            throw LOGGER.logThrowableAsError(new NullPointerException(valueName + " cannot be null"));
        }

        return value;
    }
}
