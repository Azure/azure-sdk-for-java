package com.microsoft.azure.eventhubs.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

public class EventHubsPerfStressOptions extends PerfStressOptions {

    @Parameter(names = { "-bs", "--batchsize" }, description = "Size of the batch (in bytes)")
    private Integer batchSize = null;

    @Parameter(names = { "-e", "--events" }, description = "Number of events")
    private int events = 1;

    @Parameter(names = { "-pk", "--partitionKey" }, description = "Target Partition Key")
    private String paritionKey = null;

    @Parameter(names = { "-pi", "--partitionId" }, description = "Target Partition Id")
    private int paritionId = 0;



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
    public int getPartitionId() {
        return paritionId;
    }
}
