// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class handles selecting the proxy during a request.
 */
public final class OkHttpProxySelector extends ProxySelector {
    private final Proxy.Type proxyType;
    private final SocketAddress proxyAddress;
    private final Pattern nonProxyHostsPattern;

    public OkHttpProxySelector(Proxy.Type proxyType, SocketAddress proxyAddress, String nonProxyHosts) {
        this.proxyType = proxyType;
        this.proxyAddress = proxyAddress;
        this.nonProxyHostsPattern = Pattern.compile(nonProxyHosts);
    }

    @Override
    public List<Proxy> select(URI uri) {
        /*
         * If the 'URI' the request is being sent to matches the 'nonProxyHostsPattern' return no options for proxying,
         * otherwise return the proxy.
         */
        return nonProxyHostsPattern.matcher(uri.toString()).find()
            ? null
            : Collections.singletonList(new Proxy(proxyType, proxyAddress));
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        // Ignored.
    }
}
