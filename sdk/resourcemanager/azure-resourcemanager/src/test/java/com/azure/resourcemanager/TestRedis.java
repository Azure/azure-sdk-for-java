// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisCaches;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;

public class TestRedis extends TestTemplate<RedisCache, RedisCaches> {
    @Override
    public RedisCache createResource(RedisCaches resources) throws Exception {
        final String redisName = resources.manager().sdkContext().randomResourceName("redis", 10);
        final RedisCache[] redisCaches = new RedisCache[1];
        final SettableFuture<RedisCache> future = SettableFuture.create();

        Flux<Indexable> resourceStream =
            resources
                .define(redisName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withStandardSku()
                .withTag("mytag", "testtag")
                .createAsync();

        Utils.<RedisCache>rootResource(resourceStream.last()).subscribe(future::set);

        redisCaches[0] = future.get();

        Assertions.assertEquals(redisCaches[0].name(), redisName);

        return redisCaches[0];
    }

    @Override
    public RedisCache updateResource(RedisCache resource) throws Exception {
        resource = resource.update().withPremiumSku(2).apply();

        Assertions.assertTrue(resource.isPremium());

        return resource;
    }

    @Override
    public void print(RedisCache resource) {
        System.out.println("Redis Cache: " + resource.id() + ", Name: " + resource.name());
    }
}
