// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;

/**
 * Resource manager for Redis cache.
 */
public class RedisCacheManager extends AzureManager<RedisCache, String> {

    private final AzureResourceManager azureResourceManager;

    public RedisCacheManager(AzureResourceManager azureResourceManager, AzureContextProperties azureContextProperties) {
        super(azureContextProperties);
        this.azureResourceManager = azureResourceManager;
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
        try {
            return azureResourceManager.redisCaches().getByResourceGroup(resourceGroup, name);
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public RedisCache internalCreate(String name) {
        return azureResourceManager.redisCaches()
                                   .define(name)
                                   .withRegion(region)
                                   .withExistingResourceGroup(resourceGroup)
                                   .withBasicSku()
                                   .create();
    }
}
