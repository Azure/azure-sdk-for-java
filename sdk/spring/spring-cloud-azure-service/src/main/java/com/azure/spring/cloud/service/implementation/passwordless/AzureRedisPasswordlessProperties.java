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

    private static final String REDIS_SCOPE_AZURE = "https://*.cacheinfra.windows.net:10225/appid/.default";
    private static final String REDIS_SCOPE_AZURE_CHINA = "https://*.cacheinfra.windows.net.china:10225/appid/.default";
    private static final String REDIS_SCOPE_AZURE_GERMANY = "https://*.cacheinfra.windows.net.germany:10225/appid/.default";
    private static final String REDIS_SCOPE_AZURE_US_GOVERNMENT = "https://*.cacheinfra.windows.us.government.net:10225/appid/.default";

    private static final Map<CloudType, String> REDIS_SCOPE_MAP = new HashMap<CloudType, String>() {
        {
            put(AzureProfileOptionsProvider.CloudType.AZURE, REDIS_SCOPE_AZURE);
            put(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, REDIS_SCOPE_AZURE_CHINA);
            put(AzureProfileOptionsProvider.CloudType.AZURE_GERMANY, REDIS_SCOPE_AZURE_GERMANY);
            put(AzureProfileOptionsProvider.CloudType.AZURE_US_GOVERNMENT, REDIS_SCOPE_AZURE_US_GOVERNMENT);
        }
    };

    @Override
    public String getScopes() {
        return super.getScopes() == null ? getRedisScopes() : super.getScopes();
    }

    private String getRedisScopes() {
        return REDIS_SCOPE_MAP.getOrDefault(getProfile().getCloudType(), REDIS_SCOPE_AZURE);
    }

}
