package com.azure.messaging.eventhubs.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

public class EventHubsPerfStressOptions extends PerfStressOptions {

    @Parameter(names = { "-bs", "--batchsize" }, description = "Size of the batch (in bytes)")
    private Long batchSize = null;

    @Parameter(names = { "-e", "--events" }, description = "Number of events")
    private int events = 1;


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
    public Long getBatchSize() {
        return batchSize;
    }
}
