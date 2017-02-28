/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisCaches;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Completable;
import rx.Observable;

/**
 * The implementation of RedisCaches and its parent interfaces.
 */
@LangDefinition
class RedisCachesImpl
        extends GroupableResourcesImpl<
                RedisCache,
                RedisCacheImpl,
                RedisResourceInner,
                RedisInner,
                RedisManager>
        implements RedisCaches {

    RedisCachesImpl(final RedisManager redisManager) {
        super(redisManager.inner().redis(), redisManager);
    }

    @Override
    public PagedList<RedisCache> list() {
        return wrapList(this.inner().list());
    }

    @Override
    public PagedList<RedisCache> listByGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Observable<RedisResourceInner> getAsync(String resourceGroupName, String name) {
        return this.inner().getAsync(resourceGroupName, name);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public RedisCacheImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected RedisCacheImpl wrapModel(String name) {
        return new RedisCacheImpl(
                name,
                new RedisResourceInner(),
                this.manager());
    }

    @Override
    protected RedisCacheImpl wrapModel(RedisResourceInner redisResourceInner) {
        if (redisResourceInner == null) {
            return null;
        }
        return new RedisCacheImpl(
                redisResourceInner.name(),
                redisResourceInner,
                this.manager());
    }
}
