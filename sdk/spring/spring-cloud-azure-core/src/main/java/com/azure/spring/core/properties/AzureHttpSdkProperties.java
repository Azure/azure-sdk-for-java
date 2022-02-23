// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties;

import com.azure.spring.core.properties.client.HttpClientProperties;
import com.azure.spring.core.properties.proxy.HttpProxyProperties;
import com.azure.spring.core.properties.retry.HttpRetryProperties;

/**
 * Unified properties for Azure SDK clients.
 */
public abstract class AzureHttpSdkProperties extends AzureSdkProperties {

    private final HttpClientProperties client = new HttpClientProperties();
    private final HttpProxyProperties proxy = new HttpProxyProperties();
    private final HttpRetryProperties retry = new HttpRetryProperties();

    @Override
    public HttpClientProperties getClient() {
        return client;
    }

    @Override
    public HttpProxyProperties getProxy() {
        return proxy;
    }

    @Override
    public HttpRetryProperties getRetry() {
        return retry;
    }
}
