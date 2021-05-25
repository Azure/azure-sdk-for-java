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
    @Parameter(names = {"--output" }, description = "Name of a file to output results to. If null, then System.out.")
    private String outputFile;

    @Parameter(names = {"-scs", "--storageConnectionString"}, description = "Connection string for Storage account.",
        required = true)
    private String storageConnectionString;

    @Parameter(names = {"-se", "--storageEndpoint"}, description = "Endpoint for storage account",
        required = true)
    private String storageEndpoint;

    @Parameter(names = {"-e", "--eventsToSend"}, description = "Number of events to send per partition.")
    private int numberOfEvents = 1000;

    @Parameter(names = {"-b", "--batchSize"}, description = "Number of events to receive as a batch.")
    private int batchSize = 100;

    @Parameter(names = {"--batch"}, description = "Use batched receive.")
    private boolean isBatched = false;

    /**
     * Creates a new instance of the options.
     */
    public EventProcessorOptions() {
        super();
    }

    /**
     * Gets the name of the output file to write results to.
     *
     * @return The name of the output file to write results to. {@code null} to output to System.out.
     */
    public String getOutputFile() {
        return outputFile;
    }

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
     * Gets the number of events to send.
     *
     * @return Number of events to send per partition.
     */
    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    /**
     * Gets the number of events to receive per batch.
     *
     * @return The number of events to receive per batch.
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Gets whether or not to receive as a batch or not.
     *
     * @return Whether to receive as a batch or not. Default is {@code false}.
     */
    public boolean isBatched() {
        return isBatched;
    }
}
