// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Configuration;
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
        PROXY_PASSWORD);
    private static final String AZURE_HTTP_PROXY_WITH_USERNAME = String.format("%s://%s@%s", HTTP, PROXY_USER,
        PROXY_PASSWORD);

    private static final String AZURE_HTTPS_PROXY_WITH_USER_AND_PASS = String.format("%s://%s:%s@%s", HTTPS, PROXY_HOST,
        PROXY_USER, PROXY_PASSWORD);
    private static final String AZURE_HTTP_PROXY_WITH_USER_AND_PASS = String.format("%s://%s:%s@%s", HTTP, PROXY_HOST,
        PROXY_USER, PROXY_PASSWORD);

    private static final Configuration BASE_JAVA_HTTPS_CONFIGURATION = new Configuration()
        .put(JAVA_HTTPS_PROXY_HOST, PROXY_HOST)
        .put(JAVA_HTTPS_PROXY_PORT, "443");

    private static final Configuration BASE_JAVA_HTTP_CONFIGURATION = new Configuration()
        .put(JAVA_HTTP_PROXY_HOST, PROXY_HOST)
        .put(JAVA_HTTP_PROXY_PORT, "80");

    /**
     * Tests that loading a basic configuration from the environment works.
     */
    @ParameterizedTest
    @MethodSource("loadFromEnvironmentSupplier")
    public void loadFromEnvironment(Configuration configuration, String host, int port) {
        ProxyOptions proxyOptions = ProxyOptions.loadFromEnvironment(configuration);

        assertNotNull(proxyOptions);
        assertEquals(host, proxyOptions.getAddress().getHostName());
        assertEquals(port, proxyOptions.getAddress().getPort());
        assertNull(proxyOptions.getUsername());
        assertNull(proxyOptions.getPassword());
        assertNull(proxyOptions.getNonProxyHosts());
    }

    private static Stream<Arguments> loadFromEnvironmentSupplier() {
        return Stream.of(
            // Azure HTTPS
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY),
                PROXY_HOST, 443),

            // Azure HTTP
            Arguments.of(new Configuration().put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY),
                PROXY_HOST, 80),

            // Java HTTPS
            Arguments.of(BASE_JAVA_HTTPS_CONFIGURATION.clone().put(JAVA_PROXY_PREREQUISITE, "true"),
                PROXY_HOST, 443),

            // Java HTTP
            Arguments.of(BASE_JAVA_HTTP_CONFIGURATION.clone().put(JAVA_PROXY_PREREQUISITE, "true"),
                PROXY_HOST, 80)
        );
    }

    /**
     * Tests that passing {@link Configuration#NONE} into {@link ProxyOptions#loadFromEnvironment(Configuration)}
     * will throw an {@link IllegalArgumentException}.
     */
    @Test
    public void loadFromEnvironmentThrowsWhenPassedConfigurationNone() {
        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.loadFromEnvironment(Configuration.NONE));
    }

    /**
     * Tests that when Java system proxies will only be used if {@code java.net.useSystemProxies} is {@code true}.
     */
    @ParameterizedTest
    @MethodSource("javaProxiesRequireUseSystemProxiesSupplier")
    public void javaProxiesRequireUseSystemProxies(Configuration configuration) {
        assertNull(ProxyOptions.loadFromEnvironment(configuration));
    }

    private static Stream<Arguments> javaProxiesRequireUseSystemProxiesSupplier() {
        return Stream.of(
            // Java HTTPS configuration without 'java.net.useSystemProxies' set.
            Arguments.of(BASE_JAVA_HTTPS_CONFIGURATION),

            // Java HTTP configuration without 'java.net.useSystemProxies' set.
            Arguments.of(BASE_JAVA_HTTP_CONFIGURATION)
        );
    }
}
