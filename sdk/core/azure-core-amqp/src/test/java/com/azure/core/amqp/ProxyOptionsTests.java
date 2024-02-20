// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.amqp.ProxyOptions.PROXY_PASSWORD;
import static com.azure.core.amqp.ProxyOptions.PROXY_USERNAME;
import static com.azure.core.amqp.ProxyOptions.SYSTEM_DEFAULTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyOptionsTests {
    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "5679";
    private static final Proxy PROXY_ADDRESS
        = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));
    public static final String JAVA_NET_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";
    public static final String SOME_USERNAME = "some_username";
    public static final String SOME_PASSWORD = "some_password";
    public static final String AUTH_TYPE = "BASIC";
    private static final ConfigurationSource EMPTY_SOURCE = new ConfigurationSource() {
        @Override
        public Map<String, String> getProperties(String source) {
            return Collections.emptyMap();
        }
    };

    /**
     * Test System default proxy configuration properties are set
     */
    @Test
    public void systemDefaultProxyConfiguration() {
        assertEquals(ProxyAuthenticationType.NONE, SYSTEM_DEFAULTS.getAuthentication());
        assertNull(SYSTEM_DEFAULTS.getCredential());
        assertNull(SYSTEM_DEFAULTS.getProxyAddress());
    }

    @Test
    public void closeClearsPasswordArray() {
        ProxyOptions proxyConfig
            = new ProxyOptions(ProxyAuthenticationType.BASIC, PROXY_ADDRESS, PROXY_USERNAME, PROXY_PASSWORD);
        assertArrayEquals(PROXY_PASSWORD.toCharArray(), proxyConfig.getCredential().getPassword());
        proxyConfig.close();
        assertNotEquals(PROXY_PASSWORD.toCharArray()[0], proxyConfig.getCredential().getPassword()[0]);
    }

    @Test
    public void emptyConfiguration() {
        assertIsSystemDefaultProxy(ProxyOptions.fromConfiguration(new ConfigurationBuilder().build()));
    }

    @Test
    public void noneConfigurationReturnsDefault() {
        assertIsSystemDefaultProxy(ProxyOptions.fromConfiguration(Configuration.NONE));
    }

    @Test
    public void noHostProperty() {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.authentication-type", "BASIC")
            .putProperty("amqp.proxy.type", "SOCKS")
            .putProperty("amqp.proxy.port", "123")
            .putProperty("amqp.proxy.username", "user")
            .putProperty("amqp.proxy.password", "password")
            .build();
        assertIsSystemDefaultProxy(ProxyOptions.fromConfiguration(configuration));
    }

    @Test
    public void noTypePropertyDefaultsToHttp() {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.authentication-type", "BASIC")
            .putProperty("amqp.proxy.port", "123")
            .putProperty("amqp.proxy.username", "user")
            .putProperty("amqp.proxy.password", "password")
            .build();

        ProxyOptions options = ProxyOptions.fromConfiguration(configuration);
        assertEquals(Proxy.Type.HTTP, options.getProxyAddress().type());
    }

    @Test
    public void validProperties() {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.port", "123")
            .putProperty("amqp.proxy.type", "SOCKS")
            .putProperty("amqp.proxy.username", "user")
            .putProperty("amqp.proxy.password", "password")
            .build();

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertNotNull(proxyOptions);

        assertInstanceOf(InetSocketAddress.class, proxyOptions.getProxyAddress().address());
        InetSocketAddress socketAddr = (InetSocketAddress) proxyOptions.getProxyAddress().address();

        assertFalse(socketAddr.isUnresolved());
        assertEquals(Proxy.Type.SOCKS, proxyOptions.getProxyAddress().type());
        assertEquals("localhost", socketAddr.getHostName());
        assertEquals(123, socketAddr.getPort());

        assertEquals(ProxyAuthenticationType.NONE, proxyOptions.getAuthentication());
        assertEquals("user", proxyOptions.getCredential().getUserName());
        assertEquals("password", new String(proxyOptions.getCredential().getPassword()));
    }

    @Test
    public void validGlobalAndLocalProperties() {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.port", "123")
            .putProperty("amqp.proxy.type", "SOCKS")
            .putProperty("amqp.proxy.authentication-type", "DIGEST")
            .putProperty("foo.amqp.proxy.username", "user")
            .putProperty("foo.amqp.proxy.password", "password")
            .buildSection("foo");

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertNotNull(proxyOptions);

        assertInstanceOf(InetSocketAddress.class, proxyOptions.getProxyAddress().address());
        InetSocketAddress socketAddr = (InetSocketAddress) proxyOptions.getProxyAddress().address();

        assertFalse(socketAddr.isUnresolved());
        assertEquals(Proxy.Type.SOCKS, proxyOptions.getProxyAddress().type());
        assertEquals("localhost", socketAddr.getHostName());
        assertEquals(123, socketAddr.getPort());

        assertEquals(ProxyAuthenticationType.DIGEST, proxyOptions.getAuthentication());
        assertEquals("user", proxyOptions.getCredential().getUserName());
        assertEquals("password", new String(proxyOptions.getCredential().getPassword()));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "" })
    public void portNullAndEmpty(String port) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.authentication-type", "BASIC")
            .putProperty("amqp.proxy.type", "SOCKS");

        if (port != null) {
            configBuilder.putProperty("amqp.proxy.port", port);
        }

        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.fromConfiguration(configBuilder.build()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "   ", "not-an-int" })
    public void invalidPortThrows(String port) {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.port", port)
            .putProperty("amqp.proxy.authentication-type", "BASIC")
            .putProperty("amqp.proxy.type", "SOCKS")
            .build();

        assertThrows(NumberFormatException.class, () -> ProxyOptions.fromConfiguration(configuration));
    }

    @ParameterizedTest
    @ValueSource(strings = { "NONE", "BASIC", "DIGEST" })
    public void validAuthenticationTypes(String authType) {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.port", "123")
            .putProperty("amqp.proxy.authentication-type", authType)
            .putProperty("amqp.proxy.type", "SOCKS")
            .build();

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertEquals(authType, proxyOptions.getAuthentication().toString());
    }

    @Test
    public void invalidAuthenticationType() {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.port", "123")
            .putProperty("amqp.proxy.authentication-type", "foo")
            .putProperty("amqp.proxy.type", "SOCKS")
            .build();

        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.fromConfiguration(configuration));
    }

    @ParameterizedTest
    @ValueSource(strings = { "HTTP", "SOCKS" })
    public void validProxyTypes(String proxyType) {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.port", "443")
            .putProperty("amqp.proxy.type", proxyType)
            .build();

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertEquals(proxyType, proxyOptions.getProxyAddress().type().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "DIRECT", "foo" })
    public void invalidProxyTypes(String proxyType) {
        Configuration configuration = new ConfigurationBuilder().putProperty("amqp.proxy.hostname", "localhost")
            .putProperty("amqp.proxy.port", "443")
            .putProperty("amqp.proxy.type", proxyType)
            .build();

        // direct is valid Proxy.Type, but it's not possible to create a Proxy with DIRECT type
        assertThrows(IllegalArgumentException.class, () -> ProxyOptions.fromConfiguration(configuration));
    }

    @MethodSource("getProxyConfigurations")
    @ParameterizedTest
    public void testProxyOptionsFromEnvironmentConfiguration(String proxyConfiguration, boolean expectSystemDefault) {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, proxyConfiguration)
                .put(ProxyOptions.PROXY_AUTHENTICATION_TYPE, AUTH_TYPE)
                .put(ProxyOptions.PROXY_USERNAME, SOME_USERNAME)
                .put(ProxyOptions.PROXY_PASSWORD, SOME_PASSWORD)).build();

        ProxyOptions proxyConfig = ProxyOptions.fromConfiguration(configuration);
        if (expectSystemDefault) {
            assertIsSystemDefaultProxy(proxyConfig);
        } else {
            assertNotNull(proxyConfig);
            assertEquals(ProxyAuthenticationType.BASIC, proxyConfig.getAuthentication());
            assertNotNull(proxyConfig.getProxyAddress());
            assertNotNull(proxyConfig.getCredential());
            assertEquals(SOME_USERNAME, proxyConfig.getCredential().getUserName());
            assertArrayEquals(SOME_PASSWORD.toCharArray(), proxyConfig.getCredential().getPassword());
        }
    }

    @Test
    public void testNullProxyAddress() {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put(JAVA_NET_USE_SYSTEM_PROXIES, "true")
                .put(ProxyOptions.PROXY_AUTHENTICATION_TYPE, AUTH_TYPE)
                .put(ProxyOptions.PROXY_USERNAME, SOME_USERNAME)
                .put(ProxyOptions.PROXY_PASSWORD, SOME_PASSWORD)).build();

        ProxyOptions proxyConfig = ProxyOptions.fromConfiguration(configuration);
        assertIsSystemDefaultProxy(proxyConfig);
    }

    @Test
    public void mixedConfigurationExplicitWins() {
        Configuration configuration = new ConfigurationBuilder(
            new TestConfigurationSource().put("amqp.proxy.hostname", "localhost")
                .put("amqp.proxy.port", "4242")
                .put("amqp.proxy.authentication-type", "DIGEST"),
            EMPTY_SOURCE,
            new TestConfigurationSource().put(JAVA_NET_USE_SYSTEM_PROXIES, "true")
                .put(Configuration.PROPERTY_HTTP_PROXY, PROXY_HOST)
                .put(ProxyOptions.PROXY_AUTHENTICATION_TYPE, AUTH_TYPE)
                .put(ProxyOptions.PROXY_USERNAME, SOME_USERNAME)
                .put(ProxyOptions.PROXY_PASSWORD, SOME_PASSWORD)).build();

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertEquals(ProxyAuthenticationType.DIGEST, proxyOptions.getAuthentication());

        InetSocketAddress socketAddr = (InetSocketAddress) proxyOptions.getProxyAddress().address();

        assertFalse(socketAddr.isUnresolved());
        assertEquals(Proxy.Type.HTTP, proxyOptions.getProxyAddress().type());
        assertEquals("localhost", socketAddr.getHostName());
        assertEquals(4242, socketAddr.getPort());
        assertNull(proxyOptions.getCredential());
    }

    @Test
    public void mixedConfigurationNoExplicitHost() {
        Configuration configuration = new ConfigurationBuilder(
            new TestConfigurationSource().put("amqp.proxy.port", "4242")
                .put("amqp.proxy.authentication-type", "DIGEST"),
            EMPTY_SOURCE,
            new TestConfigurationSource().put(JAVA_NET_USE_SYSTEM_PROXIES, "true")
                .put(Configuration.PROPERTY_HTTP_PROXY, "localhost:4242")
                .put(ProxyOptions.PROXY_AUTHENTICATION_TYPE, "BASIC")
                .put(ProxyOptions.PROXY_USERNAME, SOME_USERNAME)
                .put(ProxyOptions.PROXY_PASSWORD, SOME_PASSWORD)).build();

        ProxyOptions proxyOptions = ProxyOptions.fromConfiguration(configuration);
        assertEquals(ProxyAuthenticationType.BASIC, proxyOptions.getAuthentication());

        InetSocketAddress socketAddr = (InetSocketAddress) proxyOptions.getProxyAddress().address();

        assertFalse(socketAddr.isUnresolved());
        assertEquals(Proxy.Type.HTTP, proxyOptions.getProxyAddress().type());
        assertEquals("localhost", socketAddr.getHostName());
        assertEquals(4242, socketAddr.getPort());
        assertNotNull(proxyOptions.getCredential());
        assertEquals(SOME_USERNAME, proxyOptions.getCredential().getUserName());
        assertEquals(SOME_PASSWORD, new String(proxyOptions.getCredential().getPassword()));
    }

    @Test
    public void testSystemProxiesConfiguration() {
        Configuration configuration = new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE,
            new TestConfigurationSource().put(Configuration.PROPERTY_HTTP_PROXY, "invalid_proxy")
                .put(Configuration.PROPERTY_HTTPS_PROXY, "https://localhost:80")
                .put(JAVA_NET_USE_SYSTEM_PROXIES, "true")).build();

        ProxyOptions proxyConfig = ProxyOptions.fromConfiguration(configuration);
        assertNotNull(proxyConfig);
        assertEquals(ProxyAuthenticationType.NONE, proxyConfig.getAuthentication());
        assertNull(proxyConfig.getCredential());
        assertNotNull(proxyConfig.getProxyAddress());
    }

    public static Stream<Arguments> getProxyConfigurations() {
        return Stream.of(Arguments.of("http://localhost:8080", true), Arguments.of("localhost:8080", false),
            Arguments.of("localhost_8080", true), Arguments.of("http://example.com:8080", true),
            Arguments.of("http://sub.example.com:8080", true), Arguments.of(":8080", true),
            Arguments.of("http://localhost", true), Arguments.of("sub.example.com:8080", false),
            Arguments.of("https://username:password@sub.example.com:8080", true),
            Arguments.of("https://username:password@sub.example.com", true));
    }

    private void assertIsSystemDefaultProxy(ProxyOptions proxyConfig) {
        assertEquals(ProxyOptions.SYSTEM_DEFAULTS.getAuthentication(), proxyConfig.getAuthentication());
        assertNull(proxyConfig.getCredential());
        assertNull(proxyConfig.getProxyAddress());
    }
}
