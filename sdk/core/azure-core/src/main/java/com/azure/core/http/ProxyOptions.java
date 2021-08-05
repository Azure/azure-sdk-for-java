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
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This represents proxy configuration to be used in http clients..
 */
public class ProxyOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ProxyOptions.class);

    private static final String INVALID_CONFIGURATION_MESSAGE = "'configuration' cannot be 'Configuration.NONE'.";
    private static final String INVALID_AZURE_PROXY_URL = "Configuration {} is an invalid URL and is being ignored.";

    /*
     * This indicates whether system proxy configurations (HTTPS_PROXY, HTTP_PROXY) are allowed to be used.
     */
    private static final String JAVA_SYSTEM_PROXY_PREREQUISITE = "java.net.useSystemProxies";

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
     * <p>
     * The expected format of the passed string is a {@code '|'} delimited list of hosts which should bypass the proxy.
     * Individual host strings may contain regex characters such as {@code '*'}.
     *
     * @param nonProxyHosts Hosts that bypass the proxy.
     * @return the updated ProxyOptions object
     */
    public ProxyOptions setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = sanitizeJavaHttpNonProxyHosts(nonProxyHosts);
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
     * <p>
     * If a proxy is found and loaded the proxy address is DNS resolved.
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
     * @param configuration The {@link Configuration} that is used to load proxy configurations from the environment. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used. If {@link
     * Configuration#NONE} is passed {@link IllegalArgumentException} will be thrown.
     * @return A {@link ProxyOptions} reflecting a proxy loaded from the environment, if no proxy is found {@code null}
     * will be returned.
     * @throws IllegalArgumentException If {@code configuration} is {@link Configuration#NONE}.
     */
    public static ProxyOptions fromConfiguration(Configuration configuration) {
        return fromConfiguration(configuration, false);
    }

    /**
     * Attempts to load a proxy from the environment.
     * <p>
     * If a proxy is found and loaded, the proxy address is DNS resolved based on {@code createUnresolved}. When {@code
     * createUnresolved} is true resolving {@link #getAddress()} may be required before using the address in network
     * calls.
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
     * <p>
     * {@code null} will be returned if no proxy was found in the environment.
     *
     * @param configuration The {@link Configuration} that is used to load proxy configurations from the environment. If
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used. If {@link
     * Configuration#NONE} is passed {@link IllegalArgumentException} will be thrown.
     * @param createUnresolved Flag determining whether the returned {@link ProxyOptions} is unresolved.
     * @return A {@link ProxyOptions} reflecting a proxy loaded from the environment, if no proxy is found {@code null}
     * will be returned.
     * @throws IllegalArgumentException If {@code configuration} is {@link Configuration#NONE}.
     */
    public static ProxyOptions fromConfiguration(Configuration configuration, boolean createUnresolved) {
        if (configuration == Configuration.NONE) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(INVALID_CONFIGURATION_MESSAGE));
        }

        Configuration proxyConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        return attemptToLoadProxy(proxyConfiguration, createUnresolved);
    }

    private static ProxyOptions attemptToLoadProxy(Configuration configuration, boolean createUnresolved) {
        ProxyOptions proxyOptions;

        // Only use system proxies when the prerequisite property is 'true'.
        if (Boolean.parseBoolean(configuration.get(JAVA_SYSTEM_PROXY_PREREQUISITE))) {
            proxyOptions = attemptToLoadSystemProxy(configuration, createUnresolved,
                Configuration.PROPERTY_HTTPS_PROXY);
            if (proxyOptions != null) {
                LOGGER.verbose("Using proxy created from HTTPS_PROXY environment variable.");
                return proxyOptions;
            }

            proxyOptions = attemptToLoadSystemProxy(configuration, createUnresolved, Configuration.PROPERTY_HTTP_PROXY);
            if (proxyOptions != null) {
                LOGGER.verbose("Using proxy created from HTTP_PROXY environment variable.");
                return proxyOptions;
            }
        }

        proxyOptions = attemptToLoadJavaProxy(configuration, createUnresolved, HTTPS);
        if (proxyOptions != null) {
            LOGGER.verbose("Using proxy created from JVM HTTPS system properties.");
            return proxyOptions;
        }

        proxyOptions = attemptToLoadJavaProxy(configuration, createUnresolved, HTTP);
        if (proxyOptions != null) {
            LOGGER.verbose("Using proxy created from JVM HTTP system properties.");
            return proxyOptions;
        }

        return null;
    }

    private static ProxyOptions attemptToLoadSystemProxy(Configuration configuration, boolean createUnresolved,
        String proxyProperty) {
        String proxyConfiguration = configuration.get(proxyProperty);

        // No proxy configuration setup.
        if (CoreUtils.isNullOrEmpty(proxyConfiguration)) {
            return null;
        }

        try {
            URL proxyUrl = new URL(proxyConfiguration);
            int port = (proxyUrl.getPort() == -1) ? proxyUrl.getDefaultPort() : proxyUrl.getPort();

            InetSocketAddress socketAddress = (createUnresolved)
                ? InetSocketAddress.createUnresolved(proxyUrl.getHost(), port)
                : new InetSocketAddress(proxyUrl.getHost(), port);

            ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, socketAddress);

            String nonProxyHostsString = configuration.get(Configuration.PROPERTY_NO_PROXY);
            if (!CoreUtils.isNullOrEmpty(nonProxyHostsString)) {
                proxyOptions.nonProxyHosts = sanitizeNoProxy(nonProxyHostsString);
            }

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

    /*
     * Helper function that sanitizes 'NO_PROXY' into a Pattern safe string.
     */
    private static String sanitizeNoProxy(String noProxyString) {
        /*
         * The 'NO_PROXY' environment variable is expected to be delimited by ','.
         */
        String[] nonProxyHosts = noProxyString.split(",");

        // Do an in-place replacement with the sanitized value.
        for (int i = 0; i < nonProxyHosts.length; i++) {
            /*
             * 'NO_PROXY' doesn't have a strongly standardized format, for now we are going to support values beginning
             * and ending with '*' or '.' to exclude an entire domain and will quote the value between the prefix and
             * suffix. In the future this may need to be updated to support more complex scenarios required by
             * 'NO_PROXY' users such as wild cards within the domain exclusion.
             */
            String prefixWildcard = "";
            String suffixWildcard = "";
            String body = nonProxyHosts[i];

            /*
             * First check if the non-proxy host begins with a qualified quantifier and extract it from being quoted,
             * then check if it is a non-qualified quantifier and qualifier and extract it from being quoted.
             */
            if (body.startsWith(".*")) {
                prefixWildcard = ".*";
                body = body.substring(2);
            } else if (body.startsWith("*") || body.startsWith(".")) {
                prefixWildcard = ".*";
                body = body.substring(1);
            }

            /*
             * First check if the non-proxy host ends with a qualified quantifier and extract it from being quoted,
             * then check if it is a non-qualified quantifier and qualifier and extract it from being quoted.
             */
            if (body.endsWith(".*")) {
                suffixWildcard = ".*";
                body = body.substring(0, body.length() - 2);
            } else if (body.endsWith("*") || body.endsWith(".")) {
                suffixWildcard = ".*";
                body = body.substring(0, body.length() - 1);
            }

            /*
             * Replace the non-proxy host with the sanitized value.
             *
             * The body of the non-proxy host is quoted to handle scenarios such a '127.0.0.1' or '*.azure.com' where
             * without quoting the '.' in the string would be treated as the match any character instead of the literal
             * '.' character.
             */
            nonProxyHosts[i] = prefixWildcard + Pattern.quote(body) + suffixWildcard;
        }

        return String.join("|", nonProxyHosts);
    }

    private static ProxyOptions attemptToLoadJavaProxy(Configuration configuration, boolean createUnresolved,
        String type) {
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

        InetSocketAddress socketAddress = (createUnresolved)
            ? InetSocketAddress.createUnresolved(host, port)
            : new InetSocketAddress(host, port);

        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, socketAddress);

        String nonProxyHostsString = configuration.get(JAVA_NON_PROXY_HOSTS);
        if (!CoreUtils.isNullOrEmpty(nonProxyHostsString)) {
            proxyOptions.setNonProxyHosts(nonProxyHostsString);
        }

        String username = configuration.get(type + "." + JAVA_PROXY_USER);
        String password = configuration.get(type + "." + JAVA_PROXY_PASSWORD);

        if (username != null && password != null) {
            proxyOptions.setCredentials(username, password);
        }

        return proxyOptions;
    }

    /*
     * Helper function that sanitizes 'http.nonProxyHosts' into a Pattern safe string.
     */
    private static String sanitizeJavaHttpNonProxyHosts(String nonProxyHostsString) {
        /*
         * The 'http.nonProxyHosts' system property is expected to be delimited by '|'.
         */
        String[] nonProxyHosts = nonProxyHostsString.split("\\|");

        // Do an in-place replacement with the sanitized value.
        for (int i = 0; i < nonProxyHosts.length; i++) {
            /*
             * 'http.nonProxyHosts' values are allowed to begin and end with '*' but this is an invalid value for a
             * pattern, so we need to qualify the quantifier with the match all '.' character.
             */
            String prefixWildcard = "";
            String suffixWildcard = "";
            String body = nonProxyHosts[i];

            if (body.startsWith("*")) {
                prefixWildcard = ".*";
                body = body.substring(1);
            }

            if (body.endsWith("*")) {
                suffixWildcard = ".*";
                body = body.substring(0, body.length() - 1);
            }

            /*
             * Replace the non-proxy host with the sanitized value.
             *
             * The body of the non-proxy host is quoted to handle scenarios such a '127.0.0.1' or '*.azure.com' where
             * without quoting the '.' in the string would be treated as the match any character instead of the literal
             * '.' character.
             */
            nonProxyHosts[i] = prefixWildcard + Pattern.quote(body) + suffixWildcard;
        }

        return String.join("|", nonProxyHosts);
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
