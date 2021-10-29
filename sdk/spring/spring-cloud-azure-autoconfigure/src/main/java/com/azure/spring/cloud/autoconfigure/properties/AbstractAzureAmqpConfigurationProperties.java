// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.core.properties.proxy.ProxyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 */
public abstract class AbstractAzureAmqpConfigurationProperties extends AbstractAzureServiceConfigurationProperties {

    @NestedConfigurationProperty
    protected final AmqpClientConfigurationProperties client = new AmqpClientConfigurationProperties();

    @NestedConfigurationProperty
    protected final ProxyProperties proxy = new ProxyProperties();

    @Override
    public AmqpClientConfigurationProperties getClient() {
        return client;
    }

    @Override
    public ProxyProperties getProxy() {
        return proxy;
    }
}
