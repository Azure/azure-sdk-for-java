package com.azure.perf.test.core;

/**
 * Represents Mock Event Context for {@link MockEventProcessor}
 */
public class MockEventContext {
    private String eventData;
    private int partition;

    /**
     * Creates an instance of Mock Event Context
     * @param partition the target partition
     * @param eventData the data for the partition
     */
    public MockEventContext(int partition, String eventData) {
        this.partition = partition;
        this.eventData = eventData;
    }

    /**
     * Get the event data.
     * @return
     */
    public String getEventData() {
        return eventData;
    }

    /**
     * Get the target partition
     *
     * @return the target partition
     */
    public int getPartition() {
        return partition;
    }
}
