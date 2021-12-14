package com.azure.perf.test.core;

public class MockEventContext {
    private String eventData;
    private int partition;

    public MockEventContext(int partition, String eventData) {
        this.partition = partition;
        this.eventData = eventData;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }
}
