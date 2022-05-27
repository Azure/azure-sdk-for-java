// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.properties;

import com.azure.spring.cloud.core.properties.client.AmqpClientProperties;
import com.azure.spring.cloud.core.properties.proxy.AmqpProxyProperties;
import com.azure.spring.cloud.core.properties.retry.AmqpRetryProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureAmqpSdkProperties extends AzureSdkProperties implements RetryOptionsProvider {

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
