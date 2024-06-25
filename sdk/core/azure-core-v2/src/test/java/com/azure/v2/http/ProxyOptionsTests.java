// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.http;

import io.clientcore.core.util.configuration.Configuration;
import com.azure.core.v2.util.ConfigurationBuilder;
import com.azure.core.v2.util.ConfigurationSource;
import com.azure.core.v2.util.CoreUtils;
import com.azure.core.v2.util.TestConfigurationSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.azure.core.http.ProxyOptions.fromConfiguration;
import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link ProxyOptions}.
 */
public class ProxyOptionsTests {
    private static final String HTTPS = "https";
    private static final String HTTP = "http";

    private static final String PROXY_HOST = "localhost";
    private static final String FAKE_PROXY_USER_PLACEHOLDER = "fakeProxyUserPlaceholder";
    private static final String FAKE_PROXY_PASSWORD_PLACEHOLDER = "fakeProxyPasswordPlaceholder";
    private static final String NON_PROXY_HOSTS = "notlocalhost";

    private static final String JAVA_SYSTEM_PROXY_PREREQUISITE = "java.net.useSystemProxies";
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

    private static final String AZURE_HTTPS_PROXY_WITH_USERNAME
        = String.format("%s://%s@%s", HTTPS, FAKE_PROXY_USER_PLACEHOLDER, PROXY_HOST);
    private static final String AZURE_HTTP_PROXY_WITH_USERNAME
        = String.format("%s://%s@%s", HTTP, FAKE_PROXY_USER_PLACEHOLDER, PROXY_HOST);

    private static final String AZURE_HTTPS_PROXY_WITH_USER_AND_PASS = String.format("%s://%s:%s@%s", HTTPS,
        FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER, PROXY_HOST);
    private static final String AZURE_HTTP_PROXY_WITH_USER_AND_PASS = String.format("%s://%s:%s@%s", HTTP,
        FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER, PROXY_HOST);
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    /**
     * Tests that loading a basic configuration from the environment works.
     */
    @ParameterizedTest
    @MethodSource("loadFromEnvironmentSupplier")
    public void loadFromEnvironment(Configuration configuration, String expectedHost, int expectedPort,
        String expectedUsername, String expectedPassword, String expectedNonProxyHosts) {
        ProxyOptions proxyOptions = fromConfiguration(configuration);

        assertNotNull(proxyOptions);
        assertFalse(proxyOptions.getAddress().isUnresolved());
        assertEquals(expectedHost, proxyOptions.getAddress().getHostName());
        assertEquals(expectedPort, proxyOptions.getAddress().getPort());
        assertEquals(expectedUsername, proxyOptions.getUsername());
        assertEquals(expectedPassword, proxyOptions.getPassword());
        assertEquals(expectedNonProxyHosts, proxyOptions.getNonProxyHosts());
    }

    /**
     * Tests that loading a basic configuration form the environment works and does not resolve the proxy when {@code
     * resolveProxy} is false.
     */
    @ParameterizedTest
    @MethodSource("loadFromEnvironmentSupplier")
    public void loadFromEnvironmentUnresolved(Configuration configuration, String expectedHost, int expectedPort,
        String expectedUsername, String expectedPassword, String expectedNonProxyHosts) {
        ProxyOptions proxyOptions = fromConfiguration(configuration, true);

        assertNotNull(proxyOptions);
        assertTrue(proxyOptions.getAddress().isUnresolved());
        assertEquals(expectedHost, proxyOptions.getAddress().getHostName());
        assertEquals(expectedPort, proxyOptions.getAddress().getPort());
        assertEquals(expectedUsername, proxyOptions.getUsername());
        assertEquals(expectedPassword, proxyOptions.getPassword());
        assertEquals(expectedNonProxyHosts, proxyOptions.getNonProxyHosts());
    }

    @ParameterizedTest
    @MethodSource("loadFromExplicitConfigurationSupplier")
    public void loadFromExplicitConfiguration(Configuration configuration, String expectedHost, int expectedPort,
        String expectedUsername, String expectedPassword, String expectedNonProxyHosts) {
        ProxyOptions proxyOptions = fromConfiguration(configuration);

        assertNotNull(proxyOptions);
        assertFalse(proxyOptions.getAddress().isUnresolved());
        assertEquals(expectedHost, proxyOptions.getAddress().getHostName());
        assertEquals(expectedPort, proxyOptions.getAddress().getPort());
        assertEquals(expectedUsername, proxyOptions.getUsername());
        assertEquals(expectedPassword, proxyOptions.getPassword());
        assertEquals(expectedNonProxyHosts, proxyOptions.getNonProxyHosts());
    }

    @ParameterizedTest
    @MethodSource("loadFromExplicitConfigurationSupplier")
    public void loadFromExplicitUnresolved(Configuration configuration, String expectedHost, int expectedPort,
        String expectedUsername, String expectedPassword, String expectedNonProxyHosts) {
        ProxyOptions proxyOptions = fromConfiguration(configuration, true);

        assertNotNull(proxyOptions);
        assertTrue(proxyOptions.getAddress().isUnresolved());
        assertEquals(expectedHost, proxyOptions.getAddress().getHostName());
        assertEquals(expectedPort, proxyOptions.getAddress().getPort());
        assertEquals(expectedUsername, proxyOptions.getUsername());
        assertEquals(expectedPassword, proxyOptions.getPassword());
        assertEquals(expectedNonProxyHosts, proxyOptions.getNonProxyHosts());
    }

    @Test
    public void mixedExplicitAndEnvironmentConfigurationIsNotSupported() {
        ConfigurationSource systemProps
            = new TestConfigurationSource().put("https.proxyHost", "ignored").put("https.proxyPort", "42");

        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, systemProps, EMPTY_SOURCE)
            .putProperty("foo.http.proxy.username", FAKE_PROXY_USER_PLACEHOLDER)
            .putProperty("http.proxy.password", FAKE_PROXY_PASSWORD_PLACEHOLDER)
            .putProperty("foo.http.proxy.hostname", PROXY_HOST)
            .buildSection("foo");

        ProxyOptions proxyOptions = fromConfiguration(configuration, true);

        assertNotNull(proxyOptions);
        assertTrue(proxyOptions.getAddress().isUnresolved());
        assertEquals(Proxy.Type.HTTP, proxyOptions.getType().toProxyType());
        assertEquals(PROXY_HOST, proxyOptions.getAddress().getHostName());
        assertEquals(443, proxyOptions.getAddress().getPort());
        assertEquals(FAKE_PROXY_USER_PLACEHOLDER, proxyOptions.getUsername());
        assertEquals(FAKE_PROXY_PASSWORD_PLACEHOLDER, proxyOptions.getPassword());
    }

    @Test
    public void envConfigurationInExplicit() {
        Configuration configuration = new ConfigurationBuilder().putProperty("https.proxyHost", PROXY_HOST)
            .putProperty("https.proxyPort", "8080")
            .putProperty("http.proxy.username", FAKE_PROXY_USER_PLACEHOLDER)
            .putProperty("http.proxy.password", FAKE_PROXY_PASSWORD_PLACEHOLDER)
            .buildSection("foo");

        ProxyOptions proxyOptions = fromConfiguration(configuration, true);

        assertNull(proxyOptions);
    }

    @ParameterizedTest
    @NullSource
    public void defaultHttpPortNull(String port) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder().putProperty("http.proxy.hostname", PROXY_HOST)
            .putProperty("http.proxy.username", FAKE_PROXY_USER_PLACEHOLDER)
            .putProperty("http.proxy.password", FAKE_PROXY_PASSWORD_PLACEHOLDER);

        if (port != null) {
            configBuilder.putProperty("http.proxy.port", port);
        }

        ProxyOptions proxyOptions = fromConfiguration(configBuilder.build());

        assertNotNull(proxyOptions);
        assertEquals(443, proxyOptions.getAddress().getPort());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "not-an-int" })
    public void invalidHttpPortExplicitConfigThrows(String port) {
        Configuration configuration = new ConfigurationBuilder().putProperty("http.proxy.hostname", PROXY_HOST)
            .putProperty("http.proxy.username", FAKE_PROXY_USER_PLACEHOLDER)
            .putProperty("http.proxy.password", FAKE_PROXY_PASSWORD_PLACEHOLDER)
            .putProperty("http.proxy.port", port)
            .build();

        assertThrows(NumberFormatException.class, () -> fromConfiguration(configuration));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "not-an-int" })
    public void invalidHttpsPortEnvironmentConfigDefault(String port) {
        ConfigurationSource systemProps
            = new TestConfigurationSource().put("https.proxyHost", PROXY_HOST).put("https.proxyPort", port);

        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, systemProps, EMPTY_SOURCE).build();

        ProxyOptions proxyOptions = fromConfiguration(configuration);

        assertNotNull(proxyOptions);
        assertEquals(443, proxyOptions.getAddress().getPort());
    }

    @ParameterizedTest
    @ValueSource(strings = { "   ", "not-an-int" })
    public void invalidHttpPortEnvironmentConfigDefault(String port) {
        ConfigurationSource systemProps
            = new TestConfigurationSource().put("http.proxyHost", PROXY_HOST).put("http.proxyPort", port);

        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, systemProps, EMPTY_SOURCE).build();

        ProxyOptions proxyOptions = fromConfiguration(configuration);

        assertNotNull(proxyOptions);
        assertEquals(80, proxyOptions.getAddress().getPort());
    }

    private static Stream<Arguments> loadFromEnvironmentSupplier() {
        return Stream.of(
            // Basic Azure HTTPS proxy.
            Arguments.of(
                setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationSource().put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY)),
                PROXY_HOST, 443, null, null, null),

            // Username only Azure HTTPS proxy.
            Arguments.of(
                setJavaSystemProxyPrerequisiteToTrue(new TestConfigurationSource()
                    .put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_WITH_USERNAME)),
                PROXY_HOST, 443, null, null, null),

            // Complete Azure HTTPS proxy.
            Arguments.of(
                setJavaSystemProxyPrerequisiteToTrue(new TestConfigurationSource()
                    .put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_WITH_USER_AND_PASS)),
                PROXY_HOST, 443, FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER, null),

            // Azure HTTPS proxy with non-proxying hosts.
            Arguments.of(
                setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationSource().put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY)
                        .put(Configuration.PROPERTY_NO_PROXY, NON_PROXY_HOSTS)),
                PROXY_HOST, 443, null, null, "(" + NON_PROXY_HOSTS + ")"),

            // Basic Azure HTTP proxy.
            Arguments.of(
                setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY)),
                PROXY_HOST, 80, null, null, null),

            // Username only Azure HTTP proxy.
            Arguments.of(
                setJavaSystemProxyPrerequisiteToTrue(new TestConfigurationSource()
                    .put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_WITH_USERNAME)),
                PROXY_HOST, 80, null, null, null),

            // Complete Azure HTTP proxy.
            Arguments.of(
                setJavaSystemProxyPrerequisiteToTrue(new TestConfigurationSource()
                    .put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_WITH_USER_AND_PASS)),
                PROXY_HOST, 80, FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER, null),

            // Azure HTTP proxy with non-proxying hosts.
            Arguments.of(
                setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY)
                        .put(Configuration.PROPERTY_NO_PROXY, NON_PROXY_HOSTS)),
                PROXY_HOST, 80, null, null, "(" + NON_PROXY_HOSTS + ")"),

            /*
             * Setting up tests for loading the Java environment proxy configurations takes additional work as each
             * piece of the proxy configuration is a separate environment value. The non-proxy hosts will be checked
             * against the global environment value when it is not being set by the configuration passed by the test
             * as this value may be setup by the JVM.
             */

            // Basic Java HTTPS proxy.
            Arguments.of(createJavaEnvConfiguration(443, null, null, null, true), PROXY_HOST, 443, null, null, null),

            // Username only Java HTTPS proxy.
            Arguments.of(createJavaEnvConfiguration(443, FAKE_PROXY_USER_PLACEHOLDER, null, null, true), PROXY_HOST,
                443, null, null, null),

            // Complete Java HTTPS proxy.
            Arguments.of(createJavaEnvConfiguration(443, FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER,
                null, true), PROXY_HOST, 443, FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER, null),

            // Java HTTPS proxy with non-proxying hosts.
            Arguments.of(createJavaEnvConfiguration(443, null, null, NON_PROXY_HOSTS, true), PROXY_HOST, 443, null,
                null, "(" + NON_PROXY_HOSTS + ")"),

            // Basic Java HTTP proxy.
            Arguments.of(createJavaEnvConfiguration(80, null, null, null, false), PROXY_HOST, 80, null, null, null),

            // Username only Java HTTP proxy.
            Arguments.of(createJavaEnvConfiguration(80, FAKE_PROXY_USER_PLACEHOLDER, null, null, false), PROXY_HOST, 80,
                null, null, null),

            // Complete Java HTTP proxy.
            Arguments.of(createJavaEnvConfiguration(80, FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER,
                null, false), PROXY_HOST, 80, FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER, null),

            // Java HTTP proxy with non-proxying hosts.
            Arguments.of(createJavaEnvConfiguration(80, null, null, NON_PROXY_HOSTS, false), PROXY_HOST, 80, null, null,
                "(" + NON_PROXY_HOSTS + ")"));
    }

    private static Stream<Arguments> loadFromExplicitConfigurationSupplier() {
        return Stream.of(
            /*
             * Setting up tests for loading the Java exokicit proxy configurations takes additional work as each
             * piece of the proxy configuration is a separate environment value. The non-proxy hosts will be checked
             * against the global environment value when it is not being set by the configuration passed by the test
             * as this value may be setup by the JVM.
             */

            // Basic Java HTTPS proxy.
            Arguments.of(createExplicitConfiguration(443, null, null, null), PROXY_HOST, 443, null, null, null),

            // Username only Java HTTPS proxy.
            Arguments.of(createExplicitConfiguration(443, FAKE_PROXY_USER_PLACEHOLDER, null, null), PROXY_HOST, 443,
                null, null, null),

            // Complete Java HTTPS proxy.
            Arguments.of(
                createExplicitConfiguration(443, FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER, null),
                PROXY_HOST, 443, FAKE_PROXY_USER_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER, null),

            // Java HTTPS proxy with non-proxying hosts.
            Arguments.of(createExplicitConfiguration(443, null, null, NON_PROXY_HOSTS), PROXY_HOST, 443, null, null,
                "(" + NON_PROXY_HOSTS + ")"));
    }

    /**
     * Tests that passing {@link Configuration#NONE} into {@link ProxyOptions#fromConfiguration(Configuration)} will
     * return null/
     */
    @Test
    public void loadFromEnvironmentThrowsWhenPassedConfigurationNone() {
        assertNull(fromConfiguration(Configuration.NONE));
        assertNull(fromConfiguration(Configuration.NONE, true));
    }

    /**
     * Tests that when Java system proxies will only be used if {@code java.net.useSystemProxies} is {@code true}.
     */
    @ParameterizedTest
    @MethodSource("systemProxiesRequireUseSystemProxiesSupplier")
    public void systemProxiesRequireUseSystemProxies(Configuration configuration) {
        assertNull(fromConfiguration(configuration));
        assertNull(fromConfiguration(configuration, true));
    }

    private static Stream<Arguments> systemProxiesRequireUseSystemProxiesSupplier() {
        ConfigurationSource envVarHttpsSource
            = new TestConfigurationSource().put(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY)
                .put(JAVA_NON_PROXY_HOSTS, null);
        ConfigurationSource envVarHttpSource
            = new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY)
                .put(JAVA_NON_PROXY_HOSTS, null);

        return Stream.of(
            // Java HTTPS configuration without 'java.net.useSystemProxies' set.
            Arguments.of(new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, envVarHttpsSource).build()),

            // Java HTTP configuration without 'java.net.useSystemProxies' set.
            Arguments.of(new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, envVarHttpSource).build()));
    }

    private static Configuration createJavaEnvConfiguration(int port, String username, String password,
        String nonProxyHosts, boolean isHttps) {
        TestConfigurationSource testSource = new TestConfigurationSource().put(JAVA_NON_PROXY_HOSTS, nonProxyHosts);

        if (isHttps) {
            testSource.put(JAVA_HTTPS_PROXY_HOST, PROXY_HOST)
                .put(JAVA_HTTPS_PROXY_PORT, String.valueOf(port))
                .put(JAVA_HTTPS_PROXY_USER, username)
                .put(JAVA_HTTPS_PROXY_PASSWORD, password);
        } else {
            testSource.put(JAVA_HTTP_PROXY_HOST, PROXY_HOST)
                .put(JAVA_HTTP_PROXY_PORT, String.valueOf(port))
                .put(JAVA_HTTP_PROXY_USER, username)
                .put(JAVA_HTTP_PROXY_PASSWORD, password);
        }

        return new ConfigurationBuilder(EMPTY_SOURCE, testSource, EMPTY_SOURCE).build();
    }

    private static Configuration createExplicitConfiguration(int port, String username, String password,
        String nonProxyHosts) {
        TestConfigurationSource explicitSource = new TestConfigurationSource()
            .put("http.proxy.non-proxy-hosts", CoreUtils.isNullOrEmpty(nonProxyHosts) ? "" : nonProxyHosts)
            .put("http.proxy.hostname", PROXY_HOST)
            .put("http.proxy.port", String.valueOf(port))
            .put("http.proxy.username", username)
            .put("http.proxy.password", password);

        return new ConfigurationBuilder(explicitSource, EMPTY_SOURCE, EMPTY_SOURCE).build();
    }

    @ParameterizedTest
    @MethodSource("nonProxyHostsSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public void nonProxyHosts(Pattern pattern, String host, boolean expected) {
        assertEquals(expected, pattern.matcher(host).find(),
            () -> String.format("Expected Pattern '%s' to match '%s'.", pattern.pattern(), host));
    }

    private static Stream<Arguments> nonProxyHostsSupplier() {
        String javaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.prod.linkedin.com",
            "*.azure.linkedin.com", "*.blob.core.windows.net");
        String noProxyNonProxyHosts = String.join(",", "localhost", "127.0.0.1", ".*.prod.linkedin.com",
            ".azure.linkedin.com", "*.blob.core.windows.net");

        /*
         * This emulates loading a Java formatted proxy.
         */
        TestConfigurationSource sysPropSource = new TestConfigurationSource().put("http.proxyHost", "localhost")
            .put("http.proxyPort", "7777")
            .put("http.nonProxyHosts", javaNonProxyHosts);
        Configuration javaProxyConfiguration
            = new ConfigurationBuilder(EMPTY_SOURCE, sysPropSource, EMPTY_SOURCE).build();

        Pattern javaProxyConfigurationPattern
            = compile(fromConfiguration(javaProxyConfiguration).getNonProxyHosts(), Pattern.CASE_INSENSITIVE);

        /*
         * This emulates loading an environment formatted proxy.
         */
        TestConfigurationSource envSource
            = new TestConfigurationSource().put(Configuration.PROPERTY_HTTPS_PROXY, "http://localhost:7777")
                .put(Configuration.PROPERTY_NO_PROXY, noProxyNonProxyHosts);
        Configuration environmentProxyConfiguration = setJavaSystemProxyPrerequisiteToTrue(envSource);

        Pattern environmentProxyConfigurationPattern
            = compile(fromConfiguration(environmentProxyConfiguration).getNonProxyHosts(), Pattern.CASE_INSENSITIVE);

        /*
         * This emulates configuring a proxy in code using the expected Java formatted proxy.
         */
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 7777))
            .setNonProxyHosts(javaNonProxyHosts);

        Pattern codeProxyPattern = compile(proxyOptions.getNonProxyHosts(), Pattern.CASE_INSENSITIVE);

        Pattern[] patterns
            = new Pattern[] { javaProxyConfigurationPattern, environmentProxyConfigurationPattern, codeProxyPattern };

        String[] expectedMatchHosts = new String[] {
            "localhost",
            "127.0.0.1",
            "azp.prod.linkedin.com",
            "azp.azure.linkedin.com",
            "azp.blob.core.windows.net" };

        String[] expectedNonMatchHosts = new String[] {
            "example.com",
            "portal.azure.com",
            "linkedin.com",
            "127a0b0c1",
            "azpaprodblinkedinccom",
            "azpaazureblinkedinccom",
            "azpablobbcorecwindowsdnet" };

        List<Arguments> argumentsList = new ArrayList<>();
        for (Pattern pattern : patterns) {
            for (String expectedMatchHost : expectedMatchHosts) {
                argumentsList.add(Arguments.arguments(pattern, expectedMatchHost, true));
            }

            for (String expectedNonMatchHost : expectedNonMatchHosts) {
                argumentsList.add(Arguments.arguments(pattern, expectedNonMatchHost, false));
            }
        }

        return argumentsList.stream();
    }

    @Test
    public void sanitizeNoProxyDoesNotSplitEscapedCommas() {
        String noProxy = "noproxy\\,withescapedcomma";

        assertEquals("(" + noProxy + ")", ProxyOptions.sanitizeNoProxy(noProxy));
    }

    @Test
    public void sanitizeJavaHttpNonProxyHostsDoesNotSplitEscapedPipes() {
        String nonProxyHosts = "nonproxyhosts\\|withescapedpipe";

        assertEquals("(" + nonProxyHosts + ")", ProxyOptions.sanitizeNoProxy(nonProxyHosts));
    }

    private static Configuration setJavaSystemProxyPrerequisiteToTrue(TestConfigurationSource source) {
        return new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, source.put(JAVA_SYSTEM_PROXY_PREREQUISITE, "true"))
            .build();
    }
}
