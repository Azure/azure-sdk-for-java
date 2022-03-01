// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.properties;

import com.azure.spring.core.aware.RetryOptionsAware;
import com.azure.spring.core.properties.client.HttpClientProperties;
import com.azure.spring.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureHttpSdkProperties extends AzureSdkProperties implements RetryOptionsAware {

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
