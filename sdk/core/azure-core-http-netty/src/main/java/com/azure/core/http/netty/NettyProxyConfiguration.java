// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.util.Configuration;
import reactor.netty.tcp.ProxyProvider;

import java.util.Objects;
import java.util.function.Function;

/**
 * This class handles loading configurations from the environment used to apply a proxy to Reactor Netty.
 */
final class NettyProxyConfiguration {
    // The JVM uses this property to determine if it is allowed to use environment configured proxies.
    private static final Function<Configuration, Boolean> JAVA_PROXY_PREREQUISITE =
        configuration -> Boolean.parseBoolean(loadConfigurationValue(configuration, "java.net.useSystemProxies"));

    private static final NettyProxyConfiguration AZURE_HTTPS_PROXY_CONFIGURATION =
        new NettyProxyConfiguration("https", Configuration.PROPERTY_HTTPS_PROXY, (ignored) -> true)
            .setNonProxyHostsProperty(Configuration.PROPERTY_NO_PROXY);

    private static final NettyProxyConfiguration AZURE_HTTP_PROXY_CONFIGURATION =
        new NettyProxyConfiguration("http", Configuration.PROPERTY_HTTP_PROXY, (ignored) -> true)
            .setNonProxyHostsProperty(Configuration.PROPERTY_NO_PROXY);

    private static final NettyProxyConfiguration JAVA_HTTPS_PROXY_CONFIGURATION =
        new NettyProxyConfiguration("https", "https.proxyHost", JAVA_PROXY_PREREQUISITE)
            .setPortProperty("https.proxyPort")
            .setUsernameProperty("https.proxyUser")
            .setPasswordProperty("https.proxyPassword")
            .setNonProxyHostsProperty("http.nonProxyHosts");

    private static final NettyProxyConfiguration JAVA_HTTP_PROXY_CONFIGURATION =
        new NettyProxyConfiguration("http", "http.proxyHost", JAVA_PROXY_PREREQUISITE)
            .setPortProperty("http.proxyPort")
            .setUsernameProperty("http.proxyUser")
            .setPasswordProperty("http.proxyPassword")
            .setNonProxyHostsProperty("http.nonProxyHosts");

    /*
     * The order which configurations are checked for in the environment.
     *
     * Azure proxy configurations are preferred over Java configurations as Azure configurations are more closely
     * scope to the use of an SDK. Additionally, more secure proxies are chosen over less secure proxies, hence HTTPS
     * is preferred over HTTP.
     */
    static final NettyProxyConfiguration[] PROXY_CONFIGURATIONS_LOAD_ORDER = {
        AZURE_HTTPS_PROXY_CONFIGURATION,
        AZURE_HTTP_PROXY_CONFIGURATION,
        JAVA_HTTPS_PROXY_CONFIGURATION,
        JAVA_HTTP_PROXY_CONFIGURATION
    };

    private final String type;
    private final String hostProperty;
    private final Function<Configuration, Boolean> prerequisiteValidator;

    private String portProperty;
    private String usernameProperty;
    private String passwordProperty;
    private String nonProxyHostsProperty;

    private NettyProxyConfiguration(String proxyType, String hostProperty,
        Function<Configuration, Boolean> prerequisiteValidator) {
        this.type = Objects.requireNonNull(proxyType);
        this.hostProperty = Objects.requireNonNull(hostProperty);
        this.prerequisiteValidator = Objects.requireNonNull(prerequisiteValidator);
    }

    private NettyProxyConfiguration setPortProperty(String portProperty) {
        this.portProperty = portProperty;
        return this;
    }

    private NettyProxyConfiguration setUsernameProperty(String usernameProperty) {
        this.usernameProperty = usernameProperty;
        return this;
    }

    private NettyProxyConfiguration setPasswordProperty(String passwordProperty) {
        this.passwordProperty = passwordProperty;
        return this;
    }

    private NettyProxyConfiguration setNonProxyHostsProperty(String nonProxyHostsProperty) {
        this.nonProxyHostsProperty = nonProxyHostsProperty;
        return this;
    }

    /*
     * Returns whether Reactor Netty would be able to apply this configuration if it is found.
     */
    boolean canProxyConfigurationBeApplied(Configuration configuration) {
        return prerequisiteValidator.apply(configuration);
    }

    /*
     * Returns the type of proxy this configuration represents.
     *
     * If the type is unknown an IllegalStateException will be thrown.
     */
    ProxyProvider.Proxy getType() {
        switch (type) {
            case "https":
            case "http":
                return ProxyProvider.Proxy.HTTP;
            default:
                throw new IllegalStateException("Unsupported 'type', unable to return 'ProxyProvider.Proxy'.");
        }
    }

    /*
     * Returns the host that the represents the proxy.
     */
    String getHost(Configuration configuration) {
        return loadConfigurationValue(configuration, hostProperty);
    }

    /*
     * Returns the port to send proxy requests.
     *
     * If there is no port listed or the value is an invalid integer the default port for the protocol will be used,
     * 443 for HTTPS and 80 for HTTP.
     *
     * If the type is unknown an IllegalStateException will be thrown.
     */
    int getPort(Configuration configuration) {
        try {
            return Integer.parseInt(loadConfigurationValue(configuration, portProperty));
        } catch (NumberFormatException ignored) {
            switch (type) {
                case "https":
                    return 443;
                case "http":
                    return 80;
                default:
                    throw new IllegalStateException("Unsupported 'type', unable to return a default port.");
            }
        }
    }

    /*
     * Returns the username used to authenticate to the proxy.
     */
    String getUsername(Configuration configuration) {
        return loadConfigurationValue(configuration, usernameProperty);
    }

    /*
     * Returns the password used to authenticate to the proxy.
     */
    String getPassword(Configuration configuration) {
        return loadConfigurationValue(configuration, passwordProperty);
    }

    /*
     * Returns a pipe ('|') delimited list of hosts, which may include wildcards, represented as a string which contains
     * the hosts that bypass the proxy.
     */
    String getNonProxyHosts(Configuration configuration) {
        return loadConfigurationValue(configuration, nonProxyHostsProperty);
    }

    private static String loadConfigurationValue(Configuration configuration, String name) {
        return (name == null) ? null : configuration.get(name);
    }
}
