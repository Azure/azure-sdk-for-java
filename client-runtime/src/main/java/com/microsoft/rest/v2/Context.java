/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import java.util.Optional;

/**
 * {@code Context} offers a means of passing arbitrary data (key/value pairs) to an {@link com.microsoft.rest.v2.http.HttpPipeline}'s
 * policy objects. Most applications do not need to pass arbitrary data to the pipeline and can pass
 * {@code Context.NONE} or {@code null}. Each context object is immutable. The {@code withContext} with data
 * method creates a new {@code Context} object that refers to its parent, forming a linked list.
 */
public class Context {
    // All fields must be immutable.

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
     * @param key
     *      The key.
     * @param value
     *      The value.
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
     * Adds a new immutable {@link Context} object with the specified key/value pair to the existing {@link Context}
     * chain.
     *
     * @param key
     *      The key.
     * @param value
     *      The value.
     * @return
     *      The new {@link Context} object containing the specified pair added to the set of pairs.
     */
    public Context addData(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return new Context(this, key, value);
    }

    /**
     * Scans a linked-list of {@link Context} objects looking for one with the specified key. Note that the first key
     * found, i.e. the most recently added, will be returned.
     *
     * @param key
     *      The key to search for.
     * @return
     *      The value of the key if it exists.
     */
    public Optional<Object> getData(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        for (Context c = this; c != null; c = c.parent) {
            if (c.key.equals(key)) {
                return Optional.of(c.value);
            }
        }
        return Optional.empty();
    }
}
