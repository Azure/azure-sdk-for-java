// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.core.aware.AzureProfileAware;

import static com.azure.spring.core.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;

/**
 * Skeleton implementation of a {@link AzureProfileAware.Profile}.
 */
public abstract class AzureProfileAdapter implements AzureProfileAware.Profile {

    /**
     * Change the environment according to the cloud type set.
     */
    protected void changeEnvironmentAccordingToCloud() {
        AzureProfileAware.AzureEnvironment defaultEnvironment = decideAzureEnvironment(this.getCloud());
        copyPropertiesIgnoreNull(defaultEnvironment, this.getEnvironment());
    }

    /**
     * Get the Azure environment.
     * @return The Azure environment.
     */
    public abstract AzureProfileAware.AzureEnvironment getEnvironment();

    private AzureProfileAware.AzureEnvironment decideAzureEnvironment(AzureProfileAware.CloudType cloud) {
        AzureEnvironment managementAzureEnvironment = decideManagementAzureEnvironment(cloud, null);
        return getEnvironment().fromManagementAzureEnvironment(managementAzureEnvironment);
    }

    /**
     * Decide the corresponding {@link AzureEnvironment} by the {@link com.azure.spring.core.aware.AzureProfileAware.CloudType}.
     * @param cloudType The provided cloud type.
     * @param defaultManagementEnvironment The default management {@link AzureEnvironment}.
     * @return The corresponding {@link AzureEnvironment}.
     */
    public static AzureEnvironment decideManagementAzureEnvironment(AzureProfileAware.CloudType cloudType,
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
