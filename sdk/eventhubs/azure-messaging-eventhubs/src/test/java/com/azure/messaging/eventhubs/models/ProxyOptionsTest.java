// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.azure.core.amqp.ProxyOptions.SYSTEM_DEFAULTS;

public class ProxyOptionsTest {

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
        ProxyOptions proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        validateProxyConfiguration(proxyOptions, proxyAuthenticationType);
    }

    @ParameterizedTest
    @EnumSource(ProxyAuthenticationType.class)
    public void testIsProxyAddressConfigured(ProxyAuthenticationType proxyAuthenticationType) {
        ProxyOptions proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertTrue(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, null, PROXY_PASSWORD);
        Assertions.assertTrue(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, null);
        Assertions.assertTrue(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, null, null);
        Assertions.assertTrue(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertFalse(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, null, PROXY_PASSWORD);
        Assertions.assertFalse(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, PROXY_USERNAME, null);
        Assertions.assertFalse(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, null, null);
        Assertions.assertFalse(proxyOptions.isProxyAddressConfigured());
    }

    @ParameterizedTest
    @EnumSource(ProxyAuthenticationType.class)
    public void testHasUserDefinedCredentials(ProxyAuthenticationType proxyAuthenticationType) {
        ProxyOptions proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertTrue(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, null, PROXY_PASSWORD);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, null);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, null, null);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertTrue(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, null, PROXY_PASSWORD);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, PROXY_USERNAME, null);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, null, null);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());
    }

    private static void validateProxyConfiguration(ProxyOptions proxyOptions, ProxyAuthenticationType proxyAuthenticationType) {
        String proxyAddressStr = proxyOptions.getProxyAddress().address().toString();
        ProxyAuthenticationType authentication = proxyOptions.getAuthentication();
        Assertions.assertEquals(HTTP_PROXY, proxyAddressStr);
        Assertions.assertEquals(PROXY_USERNAME, proxyOptions.getCredential().getUserName());
        Assertions.assertEquals(PROXY_PASSWORD, new String(proxyOptions.getCredential().getPassword()));
        Assertions.assertTrue(proxyAuthenticationType.equals(authentication));
    }
}
