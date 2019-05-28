package com.azure.eventhubs;

/**
 * The set of options that can be specified when sending a set of events to configure how the event data is packaged
 * into batches.
 */
public class EventBatchingOptions {
    private int maximumSizeInBytes;
    private String batchLabel;

    public EventBatchingOptions() {
        this.maximumSizeInBytes = EventSender.MAX_MESSAGE_LENGTH_BYTES;
    }

    /**
     * Sets the maximum size to allow for a single batch of events, in bytes. If this size is exceeded, an exception
     * will be thrown and the send operation will fail.
     *
     * @param maximumSizeInBytes The maximum size to allow for a single batch of events.
     * @return The updated EventBatchingOptions object.
     */
    public EventBatchingOptions maximumSizeInBytes(int maximumSizeInBytes) {
        this.maximumSizeInBytes = maximumSizeInBytes;
        return this;
    }

    /**
     * Gets the maximum size to allow for a single batch of events, in bytes. If this size is exceeded, an exception
     * will be thrown and the send operation will fail.
     *
     * @return The maximum size to allow for a single batch of events, in bytes.
     */
    public int maximumSizeInBytes() {
        return maximumSizeInBytes;
    }

    /**
     * Sets a label on an event batch to be identified as part of a group, which hints to the Event Hubs service that
     * reasonable efforts should be made to use the same partition for events belonging to that group.
     *
     * This should be specified only when there is a need to try and group events by partition, but there is flexibility
     * in allowing them to appear in other partitions at the discretion of the service, such as when a partition is
     * unavailable.
     *
     * If ensuring that a batch of events is sent only to a specific partition, it is recommended that the identifier of
     * the position be specified directly when sending the batch.
     *
     * @param batchLabel The label of an event batch.
     * @return The updated EventBatchingOptions object.
     */
    public EventBatchingOptions batchLabel(String batchLabel) {
        this.batchLabel = batchLabel;
        return this;
    }

    /**
     * Gets the label on an event batch. If specified, hints to the Event Hubs service that these events belong to the
     * same group and should belong to the same partition.
     *
     * @return The label on an event batch.
     */
    public String batchLabel() {
        return batchLabel;
    }
}
