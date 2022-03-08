// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider;

/**
 * Interface to be implemented by classes that wish to provide the proxy options.
 */
public interface ProxyOptionsProvider {

    /**
     * Get the proxy configuration.
     * @return the proxy configuration.
     */
    ProxyOptions getProxy();

    /**
     * Interface to be implemented by classes that wish to describe the http based client proxy.
     */
    interface HttpProxyOptions extends ProxyOptions {

        /**
         * Get the http non-proxy host.
         * @return the non-proxy host.
         */
        String getNonProxyHosts();
    }

    /**
     * Interface to be implemented by classes that wish to describe the amqp based client proxy.
     */
    interface AmqpProxyOptions extends ProxyOptions {

        /**
         * Get the proxy authentication type.
         * @return the proxy authentication type.
         */
        String getAuthenticationType();
    }

    /**
     * Interface to be implemented by classes that wish to describe the proxy.
     */
    interface ProxyOptions {

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
