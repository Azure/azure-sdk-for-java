// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * This represents proxy configuration to be used in http clients..
 */
public class ProxyOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ProxyOptions.class);

    private static final String INVALID_CONFIGURATION_MESSAGE = "'configuration' cannot be 'Configuration.NONE'.";
    private static final String INVALID_AZURE_PROXY_URL = "Configuration {} is an invalid URL and is being ignored.";

    /*
     * This indicates whether Java environment proxy configurations are allowed to be used.
     */
    private static final String JAVA_PROXY_PREREQUISITE = "java.net.useSystemProxies";

    /*
     * Java environment variables related to proxies. The protocol is removed since these are the same for 'https' and
     * 'http', the exception is 'http.nonProxyHosts' as it is used for both.
     */
    private static final String JAVA_PROXY_HOST = "proxyHost";
    private static final String JAVA_PROXY_PORT = "proxyPort";
    private static final String JAVA_PROXY_USER = "proxyUser";
    private static final String JAVA_PROXY_PASSWORD = "proxyPassword";
    private static final String JAVA_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String HTTPS = "https";
    private static final int DEFAULT_HTTPS_PORT = 443;

    private static final String HTTP = "http";
    private static final int DEFAULT_HTTP_PORT = 80;

    private static final List<Function<Configuration, ProxyOptions>> ENVIRONMENT_LOAD_ORDER = Arrays.asList(
        configuration -> attemptToLoadAzureProxy(configuration, Configuration.PROPERTY_HTTPS_PROXY),
        configuration -> attemptToLoadAzureProxy(configuration, Configuration.PROPERTY_HTTP_PROXY),
        configuration -> attemptToLoadJavaProxy(configuration, HTTPS),
        configuration -> attemptToLoadJavaProxy(configuration, HTTP)
    );

    private final InetSocketAddress address;
    private final Type type;
    private String username;
    private String password;
    private String nonProxyHosts;

    /**
     * Creates ProxyOptions.
     *
     * @param type the proxy type
     * @param address the proxy address (ip and port number)
     */
    public ProxyOptions(Type type, InetSocketAddress address) {
        this.type = type;
        this.address = address;
    }

    /**
     * Set the proxy credentials.
     *
     * @param username proxy user name
     * @param password proxy password
     * @return the updated ProxyOptions object
     */
    public ProxyOptions setCredentials(String username, String password) {
        this.username = Objects.requireNonNull(username, "'username' cannot be null.");
        this.password = Objects.requireNonNull(password, "'password' cannot be null.");
        return this;
    }

    /**
     * Sets the hosts which bypass the proxy.
     *
     * <p>
     * The expected format of the passed string is a {@code '|'} delimited list of hosts which should bypass the proxy.
     * Individual host strings may contain regex characters such as {@code '*'}.
     *
     * @param nonProxyHosts Hosts that bypass the proxy.
     * @return the updated ProxyOptions object
     */
    public ProxyOptions setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
        return this;
    }

    /**
     * @return the address of the proxy.
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * @return the type of the proxy.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the proxy user name.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @return the proxy password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @return the hosts that bypass the proxy.
     */
    public String getNonProxyHosts() {
        return this.nonProxyHosts;
    }

    /**
     * Attempts to load a proxy from the environment.
     *
     * <p>
     * Environment configurations are loaded in this order:
     * <ol>
     *     <li>Azure HTTPS</li>
     *     <li>Azure HTTP</li>
     *     <li>Java HTTPS</li>
     *     <li>Java HTTP</li>
     * </ol>
     *
     * Azure proxy configurations will be preferred over Java proxy configurations as they are more closely scoped to
     * the purpose of the SDK. Additionally, more secure protocols, HTTPS vs HTTP, will be preferred.
     *
     * <p>
     * {@code null} will be returned if no proxy was found in the environment.
     *
     * @param configuration The {@link Configuration} that is used to load proxy configurations from the environment.
     * If {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used. If
     * {@link Configuration#NONE} is passed {@link IllegalArgumentException} will be thrown.
     * @return A {@link ProxyOptions} reflecting a proxy loaded from the environment, if no proxy is found {@code null}
     * will be returned.
     * @throws IllegalArgumentException If {@code configuration} is {@link Configuration#NONE}.
     */
    public static ProxyOptions fromConfiguration(Configuration configuration) {
        if (configuration == Configuration.NONE) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(INVALID_CONFIGURATION_MESSAGE));
        }

        Configuration proxyConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        for (Function<Configuration, ProxyOptions> loader : ENVIRONMENT_LOAD_ORDER) {
            ProxyOptions proxyOptions = loader.apply(proxyConfiguration);
            if (proxyOptions != null) {
                return proxyOptions;
            }
        }

        return null;
    }

    private static ProxyOptions attemptToLoadAzureProxy(Configuration configuration, String proxyProperty) {
        String proxyConfiguration = configuration.get(proxyProperty);

        // No proxy configuration setup.
        if (CoreUtils.isNullOrEmpty(proxyConfiguration)) {
            return null;
        }

        try {
            URL proxyUrl = new URL(proxyConfiguration);
            int port = (proxyUrl.getPort() == -1) ? proxyUrl.getDefaultPort() : proxyUrl.getPort();
            ProxyOptions proxyOptions = new ProxyOptions(Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), port))
                .setNonProxyHosts(configuration.get(Configuration.PROPERTY_NO_PROXY));

            String userInfo = proxyUrl.getUserInfo();
            if (userInfo != null) {
                String[] usernamePassword = userInfo.split(":", 2);
                if (usernamePassword.length == 2) {
                    try {
                        proxyOptions.setCredentials(
                            URLDecoder.decode(usernamePassword[0], StandardCharsets.UTF_8.toString()),
                            URLDecoder.decode(usernamePassword[1], StandardCharsets.UTF_8.toString())
                        );
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }

            return proxyOptions;
        } catch (MalformedURLException ex) {
            LOGGER.warning(INVALID_AZURE_PROXY_URL, proxyProperty);
            return null;
        }
    }

    private static ProxyOptions attemptToLoadJavaProxy(Configuration configuration, String type) {
        // Not allowed to use Java proxies
        if (!Boolean.parseBoolean(configuration.get(JAVA_PROXY_PREREQUISITE))) {
            return null;
        }

        String host = configuration.get(type + "." + JAVA_PROXY_HOST);

        // No proxy configuration setup.
        if (CoreUtils.isNullOrEmpty(host)) {
            return null;
        }

        int port;
        try {
            port = Integer.parseInt(configuration.get(type + "." + JAVA_PROXY_PORT));
        } catch (NumberFormatException ex) {
            port = HTTPS.equals(type) ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
        }

        ProxyOptions proxyOptions = new ProxyOptions(Type.HTTP, new InetSocketAddress(host, port))
            .setNonProxyHosts(configuration.get(JAVA_NON_PROXY_HOSTS));

        String username = configuration.get(type + "." + JAVA_PROXY_USER);
        String password = configuration.get(type + "." + JAVA_PROXY_PASSWORD);

        if (username != null && password != null) {
            proxyOptions.setCredentials(username, password);
        }

        return proxyOptions;
    }

    /**
     * The type of the proxy.
     */
    public enum Type {
        /**
         * HTTP proxy type.
         */
        HTTP(Proxy.Type.HTTP),

        /**
         * SOCKS4 proxy type.
         */
        SOCKS4(Proxy.Type.SOCKS),

        /**
         * SOCKS5 proxy type.
         */
        SOCKS5(Proxy.Type.SOCKS);

        private final Proxy.Type proxyType;

        Type(Proxy.Type proxyType) {
            this.proxyType = proxyType;
        }

        /**
         * Get the {@link Proxy.Type} equivalent of this type.
         *
         * @return the proxy type
         */
        public Proxy.Type toProxyType() {
            return proxyType;
        }
    }
}
