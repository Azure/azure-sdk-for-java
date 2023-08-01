// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.proxy;

import com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType;
import com.microsoft.azure.proton.transport.proxy.ProxyConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.net.InetSocketAddress;
import java.net.Proxy;

@RunWith(value = Theories.class)
public class ProxyConfigurationTest {
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password!";
    private static final char[] PASSWORD_CHARS = PASSWORD.toCharArray();
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final ProxyAuthenticationType AUTHENTICATION_TYPE = ProxyAuthenticationType.BASIC;

    @DataPoints("userConfigurations")
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

        Assert.assertFalse(configuration.isProxyAddressConfigured());
        Assert.assertFalse(configuration.hasUserDefinedCredentials());

        Assert.assertNull(configuration.proxyAddress());
        Assert.assertNull(configuration.credentials());
        Assert.assertNull(configuration.authentication());
    }

    @Test
    public void userDefinedConfiguration() {
        ProxyConfiguration configuration = new ProxyConfiguration(AUTHENTICATION_TYPE, PROXY, USERNAME, PASSWORD);

        Assert.assertTrue(configuration.isProxyAddressConfigured());
        Assert.assertTrue(configuration.hasUserDefinedCredentials());

        Assert.assertEquals(AUTHENTICATION_TYPE, configuration.authentication());
        Assert.assertEquals(PROXY, configuration.proxyAddress());
        Assert.assertEquals(USERNAME, configuration.credentials().getUserName());
        Assert.assertArrayEquals(PASSWORD_CHARS, configuration.credentials().getPassword());
    }

    /**
     * Verify that if the user has not provided a username or password, we cannot construct valid credentials from that.
     */
    @Theory
    public void userDefinedConfigurationMissingData(@FromDataPoints("userConfigurations") ProxyConfiguration configuration) {
        Assert.assertTrue(configuration.isProxyAddressConfigured());
        Assert.assertFalse(configuration.hasUserDefinedCredentials());

        Assert.assertNull(configuration.credentials());

        Assert.assertEquals(AUTHENTICATION_TYPE, configuration.authentication());
        Assert.assertEquals(PROXY, configuration.proxyAddress());
    }

    /**
     * Verify that if the user has not provided a proxy address, we will use the system-wide configured proxy.
     */
    @Test
    public void userDefinedConfigurationNoProxyAddress() {
        ProxyAuthenticationType type = ProxyAuthenticationType.DIGEST;
        ProxyConfiguration configuration = new ProxyConfiguration(type, null, USERNAME, PASSWORD);

        Assert.assertFalse(configuration.isProxyAddressConfigured());
        Assert.assertTrue(configuration.hasUserDefinedCredentials());

        Assert.assertEquals(type, configuration.authentication());
        Assert.assertNotNull(configuration.credentials());

        Assert.assertEquals(USERNAME, configuration.credentials().getUserName());
        Assert.assertArrayEquals(PASSWORD_CHARS, configuration.credentials().getPassword());
    }
}
