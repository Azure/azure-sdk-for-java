// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy;


import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;

/**
 * Proxy properties for all Azure AMQP SDKs.
 */
public class AmqpProxyConfigurationProperties extends ProxyConfigurationProperties
    implements ProxyOptionsProvider.AmqpProxyOptions {

    /**
     * Authentication type used against the proxy. For instance, 'none', 'basic', 'digest'. The default value is `'none'`.
     */
    private String authenticationType;

    @Override
    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }
}
