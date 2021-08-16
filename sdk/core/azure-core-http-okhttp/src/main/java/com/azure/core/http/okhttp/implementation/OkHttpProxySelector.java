// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * This class handles selecting the proxy during a request.
 */
public final class OkHttpProxySelector extends ProxySelector {
    private final Proxy.Type proxyType;
    private final Supplier<InetSocketAddress> proxyAddressSupplier;
    private final Pattern nonProxyHostsPattern;

    public OkHttpProxySelector(Proxy.Type proxyType, Supplier<InetSocketAddress> proxyAddressSupplier,
        String nonProxyHosts) {
        this.proxyType = proxyType;
        this.proxyAddressSupplier = proxyAddressSupplier;
        this.nonProxyHostsPattern = (nonProxyHosts == null)
            ? null
            : Pattern.compile(nonProxyHosts, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public List<Proxy> select(URI uri) {
        /*
         * If the host of the URI matches the nonProxyHostsPattern return no options for proxying, otherwise return the
         * proxy.
         */
        return (nonProxyHostsPattern == null || !nonProxyHostsPattern.matcher(uri.getHost()).matches())
            ? Collections.singletonList(new Proxy(proxyType, getProxyAddress(proxyAddressSupplier)))
            : null;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        // Ignored.
    }

    private static InetSocketAddress getProxyAddress(Supplier<InetSocketAddress> proxyAddressSupplier) {
        InetSocketAddress socketAddress = proxyAddressSupplier.get();
        if (!socketAddress.isUnresolved()) {
            return socketAddress;
        }

        return new InetSocketAddress(socketAddress.getHostString(), socketAddress.getPort());
    }
}
