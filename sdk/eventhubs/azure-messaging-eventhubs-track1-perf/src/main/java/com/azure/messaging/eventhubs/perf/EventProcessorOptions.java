// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.beust.jcommander.Parameter;

/**
 * Options for Event Processor tests.
 *
 * @see EventProcessorTest
 */
public class EventProcessorOptions extends EventHubsReceiveOptions {
    @Parameter(names = {"-scs", "--storageConnectionString"}, description = "Connection string for Storage account.",
        required = true)
    private String storageConnectionString;

    @Parameter(names = {"-se", "--storageEndpoint"}, description = "Endpoint for storage account",
        required = true)
    private String storageEndpoint;

    @Parameter(names = {"-e", "--eventsToSend"}, description = "Number of events to send per partition.")
    private int eventsToSend = 100000;

    /**
     * Gets the connection string for the storage account.
     *
     * @return The connection string for the storage account.
     */
    public String getStorageConnectionString() {
        return storageConnectionString;
    }

    /**
     * Gets the endpoint for the storage account. For example: {@literal https://foo.blob.core.windows.net/}
     *
     * @return The endpoint for the storage account.
     */
    public String getStorageEndpoint() {
        return storageEndpoint;
    }

    /**
     * Gets the number of events to send when preparing test.
     *
     * @return Number of events to send per partition.
     */
    public int getEventsToSend() {
        return eventsToSend;
    }
}
