// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

/**
 * Represents Mock Event Context for {@link MockEventProcessor}
 */
public class MockEventContext {
    private final String eventData;
    private final int partition;

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
     * @return the event data.
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
