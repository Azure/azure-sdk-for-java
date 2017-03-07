/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.servicebus.implementation.QueueResourceInner;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * Type representing service bus queue.
 */
@Fluent
public interface Queue extends
        IndependentChildResource<ServiceBusManager, QueueResourceInner>,
        Refreshable<Queue>,
        Updatable<Queue.Update> {

    /**
     * The duration of a peek-lock; that is, the amount of time that the message is locked for other receivers. The maximum value for LockDuration is 5 minutes; the default value is 1 minute. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String lockDuration();
    /**
     * Last time a message was sent, or the last time there was a receive request to this queue.
     */
    DateTime accessedAt();
    /**
     * the TimeSpan idle interval after which the queue is automatically deleted. The minimum duration is 5 minutes. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String autoDeleteOnIdle();
    /**
     * The exact time the message was created.
     */
    DateTime createdAt();
    /**
     * The default message time to live value. This is the duration after which the message expires, starting from when the message is sent to Service Bus. This is the default value used when TimeToLive is not set on a message itself.
     */
    String defaultMessageTimeToLive();
    /**
     * TimeSpan structure that defines the duration of the duplicate detection history. The default value is 10 minutes. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String duplicateDetectionHistoryTimeWindow();
    /**
     * A value that indicates whether server-side batched operations are enabled.
     */
    boolean enableBatchedOperations();
    /**
     * A value that indicates whether this queue has dead letter support when a message expires.
     */
    boolean deadLetteringOnMessageExpiration();
    /**
     * A value that indicates whether Express Entities are enabled. An express queue holds a message in memory temporarily before writing it to persistent storage.
     */
    boolean enableExpress();
    /**
     * A value that indicates whether the queue is to be partitioned across multiple message brokers.
     */
    boolean enablePartitioning();
    /**
     * The maximum delivery count. A message is automatically deadlettered after this number of deliveries.
     */
    int maxDeliveryCount();
    /**
     * The maximum size of the queue in megabytes, which is the size of memory allocated for the queue.
     */
    int maxSizeInMegabytes();
    /**
     * The number of messages in the queue.
     */
    int messageCount();
    /**
     * Number of active messages in the queue, topic, or subscription.
     */
    int activeMessageCount();
    /**
     * Number of messages that are dead lettered.
     */
    int deadLetterMessageCount();
    /**
     * Number of scheduled messages.
     */
    int scheduledMessageCount();
    /**
     * Number of messages transferred into dead letters.
     */
    int transferDeadLetterMessageCount();
    /**
     * Number of messages transferred to another queue, topic, or subscription.
     */
    int transferMessageCount();
    /**
     * A value indicating if this queue requires duplicate detection.
     */
    boolean requiresDuplicateDetection();
    /**
     * A value that indicates whether the queue supports the concept of sessions.
     */
    boolean requiresSession();
    /**
     * The size of the queue, in bytes.
     */
    int sizeInBytes();
    /**
     * Enumerates the possible values for the status of a messaging entity.
     */
    EntityStatus status();
    /**
     * A value that indicates whether the queue supports ordering.
     */
    boolean supportOrdering();
    /**
     * The exact time the message was updated.
     */
    DateTime updatedAt();

    /**
     * The entirety of the queue definition.
     */
    interface Definition extends
        Queue.DefinitionStages.Blank,
        Queue.DefinitionStages.WithGroup,
        Queue.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of queue definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a queue definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the queue definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
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
            WithCreate withSizeInMB(int sizeInMB);
        }

        /**
         * The stage of the queue definition allowing to specify partitioning behaviour.
         */
        interface WithPartitioning {
            /**
             * Specifies that the default partitioning should be disabled on this queue.
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
             *
             * @param ttl time to live duration
             * @return the next stage of queue definition
             */
            WithCreate withDefaultMessageTTL(Period ttl);
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
             * With batching service bus can batch multiple message when it write or delete messages
             * from it's internal store.
             *
             * @return the next stage of queue definition
             */
            WithCreate withoutMessageBatching();
        }

        // TODO MessageOrdering

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
            WithCreate withDuplicateMessageDetection(Period duplicateDetectionHistoryDuration);
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
             * Creates an authorization rule for the queue.
             *
             * @param name rule name
             * @param rights rule rights
             * @return next stage of the queue definition
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
     * The template for a queue update operation, containing all the settings that can be modified.
     */
    interface Update extends
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

    interface UpdateStages {
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
            Update withSizeInMB(int sizeInMB);
        }

        /**
         * The stage of the queue definition allowing to define auto delete behaviour.
         */
        interface WithDeleteOnIdle {
            /**
             * The idle interval after which the queue is automatically deleted.
             *
             * @param durationInMinutes idle duration in minutes
             * @return the next stage of queue definition
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
             * @return the next stage of queue definition
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
             * @return the next stage of queue definition
             */
            Update withDefaultMessageTTL(Period ttl);
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
            Update withSession();

            /**
             * Specifies that session support should be disabled for the queue.
             *
             * @return the next stage of queue definition
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
             * Specifies that service bus can batch multiple message when it write messages to or delete
             * messages from it's internal store. This increases the throughput.
             *
             * @return the next stage of queue update
             */
            Update withMessageBatching();

            /**
             * Specifies that batching of messages should be disabled when service bus write messages to
             * or delete messages from it's internal store.
             *
             * @return the next stage of queue update
             */
            Update withoutMessageBatching();
        }

        // TODO MessageOrdering

        /**
         * The stage of the queue definition allowing to specify duration of the duplicate message
         * detection history.
         */
        interface WithDuplicateMessageDetection {
            /**
             * Specifies the duration of the duplicate message detection history.
             *
             * @param duration duration of the history
             * @return the next stage of queue definition
             */
            Update withDuplicateMessageDetectionHistoryDuration(Period duration);
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
            Update withExpiredMessageMovedToDeadLetterQueue();

            /**
             * Specifies that expired message should not be moved to dead-letter queue.
             *
             * @return the next stage of queue definition
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
             * @return the next stage of queue definition
             */
            Update withMessageMovedToDeadLetterQueueOnMaxDeliveryCount(int deliveryCount);
        }

        /**
         * The stage of the queue definition allowing to add an authorization rule for accessing
         * the queue.
         */
        interface WithAuthorizationRule {
            /**
             * Creates an authorization rule for the queue.
             *
             * @param name rule name
             * @param rights rule rights
             * @return next stage of the queue definition
             */
            Update withNewAuthorizationRule(String name, AccessRights... rights);
        }
    }

}
