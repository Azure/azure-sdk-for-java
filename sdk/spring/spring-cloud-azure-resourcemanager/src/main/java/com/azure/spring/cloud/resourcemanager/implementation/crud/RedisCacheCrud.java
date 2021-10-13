// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;

/**
 * Resource manager for Redis cache.
 */
public class RedisCacheCrud extends AbstractResourceCrud<RedisCache, String> {

    public RedisCacheCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
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
            return resourceManager.redisCaches().getByResourceGroup(resourceMetadata.getResourceGroup(),
                                                                    name);
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
        return resourceManager.redisCaches()
                              .define(name)
                              .withRegion(resourceMetadata.getRegion())
                              .withExistingResourceGroup(resourceMetadata.getResourceGroup())
                              .withBasicSku()
                              .create();
    }
}
