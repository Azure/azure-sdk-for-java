// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.models.CreateQueueOptions;
import com.azure.messaging.servicebus.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.models.CreateTopicOptions;
import com.azure.messaging.servicebus.models.QueueProperties;
import com.azure.messaging.servicebus.models.SubscriptionProperties;
import com.azure.messaging.servicebus.models.TopicProperties;

import java.util.Objects;

/**
 * Used to access internal methods on {@link QueueProperties}.
 */
public final class EntityHelper {
    private static QueueAccessor queueAccessor;
    private static SubscriptionAccessor subscriptionAccessor;
    private static TopicAccessor topicAccessor;

    static {
        try {
            Class.forName(QueueProperties.class.getName(), true, QueueProperties.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(e));
        }

        try {
            Class.forName(TopicDescription.class.getName(), true, TopicDescription.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(e));
        }

        try {
            Class.forName(SubscriptionDescription.class.getName(), true,
                SubscriptionDescription.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(e));
        }

        try {
            Class.forName(SubscriptionProperties.class.getName(), true,
                SubscriptionProperties.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Creates a new subscription given the options.
     *
     * @param options Options to create topic with.
     *
     * @return A new {@link SubscriptionProperties} with the set options.
     */
    public static SubscriptionProperties createSubscription(CreateSubscriptionOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        if (subscriptionAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'subscriptionAccessor' should not be null."));
        }

        return subscriptionAccessor.createSubscription(options);
    }

    /**
     * Creates a new topic given the options.
     *
     * @param options Options to create topic with.
     *
     * @return A new {@link TopicProperties} with the set options.
     */
    public static TopicProperties createTopic(CreateTopicOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        if (topicAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'topicAccessor' should not be null."));
        }

        return topicAccessor.createTopic(options);
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param description Options to create queue with.
     * @return A new {@link QueueProperties} with the set options.
     */
    public static QueueDescription toImplementation(QueueProperties description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (queueAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        return queueAccessor.createQueue(description);
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param description Options to create queue with.
     * @return A new {@link QueueProperties} with the set options.
     */
    public static QueueProperties toModel(QueueDescription description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (queueAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        return queueAccessor.createQueue(description);
    }

    /**
     * Sets the queue accessor.
     *
     * @param accessor The queue accessor to set on the queue helper.
     */
    public static void setQueueAccessor(QueueAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (EntityHelper.queueAccessor != null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'accessor' is already set."));
        }

        EntityHelper.queueAccessor = accessor;
    }

    /**
     * Sets the queue name on a {@link QueueProperties}.
     *
     * @param queueProperties Queue to set name on.
     * @param name Name of the queue.
     */
    public static void setQueueName(QueueProperties queueProperties, String name) {
        if (queueAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        queueAccessor.setName(queueProperties, name);
    }

    /**
     * Sets the subscription accessor.
     *
     * @param accessor The subscription accessor.
     */
    public static void setSubscriptionAccessor(SubscriptionAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (EntityHelper.subscriptionAccessor != null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'subscriptionAccessor' is already set."));
        }

        EntityHelper.subscriptionAccessor = accessor;
    }

    /**
     * Sets the subscription name on a {@link SubscriptionProperties}.
     *
     * @param subscription Subscription to set name on.
     * @param subscriptionName Name of the subscription.
     */
    public static void setSubscriptionName(SubscriptionProperties subscription, String subscriptionName) {
        if (subscriptionAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'subscriptionAccessor' should not be null."));
        }

        subscriptionAccessor.setSubscriptionName(subscription, subscriptionName);
    }

    /**
     * Sets the queue accessor.
     *
     * @param accessor The queue accessor to set on the queue helper.
     */
    public static void setTopicAccessor(TopicAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (EntityHelper.topicAccessor != null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'topicAccessor' is already set."));
        }

        EntityHelper.topicAccessor = accessor;
    }

    /**
     * Sets the topic name on a {@link SubscriptionProperties}.
     *
     * @param subscription Subscription to set name on.
     * @param topicName Name of the topic.
     */
    public static void setTopicName(SubscriptionProperties subscription, String topicName) {
        if (subscriptionAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'subscriptionAccessor' should not be null."));
        }

        subscriptionAccessor.setTopicName(subscription, topicName);
    }

    /**
     * Sets the topic name on a {@link TopicProperties}.
     *
     * @param topicProperties Topic to set name on.
     * @param topicName Name of the topic.
     */
    public static void setTopicName(TopicProperties topicProperties, String topicName) {
        if (topicAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'topicAccessor' should not be null."));
        }

        topicAccessor.setName(topicProperties, topicName);
    }

    public static QueueDescription getQueueDescription(CreateQueueOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return new QueueDescription()
            .setAutoDeleteOnIdle(options.getAutoDeleteOnIdle())
            .setDefaultMessageTimeToLive(options.getDefaultMessageTimeToLive())
            .setDeadLetteringOnMessageExpiration(options.deadLetteringOnMessageExpiration())
            .setDuplicateDetectionHistoryTimeWindow(options.getDuplicateDetectionHistoryTimeWindow())
            .setEnableBatchedOperations(options.enableBatchedOperations())
            .setEnablePartitioning(options.enablePartitioning())
            .setForwardTo(options.getForwardTo())
            .setForwardDeadLetteredMessagesTo(options.getForwardDeadLetteredMessagesTo())
            .setLockDuration(options.getLockDuration())
            .setMaxDeliveryCount(options.getMaxDeliveryCount())
            .setMaxSizeInMegabytes(options.getMaxSizeInMegabytes())
            .setRequiresDuplicateDetection(options.requiresDuplicateDetection())
            .setRequiresSession(options.requiresSession())
            .setStatus(options.getStatus())
            .setUserMetadata(options.getUserMetadata());
    }

    /**
     * Interface for accessing methods on a queue.
     */
    public interface QueueAccessor {
        /**
         * Creates a new queue from the given {@code queueDescription}.
         *
         * @param queueDescription Queue description to use.
         * @return A new queue with the properties set.
         */
        QueueDescription createQueue(QueueProperties queueDescription);

        /**
         * Creates a new queue from the given {@code queueDescription}.
         *
         * @param queueDescription Queue description to use.
         * @return A new queue with the properties set.
         */
        QueueProperties createQueue(QueueDescription queueDescription);

        /**
         * Sets the name on a queueDescription.
         *
         * @param queueProperties Queue to set name on.
         * @param name Name of the queue.
         */
        void setName(QueueProperties queueProperties, String name);
    }

    /**
     * Interface for accessing methods on a subscription.
     */
    public interface SubscriptionAccessor {
        /**
         * Creates a subscription with the given options.
         *
         * @param options Options used to create subscription.
         * @return A new subscription.
         */
        SubscriptionProperties createSubscription(CreateSubscriptionOptions options);

        /**
         * Sets the topic name on a subscription.
         *
         * @param subscriptionProperties Subscription to set name on.
         * @param topicName Name of the topic.
         */
        void setTopicName(SubscriptionProperties subscriptionProperties, String topicName);

        /**
         * Sets the subscription name on a subscription description.
         *
         * @param subscriptionProperties Subscription to set name on.
         * @param subscriptionName Name of the subscription.
         */
        void setSubscriptionName(SubscriptionProperties subscriptionProperties, String subscriptionName);
    }

    /**
     * Interface for accessing methods on a topic.
     */
    public interface TopicAccessor {
        /**
         * Sets properties on the TopicProperties based on the CreateTopicOptions.
         *
         * @param options The options to set.
         *
         * @return A new topic with the properties set.
         */
        TopicProperties createTopic(CreateTopicOptions options);

        /**
         * Sets the name on a topicDescription.
         *
         * @param topicProperties Topic to set name.
         * @param name Name of the topic.
         */
        void setName(TopicProperties topicProperties, String name);
    }
}
