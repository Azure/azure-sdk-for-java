// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.proxy;

import com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType;
import com.microsoft.azure.proton.transport.proxy.ProxyConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyConfigurationTest {
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password!";
    private static final char[] PASSWORD_CHARS = PASSWORD.toCharArray();
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final ProxyAuthenticationType AUTHENTICATION_TYPE = ProxyAuthenticationType.BASIC;

    public static ProxyConfiguration[] userConfigurations() {
        return new ProxyConfiguration[]{
            new ProxyConfiguration(AUTHENTICATION_TYPE, PROXY, null, PASSWORD),
            new ProxyConfiguration(AUTHENTICATION_TYPE, PROXY, USERNAME, null),
            new ProxyConfiguration(AUTHENTICATION_TYPE, PROXY, null, null),
        };
    }

    @Test
    public void systemConfiguredConfiguration() {
        ProxyConfiguration configuration = ProxyConfiguration.SYSTEM_DEFAULTS;

        Assertions.assertFalse(configuration.isProxyAddressConfigured());
        Assertions.assertFalse(configuration.hasUserDefinedCredentials());

        Assertions.assertNull(configuration.proxyAddress());
        Assertions.assertNull(configuration.credentials());
        Assertions.assertNull(configuration.authentication());
    }

    @Test
    public void userDefinedConfiguration() {
        ProxyConfiguration configuration = new ProxyConfiguration(AUTHENTICATION_TYPE, PROXY, USERNAME, PASSWORD);

        Assertions.assertTrue(configuration.isProxyAddressConfigured());
        Assertions.assertTrue(configuration.hasUserDefinedCredentials());

        Assertions.assertEquals(AUTHENTICATION_TYPE, configuration.authentication());
        Assertions.assertEquals(PROXY, configuration.proxyAddress());
        Assertions.assertEquals(USERNAME, configuration.credentials().getUserName());
        Assertions.assertArrayEquals(PASSWORD_CHARS, configuration.credentials().getPassword());
    }

    /**
     * Verify that if the user has not provided a username or password, we cannot construct valid credentials from that.
     */
    @ParameterizedTest
    @MethodSource("userConfigurations")
    public void userDefinedConfigurationMissingData(ProxyConfiguration configuration) {
        Assertions.assertTrue(configuration.isProxyAddressConfigured());
        Assertions.assertFalse(configuration.hasUserDefinedCredentials());

        Assertions.assertNull(configuration.credentials());

        Assertions.assertEquals(AUTHENTICATION_TYPE, configuration.authentication());
        Assertions.assertEquals(PROXY, configuration.proxyAddress());
    }

    /**
     * Verify that if the user has not provided a proxy address, we will use the system-wide configured proxy.
     */
    @Test
    public void userDefinedConfigurationNoProxyAddress() {
        ProxyAuthenticationType type = ProxyAuthenticationType.DIGEST;
        ProxyConfiguration configuration = new ProxyConfiguration(type, null, USERNAME, PASSWORD);

        Assertions.assertFalse(configuration.isProxyAddressConfigured());
        Assertions.assertTrue(configuration.hasUserDefinedCredentials());

        Assertions.assertEquals(type, configuration.authentication());
        Assertions.assertNotNull(configuration.credentials());

        Assertions.assertEquals(USERNAME, configuration.credentials().getUserName());
        Assertions.assertArrayEquals(PASSWORD_CHARS, configuration.credentials().getPassword());
    }
}
