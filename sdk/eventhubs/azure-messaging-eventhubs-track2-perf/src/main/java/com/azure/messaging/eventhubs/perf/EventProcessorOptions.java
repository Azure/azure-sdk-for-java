// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.beust.jcommander.Parameter;

/**
 * Options for Event Processor tests.
 *
 * @see EventProcessorTest
 */
public class EventProcessorOptions extends EventHubsOptions {
    @Parameter(names = {"-cg", "--consumerGroup"}, description = "Name of the consumer group.")
    private String consumerGroup = "$Default";

    @Parameter(names = { "--prefetch" }, description = "Prefetch for the receiver.")
    private int prefetch = 500;

    @Parameter(names = {"-scs", "--storageConnectionString"}, description = "Connection string for Storage account.",
        required = true)
    private String storageConnectionString;

    @Parameter(names = {"-se", "--storageEndpoint"}, description = "Endpoint for storage account",
        required = true)
    private String storageEndpoint;

    @Parameter(names = {"-e", "--eventsToSend"}, description = "Number of events to send per partition.")
    private int eventsToSend = 100000;

    @Parameter(names = {"--publish"}, description = "Switch to indicate whether to publish messages or not.")
    private boolean publishMessages = false;

    @Parameter(names = {"-b", "--batchSize"}, description = "Number of events to receive as a batch.")
    private int batchSize = 100;

    @Parameter(names = {"--batch"}, description = "Use batched receive.")
    private boolean isBatched = false;

    /**
     * Gets the consumer group for receiving messages.
     *
     * @return The consumer group for receiving messages.
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Gets the prefetch for the receiver.
     *
     * @return The prefetch for the receiver.
     */
    public int getPrefetch() {
        return prefetch;
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
    public int getEventsToSend() {
        return eventsToSend;
    }

    /**
     * Gets whether to publish messages or not.
     *
     * @return Gets whether to publish messages to the event hub before running the test.
     */
    public boolean publishMessages() {
        return publishMessages;
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
