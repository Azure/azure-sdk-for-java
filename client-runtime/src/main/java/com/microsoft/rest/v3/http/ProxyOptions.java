/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

import reactor.netty.tcp.ProxyProvider.Proxy;

import java.net.InetSocketAddress;

/**
 * Optional configurations for proxy.
 */
public class ProxyOptions {
    private final InetSocketAddress address;
    private final Type type;

    /**
     * Creates a default proxy options object.
     */
    public ProxyOptions(Type type, InetSocketAddress address) {
        this.type = type;
        this.address = address;
    }

    /**
     * @return the address of the proxy.
     */
    public InetSocketAddress address() {
        return address;
    }

    /**
     * @return the type of the proxy.
     */
    public Type type() {
        return type;
    }

    /**
     * The type of the proxy, mapping the types defined in Reactor-Netty.
     */
    public enum Type {
        HTTP(Proxy.HTTP),
        SOCKS4(Proxy.SOCKS4),
        SOCKS5(Proxy.SOCKS5);

        private final Proxy value;

        Type(Proxy reactorProxyType) {
            this.value = reactorProxyType;
        }

        Proxy value() {
            return value;
        }
    }
}
