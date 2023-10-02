// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http;

import com.typespec.core.implementation.ImplUtils;
import com.typespec.core.util.Configuration;
import com.typespec.core.util.ConfigurationProperty;
import com.typespec.core.util.ConfigurationPropertyBuilder;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This represents proxy configuration to be used in http clients..
 */
public class ProxyOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ProxyOptions.class);
    private static final String INVALID_AZURE_PROXY_URL = "URL is invalid and is being ignored.";

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

    /*
     * The 'http.nonProxyHosts' system property is expected to be delimited by '|', but don't split escaped '|'s.
     */
    private static final Pattern HTTP_NON_PROXY_HOSTS_SPLIT = Pattern.compile("(?<!\\\\)\\|");

    /*
     * The 'NO_PROXY' environment variable is expected to be delimited by ',', but don't split escaped ','s.
     */
    private static final Pattern NO_PROXY_SPLIT = Pattern.compile("(?<!\\\\),");

    private static final Pattern UNESCAPED_PERIOD = Pattern.compile("(?<!\\\\)\\.");
    private static final Pattern ANY = Pattern.compile("\\*");

    private static final ConfigurationProperty<String> NON_PROXY_PROPERTY = ConfigurationPropertyBuilder.ofString(ConfigurationProperties.HTTP_PROXY_NON_PROXY_HOSTS)
        .shared(true)
        .logValue(true)
        .build();
    private static final ConfigurationProperty<String> HOST_PROPERTY = ConfigurationPropertyBuilder.ofString(ConfigurationProperties.HTTP_PROXY_HOST)
        .shared(true)
        .logValue(true)
        .build();
    private static final ConfigurationProperty<Integer> PORT_PROPERTY = ConfigurationPropertyBuilder.ofInteger(ConfigurationProperties.HTTP_PROXY_PORT)
        .shared(true)
        .defaultValue(DEFAULT_HTTPS_PORT)
        .build();
    private static final ConfigurationProperty<String> USER_PROPERTY = ConfigurationPropertyBuilder.ofString(ConfigurationProperties.HTTP_PROXY_USER)
        .shared(true)
        .logValue(true)
        .build();
    private static final ConfigurationProperty<String> PASSWORD_PROPERTY = ConfigurationPropertyBuilder.ofString(ConfigurationProperties.HTTP_PROXY_PASSWORD)
        .shared(true)
        .build();

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
     * Gets the address of the proxy.
     *
     * @return the address of the proxy.
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    /**
     * Gets the type of the prxoy.
     *
     * @return the type of the proxy.
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the proxy username.
     *
     * @return the proxy username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets the proxy password.
     *
     * @return the proxy password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Gets the host that bypass the proxy.
     *
     * @return the hosts that bypass the proxy.
     */
    public String getNonProxyHosts() {
        return this.nonProxyHosts;
    }

    /**
     * Attempts to load a proxy from the configuration.
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
     * {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @return A {@link ProxyOptions} reflecting a proxy loaded from the environment, if no proxy is found {@code null}
     * will be returned.
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
     */
    public static ProxyOptions fromConfiguration(Configuration configuration, boolean createUnresolved) {
        Configuration proxyConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        return attemptToLoadProxy(proxyConfiguration, createUnresolved);
    }

    private static ProxyOptions attemptToLoadProxy(Configuration configuration, boolean createUnresolved) {
        if (configuration == Configuration.NONE) {
            return null;
        }

        ProxyOptions proxyOptions = null;
        // System proxy configuration is only possible through system properties.
        // Only use system proxies when the prerequisite property is 'true'.
        if (Boolean.parseBoolean(configuration.get(JAVA_SYSTEM_PROXY_PREREQUISITE))) {
            proxyOptions = attemptToLoadSystemProxy(configuration, createUnresolved, Configuration.PROPERTY_HTTPS_PROXY);
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

        proxyOptions = attemptToLoadAzureSdkProxy(configuration, createUnresolved);
        if (proxyOptions != null) {
            return proxyOptions;
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
            // TODO (alzimmer): UrlBuilder needs to add support for userinfo
            //  https://www.rfc-editor.org/rfc/rfc3986#section-3.2.1
            URL proxyUrl = ImplUtils.createUrl(proxyConfiguration);
            int port = (proxyUrl.getPort() == -1) ? proxyUrl.getDefaultPort() : proxyUrl.getPort();

            InetSocketAddress socketAddress = (createUnresolved)
                ? InetSocketAddress.createUnresolved(proxyUrl.getHost(), port)
                : new InetSocketAddress(proxyUrl.getHost(), port);

            ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, socketAddress);

            String nonProxyHostsString = configuration.get(Configuration.PROPERTY_NO_PROXY);
            if (!CoreUtils.isNullOrEmpty(nonProxyHostsString)) {
                proxyOptions.nonProxyHosts = sanitizeNoProxy(nonProxyHostsString);

                LOGGER.atVerbose()
                    .addKeyValue("regex", proxyOptions.nonProxyHosts)
                    .log("Using non-proxy hosts");
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
            LOGGER.atWarning()
                .addKeyValue("url", proxyProperty)
                .log(INVALID_AZURE_PROXY_URL);
            return null;
        }
    }

    /*
     * Helper function that sanitizes 'NO_PROXY' into a Pattern safe string.
     */
    static String sanitizeNoProxy(String noProxyString) {
        return sanitizeNonProxyHosts(NO_PROXY_SPLIT.split(noProxyString));
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

        String nonProxyHostsString = configuration.get(JAVA_NON_PROXY_HOSTS);
        String username = configuration.get(type + "." + JAVA_PROXY_USER);
        String password = configuration.get(type + "." + JAVA_PROXY_PASSWORD);

        return createOptions(host, port, nonProxyHostsString, username, password, createUnresolved);
    }

    private static ProxyOptions attemptToLoadAzureSdkProxy(Configuration configuration, boolean createUnresolved) {
        String host = configuration.get(HOST_PROPERTY);

        // No proxy configuration setup.
        if (CoreUtils.isNullOrEmpty(host)) {
            return null;
        }

        int port = configuration.get(PORT_PROPERTY);
        String nonProxyHostsString = configuration.get(NON_PROXY_PROPERTY);
        String username = configuration.get(USER_PROPERTY);
        String password = configuration.get(PASSWORD_PROPERTY);

        return createOptions(host, port, nonProxyHostsString, username, password, createUnresolved);
    }

    private static ProxyOptions createOptions(String host, int port, String nonProxyHostsString, String username, String password, boolean createUnresolved) {
        InetSocketAddress socketAddress = (createUnresolved)
            ? InetSocketAddress.createUnresolved(host, port)
            : new InetSocketAddress(host, port);

        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, socketAddress);

        if (!CoreUtils.isNullOrEmpty(nonProxyHostsString)) {
            proxyOptions.nonProxyHosts = sanitizeJavaHttpNonProxyHosts(nonProxyHostsString);

            LOGGER.atVerbose()
                .addKeyValue("regex", proxyOptions.nonProxyHosts)
                .log("Using non-proxy host regex");
        }

        if (username != null && password != null) {
            proxyOptions.setCredentials(username, password);
        }

        return proxyOptions;
    }
    /*
     * Helper function that sanitizes 'http.nonProxyHosts' into a Pattern safe string.
     */
    static String sanitizeJavaHttpNonProxyHosts(String nonProxyHostsString) {
        return sanitizeNonProxyHosts(HTTP_NON_PROXY_HOSTS_SPLIT.split(nonProxyHostsString));
    }

    private static String sanitizeNonProxyHosts(String[] nonProxyHosts) {
        StringBuilder sanitizedBuilder = new StringBuilder();

        for (int i = 0; i < nonProxyHosts.length; i++) {
            if (i > 0) {
                sanitizedBuilder.append("|");
            }

            String prefixWildcard = "";
            String suffixWildcard = "";
            String sanitizedNonProxyHost = nonProxyHosts[i];

            /*
             * If the non-proxy host begins with either '.', '*', '.*', or any of the previous with a trailing '?'
             * substring the non-proxy host and set the wildcard prefix.
             */
            if (sanitizedNonProxyHost.startsWith(".")) {
                prefixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(1);
            } else if (sanitizedNonProxyHost.startsWith(".?")) {
                prefixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(2);
            } else if (sanitizedNonProxyHost.startsWith("*?")) {
                prefixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(2);
            } else if (sanitizedNonProxyHost.startsWith("*")) {
                prefixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(1);
            } else if (sanitizedNonProxyHost.startsWith(".*?")) {
                prefixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(3);
            } else if (sanitizedNonProxyHost.startsWith(".*")) {
                prefixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(2);
            }

            /*
             * Same with the ending of the non-proxy host, if it has a suffix wildcard trim the non-proxy host and
             * retain the suffix wildcard.
             */
            if (sanitizedNonProxyHost.endsWith(".")) {
                suffixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(0, sanitizedNonProxyHost.length() - 2);
            } else if (sanitizedNonProxyHost.endsWith(".?")) {
                suffixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(0, sanitizedNonProxyHost.length() - 3);
            } else if (sanitizedNonProxyHost.endsWith("*?")) {
                suffixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(0, sanitizedNonProxyHost.length() - 3);
            } else if (sanitizedNonProxyHost.endsWith("*")) {
                suffixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(0, sanitizedNonProxyHost.length() - 2);
            } else if (sanitizedNonProxyHost.endsWith(".*?")) {
                suffixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(0, sanitizedNonProxyHost.length() - 4);
            } else if (sanitizedNonProxyHost.endsWith(".*")) {
                suffixWildcard = ".*?";
                sanitizedNonProxyHost = sanitizedNonProxyHost.substring(0, sanitizedNonProxyHost.length() - 3);
            }

            try {
                // Sanitize the non-proxy host before any appending to prevent errant characters being added to the
                // final response if the non-proxy host isn't a valid Pattern.
                String attemptToSanitizeAsRegex = sanitizedNonProxyHost;
                attemptToSanitizeAsRegex = UNESCAPED_PERIOD.matcher(attemptToSanitizeAsRegex).replaceAll("\\\\.");
                attemptToSanitizeAsRegex = ANY.matcher(attemptToSanitizeAsRegex).replaceAll("\\.*?");
                sanitizedNonProxyHost = Pattern.compile(attemptToSanitizeAsRegex).pattern();
            } catch (PatternSyntaxException ex) {
                /*
                 * Replace the non-proxy host with the sanitized value.
                 *
                 * The body of the non-proxy host is quoted to handle scenarios such a '127.0.0.1' or '*.azure.com'
                 * where without quoting the '.' in the string would be treated as the match any character instead of
                 * the literal '.' character.
                 */
                sanitizedNonProxyHost = Pattern.quote(sanitizedNonProxyHost);
            }

            sanitizedBuilder.append("(")
                .append(prefixWildcard)
                .append(sanitizedNonProxyHost)
                .append(suffixWildcard)
                .append(")");
        }

        return sanitizedBuilder.toString();
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

    /**
     * Lists available configuration property names for HTTP {@link ProxyOptions}.
     */
    private static class ConfigurationProperties {
        /**
         * Represents a list of hosts that should be reached directly, bypassing the proxy.
         * This is a list of patterns separated by '|'. The patterns may start or end with a '*' for wildcards.
         * Any host matching one of these patterns will be reached through a direct connection instead of through a proxy.
         * <p>
         * Default value is {@code null}
         */
        public static final String HTTP_PROXY_NON_PROXY_HOSTS = "http.proxy.non-proxy-hosts";

        /**
         * The HTTP host name of the proxy server.
         * <p>
         * Default value is {@code null}.
         */
        public static final String HTTP_PROXY_HOST = "http.proxy.hostname";

        /**
         * The port number of the proxy server.
         * <p>
         * Default value is {@code 443}.
         */
        public static final String HTTP_PROXY_PORT = "http.proxy.port";

        /**
         * The HTTP proxy server user.
         * Default value is {@code null}.
         */
        public static final String HTTP_PROXY_USER = "http.proxy.username";

        /**
         * The HTTP proxy server password.
         * Default value is {@code null}.
         */
        public static final String HTTP_PROXY_PASSWORD = "http.proxy.password";
    }
}
