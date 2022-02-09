// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.servicebus.fluent.models.SBQueueInner;
import com.azure.resourcemanager.servicebus.ServiceBusManager;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Type representing Service Bus queue.
 */
@Fluent
public interface Queue extends
    IndependentChildResource<ServiceBusManager, SBQueueInner>,
    Refreshable<Queue>,
    Updatable<Queue.Update> {
    /**
     * @return the exact time the queue was created
     */
    OffsetDateTime createdAt();
    /**
     * @return last time a message was sent, or the last time there was a receive request to this queue
     */
    OffsetDateTime accessedAt();
    /**
     * @return the exact time the queue was updated
     */
    OffsetDateTime updatedAt();
    /**
     * @return the maximum size of memory allocated for the queue in megabytes
     */
    long maxSizeInMB();
    /**
     * @return current size of the queue, in bytes
     */
    long currentSizeInBytes();
    /**
     * @return indicates whether server-side batched operations are enabled
     */
    boolean isBatchedOperationsEnabled();
    /**
     * @return indicates whether this queue has dead letter support when a message expires
     */
    boolean isDeadLetteringEnabledForExpiredMessages();
    /**
     * @return indicates whether express entities are enabled
     */
    boolean isExpressEnabled();
    /**
     * @return indicates whether the queue is to be partitioned across multiple message brokers
     */
    boolean isPartitioningEnabled();
    /**
     * @return indicates whether the queue supports sessions
     */
    boolean isSessionEnabled();
    /**
     * @return indicates if this queue requires duplicate detection
     */
    boolean isDuplicateDetectionEnabled();
    /**
     * @return the duration of peek-lock which is the amount of time that the message is locked for other receivers
     */
    long lockDurationInSeconds();
    /**
     * @return the idle duration after which the queue is automatically deleted
     */
    long deleteOnIdleDurationInMinutes();
    /**
     * @return the duration after which the message expires, starting from when the message is sent to queue
     */
    Duration defaultMessageTtlDuration();
    /**
     * @return the duration of the duplicate detection history
     */
    Duration duplicateMessageDetectionHistoryDuration();
    /**
     * @return the maximum number of a message delivery before marking it as dead-lettered
     */
    int maxDeliveryCountBeforeDeadLetteringMessage();
    /**
     * @return the number of messages in the queue
     */
    long messageCount();
    /**
     * @return number of active messages in the queue
     */
    long activeMessageCount();
    /**
     * @return number of messages in the dead-letter queue
     */
    long deadLetterMessageCount();
    /**
     * @return number of messages sent to the queue that are yet to be released
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
     * @return the current status of the queue
     */
    EntityStatus status();
    /**
     * @return entry point to manage authorization rules for the Service Bus queue
     */
    QueueAuthorizationRules authorizationRules();

    /**
     * The entirety of the Service Bus queue definition.
     */
    interface Definition extends
            Queue.DefinitionStages.Blank,
            Queue.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Service Bus queue definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a queue definition.
         */
        interface Blank extends WithCreate {
        }

        /**
         * The stage of the queue definition allowing to specify size.
         */
        interface WithSize {
            /**
             * Specifies the maximum size of memory allocated for the queue.
             *
             * @param sizeInMB size in MB
             * @return the next stage of queue definition
             */
            WithCreate withSizeInMB(long sizeInMB);
        }

        /**
         * The stage of the queue definition allowing to specify partitioning behaviour.
         */
        interface WithPartitioning {
            /**
             * Specifies that partitioning should be enabled on this queue.
             *
             * @return the next stage of queue definition
             */
            WithCreate withPartitioning();

            /**
             * Specifies that the default partitioning should be disabled on this queue.
             * Note: if the parent Service Bus is Premium SKU then partition cannot be
             * disabled
             *
             * @return the next stage of queue definition
             */
            WithCreate withoutPartitioning();
        }

        /**
         * The stage of the queue definition allowing to define auto delete behaviour.
         */
        interface WithDeleteOnIdle {
            /**
             * The idle interval after which the queue is automatically deleted.
             * Note: unless it is explicitly overridden the default delete on idle duration
             * is infinite (TimeSpan.Max).
             *
             * @param durationInMinutes idle duration in minutes
             * @return the next stage of queue definition
             */
            WithCreate withDeleteOnIdleDurationInMinutes(int durationInMinutes);
        }

        /**
         * The stage of the queue definition allowing to define duration for message lock.
         */
        interface WithMessageLockDuration {
            /**
             * Specifies the amount of time that the message is locked for other receivers.
             * Note: unless it is explicitly overridden the default lock duration is 60 seconds,
             * the maximum allowed value is 300 seconds.
             *
             * @param durationInSeconds duration of a lock in seconds
             * @return the next stage of queue definition
             */
            WithCreate withMessageLockDurationInSeconds(int durationInSeconds);
        }

        /**
         * The stage of the queue definition allowing to define default TTL for messages.
         */
        interface WithDefaultMessageTTL {
            /**
             * Specifies the duration after which the message expires.
             * Note: unless it is explicitly overridden the default ttl is infinite (TimeSpan.Max).
             *
             * @param ttl time to live duration
             * @return the next stage of queue definition
             */
            WithCreate withDefaultMessageTTL(Duration ttl);
        }

        /**
         * The stage of the queue definition allowing to enable session support.
         */
        interface WithSession {
            /**
             * Specifies that session support should be enabled for the queue.
             *
             * @return the next stage of queue definition
             */
            WithCreate withSession();
        }

        /**
         * The stage of the queue definition allowing to mark messages as express messages.
         */
        interface WithExpressMessage {
            /**
             * Specifies that messages in this queue are express hence they can be cached in memory
             * for some time before storing it in messaging store.
             * Note: By default queue is not express.
             *
             * @return the next stage of queue definition
             */
            WithCreate withExpressMessage();
        }

        /**
         * The stage of the queue definition allowing specify batching behaviour.
         */
        interface WithMessageBatching {
            /**
             * Specifies that the default batching should be disabled on this queue.
             * With batching Service Bus can batch multiple message when it write or delete messages
             * from it's internal store.
             *
             * @return the next stage of queue definition
             */
            WithCreate withoutMessageBatching();
        }

        /**
         * The stage of the queue definition allowing to specify duration of the duplicate message
         * detection history.
         */
        interface WithDuplicateMessageDetection {
            /**
             * Specifies the duration of the duplicate message detection history.
             *
             * @param duplicateDetectionHistoryDuration duration of the history
             * @return the next stage of queue definition
             */
            WithCreate withDuplicateMessageDetection(Duration duplicateDetectionHistoryDuration);
        }

        /**
         * The stage of the queue definition allowing to specify whether expired message can be moved
         * to secondary dead-letter queue.
         */
        interface WithExpiredMessageMovedToDeadLetterQueue {
            /**
             * Specifies that expired message must be moved to dead-letter queue.
             *
             * @return the next stage of queue definition
             */
            WithCreate withExpiredMessageMovedToDeadLetterQueue();
        }

        /**
         * The stage of the queue definition allowing to specify maximum delivery count of message before
         * moving it to dead-letter queue.
         */
        interface WithMessageMovedToDeadLetterQueueOnMaxDeliveryCount {
            /**
             * Specifies maximum number of times a message can be delivered. Once this count has exceeded,
             * message will be moved to dead-letter queue.
             *
             * @param deliveryCount maximum delivery count
             * @return the next stage of queue definition
             */
            WithCreate withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount);
        }

        /**
         * The stage of the queue definition allowing to add an authorization rule for accessing
         * the queue.
         */
        interface WithAuthorizationRule {
            /**
             * Creates a send authorization rule for the queue.
             *
             * @param name rule name
             * @return next stage of the queue definition
             */
            WithCreate withNewSendRule(String name);
            /**
             * Creates a listen authorization rule for the queue.
             *
             * @param name rule name
             * @return next stage of the queue definition
             */
            WithCreate withNewListenRule(String name);
            /**
             * Creates a manage authorization rule for the queue.
             *
             * @param name rule name
             * @return next stage of the queue definition
             */
            WithCreate withNewManageRule(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<Queue>,
            Queue.DefinitionStages.WithSize,
            Queue.DefinitionStages.WithPartitioning,
            Queue.DefinitionStages.WithDeleteOnIdle,
            Queue.DefinitionStages.WithMessageLockDuration,
            Queue.DefinitionStages.WithDefaultMessageTTL,
            Queue.DefinitionStages.WithSession,
            Queue.DefinitionStages.WithExpressMessage,
            Queue.DefinitionStages.WithMessageBatching,
            Queue.DefinitionStages.WithDuplicateMessageDetection,
            Queue.DefinitionStages.WithExpiredMessageMovedToDeadLetterQueue,
            Queue.DefinitionStages.WithMessageMovedToDeadLetterQueueOnMaxDeliveryCount,
            Queue.DefinitionStages.WithAuthorizationRule {
        }
    }

    /**
     * The template for Service Bus queue update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<Queue>,
        Queue.UpdateStages.WithSize,
        Queue.UpdateStages.WithDeleteOnIdle,
        Queue.UpdateStages.WithMessageLockDuration,
        Queue.UpdateStages.WithDefaultMessageTTL,
        Queue.UpdateStages.WithSession,
        Queue.UpdateStages.WithExpressMessage,
        Queue.UpdateStages.WithMessageBatching,
        Queue.UpdateStages.WithDuplicateMessageDetection,
        Queue.UpdateStages.WithExpiredMessageMovedToDeadLetterQueue,
        Queue.UpdateStages.WithMessageMovedToDeadLetterQueueOnMaxDeliveryCount,
        Queue.UpdateStages.WithAuthorizationRule {
    }

    /**
     * Grouping of Service Bus queue update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the queue definition allowing to specify size.
         */
        interface WithSize {
            /**
             * Specifies the maximum size of memory allocated for the queue.
             *
             * @param sizeInMB size in MB
             * @return the next stage of queue update
             */
            Update withSizeInMB(long sizeInMB);
        }

        /**
         * The stage of the queue definition allowing to define auto delete behaviour.
         */
        interface WithDeleteOnIdle {
            /**
             * The idle interval after which the queue is automatically deleted.
             *
             * @param durationInMinutes idle duration in minutes
             * @return the next stage of queue update
             */
            Update withDeleteOnIdleDurationInMinutes(int durationInMinutes);
        }

        /**
         * The stage of the queue definition allowing to define duration for message lock.
         */
        interface WithMessageLockDuration {
            /**
             * Specifies the amount of time that the message is locked for other receivers.
             *
             * @param durationInSeconds duration of a lock in seconds
             * @return the next stage of queue update
             */
            Update withMessageLockDurationInSeconds(int durationInSeconds);
        }

        /**
         * The stage of the queue definition allowing to define default TTL for messages.
         */
        interface WithDefaultMessageTTL {
            /**
             * Specifies the duration after which the message expires.
             *
             * @param ttl time to live duration
             * @return the next stage of queue update
             */
            Update withDefaultMessageTTL(Duration ttl);
        }

        /**
         * The stage of the queue definition allowing to enable session support.
         */
        interface WithSession {
            /**
             * Specifies that session support should be enabled for the queue.
             *
             * @return the next stage of queue update
             */
            Update withSession();

            /**
             * Specifies that session support should be disabled for the queue.
             *
             * @return the next stage of queue update
             */
            Update withoutSession();
        }

        /**
         * The stage of the queue definition allowing to mark it as either holding regular or express
         * messages.
         */
        interface WithExpressMessage {
            /**
             * Specifies that messages in this queue are express hence they can be cached in memory
             * for some time before storing it in messaging store.
             *
             * @return the next stage of queue update
             */
            Update withExpressMessage();

            /**
             * Specifies that messages in this queue are not express hence they should be cached in memory.
             *
             * @return the next stage of queue update
             */
            Update withoutExpressMessage();
        }

        /**
         * The stage of the queue definition allowing configure message batching behaviour.
         */
        interface WithMessageBatching {
            /**
             * Specifies that Service Bus can batch multiple message when it write messages to or delete
             * messages from it's internal store. This increases the throughput.
             *
             * @return the next stage of queue update
             */
            Update withMessageBatching();

            /**
             * Specifies that batching of messages should be disabled when Service Bus write messages to
             * or delete messages from it's internal store.
             *
             * @return the next stage of queue update
             */
            Update withoutMessageBatching();
        }

        /**
         * The stage of the queue definition allowing to specify duration of the duplicate message
         * detection history.
         */
        interface WithDuplicateMessageDetection {
            /**
             * Specifies the duration of the duplicate message detection history.
             *
             * @param duration duration of the history
             * @return the next stage of queue update
             */
            Update withDuplicateMessageDetectionHistoryDuration(Duration duration);

            /**
             * Specifies that duplicate message detection needs to be disabled.
             *
             * @return the next stage of queue update
             */
            Update withoutDuplicateMessageDetection();
        }

        /**
         * The stage of the queue definition allowing to specify whether expired message can be moved
         * to secondary dead-letter queue.
         */
        interface WithExpiredMessageMovedToDeadLetterQueue {
            /**
             * Specifies that expired message must be moved to dead-letter queue.
             *
             * @return the next stage of queue update
             */
            Update withExpiredMessageMovedToDeadLetterQueue();

            /**
             * Specifies that expired message should not be moved to dead-letter queue.
             *
             * @return the next stage of queue update
             */
            Update withoutExpiredMessageMovedToDeadLetterQueue();
        }

        /**
         * The stage of the queue definition allowing to specify maximum delivery count of message before
         * moving it to dead-letter queue.
         */
        interface WithMessageMovedToDeadLetterQueueOnMaxDeliveryCount {
            /**
             * Specifies maximum number of times a message can be delivered. Once this count has exceeded,
             * message will be moved to dead-letter queue.
             *
             * @param deliveryCount maximum delivery count
             * @return the next stage of queue update
             */
            Update withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount);
        }

        /**
         * The stage of the queue definition allowing to add an authorization rule for accessing
         * the queue.
         */
        interface WithAuthorizationRule {
            /**
             * Creates a send authorization rule for the queue.
             *
             * @param name rule name
             * @return next stage of the queue update
             */
            Update withNewSendRule(String name);
            /**
             * Creates a listen authorization rule for the queue.
             *
             * @param name rule name
             * @return next stage of the queue update
             */
            Update withNewListenRule(String name);
            /**
             * Creates a manage authorization rule for the queue.
             *
             * @param name rule name
             * @return next stage of the queue update
             */
            Update withNewManageRule(String name);

            /**
             * Removes an authorization rule for the queue.
             *
             * @param name rule name
             * @return next stage of the queue update
             */
            Update withoutAuthorizationRule(String name);
        }
    }
}
