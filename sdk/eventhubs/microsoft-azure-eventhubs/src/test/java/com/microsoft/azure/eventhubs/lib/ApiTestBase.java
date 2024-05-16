// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.eventhubs.ProxyConfiguration;
import org.junit.Assume;
import org.junit.BeforeClass;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ApiTestBase extends TestBase {
    // These environment variables are to test proxy support.
    // Environment name is from BaseConfiguration.java
    private static final String PROXY_ADDRESS_ENV_NAME = "AZURE_HTTP_PROXY";
    private static final String PROXY_AUTH_TYPE_ENV_NAME = "AZURE_PROXY_AUTH";
    private static final String PROXY_USERNAME = "AZURE_PROXY_USERNAME";
    private static final String PROXY_PASSWORD = "AZURE_PROXY_PASSWORD";

    @BeforeClass
    public static void skipIfNotConfigured() {
        Assume.assumeTrue(TestContext.isTestConfigurationSet());
    }

    /**
     * Gets the configured ProxyConfiguration.
     */
    public ProxyConfiguration getProxyConfiguration() {
        final String address = System.getenv(PROXY_ADDRESS_ENV_NAME);

        if (address == null) {
            return null;
        }

        final String[] hostPort = address.split(":");
        if (hostPort.length < 2) {
            this.logger.info(PROXY_ADDRESS_ENV_NAME + " cannot be parsed into a proxy");
            return null;
        }

        final String host = hostPort[0];
        final int port = Integer.parseInt(hostPort[1]);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

        final String username = System.getenv(PROXY_USERNAME);

        if (username == null) {
            return new ProxyConfiguration(ProxyConfiguration.ProxyAuthenticationType.NONE, proxy, null, null);
        }

        final ProxyConfiguration.ProxyAuthenticationType authenticationType =
            ProxyConfiguration.ProxyAuthenticationType.valueOf(System.getenv(PROXY_AUTH_TYPE_ENV_NAME));
        final String password = System.getenv(PROXY_PASSWORD);

        return new ProxyConfiguration(authenticationType, proxy, username, password);
    }
}
