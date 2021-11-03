// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.spring.core.aware.ProxyAware;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Converts a {@link ProxyAware.Proxy} to a {@link ProxyOptions}.
 */
public final class AzureAmqpProxyOptionsConverter implements Converter<ProxyAware.Proxy, ProxyOptions> {

    public static final AzureAmqpProxyOptionsConverter AMQP_PROXY_CONVERTER = new AzureAmqpProxyOptionsConverter();

    @Override
    public ProxyOptions convert(ProxyAware.Proxy proxy) {
        if (!StringUtils.hasText(proxy.getHostname())) {
            return null;
        }
        ProxyAuthenticationType authenticationType;
        switch (proxy.getAuthenticationType()) {
            case "basic":
                authenticationType = ProxyAuthenticationType.BASIC;
                break;
            case "digest":
                authenticationType = ProxyAuthenticationType.DIGEST;
                break;
            default:
                authenticationType = ProxyAuthenticationType.NONE;
        }
        Proxy.Type type;
        switch (proxy.getType()) {
            case "http":
                type = Proxy.Type.HTTP;
                break;
            case "socks":
                type = Proxy.Type.SOCKS;
                break;
            default:
                type = Proxy.Type.DIRECT;
        }
        Proxy proxyAddress = new Proxy(type, new InetSocketAddress(proxy.getHostname(), proxy.getPort()));
        return new ProxyOptions(authenticationType, proxyAddress, proxy.getUsername(), proxy.getPassword());
    }
}
