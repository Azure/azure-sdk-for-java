// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.spring.core.factory.AzureServiceClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;

public abstract class AzureServiceClientBuilderFactoryTestBase<B, P extends AzureProperties,
                                                                  T extends AzureServiceClientBuilderFactory<B>> {

    protected abstract P createMinimalServiceProperties();

}
