// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.enums;

import com.azure.core.management.AzureEnvironment;

/**
 * Enum to define all Azure environments: Azure, Azure China...
 */
public enum AzureEnvironments {

    /**
     * Public Azure.
     */
    Azure(AzureEnvironment.AZURE),

    /**
     * Azure China.
     */
    AzureChina(AzureEnvironment.AZURE_CHINA),

    /**
     * Azure US Government.
     */
    AzureUSGovernment(AzureEnvironment.AZURE_US_GOVERNMENT),

    /**
     * Azure Germany.
     */
    AZURE_GERMANY(AzureEnvironment.AZURE_GERMANY);

    private final AzureEnvironment azureEnvironment;

    AzureEnvironments(AzureEnvironment environment) {
        this.azureEnvironment = environment;
    }

    /**
     * Gets the AzureEnvironment representation for this enum.
     *
     * @return The AzureEnvironment representation for this enum.
     */
    public AzureEnvironment getAzureEnvironment() {
        return azureEnvironment;
    }
}
