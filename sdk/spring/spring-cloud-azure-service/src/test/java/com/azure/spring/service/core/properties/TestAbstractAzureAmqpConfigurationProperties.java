// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.core.properties;

import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

/**
 *
 */
public abstract class TestAbstractAzureAmqpConfigurationProperties extends TestAbstractAzureServiceConfigurationProperties {

    protected final TestAmqpClientConfigurationProperties client = new TestAmqpClientConfigurationProperties();

    protected final RetryProperties retry = new RetryProperties();

    protected final ProxyProperties proxy = new ProxyProperties();

    @Override
    public TestAmqpClientConfigurationProperties getClient() {
        return client;
    }

    @Override
    public RetryProperties getRetry() {
        return retry;
    }

    @Override
    public ProxyProperties getProxy() {
        return proxy;
    }
}
