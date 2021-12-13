// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core;

import com.azure.spring.cloud.autoconfigure.properties.core.client.HttpClientCP;
import com.azure.spring.cloud.autoconfigure.properties.core.proxy.HttpProxyCP;
import com.azure.spring.cloud.autoconfigure.properties.core.retry.HttpRetryCP;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties base class for all HTTP-based Azure Service clients.
 */
public abstract class AbstractAzureHttpCP extends AbstractAzureServiceCP {

    @NestedConfigurationProperty
    protected final HttpClientCP client = new HttpClientCP();

    @NestedConfigurationProperty
    protected final HttpProxyCP proxy = new HttpProxyCP();

    @NestedConfigurationProperty
    protected final HttpRetryCP retry = new HttpRetryCP();

    @Override
    public HttpClientCP getClient() {
        return client;
    }

    @Override
    public HttpProxyCP getProxy() {
        return proxy;
    }

    @Override
    public HttpRetryCP getRetry() {
        return retry;
    }
}
