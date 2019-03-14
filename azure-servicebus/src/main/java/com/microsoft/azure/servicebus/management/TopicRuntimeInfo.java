package com.microsoft.azure.servicebus.management;

/**
 * This provides runtime information of the topic.
 */
public class TopicRuntimeInfo extends EntityRuntimeInfo {
    private long sizeInBytes;
    private int subscriptionCount;

    TopicRuntimeInfo(String path)
    {
        this.path = path;
    }

    /**
     * @return Current size of the entity in bytes.
     */
    public long getSizeInBytes() {
        return sizeInBytes;
    }

    void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    /**
     * @return Number of subscriptions on the topic.
     */
    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    void setSubscriptionCount(int subscriptionCount) {
        this.subscriptionCount = subscriptionCount;
    }
}
