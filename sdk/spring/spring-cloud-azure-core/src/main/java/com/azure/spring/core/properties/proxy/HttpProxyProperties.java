// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.proxy;

/**
 * Extend the proxy properties for HTTP connection
 */
public class HttpProxyProperties extends ProxyProperties {
    private String nonProxyHosts;

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }
}
