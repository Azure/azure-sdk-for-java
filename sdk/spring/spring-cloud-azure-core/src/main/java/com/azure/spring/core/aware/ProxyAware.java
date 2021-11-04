// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware;

/**
 * Interface to be implemented by classes that wish to be aware of the proxy properties.
 */
public interface ProxyAware {

    Proxy getProxy();

    /**
     * Interface to be implemented by classes that wish to describe the http based client proxy.
     */
    interface HttpProxy {

        String getNonProxyHosts();
    }

    /**
     * Interface to be implemented by classes that wish to describe the proxy.
     */
    interface Proxy {

        String getType();

        String getHostname();

        int getPort();

        String getAuthenticationType();

        String getUsername();

        String getPassword();

    }

}
