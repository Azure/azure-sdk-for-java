/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisCaches;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;

import java.io.IOException;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
class RedisCachesImpl
        extends GroupableResourcesImpl<
        RedisCache,
        RedisCacheImpl,
        RedisResourceInner,
        RedisInner,
        RedisManager>
        implements RedisCaches {

    RedisCachesImpl(
            final RedisInner client,
            final RedisManager storageManager) {
        super(client, storageManager);
    }

    @Override
    public PagedList<RedisCache> list() throws CloudException, IOException {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public PagedList<RedisCache> listByGroup(String groupName) throws CloudException, IOException {
        return wrapList(this.innerCollection.listByResourceGroup(groupName));
    }

    @Override
    public RedisCache getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
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
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected RedisCacheImpl wrapModel(RedisResourceInner storageAccountInner) {
        return new RedisCacheImpl(
                storageAccountInner.name(),
                storageAccountInner,
                this.innerCollection,
                super.myManager);
    }
}
