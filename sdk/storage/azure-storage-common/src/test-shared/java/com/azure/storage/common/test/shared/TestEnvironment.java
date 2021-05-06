// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.util.Locale;

public class TestEnvironment {
    private static final ClientLogger LOGGER = new ClientLogger(TestEnvironment.class);

    private final TestMode testMode;

    private final TestAccount primaryAccount;
    private final TestAccount secondaryAccount;
    private final TestAccount blobAccount;
    private final TestAccount managedDiskAccount;
    private final TestAccount premiumAccount;
    private final TestAccount versionedAccount;
    private final TestAccount dataLakeAccount;

    public TestEnvironment() {
        this.testMode = readTestModeFromEnvironment();
        this.primaryAccount = readTestAccountFromEnvironment("PRIMARY_STORAGE_", this.testMode);
        this.secondaryAccount = readTestAccountFromEnvironment("SECONDARY_STORAGE_", this.testMode);
        this.blobAccount = readTestAccountFromEnvironment("BLOB_STORAGE_", this.testMode);
        this.managedDiskAccount = readTestAccountFromEnvironment("MANAGED_DISK_STORAGE_", this.testMode);
        this.premiumAccount = readTestAccountFromEnvironment("PREMIUM_STORAGE_", this.testMode);
        this.versionedAccount = readTestAccountFromEnvironment("VERSIONED_STORAGE_", this.testMode);
        this.dataLakeAccount = readTestAccountFromEnvironment("STORAGE_DATA_LAKE_", this.testMode);
    }

    private static TestMode readTestModeFromEnvironment() {
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException ignored) {
                LOGGER.error("Could not parse '{}' into TestMode. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        }

        LOGGER.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
        return TestMode.PLAYBACK;
    }

    private static TestAccount readTestAccountFromEnvironment(String prefix, TestMode testMode) {
        String name = "azstoragesdkaccount";
        String key = "astorageaccountkey";
        if (testMode != TestMode.PLAYBACK) {
            name = Configuration.getGlobalConfiguration().get(prefix + "ACCOUNT_NAME");
            key = Configuration.getGlobalConfiguration().get(prefix + "ACCOUNT_KEY");
        }
        String blobEndpoint = String.format("https://%s.blob.core.windows.net", name);
        String blobEndpointSecondary = String.format("https://%s-secondary.blob.core.windows.net", name);
        String dataLakeEndpoint = String.format("https://%s.dfs.core.windows.net", name);
        String queueEndpoint = String.format("https://%s.queue.core.windows.net", name);
        String fileEndpoint = String.format("https://%s.file.core.windows.net", name);

        return new TestAccount(name, key, blobEndpoint, blobEndpointSecondary,
            dataLakeEndpoint, queueEndpoint, fileEndpoint);
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

    public TestAccount getBlobAccount() {
        return blobAccount;
    }

    public TestAccount getManagedDiskAccount() {
        return managedDiskAccount;
    }

    public TestAccount getDataLakeAccount() {
        return dataLakeAccount;
    }
}
