package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.rules.RuleDescription;

import java.time.Duration;

/**
 * Represents the metadata description of the subscription.
 */
public class SubscriptionDescription {
    private String topicPath;
    private String subscriptionName;
    Duration lockDuration = ManagementClientConstants.DEFAULT_LOCK_DURATION;
    Duration defaultMessageTimeToLive = ManagementClientConstants.MAX_DURATION;
    Duration autoDeleteOnIdle = ManagementClientConstants.MAX_DURATION;
    int maxDeliveryCount = ManagementClientConstants.DEFAULT_MAX_DELIVERY_COUNT;
    String forwardTo = null;
    String forwardDeadLetteredMessagesTo = null;
    String userMetadata = null;
    boolean enableDeadLetteringOnMessageExpiration = false;
    boolean enableDeadLetteringOnFilterEvaluationException = true;
    boolean requiresSession = false;
    boolean enableBatchedOperations = true;
    EntityStatus status = EntityStatus.Active;
    RuleDescription defaultRule = null;

    /**
     * Initializes a new instance of SubscriptionDescription with the specified relative path.
     * @param topicPath - Path of the topic
     *                  Max length is 260 chars. Cannot start or end with a slash.
     *                  Cannot have restricted characters: '@','?','#','*'
     * @param subscriptionName - Name of the subscription
     *                         Max length is 50 chars. Cannot have restricted characters: '@','?','#','*','/'
     */
    public SubscriptionDescription(String topicPath, String subscriptionName)
    {
        this.setTopicPath(topicPath);
        this.setSubscriptionName(subscriptionName);
    }

    /**
     * @return the path of the topic.
     */
    public String getTopicPath() {
        return topicPath;
    }

    /**
     * @param topicPath - The path of topic.
     * Max length is 260 chars. Cannot start or end with a slash.
     * Cannot have restricted characters: '@','?','#','*'
     */
    private void setTopicPath(String topicPath) {
        EntityNameHelper.checkValidTopicName(topicPath);
        this.topicPath = topicPath;
    }

    /**
     * @return the subscription name.
     */
    public String getSubscriptionName() {
        return subscriptionName;
    }

    /**
     * @param subscriptionName - Sets the name of the subscription.
     * Max length is 50 chars. Cannot have restricted characters: '@','?','#','*','/'
     */
    private void setSubscriptionName(String subscriptionName) {
        EntityNameHelper.checkValidSubscriptionName(subscriptionName);
        this.subscriptionName = subscriptionName;
    }

    /**
     * @return the path of the subscription, including the topic.
     */
    public String getPath() {
        return EntityNameHelper.formatSubscriptionPath(this.topicPath, this.subscriptionName);
    }

    /**
     * The amount of time that the message is locked by a given receiver
     * so that no other receiver receives the same message.
     * @return The duration of a peek lock. Default value is 60 seconds.
     */
    public Duration getLockDuration()
    {
        return this.lockDuration;
    }

    /**
     * Sets The amount of time that the message is locked by a given receiver
     * so that no other receiver receives the same message.
     * @param lockDuration - The duration of a peek lock. Max value is 5 minutes.
     */
    public void setLockDuration(Duration lockDuration)
    {
        this.lockDuration = lockDuration;
        if (this.lockDuration.compareTo(ManagementClientConstants.MAX_DURATION) > 0) {
            this.lockDuration = ManagementClientConstants.MAX_DURATION;
        }
    }

    /**
     * @return This indicates whether the subscription supports the concept of session. Sessionful-messages follow FIFO ordering.
     */
    public boolean isRequiresSession() {
        return requiresSession;
    }

    /**
     * @param requiresSession - Set to true if subscription should support sessions.
     */
    public void setRequiresSession(boolean requiresSession) {
        this.requiresSession = requiresSession;
    }

    /**
     * Time-To-Live is the duration after which the message expires, starting from when
     * the message is sent to Service Bus.
     * This is the default value used when {@link IMessage#getTimeToLive()} is not set on a message itself.
     * Messages older than their TimeToLive value will expire and no longer be retained in the message store.
     * Subscribers will be unable to receive expired messages.
     * @return The default time to live value for the messages. Default value is {@link ManagementClientConstants#MAX_DURATION}
     */
    public Duration getDefaultMessageTimeToLive() {
        return defaultMessageTimeToLive;
    }

    /**
     * @param defaultMessageTimeToLive - Sets the default message time to live value.
     * Value cannot be lower than 1 second.
     * See {@link #getDefaultMessageTimeToLive()}
     */
    public void setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        if (defaultMessageTimeToLive != null &&
                (defaultMessageTimeToLive.compareTo(ManagementClientConstants.MIN_ALLOWED_TTL) < 0 ||
                        defaultMessageTimeToLive.compareTo(ManagementClientConstants.MAX_ALLOWED_TTL) > 0))
        {
            throw new IllegalArgumentException(
                    String.format("The value must be between %s and %s.",
                            ManagementClientConstants.MAX_ALLOWED_TTL,
                            ManagementClientConstants.MIN_ALLOWED_TTL));
        }

        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
    }

    /**
     * @return The idle interval after which the subscription is automatically deleted.
     * Default value is {@link ManagementClientConstants#MAX_DURATION}
     */
    public Duration getAutoDeleteOnIdle() {
        return autoDeleteOnIdle;
    }

    /**
     * @param autoDeleteOnIdle - The idle interval after which the subscription is automatically deleted.
     * The minimum duration is 5 minutes.
     */
    public void setAutoDeleteOnIdle(Duration autoDeleteOnIdle) {
        if (autoDeleteOnIdle != null &&
                autoDeleteOnIdle.compareTo(ManagementClientConstants.MIN_ALLOWED_AUTODELETE_DURATION) < 0)
        {
            throw new IllegalArgumentException(
                    String.format("The value must be greater than %s.",
                            ManagementClientConstants.MIN_ALLOWED_AUTODELETE_DURATION));
        }

        this.autoDeleteOnIdle = autoDeleteOnIdle;
        if (this.autoDeleteOnIdle.compareTo(ManagementClientConstants.MAX_DURATION) > 0) {
            this.autoDeleteOnIdle = ManagementClientConstants.MAX_DURATION;
        }
    }

    /**
     * Indicates whether this subscription has dead letter support when a message expires.
     * @return If true, the expired messages are moved to dead-letter subqueue.
     * Default value is false.
     */
    public boolean isEnableDeadLetteringOnMessageExpiration() {
        return enableDeadLetteringOnMessageExpiration;
    }

    /**
     * @param enableDeadLetteringOnMessageExpiration - True if messages should be dead-lettered on expiration.
     * See {@link #isEnableDeadLetteringOnMessageExpiration()}
     */
    public void setEnableDeadLetteringOnMessageExpiration(boolean enableDeadLetteringOnMessageExpiration) {
        this.enableDeadLetteringOnMessageExpiration = enableDeadLetteringOnMessageExpiration;
    }

    /**
     * @return boolean indicating whether messages need to be forwarded to dead-letter subqueue when subscription rule evaluation fails.
     * Default value is true.
     */
    public boolean isEnableDeadLetteringOnFilterEvaluationException() {
        return enableDeadLetteringOnFilterEvaluationException;
    }

    /**
     * @param enableDeadLetteringOnFilterEvaluationException - True if messages should be dead-lettered on filter evaluation exception.
     * See {@link #isEnableDeadLetteringOnFilterEvaluationException()}
     */
    public void setEnableDeadLetteringOnFilterEvaluationException(boolean enableDeadLetteringOnFilterEvaluationException) {
        this.enableDeadLetteringOnFilterEvaluationException = enableDeadLetteringOnFilterEvaluationException;
    }

    /**
     * The maximum delivery count of a message before it is dead-lettered.
     * The delivery count is increased when a message is received in {@link com.microsoft.azure.servicebus.ReceiveMode#PEEKLOCK} mode
     * and didn't complete the message before the message lock expired.
     * @return Default value is 10.
     */
    public int getMaxDeliveryCount() {
        return maxDeliveryCount;
    }

    /**
     * The maximum delivery count of a message before it is dead-lettered.
     * The delivery count is increased when a message is received in {@link com.microsoft.azure.servicebus.ReceiveMode#PEEKLOCK} mode
     * and didn't complete the message before the message lock expired.
     * @param maxDeliveryCount - Minimum value is 1.
     */
    public void setMaxDeliveryCount(int maxDeliveryCount) {
        if (maxDeliveryCount < ManagementClientConstants.MIN_ALLOWED_MAX_DELIVERYCOUNT)
        {
            throw new IllegalArgumentException(
                    String.format("The value must be greater than %s.",
                            ManagementClientConstants.MIN_ALLOWED_MAX_DELIVERYCOUNT));
        }

        this.maxDeliveryCount = maxDeliveryCount;
    }

    /**
     * @return Indicates whether server-side batched operations are enabled.
     * Defaults to true.
     */
    public boolean isEnableBatchedOperations() {
        return enableBatchedOperations;
    }

    /**
     * @param enableBatchedOperations - Indicates whether server-side batched operations are enabled.
     */
    public void setEnableBatchedOperations(boolean enableBatchedOperations) {
        this.enableBatchedOperations = enableBatchedOperations;
    }

    /**
     * Gets the status of the entity. When an entity is disabled, that entity cannot send or receive messages.
     * @return The current status of the queue (Enabled / Disabled).
     * The default value is Enabled.
     */
    public EntityStatus getEntityStatus() {
        return this.status;
    }

    /**
     * @param status - the status of the queue (Enabled / Disabled).
     * When an entity is disabled, that entity cannot send or receive messages.
     */
    public void setEntityStatus(EntityStatus status) {
        this.status = status;
    }

    /**
     * @return The path of the recipient entity to which all the messages sent to the subscription are forwarded to.
     * If set, user cannot manually receive messages from this subscription. The destination entity
     * must be an already existing entity.
     */
    public String getForwardTo() {
        return forwardTo;
    }

    /**
     * @param forwardTo - The path of the recipient entity to which all the messages sent to the subscription are forwarded to.
     * If set, user cannot manually receive messages from this subscription. The destination entity
     * must be an already existing entity.
     */
    public void setForwardTo(String forwardTo) {
        if (forwardTo == null || forwardTo.isEmpty()) {
            this.forwardTo = forwardTo;
            return;
        }

        EntityNameHelper.checkValidQueueName(forwardTo);
        if (this.topicPath.equals(forwardTo)) {
            throw new IllegalArgumentException("Entity cannot have auto-forwarding policy to itself");
        }

        this.forwardTo = forwardTo;
    }

    /**
     * @return The path of the recipient entity to which all the dead-lettered messages of this subscription are forwarded to.
     * If set, user cannot manually receive dead-lettered messages from this subscription. The destination
     * entity must already exist.
     */
    public String getForwardDeadLetteredMessagesTo() {
        return forwardDeadLetteredMessagesTo;
    }

    /**
     * @param forwardDeadLetteredMessagesTo - The path of the recipient entity to which all the dead-lettered messages of this subscription are forwarded to.
     * If set, user cannot manually receive dead-lettered messages from this subscription. The destination
     * entity must already exist.
     */
    public void setForwardDeadLetteredMessagesTo(String forwardDeadLetteredMessagesTo) {
        if (forwardDeadLetteredMessagesTo == null || forwardDeadLetteredMessagesTo.isEmpty()) {
            this.forwardDeadLetteredMessagesTo = forwardDeadLetteredMessagesTo;
            return;
        }

        EntityNameHelper.checkValidQueueName(forwardDeadLetteredMessagesTo);
        if (this.topicPath.equals(forwardDeadLetteredMessagesTo)) {
            throw new IllegalArgumentException("Entity cannot have auto-forwarding policy to itself");
        }

        this.forwardDeadLetteredMessagesTo = forwardDeadLetteredMessagesTo;
    }

    /**
     * @return Custom metdata that user can associate with the description.
     */
    public String getUserMetadata() {
        return userMetadata;
    }

    /**
     * @param userMetadata - Custom metdata that user can associate with the description.
     * Cannot be null. Max length is 1024 chars
     */
    public void setUserMetadata(String userMetadata) {
        if (userMetadata == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        if (userMetadata.length() > ManagementClientConstants.MAX_USERMETADATA_LENGTH) {
            throw new IllegalArgumentException("Length cannot cross " + ManagementClientConstants.MAX_USERMETADATA_LENGTH + " characters");
        }

        this.userMetadata = userMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof SubscriptionDescription)) {
            return false;
        }

        SubscriptionDescription other = (SubscriptionDescription) o;
        if (this.topicPath.equalsIgnoreCase(other.topicPath)
                && this.subscriptionName.equalsIgnoreCase(other.subscriptionName)
                && this.autoDeleteOnIdle.equals(other.autoDeleteOnIdle)
                && this.defaultMessageTimeToLive.equals(other.defaultMessageTimeToLive)
                && this.enableBatchedOperations == other.enableBatchedOperations
                && this.enableDeadLetteringOnMessageExpiration == other.enableDeadLetteringOnMessageExpiration
                && this.enableDeadLetteringOnFilterEvaluationException == other.enableDeadLetteringOnFilterEvaluationException
                && (this.forwardTo == null ? other.forwardTo == null : this.forwardTo.equalsIgnoreCase(other.forwardTo))
                && (this.forwardDeadLetteredMessagesTo == null ? other.forwardDeadLetteredMessagesTo == null : this.forwardDeadLetteredMessagesTo.equalsIgnoreCase(other.forwardDeadLetteredMessagesTo))
                && this.lockDuration.equals(other.lockDuration)
                && this.maxDeliveryCount == other.maxDeliveryCount
                && this.requiresSession == other.requiresSession
                && this.status.equals(other.status)
                && (this.userMetadata == null ? other.userMetadata == null : this.userMetadata.equals(other.userMetadata))) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getPath().hashCode();
    }
}
