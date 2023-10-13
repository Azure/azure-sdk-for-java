// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

/**
 * Interface to support CRUD of Azure Resource
 *
 * @param <T> Azure resource type
 * @param <K> Azure resource key type
 * @param <P> Azure resource properties.
 *
 */
public interface ResourceCrud<T, K, P> {

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
     * Creates a value for a given key and properties.
     *
     * @param key The key.
     * @param properties The properties for the Azure resource creation.
     * @return The created value.
     */
    T create(K key, P properties);

    /**
     * Gets or creates a value for a given key.
     *
     * @param key The key.
     * @return The retrieved or created value.
     */
    T getOrCreate(K key);

    /**
     * Gets or creates a value for a given key and properties.
     *
     * @param key The key.
     * @param properties The properties for the Azure resource creation.
     * @return The retrieved or created value.
     */
    T getOrCreate(K key, P properties);
}
