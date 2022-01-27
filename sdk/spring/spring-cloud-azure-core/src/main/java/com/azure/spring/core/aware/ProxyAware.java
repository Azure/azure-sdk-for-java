// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware;

/**
 * Interface to be implemented by classes that wish to be aware of the proxy properties.
 */
public interface ProxyAware {

    /**
     * Get the proxy configuration.
     * @return the proxy configuration.
     */
    Proxy getProxy();

    /**
     * Interface to be implemented by classes that wish to describe the http based client proxy.
     */
    interface HttpProxy {

        /**
         * Get the http non-proxy host.
         * @return the non-proxy host.
         */
        String getNonProxyHosts();
    }

    /**
     * Interface to be implemented by classes that wish to describe the proxy.
     */
    interface Proxy {

        /**
         * Get the proxy type.
         * @return the proxy type.
         */
        String getType();

        /**
         * Get the proxy hostname.
         * @return the proxy hostname.
         */
        String getHostname();

        /**
         * Get the proxy port.
         * @return the proxy port.
         */
        Integer getPort();

        /**
         * Get the proxy authentication type.
         * @return the proxy authentication type.
         */
        String getAuthenticationType();

        /**
         * Get the proxy username.
         * @return the proxy username.
         */
        String getUsername();

        /**
         * Get the proxy password.
         * @return the proxy password.
         */
        String getPassword();

    }

}
