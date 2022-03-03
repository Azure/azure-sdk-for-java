// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy;


import com.azure.spring.cloud.core.aware.ProxyOptionsAware;

/**
 * Proxy properties for all Azure AMQP SDKs.
 */
public class AmqpProxyConfigurationProperties extends ProxyConfigurationProperties
    implements ProxyOptionsAware.AmqpProxy {

    /**
     * Authentication type used against the proxy.
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
