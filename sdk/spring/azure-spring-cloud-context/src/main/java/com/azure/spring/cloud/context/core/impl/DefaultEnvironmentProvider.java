// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.identity.AzureCloud;

import static com.azure.core.management.AzureEnvironment.AZURE_CHINA;
import static com.azure.core.management.AzureEnvironment.AZURE_GERMANY;
import static com.azure.core.management.AzureEnvironment.AZURE_US_GOVERNMENT;

/**
 * A {@link EnvironmentProvider} implementation that based on {@link
 * AzureProperties}.
 *
 * @author Warren Zhu
 */
public class DefaultEnvironmentProvider implements EnvironmentProvider {

    private AzureEnvironment environment = AzureEnvironment.AZURE;
    private AzureCloud cloud = AzureCloud.Azure;

    @Override
    public AzureEnvironment getAzureEnvironment() {
        return this.environment;
    }

    @Override
    public AzureCloud getAzureCloud() {
        return this.cloud;
    }

    public void setAzureEnvironment(AzureEnvironment environment) {
        this.environment = environment;
        this.cloud = toAzureCloud(environment);
    }

    public void setAzureCloud(AzureCloud cloud) {
        this.cloud = cloud;
        this.environment = toAzureEnvironment(cloud);
    }

    private static AzureEnvironment toAzureEnvironment(AzureCloud azureCloud) {
        switch (azureCloud) {
            case AzureChina:
                return AZURE_CHINA;
            case AzureGermany:
                return AZURE_GERMANY;
            case AzureUSGovernment:
                return AZURE_US_GOVERNMENT;
            default:
                return AzureEnvironment.AZURE;
        }
    }

    private static AzureCloud toAzureCloud(AzureEnvironment azureEnvironment) {
        if (azureEnvironment == AZURE_CHINA) {
            return AzureCloud.AzureChina;
        }
        if (azureEnvironment == AZURE_GERMANY) {
            return AzureCloud.AzureGermany;
        }
        if (azureEnvironment == AZURE_US_GOVERNMENT) {
            return AzureCloud.AzureUSGovernment;
        }
        return AzureCloud.Azure;
    }

}
