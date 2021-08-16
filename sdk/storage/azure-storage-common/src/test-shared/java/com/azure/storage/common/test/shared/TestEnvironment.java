// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

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

    private final String resourceGroupName;
    private final String subscriptionId;

    private final TestAccount primaryAccount;
    private final TestAccount secondaryAccount;
    private final TestAccount managedDiskAccount;
    private final TestAccount premiumAccount;
    private final TestAccount versionedAccount;
    private final TestAccount dataLakeAccount;
    private final TestAccount premiumFileAccount;
    private final TestAccount softDeleteAccount;
    private final TestAccount dataLakeSoftDeleteAccount;

    private TestEnvironment() {
        this.testMode = readTestModeFromEnvironment();
        this.serviceVersion = readServiceVersionFromEnvironment();
        this.httpClientType = readHttpClientTypeFromEnvironment();
        System.out.printf("Tests will run with %s http client%n", this.httpClientType);
        this.resourceGroupName = Configuration.getGlobalConfiguration().get("STORAGE_RESOURCE_GROUP_NAME");
        this.subscriptionId = Configuration.getGlobalConfiguration().get("STORAGE_SUBSCRIPTION_ID");
        this.primaryAccount = readTestAccountFromEnvironment("PRIMARY_STORAGE_", this.testMode);
        this.secondaryAccount = readTestAccountFromEnvironment("SECONDARY_STORAGE_", this.testMode);
        this.managedDiskAccount = readTestAccountFromEnvironment("MANAGED_DISK_STORAGE_", this.testMode);
        this.premiumAccount = readTestAccountFromEnvironment("PREMIUM_STORAGE_", this.testMode);
        this.versionedAccount = readTestAccountFromEnvironment("VERSIONED_STORAGE_", this.testMode);
        this.dataLakeAccount = readTestAccountFromEnvironment("STORAGE_DATA_LAKE_", this.testMode);
        this.premiumFileAccount = readTestAccountFromEnvironment("PREMIUM_STORAGE_FILE_", this.testMode);
        this.softDeleteAccount = readTestAccountFromEnvironment("SOFT_DELETE_STORAGE_", this.testMode);
        this.dataLakeSoftDeleteAccount = readTestAccountFromEnvironment("STORAGE_DATA_LAKE_SOFT_DELETE_", this.testMode);
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
            LOGGER.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
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

    private static TestAccount readTestAccountFromEnvironment(String prefix, TestMode testMode) {
        String name = "azstoragesdkaccount";
        String key = "astorageaccountkey";
        String connectionString = "DefaultEndpointsProtocol=http;AccountName=teststorage;"
            + "AccountKey=atestaccountkey;EndpointSuffix=core.windows.net";
        if (testMode != TestMode.PLAYBACK) {
            name = Configuration.getGlobalConfiguration().get(prefix + "ACCOUNT_NAME");
            key = Configuration.getGlobalConfiguration().get(prefix + "ACCOUNT_KEY");
            connectionString =  Configuration.getGlobalConfiguration().get(prefix + "CONNECTION_STRING");
            if (connectionString == null || connectionString.trim().isEmpty()) {
                connectionString = String.format("DefaultEndpointsProtocol=http;AccountName=%s;"
                    + "AccountKey=%s;EndpointSuffix=core.windows.net", name, key);
            }
        }
        String blobEndpoint = String.format("http://%s.blob.core.windows.net", name);
        String blobEndpointSecondary = String.format("https://%s-secondary.blob.core.windows.net", name);
        String dataLakeEndpoint = String.format("https://%s.dfs.core.windows.net", name);
        String queueEndpoint = String.format("https://%s.queue.core.windows.net", name);
        String fileEndpoint = String.format("https://%s.file.core.windows.net", name);

        return new TestAccount(name, key, connectionString, blobEndpoint, blobEndpointSecondary,
            dataLakeEndpoint, queueEndpoint, fileEndpoint);
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

    public TestMode getTestMode() {
        return testMode;
    }

    public TestAccount getPrimaryAccount() {
        return primaryAccount;
    }

    public TestAccount getSecondaryAccount() {
        return secondaryAccount;
    }

    public TestAccount getPremiumAccount() {
        return premiumAccount;
    }

    public TestAccount getVersionedAccount() {
        return versionedAccount;
    }

    public TestAccount getManagedDiskAccount() {
        return managedDiskAccount;
    }

    public TestAccount getDataLakeAccount() {
        return dataLakeAccount;
    }

    public TestAccount getPremiumFileAccount() {
        return premiumFileAccount;
    }

    public TestAccount getSoftDeleteAccount() {
        return softDeleteAccount;
    }

    public TestAccount getDataLakeSoftDeleteAccount() {
        return dataLakeSoftDeleteAccount;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public TestHttpClientType getHttpClientType() {
        return httpClientType;
    }
}
