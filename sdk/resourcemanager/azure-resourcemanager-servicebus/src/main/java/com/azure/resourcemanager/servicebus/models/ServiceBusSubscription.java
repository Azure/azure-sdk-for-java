// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.SBSubscriptionInner;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Type representing service bus topic subscription.
 */
@Fluent
public interface ServiceBusSubscription extends
    IndependentChildResource<ServiceBusManager, SBSubscriptionInner>,
    Refreshable<ServiceBusSubscription>,
    Updatable<ServiceBusSubscription.Update> {
    /**
     * @return the exact time the message was created
     */
    OffsetDateTime createdAt();
    /**
     * @return last time there was a receive request to this subscription
     */
    OffsetDateTime accessedAt();
    /**
     * @return the exact time the message was updated
     */
    OffsetDateTime updatedAt();
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
    long lockDurationInSeconds();
    /**
     * @return the idle duration after which the subscription is automatically deleted.
     */
    long deleteOnIdleDurationInMinutes();
    /**
     * @return the duration after which the message expires, starting from when the message is sent to subscription.
     */
    Duration defaultMessageTtlDuration();
    /**
     * @return the maximum number of a message delivery before marking it as dead-lettered
     */
    int maxDeliveryCountBeforeDeadLetteringMessage();
    /**
     * @return the number of messages in the subscription
     */
    long messageCount();
    /**
     * @return number of active messages in the subscription
     */
    long activeMessageCount();
    /**
     * @return number of messages in the dead-letter subscription
     */
    long deadLetterMessageCount();
    /**
     * @return number of messages sent to the subscription that are yet to be released
     * for consumption
     */
    long scheduledMessageCount();
    /**
     * @return number of messages transferred into dead letters
     */
    long transferDeadLetterMessageCount();
    /**
     * @return number of messages transferred to another queue, topic, or subscription
     */
    long transferMessageCount();
    /**
     * @return the current status of the subscription
     */
    EntityStatus status();
    /**
     * @return indicates whether subscription has dead letter support on filter evaluation exceptions
     */
    boolean isDeadLetteringEnabledForFilterEvaluationFailedMessages();

    /**
     * The entirety of the subscription definition.
     */
    interface Definition extends
            ServiceBusSubscription.DefinitionStages.Blank,
            ServiceBusSubscription.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of queue definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a subscription definition.
         */
        interface Blank extends WithCreate {
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
            WithCreate withDefaultMessageTTL(Duration ttl);
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
        interface WithExpiredMessageMovedToDeadLetterSubscription {
            /**
             * Specifies that expired message must be moved to dead-letter subscription.
             *
             * @return the next stage of subscription definition
             */
            WithCreate withExpiredMessageMovedToDeadLetterSubscription();

            /**
             * Specifies that expired message should not be moved to dead-letter subscription.
             *
             * @return the next stage of subscription definition
             */
            WithCreate withoutExpiredMessageMovedToDeadLetterSubscription();
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
            Creatable<ServiceBusSubscription>,
            ServiceBusSubscription.DefinitionStages.WithDeleteOnIdle,
            ServiceBusSubscription.DefinitionStages.WithMessageLockDuration,
            ServiceBusSubscription.DefinitionStages.WithDefaultMessageTTL,
            ServiceBusSubscription.DefinitionStages.WithSession,
            ServiceBusSubscription.DefinitionStages.WithMessageBatching,
            ServiceBusSubscription.DefinitionStages.WithExpiredMessageMovedToDeadLetterSubscription,
            ServiceBusSubscription.DefinitionStages.WithMessageMovedToDeadLetterSubscriptionOnMaxDeliveryCount,
            ServiceBusSubscription.DefinitionStages.
                WithMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException {
        }
    }

    /**
     * The template for a subscription update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<ServiceBusSubscription>,
        ServiceBusSubscription.UpdateStages.WithDeleteOnIdle,
        ServiceBusSubscription.UpdateStages.WithMessageLockDuration,
        ServiceBusSubscription.UpdateStages.WithDefaultMessageTTL,
        ServiceBusSubscription.UpdateStages.WithSession,
        ServiceBusSubscription.UpdateStages.WithMessageBatching,
        ServiceBusSubscription.UpdateStages.WithExpiredMessageMovedToDeadLetterSubscription,
        ServiceBusSubscription.UpdateStages.WithMessageMovedToDeadLetterQueueOnMaxDeliveryCount,
        ServiceBusSubscription.UpdateStages.WithMessageMovedToDeadLetterSubscriptionOnFilterEvaluationException {
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
            Update withDefaultMessageTTL(Duration ttl);
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
         * The stage of the subscription update allowing to specify whether expired message can be moved
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
         * The stage of the subscription definition allowing to specify maximum delivery count of message before
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
