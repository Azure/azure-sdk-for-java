/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.redis.*;
import com.microsoft.azure.management.redis.RedisResource;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;

import java.io.IOException;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
class RedisResourcesImpl
        extends GroupableResourcesImpl<
            RedisResource,
            RedisResourceImpl,
            RedisResourceWithAccessKeyInner,
            RedisInner,
            RedisManager>
        implements RedisResources {

    RedisResourcesImpl(
            final RedisInner client,
            final RedisManager storageManager) {
        super(client, storageManager);
    }

    @Override
    public PagedList<RedisResource> list() throws CloudException, IOException {
        //return wrapList(this.innerCollection.list().getBody());
        return null;
    }

    @Override
    public PagedList<RedisResource> listByGroup(String groupName) throws CloudException, IOException {
        //return wrapList(this.innerCollection.listByResourceGroup(groupName).getBody());
        return null;
    }

    @Override
    public RedisResource getByGroup(String groupName, String name) throws CloudException, IOException {
        //return wrapModel(this.innerCollection.get(groupName, name).getBody());
        return null;
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
    public RedisResourceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected RedisResourceImpl wrapModel(String name) {
        return new RedisResourceImpl(
                name,
                new RedisResourceWithAccessKeyInner(),
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected RedisResourceImpl wrapModel(RedisResourceWithAccessKeyInner storageAccountInner) {
        return new RedisResourceImpl(
                storageAccountInner.name(),
                storageAccountInner,
                this.innerCollection,
                super.myManager);
    }
}
