// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.redis.RedisManager;
import com.azure.resourcemanager.redis.fluent.RedisClient;
import com.azure.resourcemanager.redis.fluent.models.OperationInner;
import com.azure.resourcemanager.redis.fluent.models.RedisResourceInner;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisCaches;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** The implementation of RedisCaches and its parent interfaces. */
public class RedisCachesImpl
    extends TopLevelModifiableResourcesImpl<RedisCache, RedisCacheImpl, RedisResourceInner, RedisClient, RedisManager>
    implements RedisCaches {

    public RedisCachesImpl(final RedisManager redisManager) {
        super(redisManager.serviceClient().getRedis(), redisManager);
    }

    @Override
    public RedisCacheImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected RedisCacheImpl wrapModel(String name) {
        return new RedisCacheImpl(name, new RedisResourceInner(), this.manager());
    }

    @Override
    protected RedisCacheImpl wrapModel(RedisResourceInner redisResourceInner) {
        if (redisResourceInner == null) {
            return null;
        }
        return new RedisCacheImpl(redisResourceInner.name(), redisResourceInner, this.manager());
    }

    @Override
    public PagedIterable<OperationInner> listOperations() {
        return new PagedIterable<>(listOperationsAsync());
    }

    @Override
    public PagedFlux<OperationInner> listOperationsAsync() {
        return this.manager().serviceClient().getOperations().listAsync();
    }
}
