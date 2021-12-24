// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.spring.core.aware.AzureProfileAware;
import org.springframework.util.Assert;

import static com.azure.spring.core.aware.AzureProfileAware.CloudType.OTHER;

/**
 * The AzureEnvironment defines all properties to Azure services, such as endpoints, resource ids, etc.
 */
public final class KnownAzureEnvironment extends AzureEnvironment {

    /**
     * Create a {@link KnownAzureEnvironment} of {@link AzureProfileAware.CloudType}.
     * @param cloudType The cloud type.
     */
    public KnownAzureEnvironment(AzureProfileAware.CloudType cloudType) {
        super(convertToManagementAzureEnvironmentByType(cloudType));
    }

    private static com.azure.core.management.AzureEnvironment convertToManagementAzureEnvironmentByType(
        AzureProfileAware.CloudType cloud) {
        Assert.isTrue(cloud != OTHER, "cloud type should not be other for PredefinedAzureEnvironment");
        switch (cloud) {
            case AZURE_CHINA:
                return com.azure.core.management.AzureEnvironment.AZURE_CHINA;
            case AZURE_US_GOVERNMENT:
                return com.azure.core.management.AzureEnvironment.AZURE_US_GOVERNMENT;
            case AZURE_GERMANY:
                return com.azure.core.management.AzureEnvironment.AZURE_GERMANY;
            default:
                return com.azure.core.management.AzureEnvironment.AZURE;
        }
    }

}
