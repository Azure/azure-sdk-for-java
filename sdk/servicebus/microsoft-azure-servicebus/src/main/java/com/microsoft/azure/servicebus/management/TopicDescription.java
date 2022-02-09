// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.IMessage;

import java.time.Duration;
import java.util.List;

/**
 * Represents the metadata description of the topic.
 */
public class TopicDescription extends UnknownPropertiesHolder {
    Duration duplicationDetectionHistoryTimeWindow = ManagementClientConstants.DEFAULT_HISTORY_DEDUP_WINDOW;
    String path;
    Duration defaultMessageTimeToLive = ManagementClientConstants.MAX_DURATION;
    Duration autoDeleteOnIdle = ManagementClientConstants.MAX_DURATION;
    String userMetadata = null;
    long maxSizeInMB = ManagementClientConstants.DEFAULT_MAX_SIZE_IN_MB;
    boolean requiresDuplicateDetection = false;
    boolean enableBatchedOperations = true;
    boolean enablePartitioning = false;
    boolean supportOrdering = false;
    EntityStatus status = EntityStatus.Active;
    List<AuthorizationRule> authorizationRules = null;
    boolean isAnonymousAccessible = false;
    boolean filterMessagesBeforePublishing = false;
    String forwardTo = null;
    boolean enableExpress = false;
    boolean enableSubscriptionPartitioning = false;

    /**
     * Initializes a new instance of TopicDescription with the specified relative path.
     * @param path - Path of the topic.
     *             Max length is 260 chars. Cannot start or end with a slash.
     *             Cannot have restricted characters: '@','?','#','*'
     */
    public TopicDescription(String path) {
        this.setPath(path);
    }

    /**
     * @return the path of the topic.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @param path - the path of topic.
     * Max length is 260 chars. Cannot start or end with a slash.
     * Cannot have restricted characters: '@','?','#','*'
     */
    private void setPath(String path) {
        EntityNameHelper.checkValidTopicName(path);
        this.path = path;
    }

    /**
     * @return the maximum size of the topic in megabytes, which is the size of memory allocated for the topic.
     * Default value is 1024.
     */
    public long getMaxSizeInMB() {
        return this.maxSizeInMB;
    }

    /**
     * @param maxSize - Sets the maximum size of the topic in megabytes, which is the size of memory allocated for the topic.
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
     * @param requiresDuplicateDetection - Set to true if duplicate detection needs to be enabled.
     * See also - {@link #isRequiresDuplicateDetection()}
     */
    public void setRequiresDuplicateDetection(boolean requiresDuplicateDetection) {
        this.requiresDuplicateDetection = requiresDuplicateDetection;
    }

    /**
     * Time-To-live is the duration after which the message expires, starting from when
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
        if (defaultMessageTimeToLive == null
                || (defaultMessageTimeToLive.compareTo(ManagementClientConstants.MIN_ALLOWED_TTL) < 0
                    || defaultMessageTimeToLive.compareTo(ManagementClientConstants.MAX_ALLOWED_TTL) > 0)) {
            throw new IllegalArgumentException(
                    String.format("The value must be between %s and %s.",
                            ManagementClientConstants.MAX_ALLOWED_TTL,
                            ManagementClientConstants.MIN_ALLOWED_TTL));
        }

        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
    }

    /**
     * @return The idle interval after which the topic is automatically deleted.
     * Default value is {@link ManagementClientConstants#MAX_DURATION}
     */
    public Duration getAutoDeleteOnIdle() {
        return autoDeleteOnIdle;
    }

    /**
     * @param autoDeleteOnIdle - The idle interval after which the topic is automatically deleted.
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
     * @return The {@link AuthorizationRule} on the topic to control user access at entity level.
     */
    public List<AuthorizationRule> getAuthorizationRules() {
        return authorizationRules;
    }

    /**
     * @param authorizationRules - The {@link AuthorizationRule} on the topic to control user access at entity level.
     */
    public void setAuthorizationRules(List<AuthorizationRule> authorizationRules) {
        this.authorizationRules = authorizationRules;
    }

    /**
     * Gets the status of the entity. When an entity is disabled, that entity cannot send or receive messages.
     * @return The current status of the topic (Enabled / Disabled).
     * The default value is Enabled.
     */
    public EntityStatus getEntityStatus() {
        return this.status;
    }

    /**
     * @param status - the status of the topic (Enabled / Disabled).
     * When an entity is disabled, that entity cannot send or receive messages.
     */
    public void setEntityStatus(EntityStatus status) {
        this.status = status;
    }

    /**
     * @return boolean indicating whether the topic is to be partitioned across multiple message brokers.
     * Defaults to false
     */
    public boolean isEnablePartitioning() {
        return enablePartitioning;
    }

    /**
     * @param enablePartitioning - true if topic is to be partitioned across multiple message brokers.
     */
    public void setEnablePartitioning(boolean enablePartitioning) {
        this.enablePartitioning = enablePartitioning;
    }

    /**
     * @return Defines whether ordering needs to be maintained. If true, messages sent to topic will be
     * forwarded to the subscription in order.
     * Defaults to false
     */
    public boolean isSupportOrdering() {
        return supportOrdering;
    }

    /**
     * @param supportOrdering - Defines whether ordering needs to be maintained. If true, messages sent to topic will be
     * forwarded to the subscription in order.
     */
    public void setSupportOrdering(boolean supportOrdering) {
        this.supportOrdering = supportOrdering;
    }

    /**
     * @return - Custom metdata that user can associate with the description.
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
            throw new IllegalArgumentException("Value cannot be null.");
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

        if (!(o instanceof TopicDescription)) {
            return false;
        }

        TopicDescription other = (TopicDescription) o;
        if (this.path.equalsIgnoreCase(other.path)
                && this.autoDeleteOnIdle.equals(other.autoDeleteOnIdle)
                && this.defaultMessageTimeToLive.equals(other.defaultMessageTimeToLive)
                && (!this.requiresDuplicateDetection || this.duplicationDetectionHistoryTimeWindow.equals(other.duplicationDetectionHistoryTimeWindow))
                && this.enableBatchedOperations == other.enableBatchedOperations
                && this.enablePartitioning == other.enablePartitioning
                && this.maxSizeInMB == other.maxSizeInMB
                && this.requiresDuplicateDetection == other.requiresDuplicateDetection
                && this.supportOrdering == other.supportOrdering
                && this.status.equals(other.status)
                && (this.userMetadata == null ? other.userMetadata == null : this.userMetadata.equals(other.userMetadata))
                && AuthorizationRuleSerializer.equals(this.authorizationRules, other.authorizationRules)
                && this.forwardTo == null ? other.forwardTo == null : this.forwardTo.equalsIgnoreCase(other.forwardTo)
                && this.enableExpress == other.enableExpress
                && this.enableSubscriptionPartitioning == other.enableSubscriptionPartitioning
                && this.isAnonymousAccessible == other.isAnonymousAccessible
                && this.filterMessagesBeforePublishing == other.filterMessagesBeforePublishing) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }
}
