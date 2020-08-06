// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusManagementAsyncClient;
import com.azure.messaging.servicebus.ServiceBusManagementClient;

import java.time.Duration;
import java.util.Objects;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DEFAULT_LOCK_DURATION;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MAX_DURATION;

/**
 * Options to set when creating a subscription.
 *
 * @see ServiceBusManagementAsyncClient#createTopic(CreateTopicOptions)
 * @see ServiceBusManagementClient#createTopic(CreateTopicOptions)
 */
@Fluent
public class CreateSubscriptionOptions {
    private final String topicName;
    private final String subscriptionName;

    private Duration autoDeleteOnIdle;
    private Duration defaultMessageTimeToLive;
    private boolean deadLetteringOnMessageExpiration;
    private boolean deadLetteringOnFilterEvaluationExceptions;
    private boolean enableBatchedOperations;
    private String forwardTo;
    private String forwardDeadLetteredMessagesTo;
    private EntityStatus status;
    private Duration lockDuration;
    private int maxDeliveryCount;
    private boolean requiresSession;
    private String userMetadata;

    /**
     * Creates an instance with the name of the subscription and its associated topic. Default values for the
     * subscription are populated. The properties populated with defaults are:
     *
     * <ul>
     *     <li>{@link #setAutoDeleteOnIdle(Duration)} is max duration value.</li>
     *     <li>{@link #setDeadLetteringOnMessageExpiration(boolean)} is false.</li>
     *     <li>{@link #setDefaultMessageTimeToLive(Duration)} is max duration value.</li>
     *     <li>{@link #setEnableBatchedOperations(boolean)} is true.</li>
     *     <li>{@link #setEnableDeadLetteringOnFilterEvaluationExceptions(boolean)} is true.</li>
     *     <li>{@link #setLockDuration(Duration)} is 1 minute.</li>
     *     <li>{@link #setMaxDeliveryCount(int)} is 10.</li>
     *     <li>{@link #setRequiresSession(boolean)} is false.</li>
     *     <li>{@link #setStatus(EntityStatus)} is {@link EntityStatus#ACTIVE}.</li>
     * </ul>
     *
     * @param topicName Name of the topic associated with this subscription.
     * @param subscriptionName Name of the subscription.
     *
     * @throws NullPointerException if {@code topicName} or {@code subscriptionName} are null.
     * @throws IllegalArgumentException if {@code topicName} or {@code subscriptionName} are empty strings.
     */
    public CreateSubscriptionOptions(String topicName, String subscriptionName) {
        final ClientLogger logger = new ClientLogger(CreateSubscriptionOptions.class);
        if (topicName == null) {
            throw logger.logExceptionAsError(new NullPointerException("'topicName' cannot be null."));
        } else if (topicName.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'topicName' cannot be an empty string."));
        } else if (subscriptionName == null) {
            throw logger.logExceptionAsError(new NullPointerException("'subscriptionName' cannot be null."));
        } else if (subscriptionName.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'subscriptionName' cannot be an empty string."));
        }

        this.topicName = topicName;
        this.subscriptionName = subscriptionName;

        // Defaults copied from .NET's implementation.
        this.autoDeleteOnIdle = MAX_DURATION;
        this.deadLetteringOnMessageExpiration = false;
        this.deadLetteringOnFilterEvaluationExceptions = true;
        this.defaultMessageTimeToLive = MAX_DURATION;
        this.enableBatchedOperations = true;
        this.lockDuration = DEFAULT_LOCK_DURATION;
        this.maxDeliveryCount = 10;
        this.requiresSession = false;
        this.status = EntityStatus.ACTIVE;
    }

    /**
     * Initializes a new instance based on the specified {@link SubscriptionProperties} instance. This is useful for
     * creating a new subscription based on the properties of an existing subscription.
     *
     * @param subscription Existing subscription to create options with.
     */
    public CreateSubscriptionOptions(SubscriptionProperties subscription) {
        Objects.requireNonNull(subscription, "'subscription' cannot be null.");
        Objects.requireNonNull(subscription.getTopicName(), "Topic name cannot be null.");
        Objects.requireNonNull(subscription.getSubscriptionName(), "Subscription name cannot be null.");

        this.topicName = subscription.getTopicName();
        this.subscriptionName = subscription.getSubscriptionName();
        this.autoDeleteOnIdle = subscription.getAutoDeleteOnIdle();
        this.deadLetteringOnMessageExpiration = subscription.deadLetteringOnMessageExpiration();
        this.deadLetteringOnFilterEvaluationExceptions = subscription.enableDeadLetteringOnFilterEvaluationExceptions();
        this.defaultMessageTimeToLive = subscription.getDefaultMessageTimeToLive();
        this.enableBatchedOperations = subscription.enableBatchedOperations();
        this.forwardTo = subscription.getForwardTo();
        this.forwardDeadLetteredMessagesTo = subscription.getForwardDeadLetteredMessagesTo();
        this.lockDuration = subscription.getLockDuration();
        this.maxDeliveryCount = subscription.getMaxDeliveryCount();
        this.requiresSession = subscription.requiresSession();
        this.status = subscription.getStatus();
        this.userMetadata = subscription.getUserMetadata();
    }

    /**
     * Gets the name of the topic under which subscription exists.
     *
     * @return The name of the topic under which subscription exists.
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Gets the name of the subscription.
     *
     * @return The name of the subscription.
     */
    public String getSubscriptionName() {
        return subscriptionName;
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
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setLockDuration(Duration lockDuration) {
        this.lockDuration = lockDuration;
        return this;
    }

    /**
     * Get the requiresSession property: A value that indicates whether the queue supports the concept of sessions.
     *
     * @return the requiresSession value.
     */
    public boolean requiresSession() {
        return this.requiresSession;
    }

    /**
     * Set the requiresSession property: A value that indicates whether the queue supports the concept of sessions.
     *
     * @param requiresSession the requiresSession value to set.
     *
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setRequiresSession(boolean requiresSession) {
        this.requiresSession = requiresSession;
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
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
        return this;
    }

    /**
     * Get the deadLetteringOnMessageExpiration property: A value that indicates whether this subscription has dead
     * letter support when a message expires.
     *
     * @return the deadLetteringOnMessageExpiration value.
     */
    public boolean deadLetteringOnMessageExpiration() {
        return this.deadLetteringOnMessageExpiration;
    }

    /**
     * Set the deadLetteringOnMessageExpiration property: A value that indicates whether this subscription has dead
     * letter support when a message expires.
     *
     * @param deadLetteringOnMessageExpiration the deadLetteringOnMessageExpiration value to set.
     *
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setDeadLetteringOnMessageExpiration(boolean deadLetteringOnMessageExpiration) {
        this.deadLetteringOnMessageExpiration = deadLetteringOnMessageExpiration;
        return this;
    }

    /**
     * Get the deadLetteringOnFilterEvaluationExceptions property: A value that indicates whether this subscription has
     * dead letter support when a message expires.
     *
     * @return the deadLetteringOnFilterEvaluationExceptions value.
     */
    public boolean enableDeadLetteringOnFilterEvaluationExceptions() {
        return this.deadLetteringOnFilterEvaluationExceptions;
    }

    /**
     * Set the deadLetteringOnFilterEvaluationExceptions property: A value that indicates whether this subscription has
     * dead letter support when a message expires.
     *
     * @param deadLetteringOnFilterEvaluationExceptions the deadLetteringOnFilterEvaluationExceptions value to set.
     *
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setEnableDeadLetteringOnFilterEvaluationExceptions(
        boolean deadLetteringOnFilterEvaluationExceptions) {
        this.deadLetteringOnFilterEvaluationExceptions = deadLetteringOnFilterEvaluationExceptions;
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
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setMaxDeliveryCount(int maxDeliveryCount) {
        this.maxDeliveryCount = maxDeliveryCount;
        return this;
    }

    /**
     * Get the enableBatchedOperations property: Value that indicates whether server-side batched operations are
     * enabled.
     *
     * @return the enableBatchedOperations value.
     */
    public boolean enableBatchedOperations() {
        return this.enableBatchedOperations;
    }

    /**
     * Set the enableBatchedOperations property: Value that indicates whether server-side batched operations are
     * enabled.
     *
     * @param enableBatchedOperations the enableBatchedOperations value to set.
     *
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setEnableBatchedOperations(boolean enableBatchedOperations) {
        this.enableBatchedOperations = enableBatchedOperations;
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
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setStatus(EntityStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get the forwardTo property: The name of the recipient entity to which all the messages sent to the subscription
     * are forwarded to.
     *
     * @return the forwardTo value.
     */
    public String getForwardTo() {
        return this.forwardTo;
    }

    /**
     * Set the forwardTo property: The name of the recipient entity to which all the messages sent to the subscription
     * are forwarded to.
     *
     * @param forwardTo the forwardTo value to set.
     *
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setForwardTo(String forwardTo) {
        this.forwardTo = forwardTo;
        return this;
    }

    /**
     * Get the userMetadata property: Metadata associated with the subscription. Maximum number of characters is 1024.
     *
     * @return the userMetadata value.
     */
    public String getUserMetadata() {
        return this.userMetadata;
    }

    /**
     * Set the userMetadata property: Metadata associated with the subscription. Maximum number of characters is 1024.
     *
     * @param userMetadata the userMetadata value to set.
     *
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setUserMetadata(String userMetadata) {
        this.userMetadata = userMetadata;
        return this;
    }

    /**
     * Get the forwardDeadLetteredMessagesTo property: The name of the recipient entity to which all the messages sent
     * to the subscription are forwarded to.
     *
     * @return the forwardDeadLetteredMessagesTo value.
     */
    public String getForwardDeadLetteredMessagesTo() {
        return this.forwardDeadLetteredMessagesTo;
    }

    /**
     * Set the forwardDeadLetteredMessagesTo property: The name of the recipient entity to which all the messages sent
     * to the subscription are forwarded to.
     *
     * @param forwardDeadLetteredMessagesTo the forwardDeadLetteredMessagesTo value to set.
     *
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setForwardDeadLetteredMessagesTo(String forwardDeadLetteredMessagesTo) {
        this.forwardDeadLetteredMessagesTo = forwardDeadLetteredMessagesTo;
        return this;
    }

    /**
     * Get the autoDeleteOnIdle property: ISO 8601 timeSpan idle interval after which the subscription is automatically
     * deleted. The minimum duration is 5 minutes.
     *
     * @return the autoDeleteOnIdle value.
     */
    public Duration getAutoDeleteOnIdle() {
        return this.autoDeleteOnIdle;
    }

    /**
     * Set the autoDeleteOnIdle property: ISO 8601 timeSpan idle interval after which the subscription is automatically
     * deleted. The minimum duration is 5 minutes.
     *
     * @param autoDeleteOnIdle the autoDeleteOnIdle value to set.
     *
     * @return the CreateSubscriptionOptions object itself.
     */
    public CreateSubscriptionOptions setAutoDeleteOnIdle(Duration autoDeleteOnIdle) {
        this.autoDeleteOnIdle = autoDeleteOnIdle;
        return this;
    }
}
