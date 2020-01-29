// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This class tests {@link ProxyOptions}.
 */
public class ProxyOptionsTests {
    private static final String HTTPS = "https";
    private static final String HTTP = "http";

    private static final String PROXY_HOST = "localhost";
    private static final String PROXY_USER = "user";
    private static final String PROXY_PASSWORD = "pass";
    private static final String NON_PROXY_HOSTS = "notlocalhost";

    private static final String JAVA_PROXY_PREREQUISITE = "java.net.useSystemProxies";
    private static final String JAVA_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String JAVA_HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String JAVA_HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String JAVA_HTTPS_PROXY_USER = "https.proxyUser";
    private static final String JAVA_HTTPS_PROXY_PASSWORD = "https.proxyPassword";

    private static final String JAVA_HTTP_PROXY_HOST = "http.proxyHost";
    private static final String JAVA_HTTP_PROXY_PORT = "http.proxyPort";
    private static final String JAVA_HTTP_PROXY_USER = "http.proxyUser";
    private static final String JAVA_HTTP_PROXY_PASSWORD = "http.proxyPassword";

    private static final String AZURE_HTTPS_PROXY_HOST_ONLY = String.format("%s://%s", HTTPS, PROXY_HOST);
    private static final String AZURE_HTTP_PROXY_HOST_ONLY = String.format("%s://%s", HTTP, PROXY_HOST);

    private static final String AZURE_HTTPS_PROXY_WITH_USERNAME = String.format("%s://%s@%s", HTTPS, PROXY_USER,
        PROXY_HOST);
    private static final String AZURE_HTTP_PROXY_WITH_USERNAME = String.format("%s://%s@%s", HTTP, PROXY_USER,
        PROXY_HOST);

    private static final String AZURE_HTTPS_PROXY_WITH_USER_AND_PASS = String.format("%s://%s:%s@%s", HTTPS, PROXY_USER,
        PROXY_PASSWORD, PROXY_HOST);
    private static final String AZURE_HTTP_PROXY_WITH_USER_AND_PASS = String.format("%s://%s:%s@%s", HTTP, PROXY_USER,
        PROXY_PASSWORD, PROXY_HOST);

    /**
     * Tests that loading a basic configuration from the environment works.
     */
    @ParameterizedTest
    @MethodSource("loadFromEnvironmentSupplier")
    public void loadFromEnvironment(Configuration configuration, String expectedHost, int expectedPort,
        String expectedUsername, String expectedPassword, String expectedNonProxyHosts) {
        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);

        assertNotNull(proxyOptions);
        assertEquals(expectedHost, proxyOptions.getAddress().getHostName());
        assertEquals(expectedPort, proxyOptions.getAddress().getPort());
        assertEquals(expectedUsername, proxyOptions.getUsername());
        assertEquals(expectedPassword, proxyOptions.getPassword());
        assertEquals(expectedNonProxyHosts, proxyOptions.getNonProxyHosts());
    }

    private static Stream<Arguments> loadFromEnvironmentSupplier() {
        return Stream.of(
            // Basic Azure HTTPS proxy.
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY),
                PROXY_HOST, 443, null, null, null),

            // Username only Azure HTTPS proxy.
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_WITH_USERNAME),
                PROXY_HOST, 443, null, null, null),

            // Complete Azure HTTPS proxy.
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTPS_PROXY,
                AZURE_HTTPS_PROXY_WITH_USER_AND_PASS), PROXY_HOST, 443, PROXY_USER, PROXY_PASSWORD, null),

            // Azure HTTPS proxy with non-proxying hosts.
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY)
                .put(Configuration.PROPERTY_NO_PROXY, NON_PROXY_HOSTS), PROXY_HOST, 443, null, null, NON_PROXY_HOSTS),

            // Basic Azure HTTP proxy.
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY),
                PROXY_HOST, 80, null, null, null),

            // Username only Azure HTTP proxy.
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_WITH_USERNAME),
                PROXY_HOST, 80, null, null, null),

            // Complete Azure HTTP proxy.
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTP_PROXY,
                AZURE_HTTP_PROXY_WITH_USER_AND_PASS), PROXY_HOST, 80, PROXY_USER, PROXY_PASSWORD, null),

            // Azure HTTP proxy with non-proxying hosts.
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY)
                .put(Configuration.PROPERTY_NO_PROXY, NON_PROXY_HOSTS), PROXY_HOST, 80, null, null, NON_PROXY_HOSTS),

            /*
             * Setting up tests for loading the Java environment proxy configurations takes additional work as each
             * piece of the proxy configuration is a separate environment value. The non-proxy hosts will be checked
             * against the global environment value when it is not being set by the configuration passed by the test
             * as this value may be setup by the JVM.
             */

            // Basic Java HTTPS proxy.
            Arguments.of(createJavaConfiguration(443, null, null, null, true, true),
                PROXY_HOST, 443, null, null, getJavaNonProxyHosts()),

            // Username only Java HTTPS proxy.
            Arguments.of(createJavaConfiguration(443, PROXY_USER, null, null, true, true),
                PROXY_HOST, 443, null, null, getJavaNonProxyHosts()),

            // Complete Java HTTPS proxy.
            Arguments.of(createJavaConfiguration(443, PROXY_USER, PROXY_PASSWORD, null, true, true),
                PROXY_HOST, 443, PROXY_USER, PROXY_PASSWORD, getJavaNonProxyHosts()),

            // Java HTTPS proxy with non-proxying hosts.
            Arguments.of(createJavaConfiguration(443, null, null, NON_PROXY_HOSTS, true, true),
                PROXY_HOST, 443, null, null, NON_PROXY_HOSTS),

            // Basic Java HTTP proxy.
            Arguments.of(createJavaConfiguration(80, null, null, null, false, true),
                PROXY_HOST, 80, null, null, getJavaNonProxyHosts()),

            // Username only Java HTTP proxy.
            Arguments.of(createJavaConfiguration(80, PROXY_USER, null, null, false, true),
                PROXY_HOST, 80, null, null, getJavaNonProxyHosts()),

            // Complete Java HTTP proxy.
            Arguments.of(createJavaConfiguration(80, PROXY_USER, PROXY_PASSWORD, null, false, true),
                PROXY_HOST, 80, PROXY_USER, PROXY_PASSWORD, getJavaNonProxyHosts()),

            // Java HTTP proxy with non-proxying hosts.
            Arguments.of(createJavaConfiguration(80, null, null, NON_PROXY_HOSTS, false, true),
                PROXY_HOST, 80, null, null, NON_PROXY_HOSTS)
        );
    }

    /**
     * Tests that passing {@link Configuration#NONE} into {@link ProxyOptions#fromConfiguration(Configuration)}
     * will throw an {@link IllegalArgumentException}.
     */
    @Test
    public void loadFromEnvironmentThrowsWhenPassedConfigurationNone() {
        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.fromConfiguration(Configuration.NONE));
    }

    /**
     * Tests that when Java system proxies will only be used if {@code java.net.useSystemProxies} is {@code true}.
     */
    @ParameterizedTest
    @MethodSource("javaProxiesRequireUseSystemProxiesSupplier")
    public void javaProxiesRequireUseSystemProxies(Configuration configuration) {
        assertNull(ProxyOptions.fromConfiguration(configuration));
    }

    private static Stream<Arguments> javaProxiesRequireUseSystemProxiesSupplier() {
        return Stream.of(
            // Java HTTPS configuration without 'java.net.useSystemProxies' set.
            Arguments.of(createJavaConfiguration(443, null, null, null, true, false)),

            // Java HTTP configuration without 'java.net.useSystemProxies' set.
            Arguments.of(createJavaConfiguration(80, null, null, null, false, false))
        );
    }

    private static Configuration createJavaConfiguration(int port, String username, String password,
        String nonProxyHosts, boolean isHttps, boolean enabled) {
        Configuration configuration = new Configuration().put(JAVA_PROXY_PREREQUISITE, String.valueOf(enabled));
        putIfNotNull(configuration, JAVA_NON_PROXY_HOSTS, nonProxyHosts);

        if (isHttps) {
            configuration.put(JAVA_HTTPS_PROXY_HOST, PROXY_HOST).put(JAVA_HTTPS_PROXY_PORT, String.valueOf(port));
            configuration = putIfNotNull(configuration, JAVA_HTTPS_PROXY_USER, username);
            configuration = putIfNotNull(configuration, JAVA_HTTPS_PROXY_PASSWORD, password);
        } else {
            configuration.put(JAVA_HTTP_PROXY_HOST, PROXY_HOST).put(JAVA_HTTP_PROXY_PORT, String.valueOf(port));
            configuration = putIfNotNull(configuration, JAVA_HTTP_PROXY_USER, username);
            configuration = putIfNotNull(configuration, JAVA_HTTP_PROXY_PASSWORD, password);
        }

        return configuration;
    }

    private static Configuration putIfNotNull(Configuration configuration, String name, String value) {
        /*
         * If the passed value is null attempt to use the global configuration value. This is done as the Configuration
         * object will attempt to load the environment if it has no value for a given name.
         */
        if (value == null) {
            value = Configuration.getGlobalConfiguration().get(name);
        }

        return CoreUtils.isNullOrEmpty(value)
            ? configuration
            : configuration.put(name, value);
    }

    private static String getJavaNonProxyHosts() {
        return Configuration.getGlobalConfiguration().get(JAVA_NON_PROXY_HOSTS);
    }
}
