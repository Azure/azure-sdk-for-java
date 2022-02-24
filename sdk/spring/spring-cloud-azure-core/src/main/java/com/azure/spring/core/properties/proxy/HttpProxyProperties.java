// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.proxy;

import com.azure.spring.core.aware.ProxyOptionsAware;

/**
 * Extend the proxy properties for HTTP connection
 */
public final class HttpProxyProperties extends ProxyProperties implements ProxyOptionsAware.HttpProxy {

    /**
     * A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.
     */
    private String nonProxyHosts;

    /**
     * Get the non-proxy hosts.
     * @return The non-proxy hosts.
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    /**
     * Set the non-proxy hosts.
     * @param nonProxyHosts The non-proxy hosts.
     */
    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

}
