// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.http.ProxyOptions;
import com.azure.spring.core.aware.ProxyAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;

/**
 * Converts a {@link ProxyAware.Proxy} to a {@link ProxyOptions}.
 */
public final class AzureHttpProxyOptionsConverter implements Converter<ProxyAware.Proxy, ProxyOptions> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureHttpProxyOptionsConverter.class);
    public static final AzureHttpProxyOptionsConverter HTTP_PROXY_CONVERTER = new AzureHttpProxyOptionsConverter();

    @Override
    public ProxyOptions convert(ProxyAware.Proxy proxy) {
        if (!StringUtils.hasText(proxy.getHostname()) || proxy.getPort() == null) {
            LOGGER.debug("Proxy hostname or port is not set.");
            return null;
        }

        final String type = proxy.getType();
        ProxyOptions.Type sdkProxyType;
        if ("http".equalsIgnoreCase(type)) {
            sdkProxyType = ProxyOptions.Type.HTTP;
        } else {
            sdkProxyType = ProxyOptions.Type.SOCKS4;
        }

        ProxyOptions proxyOptions = new ProxyOptions(sdkProxyType, new InetSocketAddress(proxy.getHostname(),
                                                                                         proxy.getPort()));
        if (StringUtils.hasText(proxy.getUsername()) && StringUtils.hasText(proxy.getPassword())) {
            proxyOptions.setCredentials(proxy.getUsername(), proxy.getPassword());
        }

        if (proxy instanceof ProxyAware.HttpProxy) {
            ProxyAware.HttpProxy httpProxyProperties = (ProxyAware.HttpProxy) proxy;
            if (StringUtils.hasText(httpProxyProperties.getNonProxyHosts())) {
                proxyOptions.setNonProxyHosts(httpProxyProperties.getNonProxyHosts());
            }
        }
        return proxyOptions;
    }
}
