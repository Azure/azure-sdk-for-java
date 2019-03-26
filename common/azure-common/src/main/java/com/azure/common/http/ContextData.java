/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.http;

import java.util.Optional;

/**
 * {@code ContextData} offers a means of passing arbitrary data (key-value pairs) to {@link HttpPipeline}'s
 * policy objects. Most applications do not need to pass arbitrary data to the pipeline and can pass
 * {@code ContextData.NONE} or {@code null}. Each context object is immutable.
 * The {@code addData(Object, Object)} method creates a new {@code ContextData} object that refers
 * to its parent, forming a linked list.
 */
public class ContextData {
    // All fields must be immutable.
    //
    /**
     * Signifies that no data need be passed to the pipeline.
     */
    public static final ContextData NONE = new ContextData(null, null, null);

    private final ContextData parent;
    private final Object key;
    private final Object value;

    /**
     * Constructs a new {@link ContextData} object.
     *
     * @param key the key
     * @param value the value
     */
    public ContextData(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        this.parent = null;
        this.key = key;
        this.value = value;
    }

    private ContextData(ContextData parent, Object key, Object value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
    }

    /**
     * Adds a new immutable {@link ContextData} object with the specified key-value pair to
     * the existing {@link ContextData} chain.
     *
     * @param key the key
     * @param value the value
     * @return the new {@link ContextData} object containing the specified pair added to the set of pairs
     */
    public ContextData addData(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        return new ContextData(this, key, value);
    }

    /**
     * Scans the linked-list of {@link ContextData} objects looking for one with the specified key.
     * Note that the first key found, i.e. the most recently added, will be returned.
     *
     * @param key the key to search for
     * @return the value of the key if it exists
     */
    public Optional<Object> getData(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        for (ContextData c = this; c != null; c = c.parent) {
            if (key.equals(c.key)) {
                return Optional.of(c.value);
            }
        }
        return Optional.empty();
    }
}