// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.autoconfigure.context.AzureContextProperties;

/**
 * A {@link EnvironmentProvider} implementation that based on {@link
 * AzureContextProperties}.
 *
 * @author Warren Zhu
 */
public class DefaultEnvironmentProvider implements EnvironmentProvider {

    private AzureEnvironment environment = AzureEnvironment.AZURE;

    @Override
    public AzureEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(String cloud) {
        this.environment = toAzureEnvironment(cloud);
    }

    public void setEnvironment(AzureEnvironment environment) {
        this.environment = environment;
    }

    private AzureEnvironment toAzureEnvironment(String cloud) {
        switch (cloud) {
            case "AzureChina":
                return AzureEnvironment.AZURE_CHINA;
            case "AzureGermany":
                return AzureEnvironment.AZURE_GERMANY;
            case "AzureUSGovernment":
                return AzureEnvironment.AZURE_US_GOVERNMENT;
            default:
                return AzureEnvironment.AZURE;
        }
    }
}
