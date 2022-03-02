// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.properties;

import com.azure.spring.core.aware.RetryOptionsAware;
import com.azure.spring.core.properties.client.AmqpClientProperties;
import com.azure.spring.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.core.properties.retry.AmqpRetryProperties;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureAmqpSdkProperties extends AzureSdkProperties implements RetryOptionsAware {

    private final AmqpClientProperties client = new AmqpClientProperties();
    private final AmqpProxyProperties proxy = new AmqpProxyProperties();
    private final AmqpRetryProperties retry = new AmqpRetryProperties();

    @Override
    public AmqpClientProperties getClient() {
        return client;
    }

    @Override
    public AmqpProxyProperties getProxy() {
        return proxy;
    }

    @Override
    public AmqpRetryProperties getRetry() {
        return retry;
    }
}
