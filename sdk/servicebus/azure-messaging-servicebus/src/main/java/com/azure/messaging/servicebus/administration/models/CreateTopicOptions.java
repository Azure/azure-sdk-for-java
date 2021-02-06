// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;


import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_DUPLICATE_DETECTION_DURATION;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_LOCK_DURATION;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_MAX_DELIVERY_COUNT;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_TOPIC_SIZE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MAX_DURATION;

/**
 * Represents the set of options that can be specified for the creation of a topic.
 *
 * @see ServiceBusAdministrationAsyncClient#createTopic(String, CreateTopicOptions)
 * @see ServiceBusAdministrationClient#createTopic(String, CreateTopicOptions)
 */
public final class CreateTopicOptions {
    private final List<AuthorizationRule> authorizationRules;

    private Duration autoDeleteOnIdle;
    private Duration defaultMessageTimeToLive;
    private Duration duplicateDetectionHistoryTimeWindow;
    private boolean enableBatchedOperations;
    private boolean enablePartitioning;
    private Duration lockDuration;
    private int maxDeliveryCount;
    private long maxSizeInMegabytes;
    private boolean requiresDuplicateDetection;
    private boolean requiresSession;
    private EntityStatus status;
    private boolean supportOrdering;
    private String userMetadata;

    /**
     * Creates an instance. Default values for the topic are populated. The properties populated with defaults are:
     *
     * <ul>
     *     <li>{@link #setAutoDeleteOnIdle(Duration)} is max duration value.</li>
     *     <li>{@link #setDefaultMessageTimeToLive(Duration)} is max duration value.</li>
     *     <li>{@link #setDuplicateDetectionHistoryTimeWindow(Duration)} is max duration value, but duplication
     *     detection is disabled.</li>
     *     <li>{@link #setDuplicateDetectionRequired(boolean)} is false.</li>
     *     <li>{@link #setBatchedOperationsEnabled(boolean)} (boolean)} is true.</li>
     *     <li>{@link #setLockDuration(Duration)} is 1 minute.</li>
     *     <li>{@link #setMaxDeliveryCount(int)} is 10.</li>
     *     <li>{@link #setMaxSizeInMegabytes(long)} is 1024MB.</li>
     *     <li>{@link #setSessionRequired(boolean)} is false.</li>
     *     <li>{@link #setStatus(EntityStatus)} is {@link EntityStatus#ACTIVE}.</li>
     * </ul>
     *
     * @throws NullPointerException if {@code topicName} is a null.
     * @throws IllegalArgumentException if {@code topicName} is an empty string.
     */
    public CreateTopicOptions() {
        this.authorizationRules = new ArrayList<>();
        this.autoDeleteOnIdle = MAX_DURATION;
        this.defaultMessageTimeToLive = MAX_DURATION;
        this.duplicateDetectionHistoryTimeWindow = DEFAULT_DUPLICATE_DETECTION_DURATION;
        this.enableBatchedOperations = true;
        this.enablePartitioning = false;
        this.lockDuration = DEFAULT_LOCK_DURATION;
        this.maxDeliveryCount = DEFAULT_MAX_DELIVERY_COUNT;
        this.maxSizeInMegabytes = DEFAULT_TOPIC_SIZE;
        this.requiresDuplicateDetection = false;
        this.requiresSession = false;
        this.status = EntityStatus.ACTIVE;
    }

    /**
     * Initializes a new instance based on the specified {@link CreateTopicOptions} instance. This is useful for
     * creating a new topic based on the properties of an existing topicOptions.
     *
     * @param topic Existing topicOptions to create options with.
     */
    public CreateTopicOptions(TopicProperties topic) {
        Objects.requireNonNull(topic, "'topic' cannot be null.");

        this.authorizationRules = new ArrayList<>(topic.getAuthorizationRules());
        this.autoDeleteOnIdle = topic.getAutoDeleteOnIdle();
        this.defaultMessageTimeToLive = topic.getDefaultMessageTimeToLive();
        this.duplicateDetectionHistoryTimeWindow = topic.getDuplicateDetectionHistoryTimeWindow();
        this.enableBatchedOperations = topic.isBatchedOperationsEnabled();
        this.enablePartitioning = topic.isPartitioningEnabled();
        this.maxSizeInMegabytes = topic.getMaxSizeInMegabytes();
        this.requiresDuplicateDetection = topic.isDuplicateDetectionRequired();
        this.supportOrdering = topic.isOrderingSupported();
        this.status = topic.getStatus();
        this.userMetadata = topic.getUserMetadata();
    }

    /**
     * Gets the authorization rules to control user access at entity level.
     *
     * @return The authorization rules to control user access at entity level.
     */
    public List<AuthorizationRule> getAuthorizationRules() {
        return authorizationRules;
    }

    /**
     * Get the autoDeleteOnIdle property: ISO 8601 timeSpan idle interval after which the queue is automatically
     * deleted. The minimum duration is 5 minutes.
     *
     * @return the autoDeleteOnIdle value.
     */
    public Duration getAutoDeleteOnIdle() {
        return this.autoDeleteOnIdle;
    }

    /**
     * Set the autoDeleteOnIdle property: ISO 8601 timeSpan idle interval after which the queue is automatically
     * deleted. The minimum duration is 5 minutes.
     *
     * @param autoDeleteOnIdle the autoDeleteOnIdle value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setAutoDeleteOnIdle(Duration autoDeleteOnIdle) {
        this.autoDeleteOnIdle = autoDeleteOnIdle;
        return this;
    }

    /**
     * Get the defaultMessageTimeToLive property: ISO 8601 default message timespan to live value. This is the duration
     * after which the message expires, starting from when the message is sent to Service Bus. This is the default value
     * used when TimeToLive is not set on a message itself.
     *
     * @return the defaultMessageTimeToLive value.
     */
    public Duration getDefaultMessageTimeToLive() {
        return this.defaultMessageTimeToLive;
    }

    /**
     * Set the defaultMessageTimeToLive property: ISO 8601 default message timespan to live value. This is the duration
     * after which the message expires, starting from when the message is sent to Service Bus. This is the default value
     * used when TimeToLive is not set on a message itself.
     *
     * @param defaultMessageTimeToLive the defaultMessageTimeToLive value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
        return this;
    }

    /**
     * Get the duplicateDetectionHistoryTimeWindow property: ISO 8601 timeSpan structure that defines the duration of
     * the duplicate detection history. The default value is 10 minutes.
     *
     * @return the duplicateDetectionHistoryTimeWindow value.
     */
    public Duration getDuplicateDetectionHistoryTimeWindow() {
        return this.duplicateDetectionHistoryTimeWindow;
    }

    /**
     * Set the duplicateDetectionHistoryTimeWindow property: ISO 8601 timeSpan structure that defines the duration of
     * the duplicate detection history. The default value is 10 minutes.
     *
     * @param duplicateDetectionHistoryTimeWindow the duplicateDetectionHistoryTimeWindow value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setDuplicateDetectionHistoryTimeWindow(Duration duplicateDetectionHistoryTimeWindow) {
        this.duplicateDetectionHistoryTimeWindow = duplicateDetectionHistoryTimeWindow;
        return this;
    }

    /**
     * Get the enableBatchedOperations property: Value that indicates whether server-side batched operations are
     * enabled.
     *
     * @return the enableBatchedOperations value.
     */
    public Boolean isBatchedOperationsEnabled() {
        return this.enableBatchedOperations;
    }

    /**
     * Set the enableBatchedOperations property: Value that indicates whether server-side batched operations are
     * enabled.
     *
     * @param enableBatchedOperations the enableBatchedOperations value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setBatchedOperationsEnabled(boolean enableBatchedOperations) {
        this.enableBatchedOperations = enableBatchedOperations;
        return this;
    }

    /**
     * Get the enablePartitioning property: A value that indicates whether the queue is to be partitioned across
     * multiple message brokers.
     *
     * @return the enablePartitioning value.
     */
    public Boolean isPartitioningEnabled() {
        return this.enablePartitioning;
    }

    /**
     * Set the enablePartitioning property: A value that indicates whether the queue is to be partitioned across
     * multiple message brokers.
     *
     * @param enablePartitioning the enablePartitioning value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setPartitioningEnabled(boolean enablePartitioning) {
        this.enablePartitioning = enablePartitioning;
        return this;
    }

    /**
     * Get the status property: Status of a Service Bus resource.
     *
     * @return the status value.
     */
    public EntityStatus getStatus() {
        return this.status;
    }

    /**
     * Set the status property: Status of a Service Bus resource.
     *
     * @param status the status value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setStatus(EntityStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Defines whether ordering needs to be maintained. If true, messages sent to topic will be forwarded to the
     * subscription in order.
     *
     * @return true if ordering should be maintained; false otherwise.
     */
    public boolean isSupportOrdering() {
        return supportOrdering;
    }

    /**
     * Defines whether ordering needs to be maintained. If true, messages sent to topic will be forwarded to the
     * subscription in order.
     *
     * @param supportOrdering true if ordering should be maintained; false otherwise.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setOrderingSupported(boolean supportOrdering) {
        this.supportOrdering = supportOrdering;
        return this;
    }

    /**
     * Get the lockDuration property: ISO 8601 timespan duration of a peek-lock; that is, the amount of time that the
     * message is locked for other receivers. The maximum value for LockDuration is 5 minutes; the default value is 1
     * minute.
     *
     * @return the lockDuration value.
     */
    public Duration getLockDuration() {
        return this.lockDuration;
    }

    /**
     * Set the lockDuration property: ISO 8601 timespan duration of a peek-lock; that is, the amount of time that the
     * message is locked for other receivers. The maximum value for LockDuration is 5 minutes; the default value is 1
     * minute.
     *
     * @param lockDuration the lockDuration value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setLockDuration(Duration lockDuration) {
        this.lockDuration = lockDuration;
        return this;
    }

    /**
     * Get the maxDeliveryCount property: The maximum delivery count. A message is automatically deadlettered after this
     * number of deliveries. Default value is 10.
     *
     * @return the maxDeliveryCount value.
     */
    public int getMaxDeliveryCount() {
        return this.maxDeliveryCount;
    }

    /**
     * Set the maxDeliveryCount property: The maximum delivery count. A message is automatically deadlettered after this
     * number of deliveries. Default value is 10.
     *
     * @param maxDeliveryCount the maxDeliveryCount value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setMaxDeliveryCount(int maxDeliveryCount) {
        this.maxDeliveryCount = maxDeliveryCount;
        return this;
    }

    /**
     * Get the maxSizeInMegabytes property: The maximum size of the queue in megabytes, which is the size of memory
     * allocated for the queue.
     *
     * @return the maxSizeInMegabytes value.
     */
    public long getMaxSizeInMegabytes() {
        return this.maxSizeInMegabytes;
    }

    /**
     * Set the maxSizeInMegabytes property: The maximum size of the queue in megabytes, which is the size of memory
     * allocated for the queue.
     *
     * @param maxSizeInMegabytes the maxSizeInMegabytes value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setMaxSizeInMegabytes(long maxSizeInMegabytes) {
        this.maxSizeInMegabytes = maxSizeInMegabytes;
        return this;
    }

    /**
     * Get the requiresDuplicateDetection property: A value indicating if this queue requires duplicate detection.
     *
     * @return the requiresDuplicateDetection value.
     */
    public Boolean isDuplicateDetectionRequired() {
        return this.requiresDuplicateDetection;
    }

    /**
     * Set the requiresDuplicateDetection property: A value indicating if this queue requires duplicate detection.
     *
     * @param requiresDuplicateDetection the requiresDuplicateDetection value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setDuplicateDetectionRequired(boolean requiresDuplicateDetection) {
        this.requiresDuplicateDetection = requiresDuplicateDetection;
        return this;
    }

    /**
     * Get the requiresSession property: A value that indicates whether the queue supports the concept of sessions.
     *
     * @return the requiresSession value.
     */
    public Boolean isSessionRequired() {
        return this.requiresSession;
    }

    /**
     * Set the requiresSession property: A value that indicates whether the queue supports the concept of sessions.
     *
     * @param requiresSession the requiresSession value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setSessionRequired(boolean requiresSession) {
        this.requiresSession = requiresSession;
        return this;
    }

    /**
     * Get the userMetadata property: Custom metdata that user can associate with the description. Max length is 1024
     * chars.
     *
     * @return the userMetadata value.
     */
    public String getUserMetadata() {
        return this.userMetadata;
    }

    /**
     * Set the userMetadata property: Custom metdata that user can associate with the description. Max length is 1024
     * chars.
     *
     * @param userMetadata the userMetadata value to set.
     *
     * @return the CreateTopicOptions object itself.
     */
    public CreateTopicOptions setUserMetadata(String userMetadata) {
        this.userMetadata = userMetadata;
        return this;
    }
}
