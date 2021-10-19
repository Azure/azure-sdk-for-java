// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.core.properties.proxy.HttpProxyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties base class for all Azure Http clients.
 */
public abstract class AbstractAzureHttpConfigurationProperties extends AbstractAzureServiceConfigurationProperties {

    @NestedConfigurationProperty
    protected final HttpClientConfigurationProperties client = new HttpClientConfigurationProperties();

    @NestedConfigurationProperty
    protected final HttpProxyProperties proxy = new HttpProxyProperties();

    @Override
    public HttpClientConfigurationProperties getClient() {
        return client;
    }

    @Override
    public HttpProxyProperties getProxy() {
        return proxy;
    }
}
