package com.azure.messaging.eventhubs.perf;

import java.util.concurrent.atomic.AtomicInteger;

public class PartitionCounter {
    private final String partitionId;
    private final AtomicInteger eventsReceived = new AtomicInteger();
    private volatile Long startTime;
    private volatile Long stopTime;

    public PartitionCounter(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void increment() {
        eventsReceived.incrementAndGet();
    }

    public int totalEvents() {
        return eventsReceived.get();
    }

    /**
     * Elapsed time in nanoseconds
     *
     * @return Elapsed time in nanoseconds.
     */
    public long elapsedTime() {
        if (stopTime == null || startTime == null) {
            return -1L;
        }
        return stopTime - startTime;
    }

    public synchronized void start() {
        if (startTime != null) {
            return;
        }

        startTime = System.nanoTime();
    }

    public synchronized void stop() {
        if (stopTime != null) {
            return;
        }

        stopTime = System.nanoTime();
    }
}
