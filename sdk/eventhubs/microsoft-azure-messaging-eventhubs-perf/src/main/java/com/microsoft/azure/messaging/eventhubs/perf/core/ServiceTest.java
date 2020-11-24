// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final EventHubClient eventHubClient;

    public ServiceTest(TOptions options) throws IOException, EventHubException {
        super(options);
        String connectionString = System.getenv("EVENTHUBS_CONNECTION_STRING");
        String eventHubName = System.getenv("EVENTHUB_NAME");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            System.out.println("Environment variable EVENTHUBS_CONNECTION_STRING must be set");
            System.exit(1);
        }

        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            System.out.println("Environment variable EVENTHUB_NAME must be set");
            System.exit(1);
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

        eventHubClient = EventHubClient.createSync(
            new ConnectionStringBuilder(connectionString).setEventHubName(eventHubName).toString(), executor);

        // Setup the service client

    }
}
