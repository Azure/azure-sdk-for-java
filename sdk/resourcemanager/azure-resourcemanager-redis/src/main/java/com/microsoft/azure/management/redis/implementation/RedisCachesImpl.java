/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisCaches;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of RedisCaches and its parent interfaces.
 */
@LangDefinition
class RedisCachesImpl
    extends TopLevelModifiableResourcesImpl<
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

    @Override
    public PagedList<OperationInner> listOperations() {
        return this.manager().inner().operations().list();
    }

    @Override
    public Observable<OperationInner> listOperationsAsync() {
        return this.manager().inner().operations().listAsync().flatMap(new Func1<Page<OperationInner>, Observable<OperationInner>>() {
            @Override
            public Observable<OperationInner> call(Page<OperationInner> pageInner) {
                return Observable.from(pageInner.items());
            }
        });
    }
}
