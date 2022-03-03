// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

/**
 * Interface to support CRUD of Azure Resource
 *
 * @param <T> Azure resource type
 * @param <K> Azure resource key type
 *
 */
public interface ResourceCrud<T, K> {

    /**
     * Gets a value for a given key.
     *
     * @param key The key.
     * @return The retrieved value.
     */
    T get(K key);

    /**
     * Checks for the existence of a given key.
     *
     * @param key The key.
     * @return Whether the key exists.
     */
    boolean exists(K key);

    /**
     * Creates a value for a given key.
     *
     * @param key The key.
     * @return The created value.
     */
    T create(K key);

    /**
     * Gets or creates a value for a given key.
     *
     * @param key The key.
     * @return The retrieved or created value.
     */
    T getOrCreate(K key);
}
