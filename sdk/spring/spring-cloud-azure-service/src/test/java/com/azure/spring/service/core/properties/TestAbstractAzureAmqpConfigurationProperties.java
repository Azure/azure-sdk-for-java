// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.core.properties;

/**
 *
 */
public abstract class TestAbstractAzureAmqpConfigurationProperties extends TestAbstractAzureServiceConfigurationProperties {

    protected final TestAmqpClientConfigurationProperties client = new TestAmqpClientConfigurationProperties();

    @Override
    public TestAmqpClientConfigurationProperties getClient() {
        return client;
    }
}
