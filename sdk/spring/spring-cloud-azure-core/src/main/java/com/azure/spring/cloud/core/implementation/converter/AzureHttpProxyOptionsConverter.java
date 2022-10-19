// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.http.ProxyOptions;
import com.azure.spring.cloud.core.provider.ProxyOptionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;

/**
 * Converts a {@link ProxyOptionsProvider.HttpProxyOptions} to a {@link ProxyOptions}.
 */
public final class AzureHttpProxyOptionsConverter implements Converter<ProxyOptionsProvider.HttpProxyOptions, ProxyOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureHttpProxyOptionsConverter.class);
    public static final AzureHttpProxyOptionsConverter HTTP_PROXY_CONVERTER = new AzureHttpProxyOptionsConverter();

    private AzureHttpProxyOptionsConverter() {

    }

    @Override
    public ProxyOptions convert(ProxyOptionsProvider.HttpProxyOptions proxy) {
        if (!StringUtils.hasText(proxy.getHostname()) || proxy.getPort() == null) {
            LOGGER.debug("Proxy hostname or port is not set.");
            return null;
        }

        final String type = proxy.getType();
        ProxyOptions.Type sdkProxyType = null;
        if ("http".equalsIgnoreCase(type)) {
            sdkProxyType = com.azure.core.http.ProxyOptions.Type.HTTP;
        } else if ("socks".equalsIgnoreCase(type) || "socks4".equalsIgnoreCase(type)) {
            sdkProxyType = com.azure.core.http.ProxyOptions.Type.SOCKS4;
        } else if ("socks5".equalsIgnoreCase(type)) {
            sdkProxyType = com.azure.core.http.ProxyOptions.Type.SOCKS5;
        } else {
            throw new IllegalArgumentException("Wrong proxy type provided!");
        }

        ProxyOptions proxyOptions = new ProxyOptions(sdkProxyType, new InetSocketAddress(proxy.getHostname(),
                                                                                         proxy.getPort()));
        if (StringUtils.hasText(proxy.getUsername()) && StringUtils.hasText(proxy.getPassword())) {
            proxyOptions.setCredentials(proxy.getUsername(), proxy.getPassword());
        }

        if (StringUtils.hasText(proxy.getNonProxyHosts())) {
            proxyOptions.setNonProxyHosts(proxy.getNonProxyHosts());
        }
        return proxyOptions;
    }
}
