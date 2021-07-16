// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.enums;

import com.azure.core.management.AzureEnvironment;

/**
 * Enum to define all Azure environments: Azure, Azure China...
 */
public enum AzureEnvironments {

    Azure(AzureEnvironment.AZURE),
    AzureChina(AzureEnvironment.AZURE_CHINA),
    AzureUSGovernment(AzureEnvironment.AZURE_US_GOVERNMENT),
    AZURE_GERMANY(AzureEnvironment.AZURE_GERMANY);

    private final AzureEnvironment azureEnvironment;

    AzureEnvironments(AzureEnvironment environment) {
        this.azureEnvironment = environment;
    }

    public AzureEnvironment getAzureEnvironment() {
        return azureEnvironment;
    }
}
