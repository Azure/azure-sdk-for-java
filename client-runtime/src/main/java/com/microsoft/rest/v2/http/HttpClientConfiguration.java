/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.net.Proxy;

/**
 * The set of parameters used to create an HTTP client.
 */
public class HttpClientConfiguration {
    private final Proxy proxy;
    private final boolean isProxyHTTPS;

    /**
     * @return The optional proxy to use.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * Indicates whether the connection to the proxy is via HTTP or HTTPS.
     * This is unrelated to whether the final resource being accessed is over HTTP or HTTPS.
     * @return true if the proxy should be connected via HTTPS
     */
    public boolean isProxyHTTPS() {
        return isProxyHTTPS;
    }

    /**
     * Creates an HttpClientConfiguration.
     * @param proxy The optional proxy to use.
     * @param isProxyHTTPS true if the proxy should be connected via HTTPS
     */
    public HttpClientConfiguration(Proxy proxy, boolean isProxyHTTPS) {
        this.proxy = proxy;
        this.isProxyHTTPS = isProxyHTTPS;
    }
}
