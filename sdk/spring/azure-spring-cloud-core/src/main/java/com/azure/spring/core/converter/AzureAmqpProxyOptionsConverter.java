// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import org.springframework.core.convert.converter.Converter;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Converts a {@link ProxyProperties} to a {@link ProxyOptions}.
 */
public final class AzureAmqpProxyOptionsConverter implements Converter<ProxyProperties, ProxyOptions> {

    @Override
    public ProxyOptions convert(ProxyProperties properties) {
        ProxyAuthenticationType authenticationType;
        switch (properties.getAuthenticationType()) {
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
        switch (properties.getType()) {
            case "http":
                type = Proxy.Type.HTTP;
                break;
            case "socks":
                type = Proxy.Type.SOCKS;
                break;
            default:
                type = Proxy.Type.DIRECT;
        }
        Proxy proxyAddress = new Proxy(type, new InetSocketAddress(properties.getHostname(), properties.getPort()));
        return new ProxyOptions(authenticationType, proxyAddress, properties.getUsername(), properties.getPassword());
    }
}
