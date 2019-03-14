package com.microsoft.azure.servicebus.management;

/**
 * This provides runtime information of the subscription.
 */
public class SubscriptionRuntimeInfo extends EntityRuntimeInfo {
    private String topicPath;
    private String subscriptionName;
    private long messageCount;

    SubscriptionRuntimeInfo(String topicPath, String subscriptionName)
    {
        this.topicPath = topicPath;
        this.subscriptionName = subscriptionName;
        this.path = EntityNameHelper.formatSubscriptionPath(topicPath, subscriptionName);
    }

    /**
     * @return The path of the topic.
     */
    public String getTopicPath() {
        return topicPath;
    }

    /**
     * @return The name of the subscription
     */
    public String getSubscriptionName() {
        return subscriptionName;
    }

    /**
     * @return The total number of messages in the entity.
     */
    public long getMessageCount() {
        return messageCount;
    }

    void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }
}
