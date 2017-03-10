/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.azure.management.servicebus.implementation.SubscriptionResourceInner;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Type representing service bus topic subscription.
 */
@Fluent
public interface Subscription extends
        IndependentChild<ServiceBusManager>,
        Refreshable<Subscription>,
        Updatable<Subscription.Update>,
        HasInner<SubscriptionResourceInner> {
    /**
     * @return the exact time the message was created
     */
    DateTime createdAt();
    /**
     * @return last time there was a receive request to this subscription
     */
    DateTime accessedAt();
    /**
     * @return the exact time the message was updated
     */
    DateTime updatedAt();
    /**
     * @return indicates whether server-side batched operations are enabled
     */
    boolean isBatchedOperationsEnabled();
    /**
     * @return indicates whether this subscription has dead letter support when a message expires
     */
    boolean isDeadLetteringEnabledForExpiredMessages();
    /**
     * @return indicates whether the subscription supports sessions
     */
    boolean isSessionEnabled();
    /**
     * @return the duration of peek-lock which is the amount of time that the message is locked for other receivers
     */
    int lockDurationInSeconds();
    /**
     * @return the idle duration after which the subscription is automatically deleted.
     */
    int deleteOnIdleDurationInMinutes();
    /**
     * @return the duration after which the message expires, starting from when the message is sent to subscription.
     */
    Period defaultMessageTtlDuration();
    /**
     * @return the maximum number of a message delivery before marking it as dead-lettered
     */
    int maxDeliveryCountBeforeDeadLetteringMessage();
    /**
     * @return the number of messages in the subscription
     */
    int messageCount();
    /**
     * @return number of active messages in the subscription
     */
    int activeMessageCount();
    /**
     * @return number of messages in the dead-letter subscription
     */
    int deadLetterMessageCount();
    /**
     * @return number of messages sent to the subscription that are yet to be released
     * for consumption
     */
    int scheduledMessageCount();
    /**
     * @return number of messages transferred into dead letters
     */
    int transferDeadLetterMessageCount();
    /**
     * @return number of messages transferred to another queue, topic, or subscription
     */
    int transferMessageCount();
    /**
     * @return the current status of the subscription
     */
    EntityStatus status();
    /**
     * @return  indicates whether subscription has dead letter support on filter evaluation exceptions
     */
    boolean isDeadLetteringEnabledForFilterEvaluationFailedMessages();
    /**
     * @return entry point to manage authorization rules for the service bus topic subscription
     */
    SubscriptionAuthorizationRules authorizationRules();

    /**
     * The entirety of the subscription definition.
     */
    interface Definition extends
            Subscription.DefinitionStages.Blank,
            Subscription.DefinitionStages.WithGroup,
            Subscription.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of queue definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a subscription definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the subscription definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the subscription definition allowing to define auto delete behaviour.
         */
        interface WithDeleteOnIdle {
            /**
             * The idle interval after which the subscription is automatically deleted.
             * Note: unless it is explicitly overridden the default delete on idle duration
             * is infinite (TimeSpan.Max).
             *
             * @param durationInMinutes idle duration in minutes
             * @return the next stage of subscription definition
             */
            WithCreate withDeleteOnIdleDurationInMinutes(int durationInMinutes);
        }

        /**
         * The stage of the subscription definition allowing to define duration for message lock.
         */
        interface WithMessageLockDuration {
            /**
             * Specifies the amount of time that the message is locked for other receivers.
             * Note: unless it is explicitly overridden the default lock duration is 60 seconds,
             * the maximum allowed value is 300 seconds.
             *
             * @param durationInSeconds duration of a lock in seconds
             * @return the next stage of subscription definition
             */
            WithCreate withMessageLockDurationInSeconds(int durationInSeconds);
        }

        /**
         * The stage of the subscription definition allowing to define default TTL for messages.
         */
        interface WithDefaultMessageTTL {
            /**
             * Specifies the duration after which the message expires.
             * Note: unless it is explicitly overridden the default ttl is infinite (TimeSpan.Max).
             *
             * @param ttl time to live duration
             * @return the next stage of subscription definition
             */
            WithCreate withDefaultMessageTTL(Period ttl);
        }

        /**
         * The stage of the subscription definition allowing to enable session support.
         */
        interface WithSession {
            /**
             * Specifies that session support should be enabled for the subscription.
             *
             * @return the next stage of subscription definition
             */
            WithCreate withSession();
        }

        /**
         * The stage of the subscription definition allowing specify batching behaviour.
         */
        interface WithMessageBatching {
            /**
             * Specifies that the default batching should be disabled on this subscription.
             * With batching service bus can batch multiple message when it write or delete messages
             * from it's internal store.
             *
             * @return the next stage of subscription definition
             */
            WithCreate withoutMessageBatching();
        }

        /**
         * The stage of the subscription definition allowing to specify whether expired message can be moved
         * to secondary dead-letter subscription.
         */
        interface WithExpiredMessageMovedToDeadLetterQueue {
            /**
             * Specifies that expired message must be moved to dead-letter subscription.
             *
             * @return the next stage of subscription definition
             */
            WithCreate withExpiredMessageMovedToDeadLetterQueue();
        }

        /**
         * The stage of the subscription definition allowing to specify maximum delivery count of message before
         * moving it to dead-letter subscription.
         */
        interface WithMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount {
            /**
             * Specifies maximum number of times a message can be delivered. Once this count has exceeded,
             * message will be moved to dead-letter subscription.
             *
             * @param deliveryCount maximum delivery count
             * @return the next stage of subscription definition
             */
            WithCreate withMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount(int deliveryCount);
        }

        /**
         * The stage of the subscription definition allowing to specify whether message those are failed on
         * filter evaluation can be moved to secondary dead-letter subscription.
         */
        interface WithMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException {
            /**
             * Specifies that filter evaluation failed message must be moved to dead-letter subscription.
             *
             * @return the next stage of subscription definition
             */
            WithCreate withMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException();
        }

        /**
         * The stage of the queue definition allowing to add an authorization rule for accessing
         * the subscription.
         */
        interface WithAuthorizationRule {
            /**
             * Creates an authorization rule for the subscription.
             *
             * @param name rule name
             * @param rights rule rights
             * @return next stage of the subscription definition
             */
            WithCreate withNewAuthorizationRule(String name, AccessRights... rights);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
                Creatable<Queue>,
                Subscription.DefinitionStages.WithDeleteOnIdle,
                Subscription.DefinitionStages.WithMessageLockDuration,
                Subscription.DefinitionStages.WithDefaultMessageTTL,
                Subscription.DefinitionStages.WithSession,
                Subscription.DefinitionStages.WithMessageBatching,
                Subscription.DefinitionStages.WithExpiredMessageMovedToDeadLetterQueue,
                Subscription.DefinitionStages.WithMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount,
                Subscription.DefinitionStages.WithMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException,
                Subscription.DefinitionStages.WithAuthorizationRule {
        }
    }

    /**
     * The template for a subscription update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Subscription.UpdateStages.WithDeleteOnIdle,
            Subscription.UpdateStages.WithMessageLockDuration,
            Subscription.UpdateStages.WithDefaultMessageTTL,
            Subscription.UpdateStages.WithSession,
            Subscription.UpdateStages.WithMessageBatching,
            Subscription.UpdateStages.WithExpiredMessageMovedToDeadLetterSubscription,
            Subscription.UpdateStages.WithMessageMovedToDeadLetterQueueOnMaxDeliveryCount,
            Subscription.UpdateStages.WithMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException,
            Subscription.UpdateStages.WithAuthorizationRule {
    }

    /**
     * Grouping of subscription update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the subscription definition allowing to define auto delete behaviour.
         */
        interface WithDeleteOnIdle {
            /**
             * The idle interval after which the subscription is automatically deleted.
             *
             * @param durationInMinutes idle duration in minutes
             * @return the next stage of subscription update
             */
            Update withDeleteOnIdleDurationInMinutes(int durationInMinutes);
        }

        /**
         * The stage of the subscription definition allowing to define duration for message lock.
         */
        interface WithMessageLockDuration {
            /**
             * Specifies the amount of time that the message is locked for other receivers.
             *
             * @param durationInSeconds duration of a lock in seconds
             * @return the next stage of subscription update
             */
            Update withMessageLockDurationInSeconds(int durationInSeconds);
        }

        /**
         * The stage of the subscription definition allowing to define default TTL for messages.
         */
        interface WithDefaultMessageTTL {
            /**
             * Specifies the duration after which the message expires.
             *
             * @param ttl time to live duration
             * @return the next stage of subscription update
             */
            Update withDefaultMessageTTL(Period ttl);
        }

        /**
         * The stage of the subscription definition allowing to enable session support.
         */
        interface WithSession {
            /**
             * Specifies that session support should be enabled for the subscription.
             *
             * @return the next stage of subscription update
             */
            Update withSession();

            /**
             * Specifies that session support should be disabled for the subscription.
             *
             * @return the next stage of subscription update
             */
            Update withoutSession();
        }

        /**
         * The stage of the subscription definition allowing configure message batching behaviour.
         */
        interface WithMessageBatching {
            /**
             * Specifies that service bus can batch multiple message when it write messages to or delete
             * messages from it's internal store. This increases the throughput.
             *
             * @return the next stage of subscription update
             */
            Update withMessageBatching();

            /**
             * Specifies that batching of messages should be disabled when service bus write messages to
             * or delete messages from it's internal store.
             *
             * @return the next stage of subscription update
             */
            Update withoutMessageBatching();
        }

        /**
         * The stage of the queue definition allowing to specify whether expired message can be moved
         * to secondary dead-letter subscription.
         */
        interface WithExpiredMessageMovedToDeadLetterSubscription {
            /**
             * Specifies that expired message must be moved to dead-letter subscription.
             *
             * @return the next stage of subscription update
             */
            Update withExpiredMessageMovedToDeadLetterSubscription();

            /**
             * Specifies that expired message should not be moved to dead-letter subscription.
             *
             * @return the next stage of subscription update
             */
            Update withoutExpiredMessageMovedToDeadLetterSubscription();
        }

        /**
         * The stage of the queue definition allowing to specify maximum delivery count of message before
         * moving it to dead-letter queue.
         */
        interface WithMessageMovedToDeadLetterQueueOnMaxDeliveryCount {
            /**
             * Specifies maximum number of times a message can be delivered. Once this count has exceeded,
             * message will be moved to dead-letter subscription.
             *
             * @param deliveryCount maximum delivery subscription
             * @return the next stage of subscription update
             */
            Update withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount);
        }

        /**
         * The stage of the subscription definition allowing to specify whether message those are failed on
         * filter evaluation can be moved to secondary dead-letter subscription.
         */
        interface WithMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException {
            /**
             * Specifies that filter evaluation failed message must be moved to dead-letter subscription.
             *
             * @return the next stage of subscription update
             */
            Update withMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException();

            /**
             * Specifies that filter evaluation failed message should not be moved to dead-letter subscription.
             *
             * @return the next stage of subscription update
             */
            Update withoutMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException();
        }

        /**
         * The stage of the queue definition allowing to add an authorization rule for accessing
         * the subscription.
         */
        interface WithAuthorizationRule {
            /**
             * Creates an authorization rule for the subscription.
             *
             * @param name rule name
             * @param rights rule rights
             * @return next stage of the subscription update
             */
            Update withNewAuthorizationRule(String name, AccessRights... rights);

            /**
             * Removes an authorization rule for the subscription.
             *
             * @param name rule name
             * @return next stage of the subscription update
             */
            Update withoutNewAuthorizationRule(String name);
        }
    }
}
