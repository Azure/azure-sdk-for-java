// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.azure.core.amqp.ProxyOptions.*;

public class ProxyOptionsTest {

    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "5679";
    private static final Proxy PROXY_ADDRESS = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));
    /**
     * Test System default proxy configuration properties are set
     */
    @Test
    public void systemDefaultProxyConfiguration() {
        Assertions.assertEquals(ProxyAuthenticationType.NONE, SYSTEM_DEFAULTS.getAuthentication());
        Assertions.assertNull(SYSTEM_DEFAULTS.getCredential());
        Assertions.assertNull(SYSTEM_DEFAULTS.getProxyAddress());
    }

    @Test
    public void closeClearsPasswordArray() {
        ProxyOptions proxyConfig = new ProxyOptions(ProxyAuthenticationType.BASIC,
            PROXY_ADDRESS, PROXY_USERNAME, PROXY_PASSWORD);
        Assertions.assertArrayEquals(PROXY_PASSWORD.toCharArray(), proxyConfig.getCredential().getPassword());
        proxyConfig.close();
        Assertions.assertNotEquals(PROXY_PASSWORD.toCharArray()[0], proxyConfig.getCredential().getPassword()[0]);
    }
}
