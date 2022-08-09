// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.adapter;


/**
 * Describe the cache key.
 */
public interface CacheKeyDescriptor<KEY, KEYContext> {

    /**
     * Get the cache key.
     * @param keyContext The context to build cache key.
     * @return The cache key for caching.
     */
    KEY getCacheKey(KEYContext keyContext);
}
