// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.proxy;

import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;

/**
 * Proxy properties for all Azure HTTP SDKs.
 */
public class HttpProxyConfigurationProperties extends ProxyConfigurationProperties
    implements ProxyOptionsProvider.HttpProxyOptions {

    /**
     * A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.
     */
    private String nonProxyHosts;

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

}
