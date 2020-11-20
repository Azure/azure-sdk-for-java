// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final EventHubProducerAsyncClient eventHubProducerAsyncClient;
    protected final EventHubProducerClient eventHubProducerClient;

    public ServiceTest(TOptions options) {
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

        // Setup the service client
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName);

        eventHubProducerAsyncClient = builder.buildAsyncProducerClient();
        eventHubProducerClient = builder.buildProducerClient();
    }
}
