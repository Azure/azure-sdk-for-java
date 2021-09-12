// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.api;

/**
 * Interface to support CRUD of Azure Resource
 *
 * @param <T> Azure resource type
 * @param <K> Azure resource key type
 *
 */
public interface ResourceCrud<T, K> {

    T get(K key);

    boolean exists(K key);

    T create(K key);

    T getOrCreate(K key);
}
