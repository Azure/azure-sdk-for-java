// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.properties;

import com.azure.spring.cloud.core.aware.RetryOptionsAware;
import com.azure.spring.cloud.core.properties.client.AmqpClientProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.retry.AmqpRetryProperties;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureAmqpSdkProperties extends AzureSdkProperties implements RetryOptionsAware {

    private final AmqpClientProperties client = new AmqpClientProperties();
    private final ProxyProperties proxy = new ProxyProperties();
    private final AmqpRetryProperties retry = new AmqpRetryProperties();

    @Override
    public AmqpClientProperties getClient() {
        return client;
    }

    @Override
    public ProxyProperties getProxy() {
        return proxy;
    }

    @Override
    public AmqpRetryProperties getRetry() {
        return retry;
    }
}
