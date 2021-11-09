// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.proxy;

import com.azure.spring.core.aware.ProxyAware;

/**
 * Extend the proxy properties for HTTP connection
 */
public class HttpProxyProperties extends ProxyProperties implements ProxyAware.HttpProxy {

    private String nonProxyHosts;

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

}
