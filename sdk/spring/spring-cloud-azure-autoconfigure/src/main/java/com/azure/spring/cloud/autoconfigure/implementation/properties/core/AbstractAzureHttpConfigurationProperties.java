// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core;

import com.azure.spring.cloud.autoconfigure.implementation.properties.core.client.HttpClientConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy.HttpProxyConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.retry.RetryConfigurationProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties base class for all HTTP-based Azure Service clients.
 */
public abstract class AbstractAzureHttpConfigurationProperties extends AbstractAzureServiceConfigurationProperties
    implements RetryOptionsProvider {

    @NestedConfigurationProperty
    protected final HttpClientConfigurationProperties client = new HttpClientConfigurationProperties();

    @NestedConfigurationProperty
    protected final HttpProxyConfigurationProperties proxy = new HttpProxyConfigurationProperties();

    @NestedConfigurationProperty
    protected final RetryConfigurationProperties retry = new RetryConfigurationProperties();

    @Override
    public HttpClientConfigurationProperties getClient() {
        return client;
    }

    @Override
    public HttpProxyConfigurationProperties getProxy() {
        return proxy;
    }

    @Override
    public RetryConfigurationProperties getRetry() {
        return retry;
    }
}
