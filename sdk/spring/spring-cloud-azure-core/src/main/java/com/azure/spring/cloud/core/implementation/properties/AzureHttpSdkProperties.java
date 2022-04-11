// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.properties;

import com.azure.spring.cloud.core.properties.client.HttpClientProperties;
import com.azure.spring.cloud.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureHttpSdkProperties extends AzureSdkProperties implements RetryOptionsProvider {

    private final HttpClientProperties client = new HttpClientProperties();
    private final HttpProxyProperties proxy = new HttpProxyProperties();
    private final RetryProperties retry = new RetryProperties();

    @Override
    public HttpClientProperties getClient() {
        return client;
    }

    @Override
    public HttpProxyProperties getProxy() {
        return proxy;
    }

    @Override
    public RetryProperties getRetry() {
        return retry;
    }
}
