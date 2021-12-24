// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core;

import com.azure.spring.cloud.autoconfigure.properties.core.client.AmqpClientCP;
import com.azure.spring.cloud.autoconfigure.properties.core.proxy.ProxyCP;
import com.azure.spring.cloud.autoconfigure.properties.core.retry.RetryCP;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties base class for all AMQP-based Azure Service clients.
 */
public abstract class AbstractAzureAmqpCP extends AbstractAzureServiceCP {

    @NestedConfigurationProperty
    protected final AmqpClientCP client = new AmqpClientCP();

    @NestedConfigurationProperty
    protected final RetryCP retry = new RetryCP();

    @NestedConfigurationProperty
    protected final ProxyCP proxy = new ProxyCP();

    @Override
    public AmqpClientCP getClient() {
        return client;
    }

    @Override
    public RetryCP getRetry() {
        return retry;
    }

    @Override
    public ProxyCP getProxy() {
        return proxy;
    }
}
