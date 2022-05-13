// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.util.Locale;

public final class TestEnvironment {
    private static final ClientLogger LOGGER = new ClientLogger(TestEnvironment.class);
    private static final TestEnvironment INSTANCE = new TestEnvironment();

    private final TestHttpClientType httpClientType;
    private final TestMode testMode;
    private final String serviceVersion;
    private final String connectionString;

    private TestEnvironment() {
        this.testMode = readTestModeFromEnvironment();
        this.serviceVersion = readServiceVersionFromEnvironment();
        this.httpClientType = readHttpClientTypeFromEnvironment();
        this.connectionString = readConnectionStringFromEnvironment();

        System.out.printf("Tests will run with %s http client%n", this.httpClientType);
    }

    public static TestEnvironment getInstance() {
        return INSTANCE;
    }

    private static TestMode readTestModeFromEnvironment() {
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");

        TestMode testMode;
        if (azureTestMode != null) {
            try {
                testMode = TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException ignored) {
                LOGGER.error("Could not parse '{}' into TestMode. Using 'Playback' mode.", azureTestMode);
                testMode = TestMode.PLAYBACK;
            }
        } else {
            LOGGER.info("Environment variable '{}' has not been set yet. Using 'Live' mode.", "AZURE_TEST_MODE");
            testMode = TestMode.PLAYBACK;
        }

        System.out.printf("--------%s---------%n", testMode);
        return testMode;
    }

    private String readServiceVersionFromEnvironment() {
        String serviceVersion = Configuration.getGlobalConfiguration().get("AZURE_LIVE_TEST_SERVICE_VERSION");
        if (serviceVersion == null || serviceVersion.trim().isEmpty()) {
            System.out.println("Tests will run with default service version");
            return null;
        } else {
            System.out.printf("Tests will run with %s service version%n", serviceVersion);
            return serviceVersion;
        }
    }

    private static TestHttpClientType readHttpClientTypeFromEnvironment() {
        String httpClients = Configuration.getGlobalConfiguration().get("AZURE_TEST_HTTP_CLIENTS", "netty");
        switch (httpClients.toLowerCase()) {
            case "netty":
                return TestHttpClientType.NETTY;
            case "okhttp":
                return TestHttpClientType.OK_HTTP;
            default:
                throw new IllegalArgumentException("Unknown value of AZURE_TEST_HTTP_CLIENTS: " + httpClients);
        }
    }

    private static String readConnectionStringFromEnvironment() {
        return Configuration.getGlobalConfiguration().get("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
    }

    public TestMode getTestMode() {
        return testMode;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public TestHttpClientType getHttpClientType() {
        return httpClientType;
    }

    public String getConnectionString() { return connectionString; }

    public enum TestHttpClientType {
        NETTY,
        OK_HTTP
    }
}
