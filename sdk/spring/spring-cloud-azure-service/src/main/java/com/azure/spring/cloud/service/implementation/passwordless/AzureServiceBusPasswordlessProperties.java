// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.passwordless;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for passwordless connections with Azure ServiceBus.
 */
public class AzureServiceBusPasswordlessProperties extends AzurePasswordlessProperties {

    private static final String SERVICEBUS_SCOPE_AZURE = "https://servicebus.azure.net/.default";
    private static final String SERVICEBUS_SCOPE_AZURE_CHINA = "https://servicebus.azure.net/.default";
    private static final String SERVICEBUS_SCOPE_AZURE_GERMANY = "https://servicebus.azure.net/.default";
    private static final String SERVICEBUS_SCOPE_AZURE_US_GOVERNMENT = "https://servicebus.azure.net/.default";

    private static final Map<CloudType, String> SERVICEBUS_SCOPE_MAP = new HashMap<CloudType, String>() {
        {
            put(CloudType.AZURE, SERVICEBUS_SCOPE_AZURE);
            put(CloudType.AZURE_CHINA, SERVICEBUS_SCOPE_AZURE_CHINA);
            put(CloudType.AZURE_GERMANY, SERVICEBUS_SCOPE_AZURE_GERMANY);
            put(CloudType.AZURE_US_GOVERNMENT, SERVICEBUS_SCOPE_AZURE_US_GOVERNMENT);
        }
    };

    @Override
    public String getScopes() {
        return super.getScopes() == null ? getRedisScopes() : super.getScopes();
    }

    private String getRedisScopes() {
        return SERVICEBUS_SCOPE_MAP.getOrDefault(getProfile().getCloudType(), SERVICEBUS_SCOPE_AZURE);
    }

}
