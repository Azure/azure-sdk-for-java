// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisCaches;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

public class TestRedis extends TestTemplate<RedisCache, RedisCaches> {
    @Override
    public RedisCache createResource(RedisCaches resources) throws Exception {
        final String redisName = resources.manager().resourceManager().internalContext().randomResourceName("redis", 10);
        final RedisCache[] redisCaches = new RedisCache[1];

        Mono<RedisCache> resourceStream =
            resources
                .define(redisName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withStandardSku()
                .withTag("mytag", "testtag")
                .createAsync();


        redisCaches[0] = resourceStream.block();

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
