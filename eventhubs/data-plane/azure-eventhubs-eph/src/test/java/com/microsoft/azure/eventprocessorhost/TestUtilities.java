// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import org.junit.Assume;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

final class TestUtilities {
    static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private static final String EVENT_HUB_CONNECTION_STRING = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String EVENT_HUB_STORAGE_CONNECTION_STRING = "AZURE_EVENTHUBS_STORAGE_CONNECTION_STRING";

    static String getStorageConnectionString() {
        final String connectionString = System.getenv(EVENT_HUB_STORAGE_CONNECTION_STRING);

        // Cannot run integration tests without the storage connection string.
        if (connectionString == null) {
            TestBase.logInfo("SKIPPING - NO STORAGE CONNECTION STRING");
        }

        Assume.assumeTrue(connectionString != null);

        return connectionString;
    }

    static boolean isRunningOnAzure() {
        return (System.getenv(EVENT_HUB_CONNECTION_STRING) != null);
    }
}
