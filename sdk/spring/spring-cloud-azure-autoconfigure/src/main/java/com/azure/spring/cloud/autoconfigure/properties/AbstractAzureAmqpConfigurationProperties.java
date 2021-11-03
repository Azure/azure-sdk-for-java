// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 */
public abstract class AbstractAzureAmqpConfigurationProperties extends AbstractAzureServiceConfigurationProperties {

    @NestedConfigurationProperty
    protected final AmqpClientConfigurationProperties client = new AmqpClientConfigurationProperties();

    @Override
    public AmqpClientConfigurationProperties getClient() {
        return client;
    }
}
