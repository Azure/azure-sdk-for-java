// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.http.ProxyOptions;
import com.azure.spring.core.properties.ProxyProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;

/**
 * Converts a {@link ProxyProperties} to a {@link ProxyOptions}
 */
public final class AzureHttpProxyOptionsConverter implements Converter<ProxyProperties, ProxyOptions> {

    @Override
    public ProxyOptions convert(ProxyProperties proxyProperties) {
        final String type = proxyProperties.getType();
        ProxyOptions.Type sdkProxyType;
        if ("http".equalsIgnoreCase(type)) {
            sdkProxyType = ProxyOptions.Type.HTTP;
        } else {
            sdkProxyType = ProxyOptions.Type.SOCKS4;
        }

        ProxyOptions proxyOptions = new ProxyOptions(sdkProxyType, new InetSocketAddress(proxyProperties.getHostname(),
                                                                                         proxyProperties.getPort()));
        if (StringUtils.hasText(proxyProperties.getUsername()) && StringUtils.hasText(proxyProperties.getPassword())) {
            proxyOptions.setCredentials(proxyProperties.getUsername(), proxyProperties.getPassword());
        }
        // TODO (xiada) non proxy hosts
        return proxyOptions;

    }
}
