// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.models.ProxyAuthenticationType;
import com.azure.core.amqp.models.ProxyConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.azure.core.amqp.models.ProxyConfiguration.SYSTEM_DEFAULTS;

public class ProxyConfigurationTest {

    private static final String PROXY_HOST = "/127.0.0.1"; // InetAddressHolder's address starts with '/'
    private static final String PROXY_PORT = "3128";
    private static final String HTTP_PROXY = String.join(":", PROXY_HOST, PROXY_PORT);
    private static final String PROXY_USERNAME = "dummyUsername";
    private static final String PROXY_PASSWORD = "dummyPassword";

    private static Proxy proxyAddress = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));

    @Test
    public void nullProxyConfiguration() {
        Assertions.assertNull(SYSTEM_DEFAULTS.getAuthentication());
        Assertions.assertNull(SYSTEM_DEFAULTS.getCredential());
        Assertions.assertNull(SYSTEM_DEFAULTS.getProxyAddress());
    }

    @ParameterizedTest
    @EnumSource(ProxyAuthenticationType.class)
    public void validateProxyConfiguration(ProxyAuthenticationType proxyAuthenticationType) {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        validateProxyConfiguration(proxyConfiguration, proxyAuthenticationType);
    }

    @ParameterizedTest
    @EnumSource(ProxyAuthenticationType.class)
    public void testIsProxyAddressConfigured(ProxyAuthenticationType proxyAuthenticationType) {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertTrue(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, null, PROXY_PASSWORD);
        Assertions.assertTrue(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, null);
        Assertions.assertTrue(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, null, null);
        Assertions.assertTrue(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertFalse(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, null, PROXY_PASSWORD);
        Assertions.assertFalse(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, PROXY_USERNAME, null);
        Assertions.assertFalse(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, null, null);
        Assertions.assertFalse(proxyConfiguration.isProxyAddressConfigured());
    }

    @ParameterizedTest
    @EnumSource(ProxyAuthenticationType.class)
    public void testHasUserDefinedCredentials(ProxyAuthenticationType proxyAuthenticationType) {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertTrue(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, null, PROXY_PASSWORD);
        Assertions.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, null);
        Assertions.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, null, null);
        Assertions.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertTrue(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, null, PROXY_PASSWORD);
        Assertions.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, PROXY_USERNAME, null);
        Assertions.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, null, null);
        Assertions.assertFalse(proxyConfiguration.hasUserDefinedCredentials());
    }

    private static void validateProxyConfiguration(ProxyConfiguration proxyConfiguration, ProxyAuthenticationType proxyAuthenticationType) {
        String proxyAddressStr = proxyConfiguration.getProxyAddress().address().toString();
        ProxyAuthenticationType authentication = proxyConfiguration.getAuthentication();
        Assertions.assertEquals(HTTP_PROXY, proxyAddressStr);
        Assertions.assertEquals(PROXY_USERNAME, proxyConfiguration.getCredential().getUserName());
        Assertions.assertEquals(PROXY_PASSWORD, new String(proxyConfiguration.getCredential().getPassword()));
        Assertions.assertTrue(proxyAuthenticationType.equals(authentication));
    }
}
