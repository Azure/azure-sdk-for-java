/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

/**
 * The configuration to be applied on a {@link HttpClient}.
 */
public class HttpClientConfiguration {
    private ProxyOptions proxy;

    /**
     * Get proxy options.
     *
     * @return the configuration of the proxy to use
     */
    public ProxyOptions proxy() {
        return proxy;
    }

    /**
     * Sets proxy configuration.
     *
     * @param proxyOptions the proxy configuration
     * @return this HttpClientConfiguration
     */
    public HttpClientConfiguration withProxy(ProxyOptions proxyOptions) {
        this.proxy = proxyOptions;
        return this;
    }
}
