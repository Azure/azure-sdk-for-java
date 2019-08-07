// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.azure.messaging.eventhubs.models.ProxyConfiguration.SYSTEM_DEFAULTS;

@RunWith(Theories.class)
public class ProxyConfigurationTest {

    private static final String PROXY_HOST = "/127.0.0.1"; // InetAddressHolder's address starts with '/'
    private static final String PROXY_PORT = "3128";
    private static final String HTTP_PROXY = String.join(":", PROXY_HOST, PROXY_PORT);
    private static final String PROXY_USERNAME = "dummyUsername";
    private static final String PROXY_PASSWORD = "dummyPassword";

    private static Proxy proxyAddress = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));

    @DataPoints("Proxy Configuration Types")
    public static ProxyAuthenticationType[] proxyAuthenticationTypes() {
        return new ProxyAuthenticationType[] {
            ProxyAuthenticationType.BASIC, ProxyAuthenticationType.DIGEST, ProxyAuthenticationType.NONE
        };
    }

    @Test
    public void nullProxyConfiguration() {
        Assert.assertNull(SYSTEM_DEFAULTS.authentication());
        Assert.assertNull(SYSTEM_DEFAULTS.credential());
        Assert.assertNull(SYSTEM_DEFAULTS.proxyAddress());
    }

    @Theory
    public void validateProxyConfiguration(@FromDataPoints("Proxy Configuration Types") ProxyAuthenticationType proxyAuthenticationType) {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        validateProxyConfiguration(proxyConfiguration, proxyAuthenticationType);
    }

    @Theory
    public void testIsProxyAddressConfigured(@FromDataPoints("Proxy Configuration Types") ProxyAuthenticationType proxyAuthenticationType) {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        Assert.assertTrue(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, null, PROXY_PASSWORD);
        Assert.assertTrue(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, null);
        Assert.assertTrue(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, null, null);
        Assert.assertTrue(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, PROXY_USERNAME, PROXY_PASSWORD);
        Assert.assertFalse(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, null, PROXY_PASSWORD);
        Assert.assertFalse(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, PROXY_USERNAME, null);
        Assert.assertFalse(proxyConfiguration.isProxyAddressConfigured());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, null, null);
        Assert.assertFalse(proxyConfiguration.isProxyAddressConfigured());
    }

    @Theory
    public void testHasUserDefinedCredentials(@FromDataPoints("Proxy Configuration Types") ProxyAuthenticationType proxyAuthenticationType) {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, PROXY_PASSWORD);
        Assert.assertTrue(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, null, PROXY_PASSWORD);
        Assert.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, PROXY_USERNAME, null);
        Assert.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, proxyAddress, null, null);
        Assert.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, PROXY_USERNAME, PROXY_PASSWORD);
        Assert.assertTrue(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, null, PROXY_PASSWORD);
        Assert.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, PROXY_USERNAME, null);
        Assert.assertFalse(proxyConfiguration.hasUserDefinedCredentials());

        proxyConfiguration = new ProxyConfiguration(proxyAuthenticationType, null, null, null);
        Assert.assertFalse(proxyConfiguration.hasUserDefinedCredentials());
    }

    private static void validateProxyConfiguration(ProxyConfiguration proxyConfiguration, ProxyAuthenticationType proxyAuthenticationType) {
        String proxyAddressStr = proxyConfiguration.proxyAddress().address().toString();
        ProxyAuthenticationType authentication = proxyConfiguration.authentication();
        Assert.assertEquals(HTTP_PROXY, proxyAddressStr);
        Assert.assertEquals(PROXY_USERNAME, proxyConfiguration.credential().getUserName());
        Assert.assertEquals(PROXY_PASSWORD, new String(proxyConfiguration.credential().getPassword()));
        Assert.assertTrue(proxyAuthenticationType.equals(authentication));
    }
}
