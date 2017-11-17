/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management;

import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisCaches;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import org.junit.Assert;
import rx.Observable;
import rx.functions.Action1;

public class TestRedis extends TestTemplate<RedisCache, RedisCaches>  {
    @Override
    public RedisCache createResource(RedisCaches resources) throws Exception {
        final String redisName = "redis" + this.testId;
        final RedisCache[] redisCaches = new RedisCache[1];
        final SettableFuture<RedisCache> future = SettableFuture.create();

        Observable<Indexable> resourceStream = resources.define(redisName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withStandardSku()
                .withTag("mytag", "testtag")
                .createAsync();

        Utils.<RedisCache>rootResource(resourceStream)
                .subscribe(new Action1<RedisCache>() {
                    @Override
                    public void call(RedisCache redisCache) {
                        future.set(redisCache);
                    }
                });

        redisCaches[0] = future.get();

        Assert.assertEquals(redisCaches[0].name(), redisName);

        return redisCaches[0];
    }

    @Override
    public RedisCache updateResource(RedisCache resource) throws Exception {
        resource = resource.update()
                .withPremiumSku(2)
                .apply();

        Assert.assertEquals(true, resource.isPremium());

        return resource;
    }

    @Override
    public void print(RedisCache resource) {
        System.out.println(new StringBuilder().append("Redis Cache: ").append(resource.id()).append(", Name: ").append(resource.name()).toString());
    }
}
