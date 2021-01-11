// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_DUPLICATE_DETECTION_DURATION;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_LOCK_DURATION;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_MAX_DELIVERY_COUNT;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_QUEUE_SIZE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MAX_DURATION;

/**
 * Represents the set of options that can be specified for the creation of a queue.
 *
 * @see ServiceBusAdministrationAsyncClient#createQueue(String, CreateQueueOptions)
 * @see ServiceBusAdministrationClient#createQueue(String, CreateQueueOptions)
 */
@Fluent
public final class CreateQueueOptions {
    private final List<AuthorizationRule> authorizationRules;

    private Duration autoDeleteOnIdle;
    private Duration defaultMessageTimeToLive;
    private boolean deadLetteringOnMessageExpiration;
    private Duration duplicateDetectionHistoryTimeWindow;
    private boolean enableBatchedOperations;
    private boolean enablePartitioning;
    private String forwardTo;
    private String forwardDeadLetteredMessagesTo;
    private Duration lockDuration;
    private int maxDeliveryCount;
    private long maxSizeInMegabytes;
    private boolean requiresDuplicateDetection;
    private boolean requiresSession;
    private String userMetadata;
    private EntityStatus status;

    /**
     * Creates an instance with the name of the queue. Default values for the queue are populated. The properties
     * populated with defaults are:
     *
     * <ul>
     *     <li>{@link #setAutoDeleteOnIdle(Duration)} is max duration value.</li>
     *     <li>{@link #setDefaultMessageTimeToLive(Duration)} is max duration value.</li>
     *     <li>{@link #setDuplicateDetectionHistoryTimeWindow(Duration)} is max duration value, but duplication
     *     detection is disabled.</li>
     *     <li>{@link #setDuplicateDetectionRequired(boolean)} is false.</li>
     *     <li>{@link #setBatchedOperationsEnabled(boolean)} is true.</li>
     *     <li>{@link #setLockDuration(Duration)} is 1 minute.</li>
     *     <li>{@link #setMaxDeliveryCount(int)} is 10.</li>
     *     <li>{@link #setMaxSizeInMegabytes(int)} is 1024MB.</li>
     *     <li>{@link #setSessionRequired(boolean)} is false.</li>
     *     <li>{@link #setStatus(EntityStatus)} is {@link EntityStatus#ACTIVE}.</li>
     * </ul>
     *
     * @throws NullPointerException if {@code queueName} is a null.
     * @throws IllegalArgumentException if {@code queueName} is an empty string.
     */
    public CreateQueueOptions() {
        this.authorizationRules = new ArrayList<>();
        this.autoDeleteOnIdle = MAX_DURATION;
        this.defaultMessageTimeToLive = MAX_DURATION;
        this.duplicateDetectionHistoryTimeWindow = DEFAULT_DUPLICATE_DETECTION_DURATION;
        this.enableBatchedOperations = true;
        this.enablePartitioning = false;
        this.lockDuration = DEFAULT_LOCK_DURATION;
        this.maxDeliveryCount = DEFAULT_MAX_DELIVERY_COUNT;
        this.maxSizeInMegabytes = DEFAULT_QUEUE_SIZE;
        this.requiresDuplicateDetection = false;
        this.requiresSession = false;
        this.deadLetteringOnMessageExpiration = false;
        this.status = EntityStatus.ACTIVE;
    }

    /**
     * Initializes a new instance based on the specified {@link QueueProperties} instance. This is useful for creating a
     * new queue based on the properties of an existing queue.
     *
     * @param queue Existing queue to create options with.
     */
    public CreateQueueOptions(QueueProperties queue) {
        Objects.requireNonNull(queue, "'queue' cannot be null.");

        this.authorizationRules = new ArrayList<>(queue.getAuthorizationRules());
        this.autoDeleteOnIdle = queue.getAutoDeleteOnIdle();
        this.defaultMessageTimeToLive = queue.getDefaultMessageTimeToLive();

        this.deadLetteringOnMessageExpiration = queue.isDeadLetteringOnMessageExpiration();
        this.duplicateDetectionHistoryTimeWindow = queue.getDuplicateDetectionHistoryTimeWindow() != null
            ? queue.getDuplicateDetectionHistoryTimeWindow()
            : DEFAULT_DUPLICATE_DETECTION_DURATION;
        this.enableBatchedOperations = queue.isBatchedOperationsEnabled();
        this.enablePartitioning = queue.isPartitioningEnabled();
        this.forwardTo = queue.getForwardTo();
        this.forwardDeadLetteredMessagesTo = queue.getForwardDeadLetteredMessagesTo();
        this.lockDuration = queue.getLockDuration();

        this.maxDeliveryCount = queue.getMaxDeliveryCount();
        this.maxSizeInMegabytes = queue.getMaxSizeInMegabytes();
        this.requiresDuplicateDetection = queue.isDuplicateDetectionRequired();
        this.requiresSession = queue.isSessionRequired();
        this.status = queue.getStatus();
        this.userMetadata = queue.getUserMetadata();
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
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setAutoDeleteOnIdle(Duration autoDeleteOnIdle) {
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
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
        return this;
    }

    /**
     * Get the deadLetteringOnMessageExpiration property: A value that indicates whether this queue has dead letter
     * support when a message expires.
     *
     * @return the deadLetteringOnMessageExpiration value.
     */
    public boolean isDeadLetteringOnMessageExpiration() {
        return this.deadLetteringOnMessageExpiration;
    }

    /**
     * Set the deadLetteringOnMessageExpiration property: A value that indicates whether this queue has dead letter
     * support when a message expires.
     *
     * @param deadLetteringOnMessageExpiration the deadLetteringOnMessageExpiration value to set.
     *
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setDeadLetteringOnMessageExpiration(boolean deadLetteringOnMessageExpiration) {
        this.deadLetteringOnMessageExpiration = deadLetteringOnMessageExpiration;
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
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setDuplicateDetectionHistoryTimeWindow(Duration duplicateDetectionHistoryTimeWindow) {
        this.duplicateDetectionHistoryTimeWindow = duplicateDetectionHistoryTimeWindow;
        return this;
    }

    /**
     * Get the enableBatchedOperations property: Value that indicates whether server-side batched operations are
     * enabled.
     *
     * @return the enableBatchedOperations value.
     */
    public boolean isBatchedOperationsEnabled() {
        return this.enableBatchedOperations;
    }

    /**
     * Set the enableBatchedOperations property: Value that indicates whether server-side batched operations are
     * enabled.
     *
     * @param enableBatchedOperations the enableBatchedOperations value to set.
     *
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setBatchedOperationsEnabled(boolean enableBatchedOperations) {
        this.enableBatchedOperations = enableBatchedOperations;
        return this;
    }

    /**
     * Get the enablePartitioning property: A value that indicates whether the queue is to be partitioned across
     * multiple message brokers.
     *
     * @return the enablePartitioning value.
     */
    public boolean isPartitioningEnabled() {
        return this.enablePartitioning;
    }

    /**
     * Set the enablePartitioning property: A value that indicates whether the queue is to be partitioned across
     * multiple message brokers.
     *
     * @param enablePartitioning the enablePartitioning value to set.
     *
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setPartitioningEnabled(boolean enablePartitioning) {
        this.enablePartitioning = enablePartitioning;
        return this;
    }

    /**
     * Get the forwardTo property: The name of the recipient entity to which all the messages sent to the queue are
     * forwarded to.
     *
     * @return the forwardTo value.
     */
    public String getForwardTo() {
        return this.forwardTo;
    }

    /**
     * Set the forwardTo property: The name of the recipient entity to which all the messages sent to the queue are
     * forwarded to.
     *
     * @param forwardTo the forwardTo value to set.
     *
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setForwardTo(String forwardTo) {
        this.forwardTo = forwardTo;
        return this;
    }

    /**
     * Get the forwardDeadLetteredMessagesTo property: The name of the recipient entity to which all the dead-lettered
     * messages of this queue are forwarded to.
     *
     * @return the forwardDeadLetteredMessagesTo value.
     */
    public String getForwardDeadLetteredMessagesTo() {
        return this.forwardDeadLetteredMessagesTo;
    }

    /**
     * Set the forwardDeadLetteredMessagesTo property: The name of the recipient entity to which all the dead-lettered
     * messages of this queue are forwarded to.
     *
     * @param forwardDeadLetteredMessagesTo the forwardDeadLetteredMessagesTo value to set.
     *
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setForwardDeadLetteredMessagesTo(String forwardDeadLetteredMessagesTo) {
        this.forwardDeadLetteredMessagesTo = forwardDeadLetteredMessagesTo;
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
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setLockDuration(Duration lockDuration) {
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
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setMaxDeliveryCount(int maxDeliveryCount) {
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
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setMaxSizeInMegabytes(int maxSizeInMegabytes) {
        this.maxSizeInMegabytes = maxSizeInMegabytes;
        return this;
    }

    /**
     * Get the requiresDuplicateDetection property: A value indicating if this queue requires duplicate detection.
     *
     * @return the requiresDuplicateDetection value.
     */
    public boolean isDuplicateDetectionRequired() {
        return this.requiresDuplicateDetection;
    }

    /**
     * Set the requiresDuplicateDetection property: A value indicating if this queue requires duplicate detection.
     *
     * @param requiresDuplicateDetection the requiresDuplicateDetection value to set.
     *
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setDuplicateDetectionRequired(boolean requiresDuplicateDetection) {
        this.requiresDuplicateDetection = requiresDuplicateDetection;
        return this;
    }

    /**
     * Get the requiresSession property: A value that indicates whether the queue supports the concept of sessions.
     *
     * @return the requiresSession value.
     */
    public boolean isSessionRequired() {
        return this.requiresSession;
    }

    /**
     * Set the requiresSession property: A value that indicates whether the queue supports the concept of sessions.
     *
     * @param requiresSession the requiresSession value to set.
     *
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setSessionRequired(boolean requiresSession) {
        this.requiresSession = requiresSession;
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
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setStatus(EntityStatus status) {
        this.status = status;
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
     * @return the CreateQueueOptions object itself.
     */
    public CreateQueueOptions setUserMetadata(String userMetadata) {
        this.userMetadata = userMetadata;
        return this;
    }
}
