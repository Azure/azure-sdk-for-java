// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.springframework.beans.BeanUtils;

/**
 * Skeleton implementation of a {@link AzureProfileOptionsProvider.ProfileOptions}.
 */
public abstract class AzureProfileOptionsAdapter implements AzureProfileOptionsProvider.ProfileOptions {

    /**
     * Change the environment according to the cloud type set.
     */
    protected void changeEnvironmentAccordingToCloud() {
        if (this.getCloudType() == null) {
            BeanUtils.copyProperties(new AzureEnvironmentProperties(), this.getEnvironment());
        } else {
            AzureProfileOptionsProvider.AzureEnvironmentOptions defaultEnvironment = decideAzureEnvironment(this.getCloudType());
            AzurePropertiesUtils.copyPropertiesIgnoreNull(defaultEnvironment, this.getEnvironment());
        }
    }

    /**
     * Get the Azure environment.
     * @return The Azure environment.
     */
    public abstract AzureProfileOptionsProvider.AzureEnvironmentOptions getEnvironment();

    private AzureProfileOptionsProvider.AzureEnvironmentOptions decideAzureEnvironment(AzureProfileOptionsProvider.CloudType cloud) {
        AzureEnvironment managementEnv = decideAzureManagementEnvironment(cloud, null);
        AzureProfileOptionsProvider.OtherAzureEnvironment otherEnv = decideOtherAzureEnvironment(cloud, null);
        return getEnvironment().fromAzureEnvironments(managementEnv, otherEnv);
    }

    /**
     * Decide the corresponding {@link AzureEnvironment} by the {@link AzureProfileOptionsProvider.CloudType}.
     * @param cloudType The provided cloud type.
     * @param defaultManagementEnvironment The default management {@link AzureEnvironment}.
     * @return The corresponding {@link AzureEnvironment}.
     */
    public static AzureEnvironment decideAzureManagementEnvironment(AzureProfileOptionsProvider.CloudType cloudType,
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

    private AzureProfileOptionsProvider.OtherAzureEnvironment decideOtherAzureEnvironment(AzureProfileOptionsProvider.CloudType cloudType,
                                                                                          AzureProfileOptionsProvider.OtherAzureEnvironment defaultEnvironment) {
        switch (cloudType) {
            case AZURE_CHINA:
                return AzureProfileOptionsProvider.OtherAzureEnvironment.AZURE_CHINA;
            case AZURE_US_GOVERNMENT:
                return AzureProfileOptionsProvider.OtherAzureEnvironment.AZURE_US_GOVERNMENT;
            case AZURE_GERMANY:
                return AzureProfileOptionsProvider.OtherAzureEnvironment.AZURE_GERMANY;
            case AZURE:
                return AzureProfileOptionsProvider.OtherAzureEnvironment.AZURE;
            case OTHER:
                return AzureProfileOptionsProvider.OtherAzureEnvironment.OTHER;
            default:
                return defaultEnvironment;
        }
    }

}
