// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;

/**
 * Skeleton implementation of a {@link AzureProfileOptionsProvider.ProfileOptions}.
 */
public abstract class AzureProfileOptionsAdapter implements AzureProfileOptionsProvider.ProfileOptions {

    /**
     * Change the environment according to the cloud type set.
     */
    protected void changeEnvironmentAccordingToCloud() {
        AzureProfileOptionsProvider.AzureEnvironment defaultEnvironment = decideAzureEnvironment(this.getCloudType());
        AzurePropertiesUtils.copyPropertiesIgnoreNull(defaultEnvironment, this.getEnvironment());
    }

    /**
     * Get the Azure environment.
     * @return The Azure environment.
     */
    public abstract AzureProfileOptionsProvider.AzureEnvironment getEnvironment();

    private AzureProfileOptionsProvider.AzureEnvironment decideAzureEnvironment(AzureProfileOptionsProvider.CloudType cloud) {
        AzureEnvironment managementAzureEnvironment = decideManagementAzureEnvironment(cloud, null);
        return getEnvironment().fromAzureManagementEnvironment(managementAzureEnvironment);
    }

    /**
     * Decide the corresponding {@link AzureEnvironment} by the {@link AzureProfileOptionsProvider.CloudType}.
     * @param cloudType The provided cloud type.
     * @param defaultManagementEnvironment The default management {@link AzureEnvironment}.
     * @return The corresponding {@link AzureEnvironment}.
     */
    public static AzureEnvironment decideManagementAzureEnvironment(AzureProfileOptionsProvider.CloudType cloudType,
                                                                    AzureEnvironment defaultManagementEnvironment) {
        switch (cloudType) {
            case AZURE_CHINA:
                return AzureEnvironment.AZURE_CHINA;
            case AZURE_US_GOVERNMENT:
                return AzureEnvironment.AZURE_US_GOVERNMENT;
            case AZURE_GERMANY:
                return AzureEnvironment.AZURE_GERMANY;
            case AZURE:
                return AzureEnvironment.AZURE;
            default:
                return defaultManagementEnvironment;
        }
    }

}
