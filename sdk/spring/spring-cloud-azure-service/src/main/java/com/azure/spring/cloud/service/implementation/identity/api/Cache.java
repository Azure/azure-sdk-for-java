// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.api;

/**
 *
 * @param <K>
 * @param <V>
 */
public interface Cache<K, V> {

    void put(K key, V value);

    V get(K key);
}
