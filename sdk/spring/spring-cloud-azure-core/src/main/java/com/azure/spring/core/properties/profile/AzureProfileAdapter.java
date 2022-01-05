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
        switch (cloud) {
            case AZURE_CHINA:
                return getEnvironment().fromManagementAzureEnvironment(AzureEnvironment.AZURE_CHINA);
            case AZURE_US_GOVERNMENT:
                return getEnvironment().fromManagementAzureEnvironment(AzureEnvironment.AZURE_US_GOVERNMENT);
            case AZURE_GERMANY:
                return getEnvironment().fromManagementAzureEnvironment(AzureEnvironment.AZURE_GERMANY);
            case AZURE:
                return getEnvironment().fromManagementAzureEnvironment(AzureEnvironment.AZURE);
            default:
                return getEnvironment().fromManagementAzureEnvironment(null);
        }
    }

}
