// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation;

import com.azure.spring.cloud.core.implementation.factory.AzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;

public abstract class AzureServiceClientBuilderFactoryBaseTests<B, P extends AzureProperties,
                                                                  T extends AzureServiceClientBuilderFactory<B>> {

    protected abstract P createMinimalServiceProperties();

}
