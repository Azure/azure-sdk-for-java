// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.core.properties;

import com.azure.spring.core.properties.client.AmqpClientProperties;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

public abstract class AbstractAmqpProperties extends AbstractServiceProperties {

    public AbstractAmqpProperties() {
        this.proxy = new ProxyProperties();
        this.retry = new RetryProperties();
        this.client = new AmqpClientProperties();
    }
    @Override
    public ClientProperties getClient() {
        return client;
    }

    @Override
    public ProxyProperties getProxy() {
        return this.proxy;
    }

    @Override
    public RetryProperties getRetry() {
        return this.retry;
    }
}
