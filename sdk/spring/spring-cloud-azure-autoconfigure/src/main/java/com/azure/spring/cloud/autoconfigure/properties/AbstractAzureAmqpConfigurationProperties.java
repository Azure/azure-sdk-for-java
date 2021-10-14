// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.core.properties.client.AmqpClientProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 */
public abstract class AbstractAzureAmqpConfigurationProperties extends AbstractAzureServiceConfigurationProperties {

    @NestedConfigurationProperty
    protected final AmqpClientProperties client = new AmqpClientProperties();

    @Override
    public AmqpClientProperties getClient() {
        return client;
    }
}
