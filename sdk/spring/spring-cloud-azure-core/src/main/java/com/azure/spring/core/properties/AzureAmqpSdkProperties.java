// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import com.azure.spring.core.properties.client.AmqpClientProperties;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureAmqpSdkProperties extends AzureSdkProperties {

    private final AmqpClientProperties client = new AmqpClientProperties();
    private final ProxyProperties proxy = new ProxyProperties();
    private final RetryProperties retry = new RetryProperties();

    @Override
    public AmqpClientProperties getClient() {
        return client;
    }

    @Override
    public ProxyProperties getProxy() {
        return proxy;
    }

    @Override
    public RetryProperties getRetry() {
        return retry;
    }
}
