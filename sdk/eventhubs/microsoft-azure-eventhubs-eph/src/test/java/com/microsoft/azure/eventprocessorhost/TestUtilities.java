// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.ProxyConfiguration;
import org.junit.Assume;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

final class TestUtilities {
    // Copied from ApiTestBase.java
    // The name of the environment variables. We want to keep them consistent between both services.
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String EPH_STORAGE_CONNECTION_STRING_NAME = "AZURE_EPH_STORAGE_CONNECTION_STRING";
    private static final String AZURE_LOG_LEVEL_ENV_NAME = "AZURE_LOG_LEVEL";

    // These environment variables are to test proxy support.
    // Environment name is from BaseConfiguration.java
    private static final String PROXY_ADDRESS_ENV_NAME = "AZURE_HTTP_PROXY";
    private static final String PROXY_AUTH_TYPE_ENV_NAME = "AZURE_PROXY_AUTH";
    private static final String PROXY_USERNAME = "AZURE_PROXY_USERNAME";
    private static final String PROXY_PASSWORD = "AZURE_PROXY_PASSWORD";

    static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    static void skipIfAppveyor() {
        String appveyor = System.getenv("APPVEYOR"); // Set to "true" by Appveyor
        if (appveyor != null) {
            TestBase.logInfo("SKIPPING - APPVEYOR DETECTED");
        }
        Assume.assumeTrue(appveyor == null);
    }

    static String getStorageConnectionString() {
        TestUtilities.skipIfAppveyor();

        String retval = System.getenv("EPHTESTSTORAGE");

        // if EPHTESTSTORAGE is not set - we cannot run integration tests
        if (retval == null) {
            TestBase.logInfo("SKIPPING - NO STORAGE CONNECTION STRING");
        }
        Assume.assumeTrue(retval != null);

        return ((retval != null) ? retval : "");
    }

    /**
     * Gets the Event Hubs connection string if the environment variable {@link #EVENT_HUB_CONNECTION_STRING_ENV_NAME}
     * is set. Otherwise, null is returned.
     */
    static String getEventHubsConnectionString() {
        return System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);
    }

    /**
     * Gets the configured ProxyConfiguration.
     */
    static ProxyConfiguration getProxyConfiguration() {
        final String address = System.getenv(PROXY_ADDRESS_ENV_NAME);

        if (address == null) {
            return null;
        }

        final String[] hostPort = address.split(":");
        if (hostPort.length < 2) {
            TestBase.logInfo(PROXY_ADDRESS_ENV_NAME + " cannot be parsed into a proxy");
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

    static Boolean isRunningOnAzure() {
        return (System.getenv("EVENT_HUB_CONNECTION_STRING") != null);
    }
}
