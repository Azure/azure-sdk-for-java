// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.TestConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetSocketAddress;
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
    private static final String PROXY_USER = "user";
    private static final String PROXY_PASSWORD = "pass";
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
    public void mixedExplicitAndEnvironmentConfiguration() {
        Configuration configuration = new TestConfigurationBuilder()
            .addEnv("https.proxyHost", PROXY_HOST)
            .addEnv("https.proxyPort", "8080")
            .add("foo.https.proxy.username", PROXY_USER)
            .add("https.proxy.password", PROXY_PASSWORD)
            .add("foo.http.proxy.host", "ignored")
            .buildSection("foo");

        ProxyOptions proxyOptions = fromConfiguration(configuration, true);

        assertNotNull(proxyOptions);
        assertTrue(proxyOptions.getAddress().isUnresolved());
        assertEquals(PROXY_HOST, proxyOptions.getAddress().getHostName());
        assertEquals(8080, proxyOptions.getAddress().getPort());
        assertEquals(PROXY_USER, proxyOptions.getUsername());
        assertEquals(PROXY_PASSWORD, proxyOptions.getPassword());
    }

    @Test
    public void envConfigurationInExplicit() {
        Configuration configuration = new TestConfigurationBuilder()
            .add("https.proxyHost", PROXY_HOST)
            .add("https.proxyPort", "8080")
            .add("https.proxy.username", PROXY_USER)
            .add("https.proxy.password", PROXY_PASSWORD)
            .buildSection("foo");

        ProxyOptions proxyOptions = fromConfiguration(configuration, true);

        assertNull(proxyOptions);
    }

    @Test
    public void createUnresolvedExplicitOverridesConfig() {
        Configuration configuration = new TestConfigurationBuilder()
            .add("http.proxy.host", PROXY_HOST)
            .add("http.proxy.port", "80")
            .add("foo.http.proxy.username", PROXY_USER)
            .add("foo.http.proxy.password", PROXY_PASSWORD)
            .add("foo.http.proxy.create-unresolved", "false")
            .buildSection("foo");

        ProxyOptions proxyOptions = fromConfiguration(configuration, true);

        assertNotNull(proxyOptions);
        assertTrue(proxyOptions.getAddress().isUnresolved());
    }

    @Test
    public void createUnresolvedExplicit() {
        Configuration configuration = new TestConfigurationBuilder()
            .add("foo.http.proxy.host", PROXY_HOST)
            .add("foo.http.proxy.port", "80")
            .add("http.proxy.username", PROXY_USER)
            .add("http.proxy.password", PROXY_PASSWORD)
            .add("http.proxy.create-unresolved", "true")
            .buildSection("foo");

        ProxyOptions proxyOptions = fromConfiguration(configuration);

        assertNotNull(proxyOptions);
        assertTrue(proxyOptions.getAddress().isUnresolved());
    }

    @ParameterizedTest
    @MethodSource("notInts")
    public void defaultHttpPort(String port) {
        TestConfigurationBuilder configBuilder = new TestConfigurationBuilder()
            .add("http.proxy.host", PROXY_HOST)
            .add("http.proxy.username", PROXY_USER)
            .add("http.proxy.password", PROXY_PASSWORD);

        if (port != null) {
            configBuilder.add("http.proxy.port", port);
        }

        ProxyOptions proxyOptions = fromConfiguration(configBuilder.build());

        assertNotNull(proxyOptions);
        assertEquals(80, proxyOptions.getAddress().getPort());
    }

    @ParameterizedTest
    @MethodSource("notInts")
    public void defaultHttpsPort(String port) {
        TestConfigurationBuilder configBuilder = new TestConfigurationBuilder()
            .add("https.proxy.host", PROXY_HOST)
            .add("https.proxy.username", PROXY_USER)
            .add("https.proxy.password", PROXY_PASSWORD);

        if (port != null) {
            configBuilder.add("https.proxy.port", port);
        }

        ProxyOptions proxyOptions = fromConfiguration(configBuilder.build());

        assertNotNull(proxyOptions);
        assertEquals(443, proxyOptions.getAddress().getPort());
    }

    static Stream<String> notInts() {
        return Stream.of("", "   ", "not-an-int", null);
    }

    private static Stream<Arguments> loadFromEnvironmentSupplier() {
        return Stream.of(
            // Basic Azure HTTPS proxy.
            Arguments.of(setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationBuilder().addEnv(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY)),
                PROXY_HOST, 443, null, null, null),

            // Username only Azure HTTPS proxy.
            Arguments.of(setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationBuilder().addEnv(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_WITH_USERNAME)),
                PROXY_HOST, 443, null, null, null),

            // Complete Azure HTTPS proxy.
            Arguments.of(setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationBuilder().addEnv(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_WITH_USER_AND_PASS)),
                PROXY_HOST, 443, PROXY_USER, PROXY_PASSWORD, null),

            // Azure HTTPS proxy with non-proxying hosts.
            Arguments.of(setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationBuilder()
                        .addEnv(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY)
                        .addEnv(Configuration.PROPERTY_NO_PROXY, NON_PROXY_HOSTS)),
                PROXY_HOST, 443, null, null, "(" + NON_PROXY_HOSTS + ")"),

            // Basic Azure HTTP proxy.
            Arguments.of(setJavaSystemProxyPrerequisiteToTrue(
                        new TestConfigurationBuilder().addEnv(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY)),
                PROXY_HOST, 80, null, null, null),

            // Username only Azure HTTP proxy.
            Arguments.of(setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationBuilder().addEnv(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_WITH_USERNAME)),
                PROXY_HOST, 80, null, null, null),

            // Complete Azure HTTP proxy.
            Arguments.of(setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationBuilder().addEnv(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_WITH_USER_AND_PASS)),
                PROXY_HOST, 80, PROXY_USER, PROXY_PASSWORD, null),

            // Azure HTTP proxy with non-proxying hosts.
            Arguments.of(setJavaSystemProxyPrerequisiteToTrue(
                    new TestConfigurationBuilder()
                        .addEnv(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY)
                        .addEnv(Configuration.PROPERTY_NO_PROXY, NON_PROXY_HOSTS)),
                PROXY_HOST, 80, null, null, "(" + NON_PROXY_HOSTS + ")"),

            /*
             * Setting up tests for loading the Java environment proxy configurations takes additional work as each
             * piece of the proxy configuration is a separate environment value. The non-proxy hosts will be checked
             * against the global environment value when it is not being set by the configuration passed by the test
             * as this value may be setup by the JVM.
             */

            // Basic Java HTTPS proxy.
            Arguments.of(createJavaEnvConfiguration(443, null, null, null, true),
                PROXY_HOST, 443, null, null, null),

            // Username only Java HTTPS proxy.
            Arguments.of(createJavaEnvConfiguration(443, PROXY_USER, null, null, true),
                PROXY_HOST, 443, null, null, null),

            // Complete Java HTTPS proxy.
            Arguments.of(createJavaEnvConfiguration(443, PROXY_USER, PROXY_PASSWORD, null, true),
                PROXY_HOST, 443, PROXY_USER, PROXY_PASSWORD, null),

            // Java HTTPS proxy with non-proxying hosts.
            Arguments.of(createJavaEnvConfiguration(443, null, null, NON_PROXY_HOSTS, true),
                PROXY_HOST, 443, null, null, "(" + NON_PROXY_HOSTS + ")"),

            // Basic Java HTTP proxy.
            Arguments.of(createJavaEnvConfiguration(80, null, null, null, false),
                PROXY_HOST, 80, null, null, null),

            // Username only Java HTTP proxy.
            Arguments.of(createJavaEnvConfiguration(80, PROXY_USER, null, null, false),
                PROXY_HOST, 80, null, null, null),

            // Complete Java HTTP proxy.
            Arguments.of(createJavaEnvConfiguration(80, PROXY_USER, PROXY_PASSWORD, null, false),
                PROXY_HOST, 80, PROXY_USER, PROXY_PASSWORD, null),

            // Java HTTP proxy with non-proxying hosts.
            Arguments.of(createJavaEnvConfiguration(80, null, null, NON_PROXY_HOSTS, false),
                PROXY_HOST, 80, null, null, "(" + NON_PROXY_HOSTS + ")")
        );
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
            Arguments.of(createExplicitConfiguration(443, null, null, null, true),
                PROXY_HOST, 443, null, null, null),

            // Username only Java HTTPS proxy.
            Arguments.of(createExplicitConfiguration(443, PROXY_USER, null, null, true),
                PROXY_HOST, 443, null, null, null),

            // Complete Java HTTPS proxy.
            Arguments.of(createExplicitConfiguration(443, PROXY_USER, PROXY_PASSWORD, null, true),
                PROXY_HOST, 443, PROXY_USER, PROXY_PASSWORD, null),

            // Java HTTPS proxy with non-proxying hosts.
            Arguments.of(createExplicitConfiguration(443, null, null, NON_PROXY_HOSTS, true),
                PROXY_HOST, 443, null, null, "(" + NON_PROXY_HOSTS + ")"),

            // Basic Java HTTP proxy.
            Arguments.of(createExplicitConfiguration(80, null, null, null, false),
                PROXY_HOST, 80, null, null, null),

            // Username only Java HTTP proxy.
            Arguments.of(createExplicitConfiguration(80, PROXY_USER, null, null, false),
                PROXY_HOST, 80, null, null, null),

            // Complete Java HTTP proxy.
            Arguments.of(createExplicitConfiguration(80, PROXY_USER, PROXY_PASSWORD, null, false),
                PROXY_HOST, 80, PROXY_USER, PROXY_PASSWORD, null),

            // Java HTTP proxy with non-proxying hosts.
            Arguments.of(createExplicitConfiguration(80, null, null, NON_PROXY_HOSTS, false),
                PROXY_HOST, 80, null, null, "(" + NON_PROXY_HOSTS + ")")
        );
    }

    /**
     * Tests that passing {@link Configuration#NONE} into {@link ProxyOptions#fromConfiguration(Configuration)} will
     * throw an {@link IllegalArgumentException}.
     */
    @Test
    public void loadFromEnvironmentThrowsWhenPassedConfigurationNone() {
        assertThrows(IllegalArgumentException.class, () -> fromConfiguration(Configuration.NONE));
        assertThrows(IllegalArgumentException.class, () -> fromConfiguration(Configuration.NONE, true));
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
        return Stream.of(
            // Java HTTPS configuration without 'java.net.useSystemProxies' set.
            Arguments.of(new TestConfigurationBuilder().addEnv(Configuration.PROPERTY_HTTPS_PROXY, AZURE_HTTPS_PROXY_HOST_ONLY).build()),

            // Java HTTP configuration without 'java.net.useSystemProxies' set.
            Arguments.of(new TestConfigurationBuilder().addEnv(Configuration.PROPERTY_HTTP_PROXY, AZURE_HTTP_PROXY_HOST_ONLY).build())
        );
    }

    private static Configuration createJavaEnvConfiguration(int port, String username, String password,
        String nonProxyHosts, boolean isHttps) {
        TestConfigurationBuilder testSource = new TestConfigurationBuilder()
            .addEnv(JAVA_NON_PROXY_HOSTS, CoreUtils.isNullOrEmpty(nonProxyHosts) ? "" : nonProxyHosts);


        if (isHttps) {
            testSource.addEnv(JAVA_HTTPS_PROXY_HOST, PROXY_HOST)
                .addEnv(JAVA_HTTPS_PROXY_PORT, String.valueOf(port))
                .addEnv(JAVA_HTTPS_PROXY_USER, username)
                .addEnv(JAVA_HTTPS_PROXY_PASSWORD, password);
        } else {
            testSource.addEnv(JAVA_HTTP_PROXY_HOST, PROXY_HOST)
                .addEnv(JAVA_HTTP_PROXY_PORT, String.valueOf(port))
                .addEnv(JAVA_HTTP_PROXY_USER, username)
                .addEnv(JAVA_HTTP_PROXY_PASSWORD, password);
        }

        return testSource.build();
    }

    private static Configuration createExplicitConfiguration(int port, String username, String password,
                                                            String nonProxyHosts, boolean isHttps) {
        TestConfigurationBuilder testSource = new TestConfigurationBuilder()
            .add("http.proxy.non-proxy-hosts", CoreUtils.isNullOrEmpty(nonProxyHosts) ? "" : nonProxyHosts);


        if (isHttps) {
            testSource.add("https.proxy.host", PROXY_HOST)
                .add("https.proxy.port", String.valueOf(port))
                .add("https.proxy.username", username)
                .add("https.proxy.password", password);
        } else {
            testSource.add("http.proxy.host", PROXY_HOST)
                .add("http.proxy.port", String.valueOf(port))
                .add("http.proxy.username", username)
                .add("http.proxy.password", password);
        }

        return testSource.build();
    }

    @ParameterizedTest
    @MethodSource("nonProxyHostsSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public void nonProxyHosts(Pattern pattern, String host, boolean expected) {
        assertEquals(expected, pattern.matcher(host).find(), () -> String.format(
            "Expected Pattern '%s' to match '%s'.", pattern.pattern(), host));
    }

    private static Stream<Arguments> nonProxyHostsSupplier() {
        String javaNonProxyHosts = String.join("|", "localhost", "127.0.0.1", "*.prod.linkedin.com",
            "*.azure.linkedin.com", "*.blob.core.windows.net");
        String noProxyNonProxyHosts = String.join(",", "localhost", "127.0.0.1", ".*.prod.linkedin.com",
            ".azure.linkedin.com", "*.blob.core.windows.net");

        /*
         * This emulates loading a Java formatted proxy.
         */
        Configuration javaProxyConfiguration = new TestConfigurationBuilder()
            .addEnv("http.proxyHost", "localhost")
            .addEnv("http.proxyPort", "7777")
            .addEnv("http.nonProxyHosts", javaNonProxyHosts)
            .build();

        Pattern javaProxyConfigurationPattern = compile(fromConfiguration(javaProxyConfiguration)
            .getNonProxyHosts(), Pattern.CASE_INSENSITIVE);

        /*
         * This emulates loading an environment formatted proxy.
         */
        Configuration environmentProxyConfiguration = setJavaSystemProxyPrerequisiteToTrue(new TestConfigurationBuilder()
            .addEnv(Configuration.PROPERTY_HTTP_PROXY, "http://localhost:7777")
            .addEnv(Configuration.PROPERTY_NO_PROXY, noProxyNonProxyHosts));

        Pattern environmentProxyConfigurationPattern = compile(fromConfiguration(environmentProxyConfiguration)
            .getNonProxyHosts(), Pattern.CASE_INSENSITIVE);

        /*
         * This emulates configuring a proxy in code using the expected Java formatted proxy.
         */
        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 7777))
            .setNonProxyHosts(javaNonProxyHosts);

        Pattern codeProxyPattern = compile(proxyOptions.getNonProxyHosts(), Pattern.CASE_INSENSITIVE);

        Pattern[] patterns = new Pattern[]{
            javaProxyConfigurationPattern, environmentProxyConfigurationPattern, codeProxyPattern
        };

        String[] expectedMatchHosts = new String[]{
            "localhost", "127.0.0.1", "azp.prod.linkedin.com", "azp.azure.linkedin.com", "azp.blob.core.windows.net"
        };

        String[] expectedNonMatchHosts = new String[]{
            "example.com", "portal.azure.com", "linkedin.com", "127a0b0c1", "azpaprodblinkedinccom",
            "azpaazureblinkedinccom", "azpablobbcorecwindowsdnet"
        };

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

    private static Configuration setJavaSystemProxyPrerequisiteToTrue(TestConfigurationBuilder configuration) {
        return configuration.addEnv(JAVA_SYSTEM_PROXY_PREREQUISITE, "true").build();
    }
}
