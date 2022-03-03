// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.spring.cloud.core.aware.ProxyOptionsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Locale;

/**
 * Converts a {@link ProxyOptionsAware.Proxy} to a {@link ProxyOptions}.
 */
public final class AzureAmqpProxyOptionsConverter implements Converter<ProxyOptionsAware.Proxy, ProxyOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAmqpProxyOptionsConverter.class);
    public static final AzureAmqpProxyOptionsConverter AMQP_PROXY_CONVERTER = new AzureAmqpProxyOptionsConverter();

    private AzureAmqpProxyOptionsConverter() {

    }

    @Override
    public ProxyOptions convert(ProxyOptionsAware.Proxy proxy) {
        if (!StringUtils.hasText(proxy.getHostname()) || proxy.getPort() == null) {
            LOGGER.debug("Proxy hostname or port is not set.");
            return null;
        }
        ProxyAuthenticationType authenticationType = ProxyAuthenticationType.NONE;
        if (proxy instanceof ProxyOptionsAware.AmqpProxy) {
            ProxyOptionsAware.AmqpProxy amqpProxy = (ProxyOptionsAware.AmqpProxy) proxy;
            if (amqpProxy.getAuthenticationType() != null) {
                switch (amqpProxy.getAuthenticationType().toLowerCase(Locale.ROOT)) {
                    case "basic":
                        authenticationType = ProxyAuthenticationType.BASIC;
                        break;
                    case "digest":
                        authenticationType = ProxyAuthenticationType.DIGEST;
                        break;
                    default:
                }
            }
        }
        Proxy.Type type;
        if (proxy.getType() == null) {
            throw new IllegalArgumentException("Wrong proxy type provided!");
        }
        switch (proxy.getType()) {
            case "http":
                type = Proxy.Type.HTTP;
                break;
            case "socks":
                type = Proxy.Type.SOCKS;
                break;
            default:
                throw new IllegalArgumentException("Wrong proxy type provided!");
        }
        Proxy proxyAddress = new Proxy(type, new InetSocketAddress(proxy.getHostname(), proxy.getPort()));
        return new ProxyOptions(authenticationType, proxyAddress, proxy.getUsername(), proxy.getPassword());
    }
}
