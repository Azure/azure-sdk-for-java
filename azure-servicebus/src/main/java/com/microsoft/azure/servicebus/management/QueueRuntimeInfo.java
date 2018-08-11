package com.microsoft.azure.servicebus.management;

/**
 * This provides runtime information of the queue.
 */
public class QueueRuntimeInfo extends EntityRuntimeInfo {

    private long messageCount;
    private long sizeInBytes;

    QueueRuntimeInfo(String path)
    {
        this.path = path;
    }

    /**
     * The total number of messages in the entity.
     */
    public long getMessageCount() {
        return messageCount;
    }

    void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    /**
     * Current size of the entity in bytes.
     */
    public long getSizeInBytes() {
        return sizeInBytes;
    }

    void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
}
