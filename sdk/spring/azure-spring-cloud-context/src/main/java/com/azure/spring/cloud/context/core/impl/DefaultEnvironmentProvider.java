// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.core.AzureCloud;
import com.azure.spring.core.AzureProperties;

/**
 * A {@link EnvironmentProvider} implementation that based on {@link
 * AzureProperties}.
 *
 * @author Warren Zhu
 */
public class DefaultEnvironmentProvider implements EnvironmentProvider {

    private AzureEnvironment environment = AzureEnvironment.AZURE;

    @Override
    public AzureEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(AzureCloud cloud) {
        this.environment = toAzureEnvironment(cloud);
    }

    private static AzureEnvironment toAzureEnvironment(AzureCloud cloud) {
        switch (cloud) {
            case AzureChina:
                return AzureEnvironment.AZURE_CHINA;
            case AzureGermany:
                return AzureEnvironment.AZURE_GERMANY;
            case AzureUSGovernment:
                return AzureEnvironment.AZURE_US_GOVERNMENT;
            default:
                return AzureEnvironment.AZURE;
        }
    }
}
