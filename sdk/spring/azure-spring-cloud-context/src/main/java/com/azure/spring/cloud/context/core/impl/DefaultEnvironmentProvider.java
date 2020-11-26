// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.azure.spring.cloud.context.core.config.AzureProperties;

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

    public void setEnvironment(AzureEnvironment environment) {
        this.environment = environment;
    }
}
