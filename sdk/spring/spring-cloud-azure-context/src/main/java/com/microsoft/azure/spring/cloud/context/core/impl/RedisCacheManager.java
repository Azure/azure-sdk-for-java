/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;

public class RedisCacheManager extends AzureManager<RedisCache, String> {

    public RedisCacheManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
    }

    @Override
    String getResourceName(String key) {
        return key;
    }

    @Override
    String getResourceType() {
        return RedisCache.class.getSimpleName();
    }

    @Override
    public RedisCache internalGet(String name) {
        return azure.redisCaches().getByResourceGroup(azureProperties.getResourceGroup(), name);
    }

    @Override
    public RedisCache internalCreate(String name) {
        return azure.redisCaches().define(name).withRegion(azureProperties.getRegion())
                    .withExistingResourceGroup(azureProperties.getResourceGroup()).withBasicSku().create();
    }
}
