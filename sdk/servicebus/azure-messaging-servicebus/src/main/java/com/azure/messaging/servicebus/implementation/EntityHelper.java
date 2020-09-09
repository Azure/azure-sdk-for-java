// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import com.azure.messaging.servicebus.administration.models.CreateSubscriptionOptions;
import com.azure.messaging.servicebus.administration.models.CreateTopicOptions;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.RuleAction;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.implementation.models.QueueDescription;
import com.azure.messaging.servicebus.implementation.models.RuleActionImpl;
import com.azure.messaging.servicebus.implementation.models.RuleDescription;
import com.azure.messaging.servicebus.implementation.models.RuleFilterImpl;
import com.azure.messaging.servicebus.implementation.models.SubscriptionDescription;
import com.azure.messaging.servicebus.implementation.models.TopicDescription;

import java.util.Objects;

/**
 * Used to access internal methods on {@link QueueProperties}.
 */
public final class EntityHelper {
    private static QueueAccessor queueAccessor;
    private static SubscriptionAccessor subscriptionAccessor;
    private static TopicAccessor topicAccessor;
    private static RuleAccessor ruleAccessor;

    static {
        try {
            Class.forName(QueueProperties.class.getName(), true, QueueProperties.class.getClassLoader());
            Class.forName(SubscriptionProperties.class.getName(), true,
                SubscriptionProperties.class.getClassLoader());
            Class.forName(TopicProperties.class.getName(), true, TopicProperties.class.getClassLoader());
            Class.forName(RuleProperties.class.getName(), true, RuleProperties.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Gets a queue description given the options.
     *
     * @param options The options.
     *
     * @return The corresponding queue.
     */
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

    public static SubscriptionDescription getSubscriptionDescription(CreateSubscriptionOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return new SubscriptionDescription()
            .setAutoDeleteOnIdle(options.getAutoDeleteOnIdle())
            .setDefaultMessageTimeToLive(options.getDefaultMessageTimeToLive())
            .setDeadLetteringOnFilterEvaluationExceptions(options.enableDeadLetteringOnFilterEvaluationExceptions())
            .setDeadLetteringOnMessageExpiration(options.deadLetteringOnMessageExpiration())
            .setEnableBatchedOperations(options.enableBatchedOperations())
            .setForwardTo(options.getForwardTo())
            .setForwardDeadLetteredMessagesTo(options.getForwardDeadLetteredMessagesTo())
            .setLockDuration(options.getLockDuration())
            .setMaxDeliveryCount(options.getMaxDeliveryCount())
            .setRequiresSession(options.requiresSession())
            .setStatus(options.getStatus())
            .setUserMetadata(options.getUserMetadata());
    }

    public static TopicDescription getTopicDescription(CreateTopicOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        return new TopicDescription()
            .setAutoDeleteOnIdle(options.getAutoDeleteOnIdle())
            .setDefaultMessageTimeToLive(options.getDefaultMessageTimeToLive())
            .setDuplicateDetectionHistoryTimeWindow(options.getDuplicateDetectionHistoryTimeWindow())
            .setEnableBatchedOperations(options.enableBatchedOperations())
            .setEnablePartitioning(options.enablePartitioning())
            .setMaxSizeInMegabytes(options.getMaxSizeInMegabytes())
            .setRequiresDuplicateDetection(options.requiresDuplicateDetection())
            .setSupportOrdering(options.isSupportOrdering())
            .setStatus(options.getStatus())
            .setUserMetadata(options.getUserMetadata());
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param description Options to create queue with.
     *
     * @return A new {@link QueueProperties} with the set options.
     */
    public static QueueDescription toImplementation(QueueProperties description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (queueAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        return queueAccessor.toImplementation(description);
    }

    /**
     * Creates a new rule action given an existing rule action.
     *
     * @param properties Rule properties.
     * @return A new instance of {@link RuleActionImpl}.
     */
    public static RuleActionImpl toImplementation(RuleAction properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (ruleAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toImplementation(properties);
    }

    /**
     * Creates a new rule description given an existing rule.
     *
     * @param properties Rule properties.
     * @return A new instance of {@link RuleDescription}.
     */
    public static RuleDescription toImplementation(RuleProperties properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (ruleAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toImplementation(properties);
    }

    /**
     * Creates a new rule filter given an existing rule filter.
     *
     * @param properties Rule filter.
     * @return A new instance of {@link RuleFilter}.
     */
    public static RuleFilterImpl toImplementation(RuleFilter properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (ruleAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toImplementation(properties);
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param description Options to create queue with.
     *
     * @return A new {@link SubscriptionProperties} with the set options.
     */
    public static SubscriptionDescription toImplementation(SubscriptionProperties description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (subscriptionAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'subscriptionAccessor' should not be null."));
        }

        return subscriptionAccessor.toImplementation(description);
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param properties Options to create queue with.
     *
     * @return A new {@link TopicProperties} with the set options.
     */
    public static TopicDescription toImplementation(TopicProperties properties) {
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        if (topicAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'topicAccessor' should not be null."));
        }

        return topicAccessor.toImplementation(properties);
    }

    /**
     * Creates a new queue given the existing queue.
     *
     * @param description Options to create queue with.
     *
     * @return A new {@link QueueProperties} with the set options.
     */
    public static QueueProperties toModel(QueueDescription description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (queueAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'queueAccessor' should not be null."));
        }

        return queueAccessor.toModel(description);
    }

    /**
     * Gets a new rule action based on the existing rule description.
     *
     * @param description The implementation type.
     * @return A new {@link RuleProperties} with the set options.
     */
    public static RuleAction toModel(RuleActionImpl description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (ruleAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toModel(description);
    }

    /**
     * Gets a new rule action based on the existing rule description.
     *
     * @param description The implementation type.
     * @return A new {@link RuleProperties} with the set options.
     */
    public static RuleFilter toModel(RuleFilterImpl description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (ruleAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toModel(description);
    }

    /**
     * Gets a new rule based on the existing rule description.
     *
     * @param description The implementation type.
     * @return A new {@link RuleProperties} with the set options.
     */
    public static RuleProperties toModel(RuleDescription description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (ruleAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'ruleAccessor' should not be null."));
        }

        return ruleAccessor.toModel(description);
    }

    /**
     * Creates a new subscription given the options.
     *
     * @param options Options to create topic with.
     *
     * @return A new {@link SubscriptionProperties} with the set options.
     */
    public static SubscriptionProperties toModel(SubscriptionDescription options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        if (subscriptionAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'subscriptionAccessor' should not be null."));
        }

        return subscriptionAccessor.toModel(options);
    }

    /**
     * Creates a new topic given the options.
     *
     * @param description Options to create topic with.
     *
     * @return A new {@link TopicProperties} with the set options.
     */
    public static TopicProperties toModel(TopicDescription description) {
        Objects.requireNonNull(description, "'description' cannot be null.");

        if (topicAccessor == null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(
                new IllegalStateException("'topicAccessor' should not be null."));
        }

        return topicAccessor.toModel(description);
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
     * Sets the rule accessor.
     *
     * @param accessor The rule accessor.
     */
    public static void setRuleAccessor(RuleAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (EntityHelper.ruleAccessor != null) {
            throw new ClientLogger(EntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'ruleAccessor' is already set."));
        }

        EntityHelper.ruleAccessor = accessor;
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

    /**
     * Interface for accessing methods on a queue.
     */
    public interface QueueAccessor {
        /**
         * Creates a new queue from the given {@code queueDescription}.
         *
         * @param queueDescription Queue description to use.
         *
         * @return A new queue with the properties set.
         */
        QueueDescription toImplementation(QueueProperties queueDescription);

        /**
         * Creates a new queue from the given {@code queueDescription}.
         *
         * @param queueDescription Queue description to use.
         *
         * @return A new queue with the properties set.
         */
        QueueProperties toModel(QueueDescription queueDescription);

        /**
         * Sets the name on a queueDescription.
         *
         * @param queueProperties Queue to set name on.
         * @param name Name of the queue.
         */
        void setName(QueueProperties queueProperties, String name);
    }

    /**
     * Interface for accessing methods on a rule.
     */
    public interface RuleAccessor {
        RuleProperties toModel(RuleDescription ruleDescription);

        RuleAction toModel(RuleActionImpl implementation);

        RuleFilter toModel(RuleFilterImpl implementation);

        RuleDescription toImplementation(RuleProperties ruleProperties);

        RuleActionImpl toImplementation(RuleAction model);

        RuleFilterImpl toImplementation(RuleFilter model);
    }

    /**
     * Interface for accessing methods on a subscription.
     */
    public interface SubscriptionAccessor {
        /**
         * Creates a model subscription with the given implementation.
         *
         * @param subscription Options used to create subscription.
         *
         * @return A new subscription.
         */
        SubscriptionProperties toModel(SubscriptionDescription subscription);

        /**
         * Creates the implementation subscription with the given subscription.
         *
         * @param subscription Options used to create subscription.
         *
         * @return A new subscription.
         */
        SubscriptionDescription toImplementation(SubscriptionProperties subscription);

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
         * @param topic The implementation topic.
         *
         * @return A new topic with the properties set.
         */
        TopicProperties toModel(TopicDescription topic);

        /**
         * Sets properties on the TopicProperties based on the CreateTopicOptions.
         *
         * @param topic The model topic.
         *
         * @return A new topic with the properties set.
         */
        TopicDescription toImplementation(TopicProperties topic);

        /**
         * Sets the name on a topicDescription.
         *
         * @param topicProperties Topic to set name.
         * @param name Name of the topic.
         */
        void setName(TopicProperties topicProperties, String name);
    }
}
