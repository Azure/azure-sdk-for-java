// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyOptionsTest {

    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "3128";
    private static final String HTTP_PROXY = "/" + PROXY_HOST + ":" + PROXY_PORT; // InetAddressHolder's address starts with '/'
    private static final String FAKE_PROXY_USERNAME_PLACEHOLDER = "fakeUserNamePlaceholder";
    private static final String FAKE_PROXY_PASSWORD_PLACEHOLDER = "fakePasswordPlaceholder";

    private static Proxy proxyAddress = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));

    @ParameterizedTest
    @EnumSource(ProxyAuthenticationType.class)
    public void validateProxyConfiguration(ProxyAuthenticationType proxyAuthenticationType) {
        ProxyOptions proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, FAKE_PROXY_USERNAME_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        validateProxyConfiguration(proxyOptions, proxyAuthenticationType);
    }

    @ParameterizedTest
    @EnumSource(ProxyAuthenticationType.class)
    public void testIsProxyAddressConfigured(ProxyAuthenticationType proxyAuthenticationType) {
        ProxyOptions proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, FAKE_PROXY_USERNAME_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        Assertions.assertTrue(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, null, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        Assertions.assertTrue(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, FAKE_PROXY_USERNAME_PLACEHOLDER, null);
        Assertions.assertTrue(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, null, null);
        Assertions.assertTrue(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, FAKE_PROXY_USERNAME_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        Assertions.assertFalse(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, null, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        Assertions.assertFalse(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, FAKE_PROXY_USERNAME_PLACEHOLDER, null);
        Assertions.assertFalse(proxyOptions.isProxyAddressConfigured());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, null, null);
        Assertions.assertFalse(proxyOptions.isProxyAddressConfigured());
    }

    @ParameterizedTest
    @EnumSource(ProxyAuthenticationType.class)
    public void testHasUserDefinedCredentials(ProxyAuthenticationType proxyAuthenticationType) {
        ProxyOptions proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, FAKE_PROXY_USERNAME_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        Assertions.assertTrue(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, null, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, FAKE_PROXY_USERNAME_PLACEHOLDER, null);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, proxyAddress, null, null);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, FAKE_PROXY_USERNAME_PLACEHOLDER, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        Assertions.assertTrue(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, null, FAKE_PROXY_PASSWORD_PLACEHOLDER);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, FAKE_PROXY_USERNAME_PLACEHOLDER, null);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());

        proxyOptions = new ProxyOptions(proxyAuthenticationType, null, null, null);
        Assertions.assertFalse(proxyOptions.hasUserDefinedCredentials());
    }

    private static void validateProxyConfiguration(ProxyOptions proxyOptions, ProxyAuthenticationType proxyAuthenticationType) {
        String proxyAddressStr = proxyOptions.getProxyAddress().address().toString();
        ProxyAuthenticationType authentication = proxyOptions.getAuthentication();
        Assertions.assertEquals(HTTP_PROXY, proxyAddressStr);
        Assertions.assertEquals(FAKE_PROXY_USERNAME_PLACEHOLDER, proxyOptions.getCredential().getUserName());
        Assertions.assertEquals(FAKE_PROXY_PASSWORD_PLACEHOLDER, new String(proxyOptions.getCredential().getPassword()));
        Assertions.assertEquals(proxyAuthenticationType, authentication);
    }
}
