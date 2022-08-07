// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.api;

/**
 * Interface to be implemented by classes that wish to provide cache functionality.
 * @param <K> The type of cache key.
 * @param <V> The type of cache value.
 */
public interface Cache<K, V> {

    void put(K key, V value);

    V get(K key);
}
