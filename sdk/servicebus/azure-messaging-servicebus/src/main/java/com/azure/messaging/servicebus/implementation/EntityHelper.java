// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.models.CreateQueueOptions;
import com.azure.messaging.servicebus.models.QueueProperties;
import com.azure.messaging.servicebus.models.SubscriptionDescription;
import com.azure.messaging.servicebus.models.TopicDescription;

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
    }

    /**
     * Creates a new queue given the options.
     * @param options Options to create queue with.
     * @return A new {@link QueueProperties} with the set options.
     */
    public static QueueProperties createQueue(CreateQueueOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        if (queueAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        return queueAccessor.createQueue(options);
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
     * Sets the subscription name on a {@link SubscriptionDescription}.
     *
     * @param subscription Subscription to set name on.
     * @param subscriptionName Name of the subscription.
     */
    public static void setSubscriptionName(SubscriptionDescription subscription, String subscriptionName) {
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
     * Sets the topic name on a {@link SubscriptionDescription}.
     *
     * @param subscription Subscription to set name on.
     * @param topicName Name of the topic.
     */
    public static void setTopicName(SubscriptionDescription subscription, String topicName) {
        if (subscriptionAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'subscriptionAccessor' should not be null."));
        }

        subscriptionAccessor.setTopicName(subscription, topicName);
    }

    /**
     * Sets the topic name on a {@link TopicDescription}.
     *
     * @param topicDescription Topic to set name on.
     * @param topicName Name of the topic.
     */
    public static void setTopicName(TopicDescription topicDescription, String topicName) {
        if (topicAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'topicAccessor' should not be null."));
        }

        topicAccessor.setName(topicDescription, topicName);
    }

    /**
     * Interface for accessing methods on a queue.
     */
    public interface QueueAccessor {
        /**
         * Sets properties on the {@link QueueProperties} based on the CreateQueueOptions.
         *
         * @param options The create queue options to set.
         * @return A new {@link QueueProperties} with the properties set.
         */
        QueueProperties createQueue(CreateQueueOptions options);

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
         * Sets the topic name on a subscription.
         *
         * @param subscriptionDescription Subscription to set name on.
         * @param topicName Name of the topic.
         */
        void setTopicName(SubscriptionDescription subscriptionDescription, String topicName);

        /**
         * Sets the subscription name on a subscription description.
         *
         * @param subscriptionDescription Subscription to set name on.
         * @param subscriptionName Name of the subscription.
         */
        void setSubscriptionName(SubscriptionDescription subscriptionDescription, String subscriptionName);
    }

    /**
     * Interface for accessing methods on a topic.
     */
    public interface TopicAccessor {
        /**
         * Sets the name on a topicDescription.
         *
         * @param topicDescription Topic to set name.
         * @param name Name of the topic.
         */
        void setName(TopicDescription topicDescription, String name);
    }
}
