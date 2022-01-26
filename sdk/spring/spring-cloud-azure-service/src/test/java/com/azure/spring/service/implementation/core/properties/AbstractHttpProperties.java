// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.core.properties;

import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.client.HttpClientProperties;
import com.azure.spring.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.HttpRetryProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

public abstract class AbstractHttpProperties extends AbstractServiceProperties {

    public AbstractHttpProperties() {
        this.proxy = new HttpProxyProperties();
        this.retry = new HttpRetryProperties();
        this.client = new HttpClientProperties();
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
