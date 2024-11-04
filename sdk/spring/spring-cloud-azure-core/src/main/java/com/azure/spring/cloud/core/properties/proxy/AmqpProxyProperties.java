// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.proxy;

import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;

/**
 * Extend the proxy properties for AMQP connection
 */
public final class AmqpProxyProperties extends ProxyProperties implements ProxyOptionsProvider.AmqpProxyOptions {

    /**
     * Creates an instance of {@link AmqpProxyProperties}.
     */
    public AmqpProxyProperties() {
    }

    /**
     * Authentication type used against the proxy. For instance, 'none', 'basic', 'digest'. The default value is `'none'`.
     */
    private String authenticationType;

    @Override
    public String getAuthenticationType() {
        return authenticationType;
    }

    /**
     * Set the authentication type for the proxy.
     * @param authenticationType The authentication type.
     */
    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }


}
