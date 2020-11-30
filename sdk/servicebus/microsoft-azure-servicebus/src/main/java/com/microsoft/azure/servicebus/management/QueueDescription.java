// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.IMessage;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Represents the metadata description of the queue.
 */
public class QueueDescription extends UnknownPropertiesHolder {
    Duration duplicationDetectionHistoryTimeWindow = ManagementClientConstants.DEFAULT_HISTORY_DEDUP_WINDOW;
    String path;
    Duration lockDuration = ManagementClientConstants.DEFAULT_LOCK_DURATION;
    Duration defaultMessageTimeToLive = ManagementClientConstants.MAX_DURATION;
    Duration autoDeleteOnIdle = ManagementClientConstants.MAX_DURATION;
    int maxDeliveryCount = ManagementClientConstants.DEFAULT_MAX_DELIVERY_COUNT;
    String forwardTo = null;
    String forwardDeadLetteredMessagesTo = null;
    String userMetadata = null;
    long maxSizeInMB = ManagementClientConstants.DEFAULT_MAX_SIZE_IN_MB;
    boolean requiresDuplicateDetection = false;
    boolean enableDeadLetteringOnMessageExpiration = false;
    boolean requiresSession = false;
    boolean enableBatchedOperations = true;
    boolean enablePartitioning = false;
    boolean enableExpress = false;
    EntityStatus status = EntityStatus.Active;
    boolean isAnonymousAccessible = false;
    boolean supportOrdering = false;
    boolean isSupportOrderingExplicitlySet = false;
    List<AuthorizationRule> authorizationRules = null;

    /**
     * Initializes a new instance of QueueDescription with the specified relative path.
     * @param path - Path of the topic.
     *             Max length is 260 chars. Cannot start or end with a slash.
     *             Cannot have restricted characters: '@','?','#','*'
     */
    public QueueDescription(String path) {
        this.setPath(path);
    }

    /**
     * @return The path of the queue.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @param path - The path of queue. Max length is 260 chars.
     *             Cannot start or end with a slash.
     *             Cannot have restricted characters: '@','?','#','*'
     */
    private void setPath(String path) {
        EntityNameHelper.checkValidQueueName(path);
        this.path = path;
    }

    /**
     * The amount of time that the message is locked by a given receiver
     * so that no other receiver receives the same message.
     * @return The duration of a peek lock. Default value is 60 seconds.
     */
    public Duration getLockDuration() {
        return this.lockDuration;
    }

    /**
     * Sets The amount of time that the message is locked by a given receiver
     * so that no other receiver receives the same message.
     * @param lockDuration - The duration of a peek lock. Max value is 5 minutes.
     */
    public void setLockDuration(Duration lockDuration) {
        Objects.requireNonNull(lockDuration, "'lockDuration' cannot be null.");

        this.lockDuration = lockDuration;
        if (this.lockDuration.compareTo(ManagementClientConstants.MAX_DURATION) > 0) {
            this.lockDuration = ManagementClientConstants.MAX_DURATION;
        }
    }

    /**
     * @return the maximum size of the queue in megabytes, which is the size of memory allocated for the queue.
     * Default value is 1024.
     */
    public long getMaxSizeInMB() {
        return this.maxSizeInMB;
    }

    /**
     * @param maxSize - Maximum size of the queue in megabytes, which is the size of memory allocated for the queue.
     */
    public void setMaxSizeInMB(long maxSize) {
        this.maxSizeInMB = maxSize;
    }

    /**
     * If enabled, duplicate messages having same {@link IMessage#getMessageId()} and sent to queue
     * within duration of {@link #getDuplicationDetectionHistoryTimeWindow} will be discarded.
     * @return value indicating if the queue requires guard against duplicate messages.
    */
    public boolean isRequiresDuplicateDetection() {
        return requiresDuplicateDetection;
    }

    /**
     * @param requiresDuplicateDetection - True if duplicate detection needs to be enabled.
     * See also - {@link #isRequiresDuplicateDetection()}
     */
    public void setRequiresDuplicateDetection(boolean requiresDuplicateDetection) {
        this.requiresDuplicateDetection = requiresDuplicateDetection;
    }

    /**
     * @return boolean that indicates whether the queue supports the concept of session. Sessionful-messages follow FIFO ordering.
     */
    public boolean isRequiresSession() {
        return requiresSession;
    }

    /**
     * @param requiresSession - True if queue should support sessions.
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
     * @param defaultMessageTimeToLive - The default message time to live value.
     * Value cannot be lower than 1 second.
     * See {@link #getDefaultMessageTimeToLive()}
     */
    public void setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        if (defaultMessageTimeToLive == null
            || (defaultMessageTimeToLive.compareTo(ManagementClientConstants.MIN_ALLOWED_TTL) < 0
                || defaultMessageTimeToLive.compareTo(ManagementClientConstants.MAX_ALLOWED_TTL) > 0)) {
            throw new IllegalArgumentException(
                    String.format("The value must be between %s and %s.",
                    ManagementClientConstants.MAX_ALLOWED_TTL,
                            ManagementClientConstants.MIN_ALLOWED_TTL));
        }

        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
        if (this.defaultMessageTimeToLive.compareTo(ManagementClientConstants.MAX_DURATION) > 0) {
            this.defaultMessageTimeToLive = ManagementClientConstants.MAX_DURATION;
        }
    }

    /**
     * @return The idle interval after which the queue is automatically deleted.
     * Default value is {@link ManagementClientConstants#MAX_DURATION}
     */
    public Duration getAutoDeleteOnIdle() {
        return autoDeleteOnIdle;
    }

    /**
     * @param autoDeleteOnIdle - The idle interval after which the queue is automatically deleted.
     * The minimum duration is 5 minutes.
     */
    public void setAutoDeleteOnIdle(Duration autoDeleteOnIdle) {
        if (autoDeleteOnIdle == null
            || autoDeleteOnIdle.compareTo(ManagementClientConstants.MIN_ALLOWED_AUTODELETE_DURATION) < 0) {
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
     * Indicates whether this queue has dead letter support when a message expires.
     * @return If true, the expired messages are moved to dead-letter sub-queue.
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
     * @return The duration of duplicate detection history that is maintained by the service.
     * The default value is 1 minute.
     */
    public Duration getDuplicationDetectionHistoryTimeWindow() {
        return duplicationDetectionHistoryTimeWindow;
    }

    /**
     * @param duplicationDetectionHistoryTimeWindow - The duration of duplicate detection history that is maintained by the service.
     * Max value is 1 day and minimum is 20 seconds.
     */
    public void setDuplicationDetectionHistoryTimeWindow(Duration duplicationDetectionHistoryTimeWindow) {
        if (duplicationDetectionHistoryTimeWindow == null
            || (duplicationDetectionHistoryTimeWindow.compareTo(ManagementClientConstants.MIN_DUPLICATE_HISTORY_DURATION) < 0
                || duplicationDetectionHistoryTimeWindow.compareTo(ManagementClientConstants.MAX_DUPLICATE_HISTORY_DURATION) > 0)) {
            throw new IllegalArgumentException(
                    String.format("The value must be between %s and %s.",
                            ManagementClientConstants.MIN_DUPLICATE_HISTORY_DURATION,
                            ManagementClientConstants.MAX_DUPLICATE_HISTORY_DURATION));
        }

        this.duplicationDetectionHistoryTimeWindow = duplicationDetectionHistoryTimeWindow;
        if (this.duplicationDetectionHistoryTimeWindow.compareTo(ManagementClientConstants.MAX_DURATION) > 0) {
            this.duplicationDetectionHistoryTimeWindow = ManagementClientConstants.MAX_DURATION;
        }
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
        if (maxDeliveryCount < ManagementClientConstants.MIN_ALLOWED_MAX_DELIVERYCOUNT) {
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
     * @return The {@link AuthorizationRule} on the queue to control user access at entity level.
     */
    public List<AuthorizationRule> getAuthorizationRules() {
        return authorizationRules;
    }

    /**
     * @param  authorizationRules - The {@link AuthorizationRule} on the queue to control user access at entity level.
     */
    public void setAuthorizationRules(List<AuthorizationRule> authorizationRules) {
        this.authorizationRules = authorizationRules;
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
     * @return The path of the recipient entity to which all the messages sent to the queue are forwarded to.
     * If set, user cannot manually receive messages from this queue. The destination entity
     * must be an already existing entity.
     */
    public String getForwardTo() {
        return forwardTo;
    }

    /**
     * @param forwardTo - The path of the recipient entity to which all the messages sent to the queue are forwarded to.
     * If set, user cannot manually receive messages from this queue. The destination entity
     * must be an already existing entity.
     */
    public void setForwardTo(String forwardTo) {
        if (forwardTo == null || forwardTo.isEmpty()) {
            this.forwardTo = forwardTo;
            return;
        }

        EntityNameHelper.checkValidQueueName(forwardTo);
        if (this.path.equals(forwardTo)) {
            throw new IllegalArgumentException("Entity cannot have auto-forwarding policy to itself");
        }

        this.forwardTo = forwardTo;
    }

    /**
     * @return The path of the recipient entity to which all the dead-lettered messages of this queue are forwarded to.
     * If set, user cannot manually receive dead-lettered messages from this queue. The destination
     * entity must already exist.
     */
    public String getForwardDeadLetteredMessagesTo() {
        return forwardDeadLetteredMessagesTo;
    }

    /**
     * @param forwardDeadLetteredMessagesTo - The path of the recipient entity to which all the dead-lettered messages of this queue are forwarded to.
     * If set, user cannot manually receive dead-lettered messages from this queue. The destination
     * entity must already exist.
     */
    public void setForwardDeadLetteredMessagesTo(String forwardDeadLetteredMessagesTo) {
        if (forwardDeadLetteredMessagesTo == null || forwardDeadLetteredMessagesTo.isEmpty()) {
            this.forwardDeadLetteredMessagesTo = forwardDeadLetteredMessagesTo;
            return;
        }

        EntityNameHelper.checkValidQueueName(forwardDeadLetteredMessagesTo);
        if (this.path.equals(forwardDeadLetteredMessagesTo)) {
            throw new IllegalArgumentException("Entity cannot have auto-forwarding policy to itself");
        }

        this.forwardDeadLetteredMessagesTo = forwardDeadLetteredMessagesTo;
    }

    /**
     * @return boolean indicating whether the queue is to be partitioned across multiple message brokers.
     * Defaults to false
     */
    public boolean isEnablePartitioning() {
        return enablePartitioning;
    }

    /**
     * @param enablePartitioning - true if queue is to be partitioned across multiple message brokers.
     */
    public void setEnablePartitioning(boolean enablePartitioning) {
        this.enablePartitioning = enablePartitioning;
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
        Objects.requireNonNull(userMetadata, "'userMetadata' cannot be null.");

        if (userMetadata.length() > ManagementClientConstants.MAX_USERMETADATA_LENGTH) {
            throw new IllegalArgumentException("Length cannot cross " + ManagementClientConstants.MAX_USERMETADATA_LENGTH + " characters");
        }

        this.userMetadata = userMetadata;
    }
    
    boolean isSupportOrdering() {
    	if (this.isSupportOrderingExplicitlySet) {
    		return this.supportOrdering;
    	} else {
    		return !this.enablePartitioning;
    	}
    }
    
    void setSupportOrdering(boolean supportOrdering) {
    	this.supportOrdering = supportOrdering;
    	this.isSupportOrderingExplicitlySet = true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof QueueDescription)) {
            return false;
        }

        QueueDescription other = (QueueDescription) o;
        if (this.path.equalsIgnoreCase(other.path)
                && this.autoDeleteOnIdle.equals(other.autoDeleteOnIdle)
                && this.defaultMessageTimeToLive.equals(other.defaultMessageTimeToLive)
                && (!this.requiresDuplicateDetection || this.duplicationDetectionHistoryTimeWindow.equals(other.duplicationDetectionHistoryTimeWindow))
                && this.enableBatchedOperations == other.enableBatchedOperations
                && this.enableDeadLetteringOnMessageExpiration == other.enableDeadLetteringOnMessageExpiration
                && this.enablePartitioning == other.enablePartitioning
                && (this.forwardTo == null ? other.forwardTo == null : this.forwardTo.equalsIgnoreCase(other.forwardTo))
                && (this.forwardDeadLetteredMessagesTo == null ? other.forwardDeadLetteredMessagesTo == null : this.forwardDeadLetteredMessagesTo.equalsIgnoreCase(other.forwardDeadLetteredMessagesTo))
                && this.lockDuration.equals(other.lockDuration)
                && this.maxDeliveryCount == other.maxDeliveryCount
                && this.maxSizeInMB == other.maxSizeInMB
                && this.requiresDuplicateDetection == other.requiresDuplicateDetection
                && this.requiresSession == other.requiresSession
                && this.status.equals(other.status)
                && (this.userMetadata == null ? other.userMetadata == null : this.userMetadata.equals(other.userMetadata))
                && AuthorizationRuleSerializer.equals(this.authorizationRules, other.authorizationRules)
        		&& this.enableExpress == other.enableExpress 
        		&& this.isAnonymousAccessible == other.isAnonymousAccessible
        		&& this.isSupportOrdering() == other.isSupportOrdering() ) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }
}
