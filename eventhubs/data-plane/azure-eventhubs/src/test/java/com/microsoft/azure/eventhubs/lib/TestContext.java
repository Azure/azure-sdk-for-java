/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class TestContext {

    public final static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    final static String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "EVENT_HUB_CONNECTION_STRING";

    private static String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

    private TestContext() {
        // eq. of c# static class
    }

    public static ConnectionStringBuilder getConnectionString() {
        return new ConnectionStringBuilder(CONNECTION_STRING);
    }

    public static void setConnectionString(final String connectionString) {
        CONNECTION_STRING = connectionString;
    }

    public static String getConsumerGroupName() {
        return "$default";
    }

    public static boolean isTestConfigurationSet() {
        return System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME) != null;
    }
}
