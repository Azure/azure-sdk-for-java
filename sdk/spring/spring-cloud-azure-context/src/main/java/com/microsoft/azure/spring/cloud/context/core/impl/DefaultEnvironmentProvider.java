/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;

/**
 * A {@link com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider} implementation that based on
 * {@link com.microsoft.azure.spring.cloud.context.core.config.AzureProperties}.
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
