// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Locale;

/**
 * Converts a {@link ProxyOptionsProvider.AmqpProxyOptions} to a {@link ProxyOptions}.
 */
public final class AzureAmqpProxyOptionsConverter implements Converter<ProxyOptionsProvider.AmqpProxyOptions, ProxyOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAmqpProxyOptionsConverter.class);
    public static final AzureAmqpProxyOptionsConverter AMQP_PROXY_CONVERTER = new AzureAmqpProxyOptionsConverter();

    private AzureAmqpProxyOptionsConverter() {

    }

    @Override
    public ProxyOptions convert(ProxyOptionsProvider.AmqpProxyOptions proxy) {
        if (!StringUtils.hasText(proxy.getHostname()) || proxy.getPort() == null) {
            LOGGER.debug("Proxy hostname or port is not set.");
            return null;
        }

        if (proxy.getType() == null) {
            throw new IllegalArgumentException("Wrong proxy type provided!");
        }

        ProxyAuthenticationType authenticationType = ProxyAuthenticationType.NONE;
        if (proxy.getAuthenticationType() != null) {
            switch (proxy.getAuthenticationType().toLowerCase(Locale.ROOT)) {
                case "basic":
                    authenticationType = ProxyAuthenticationType.BASIC;
                    break;
                case "digest":
                    authenticationType = ProxyAuthenticationType.DIGEST;
                    break;
                default:
            }
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
                throw new IllegalArgumentException("Wrong proxy type provided!");
        }

        Proxy proxyAddress = new Proxy(type, new InetSocketAddress(proxy.getHostname(), proxy.getPort()));
        return new ProxyOptions(authenticationType, proxyAddress, proxy.getUsername(), proxy.getPassword());
    }
}
