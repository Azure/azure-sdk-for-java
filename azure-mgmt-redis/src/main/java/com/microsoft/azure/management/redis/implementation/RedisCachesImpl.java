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

    private final PatchSchedulesInner pathcSchedulesClient;

    RedisCachesImpl(
            final RedisInner client,
            final PatchSchedulesInner patchClient,
            final RedisManager redisManager) {
        super(client, redisManager);
        this.pathcSchedulesClient = patchClient;
    }

    @Override
    public PagedList<RedisCache> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public PagedList<RedisCache> listByGroup(String groupName) {
        return wrapList(this.innerCollection.listByResourceGroup(groupName));
    }

    @Override
    public RedisCache getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
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
                this.pathcSchedulesClient,
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected RedisCacheImpl wrapModel(RedisResourceInner redisResourceInner) {
        return new RedisCacheImpl(
                redisResourceInner.name(),
                redisResourceInner,
                this.pathcSchedulesClient,
                this.innerCollection,
                super.myManager);
    }
}
