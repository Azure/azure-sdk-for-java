// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.passwordless;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for passwordless connections with Azure Redis.
 */
public class AzureRedisPasswordlessProperties extends AzurePasswordlessProperties {

    // todo
    private static final Map<CloudType, String> REDIS_SCOPE_MAP = new HashMap<CloudType, String>() {
        {
            put(AzureProfileOptionsProvider.CloudType.AZURE, "https://*.cacheinfra.windows.net:10225/appid/.default");
            put(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, "https://*.cacheinfra.windows.net:10225/appid/.default");
            put(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY, "https://*.cacheinfra.windows.net:10225/appid/.default");
            put(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, "https://*.cacheinfra.windows.net:10225/appid/.default");
        }
    };


    @Override
    public String getScopes() {
        if (super.getScopes() == null) {
            super.setScopes(getRedisScopes());
        }
        return super.getScopes();
    }

    private String getRedisScopes() {
        String redisScope = REDIS_SCOPE_MAP.get(AzureProfileOptionsProvider.CloudType.AZURE);
        AzureProfileOptionsProvider.CloudType cloudType = getProfile().getCloudType();
        if (AzureProfileOptionsProvider.CloudType.AZURE.equals(cloudType)) {
            redisScope = REDIS_SCOPE_MAP.get(AzureProfileOptionsProvider.CloudType.AZURE);
        } else if (AzureProfileOptionsProvider.CloudType.AZURE_CHINA.equals(cloudType)) {
            redisScope = REDIS_SCOPE_MAP.get(AzureProfileOptionsProvider.CloudType.AZURE_CHINA);
        } else if (AzureProfileOptionsProvider.CloudType.AZURE_GERMANY.equals(cloudType)) {
            redisScope = REDIS_SCOPE_MAP.get(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY);
        } else if (AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT.equals(cloudType)) {
            redisScope = REDIS_SCOPE_MAP.get(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY);
        }
        return redisScope;
    }

}
