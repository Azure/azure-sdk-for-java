// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.iothub;

import com.azure.core.management.Region;
import com.azure.resourcemanager.iothub.models.IotHubSku;
import com.azure.resourcemanager.iothub.models.IotHubSkuInfo;

public class Constants
{
    static final String DEFAULT_INSTANCE_NAME = "JavaIotHubControlPlaneSDKTest";
    static final Region DEFAULT_REGION = Region.US_WEST_CENTRAL;

    public static class DefaultSku
    {
        static final String NAME = "S1";
        static final Long CAPACITY = 1L;
        static IotHubSkuInfo INSTANCE = new IotHubSkuInfo()
            .withCapacity(Constants.DefaultSku.CAPACITY)
            .withName(IotHubSku.fromString(Constants.DefaultSku.NAME));
    }
}
