// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

/**
 * Represents the EventHubs Service Test.
 * @param <TOptions> the options bag to use for running performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final EventHubProducerAsyncClient eventHubProducerAsyncClient;
    protected final EventHubProducerClient eventHubProducerClient;

    /**
     * Instantiates instance of the Service Test.
     * @param options The options bag to use to run performance test.
     * @throws IllegalStateException when expected configuration of environment variables is not found.
     */
    public ServiceTest(TOptions options) throws IllegalStateException {
        super(options);
        String connectionString = System.getenv("EVENTHUBS_CONNECTION_STRING");
        String eventHubName = System.getenv("EVENTHUB_NAME");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable EVENTHUBS_CONNECTION_STRING must be set");
        }

        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw new IllegalStateException("Environment variable EVENTHUB_NAME must be set");
        }

        // Setup the service client
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName);

        eventHubProducerAsyncClient = builder.buildAsyncProducerClient();
        eventHubProducerClient = builder.buildProducerClient();
    }
}
