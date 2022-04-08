// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;
import java.util.regex.Pattern;

/***
 * Utility class to help with {@link ProxyOptions}.
 */
public final class ProxyUtil {

    private static final Pattern HOST_PORT_PATTERN = Pattern.compile("^[^:]+:\\d+");
    private static final ClientLogger LOGGER = new ClientLogger(ProxyUtil.class);

    private ProxyUtil() {
        // private default constructor
    }

    /***
     * Static method to construct {@link ProxyOptions} using the values from configuration .
     *
     * @param configuration - The client library configuration
     * @return the constructed {@link ProxyOptions} object
     */
    public static ProxyOptions getDefaultProxyConfiguration(Configuration configuration) {
        Objects.requireNonNull(configuration, "'configuration' cannot be null.");
        final ProxyAuthenticationType authentication = ProxyAuthenticationType.valueOf(configuration.get(
            ProxyOptions.PROXY_AUTHENTICATION_TYPE, ProxyAuthenticationType.NONE.toString()));

        final String proxyAddress = configuration.get(Configuration.PROPERTY_HTTP_PROXY);

        if (CoreUtils.isNullOrEmpty(proxyAddress)) {
            return ProxyOptions.SYSTEM_DEFAULTS;
        }

        final boolean useSystemProxies = Boolean.parseBoolean(configuration.get("java.net.useSystemProxies"));

        if (HOST_PORT_PATTERN.matcher(proxyAddress.trim()).find()) {
            final String[] hostPort = proxyAddress.split(":");
            final String host = hostPort[0];
            final int port = Integer.parseInt(hostPort[1]);
            final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            final String username = configuration.get(ProxyOptions.PROXY_USERNAME);
            final String password = configuration.get(ProxyOptions.PROXY_PASSWORD);
            return new ProxyOptions(authentication, proxy, username, password);
        } else if (useSystemProxies) {
            // java.net.useSystemProxies needs to be set to true in this scenario.
            // If it is set to false 'ProxyOptions' in azure-core will return null.
            com.azure.core.http.ProxyOptions coreProxyOptions = com.azure.core.http.ProxyOptions
                .fromConfiguration(configuration);
            return new ProxyOptions(authentication, new Proxy(coreProxyOptions.getType().toProxyType(),
                coreProxyOptions.getAddress()), coreProxyOptions.getUsername(), coreProxyOptions.getPassword());
        } else {
            LOGGER.verbose("'HTTP_PROXY' was configured but ignored as 'java.net.useSystemProxies' wasn't "
                + "set or was false.");
            return ProxyOptions.SYSTEM_DEFAULTS;
        }
    }
}
