// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class TestContext {

    public static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";

    private static String connectionString = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

    private TestContext() {
        // eq. of c# static class
    }

    public static ConnectionStringBuilder getConnectionString() {
        return new ConnectionStringBuilder(connectionString);
    }

    public static void setConnectionString(final String connectionString) {
        TestContext.connectionString = connectionString;
    }

    public static String getConsumerGroupName() {
        return "$default";
    }

    public static boolean isTestConfigurationSet() {
        return connectionString != null;
    }
}
