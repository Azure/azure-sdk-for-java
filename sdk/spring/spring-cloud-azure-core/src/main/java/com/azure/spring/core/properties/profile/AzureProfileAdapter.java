// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.spring.core.aware.AzureProfileAware;

import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE_CHINA;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE_GERMANY;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE_US_GOVERNMENT;
import static com.azure.spring.core.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;

/**
 * Skeleton implementation of a {@link AzureProfileAware.Profile}.
 */
public abstract class AzureProfileAdapter implements AzureProfileAware.Profile {

    /**
     * Change the environment according to the cloud type set.
     */
    protected void changeEnvironmentAccordingToCloud() {
        AzureEnvironment defaultEnvironment = decideAzureEnvironment(this.getCloud());
        copyPropertiesIgnoreNull(defaultEnvironment, this.getEnvironment());
    }

    /**
     * Get the Azure environment.
     * @return The Azure environment.
     */
    public abstract AzureEnvironment getEnvironment();

    private AzureEnvironment decideAzureEnvironment(AzureProfileAware.CloudType cloud) {
        switch (cloud) {
            case AZURE_CHINA:
                return new KnownAzureEnvironment(AZURE_CHINA);
            case AZURE_US_GOVERNMENT:
                return new KnownAzureEnvironment(AZURE_US_GOVERNMENT);
            case AZURE_GERMANY:
                return new KnownAzureEnvironment(AZURE_GERMANY);
            case AZURE:
                return new KnownAzureEnvironment(AZURE);
            default:
                return new AzureEnvironment();
        }
    }

}
