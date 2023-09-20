// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf.core;


import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Represents options for EventHubs Tests.
 */
public class EventHubsPerfOptions extends PerfStressOptions {

    @Parameter(names = { "-bs", "--batchsize" }, description = "Size of the batch (in bytes)")
    private Integer batchSize = null;

    @Parameter(names = { "-ms", "--messagesize" }, description = "Size of the individual message (in bytes)")
    private int messageSize = 100;

    @Parameter(names = { "-e", "--events" }, description = "Number of events")
    private int events = 1;

    @Parameter(names = { "-pk", "--partitionKey" }, description = "Target Partition Key")
    private String paritionKey = null;

    @Parameter(names = { "-pi", "--partitionId" }, description = "Target Partition Id")
    private Integer paritionId = null;

    @Parameter(names = {"-cg", "--consumerGroup"}, description = "Name of the consumer group.")
    private String consumerGroup = "$Default";

    @Parameter(names = { "--prefetch" }, description = "Prefetch for the receiver.")
    private int prefetch = 500;

    /**
     * Get the configured events for performance test.
     * @return The events.
     */
    public int getEvents() {
        return events;
    }

    /**
     * Get the configured batch size option for performance test.
     * @return The batch size.
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Get the configured message size option for performance test.
     * @return The batch size.
     */
    public int getMessageSize() {
        return messageSize;
    }

    /**
     * Get the target partition key.
     * @return The target partition key.
     */
    public String getPartitionKey() {
        return paritionKey;
    }

    /**
     * Get the target partition id.
     * @return The target partition id.
     */
    public Integer getPartitionId() {
        return paritionId;
    }

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
}
