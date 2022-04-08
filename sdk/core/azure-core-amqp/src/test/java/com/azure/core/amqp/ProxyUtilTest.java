// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ProxyUtilTest {
    public static final String JAVA_NET_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";
    public static final String SOME_USERNAME = "some_username";
    public static final String SOME_PASSWORD = "some_password";
    public static final String AUTH_TYPE = "BASIC";

    @MethodSource("getProxyConfigurations")
    @ParameterizedTest
    public void testProxyOptionsConfiguration(String proxyConfiguration, boolean expectSystemDefault) {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        configuration = configuration.put(Configuration.PROPERTY_HTTP_PROXY, proxyConfiguration);
        configuration = configuration.put(ProxyOptions.PROXY_AUTHENTICATION_TYPE, AUTH_TYPE);
        configuration = configuration.put(ProxyOptions.PROXY_USERNAME, SOME_USERNAME);
        configuration = configuration.put(ProxyOptions.PROXY_PASSWORD, SOME_PASSWORD);

        ProxyOptions proxyConfig =  ProxyUtil.getDefaultProxyConfiguration(configuration);
        if (expectSystemDefault) {
            assertIsSystemDefaultConfig(proxyConfig);
        } else {
            Assertions.assertNotNull(proxyConfig);
            Assertions.assertEquals(ProxyAuthenticationType.BASIC, proxyConfig.getAuthentication());
            Assertions.assertNotNull(proxyConfig.getProxyAddress());
            Assertions.assertNotNull(proxyConfig.getCredential());
            Assertions.assertEquals(SOME_USERNAME, proxyConfig.getCredential().getUserName());
            Assertions.assertArrayEquals(SOME_PASSWORD.toCharArray(), proxyConfig.getCredential().getPassword());
        }
    }

    @Test
    public void testNullProxyAddress() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        configuration = configuration.put(JAVA_NET_USE_SYSTEM_PROXIES, "true");
        configuration = configuration.put(ProxyOptions.PROXY_AUTHENTICATION_TYPE, AUTH_TYPE);
        configuration = configuration.put(ProxyOptions.PROXY_USERNAME, SOME_USERNAME);
        configuration = configuration.put(ProxyOptions.PROXY_PASSWORD, SOME_PASSWORD);
        ProxyOptions proxyConfig = ProxyUtil.getDefaultProxyConfiguration(configuration);
        assertIsSystemDefaultConfig(proxyConfig);
    }

    @Test
    public void testSystemProxiesConfiguration() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        configuration = configuration.put(Configuration.PROPERTY_HTTP_PROXY, "invalid_proxy");
        configuration = configuration.put(Configuration.PROPERTY_HTTPS_PROXY, "https://localhost:80");
        configuration = configuration.put(JAVA_NET_USE_SYSTEM_PROXIES, "true");

        ProxyOptions proxyConfig =  ProxyUtil.getDefaultProxyConfiguration(configuration);
        Assertions.assertNotNull(proxyConfig);
    }

    public static Stream<Arguments> getProxyConfigurations() {
        return Stream.of(
            Arguments.of("http://localhost:8080", true),
            Arguments.of("localhost:8080", false),
            Arguments.of("localhost_8080", true),
            Arguments.of("http://example.com:8080", true),
            Arguments.of("http://sub.example.com:8080", true),
            Arguments.of(":8080", true),
            Arguments.of("http://localhost", true),
            Arguments.of("sub.example.com:8080", false),
            Arguments.of("https://username:password@sub.example.com:8080", true),
            Arguments.of("https://username:password@sub.example.com", true)
        );
    }

    private void assertIsSystemDefaultConfig(ProxyOptions proxyConfig) {
        Assertions.assertEquals(ProxyOptions.SYSTEM_DEFAULTS.getAuthentication(), proxyConfig.getAuthentication());
        Assertions.assertNull(proxyConfig.getCredential());
        Assertions.assertNull(proxyConfig.getProxyAddress());
    }
}
