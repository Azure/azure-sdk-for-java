// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.servicebus.fluent.models.SBTopicInner;
import com.azure.resourcemanager.servicebus.ServiceBusManager;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Type representing Service Bus topic.
 */
@Fluent
public interface Topic extends
    IndependentChildResource<ServiceBusManager, SBTopicInner>,
    Refreshable<Topic>,
    Updatable<Topic.Update>,
        HasInnerModel<SBTopicInner> {

    /**
     * @return the exact time the topic was created
     */
    OffsetDateTime createdAt();
    /**
     * @return last time a message was sent, or the last time there was a receive request to this topic
     */
    OffsetDateTime accessedAt();
    /**
     * @return the exact time the topic was updated
     */
    OffsetDateTime updatedAt();
    /**
     * @return the maximum size of memory allocated for the topic in megabytes
     */
    long maxSizeInMB();
    /**
     * @return current size of the topic, in bytes
     */
    long currentSizeInBytes();
    /**
     * @return indicates whether server-side batched operations are enabled
     */
    boolean isBatchedOperationsEnabled();
    /**
     * @return indicates whether express entities are enabled
     */
    boolean isExpressEnabled();
    /**
     * @return indicates whether the topic is to be partitioned across multiple message brokers
     */
    boolean isPartitioningEnabled();
    /**
     * @return indicates if this topic requires duplicate detection
     */
    boolean isDuplicateDetectionEnabled();
    /**
     * @return the idle duration after which the topic is automatically deleted
     */
    long deleteOnIdleDurationInMinutes();
    /**
     * @return the duration after which the message expires, starting from when the message is sent to topic
     */
    Duration defaultMessageTtlDuration();
    /**
     * @return the duration of the duplicate detection history
     */
    Duration duplicateMessageDetectionHistoryDuration();
    /**
     * @return number of active messages in the topic
     */
    long activeMessageCount();
    /**
     * @return number of messages in the dead-letter topic
     */
    long deadLetterMessageCount();
    /**
     * @return number of messages sent to the topic that are yet to be released
     * for consumption
     */
    long scheduledMessageCount();
    /**
     * @return number of messages transferred into dead letters
     */
    long transferDeadLetterMessageCount();
    /**
     * @return number of messages transferred to another topic, topic, or subscription
     */
    long transferMessageCount();
    /**
     * @return number of subscriptions for the topic
     */
    int subscriptionCount();
    /**
     * @return the current status of the topic
     */
    EntityStatus status();
    /**
     * @return entry point to manage subscriptions associated with the topic
     */
    ServiceBusSubscriptions subscriptions();
    /**
     * @return entry point to manage authorization rules for the Service Bus topic
     */
    TopicAuthorizationRules authorizationRules();

    /**
     * The entirety of the Service Bus topic definition.
     */
    interface Definition extends
            Topic.DefinitionStages.Blank,
            Topic.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of Service Bus topic definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a topic definition.
         */
        interface Blank extends WithCreate {
        }

        /**
         * The stage of the topic definition allowing to specify size.
         */
        interface WithSize {
            /**
             * Specifies the maximum size of memory allocated for the topic.
             *
             * @param sizeInMB size in MB
             * @return the next stage of topic definition
             */
            WithCreate withSizeInMB(long sizeInMB);
        }

        /**
         * The stage of the topic definition allowing to specify partitioning behaviour.
         */
        interface WithPartitioning {
            /**
             * Specifies that partitioning should be enabled on this topic.
             *
             * @return the next stage of topic definition
             */
            WithCreate withPartitioning();

            /**
             * Specifies that the default partitioning should be disabled on this topic.
             * Note: if the parent Service Bus is Premium SKU then partition cannot be
             * disabled
             *
             * @return the next stage of topic definition
             */
            WithCreate withoutPartitioning();
        }

        /**
         * The stage of the topic definition allowing to define auto delete behaviour.
         */
        interface WithDeleteOnIdle {
            /**
             * The idle interval after which the topic is automatically deleted.
             * Note: unless it is explicitly overridden the default delete on idle duration
             * is infinite (TimeSpan.Max).
             *
             * @param durationInMinutes idle duration in minutes
             * @return the next stage of topic definition
             */
            WithCreate withDeleteOnIdleDurationInMinutes(int durationInMinutes);
        }

        /**
         * The stage of the topic definition allowing to define default TTL for messages.
         */
        interface WithDefaultMessageTTL {
            /**
             * Specifies the duration after which the message expires.
             * Note: unless it is explicitly overridden the default ttl is infinite (TimeSpan.Max).
             *
             * @param ttl time to live duration
             * @return the next stage of topic definition
             */
            WithCreate withDefaultMessageTTL(Duration ttl);
        }

        /**
         * The stage of the topic definition allowing to mark messages as express messages.
         */
        interface WithExpressMessage {
            /**
             * Specifies that messages in this topic are express hence they can be cached in memory
             * for some time before storing it in messaging store.
             * Note: By default topic is not express.
             *
             * @return the next stage of topic definition
             */
            WithCreate withExpressMessage();
        }

        /**
         * The stage of the topic definition allowing specify batching behaviour.
         */
        interface WithMessageBatching {
            /**
             * Specifies that the default batching should be disabled on this topic.
             * With batching Service Bus can batch multiple message when it write or delete messages
             * from it's internal store.
             *
             * @return the next stage of topic definition
             */
            WithCreate withoutMessageBatching();
        }

        /**
         * The stage of the topic definition allowing to specify duration of the duplicate message
         * detection history.
         */
        interface WithDuplicateMessageDetection {
            /**
             * Specifies the duration of the duplicate message detection history.
             *
             * @param duplicateDetectionHistoryDuration duration of the history
             * @return the next stage of topic definition
             */
            WithCreate withDuplicateMessageDetection(Duration duplicateDetectionHistoryDuration);
        }

        /**
         * The stage of the Service Bus namespace update allowing to manage subscriptions for the topic.
         */
        interface WithSubscription {
            /**
             * Creates a subscription entity for the Service Bus topic.
             *
             * @param name queue name
             * @return the next stage of topic definition
             */
            WithCreate withNewSubscription(String name);
        }

        /**
         * The stage of the topic definition allowing to add an authorization rule for accessing
         * the topic.
         */
        interface WithAuthorizationRule {
            /**
             * Creates a send authorization rule for the topic.
             *
             * @param name rule name
             * @return next stage of the topic definition
             */
            WithCreate withNewSendRule(String name);
            /**
             * Creates a listen authorization rule for the topic.
             *
             * @param name rule name
             * @return next stage of the topic definition
             */
            WithCreate withNewListenRule(String name);
            /**
             * Creates a manage authorization rule for the topic.
             *
             * @param name rule name
             * @return next stage of the topic definition
             */
            WithCreate withNewManageRule(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<Topic>,
            Topic.DefinitionStages.WithSize,
            Topic.DefinitionStages.WithPartitioning,
            Topic.DefinitionStages.WithDeleteOnIdle,
            Topic.DefinitionStages.WithDefaultMessageTTL,
            Topic.DefinitionStages.WithExpressMessage,
            Topic.DefinitionStages.WithMessageBatching,
            Topic.DefinitionStages.WithDuplicateMessageDetection,
            Topic.DefinitionStages.WithSubscription,
            Topic.DefinitionStages.WithAuthorizationRule {
        }
    }

    /**
     * The template for a Service Bus topic update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<Topic>,
        Topic.UpdateStages.WithSize,
        Topic.UpdateStages.WithDeleteOnIdle,
        Topic.UpdateStages.WithDefaultMessageTTL,
        Topic.UpdateStages.WithExpressMessage,
        Topic.UpdateStages.WithMessageBatching,
        Topic.UpdateStages.WithDuplicateMessageDetection,
        Topic.UpdateStages.WithSubscription,
        Topic.UpdateStages.WithAuthorizationRule {
    }

    /**
     * Grouping of Service Bus topic update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the topic definition allowing to specify size.
         */
        interface WithSize {
            /**
             * Specifies the maximum size of memory allocated for the topic.
             *
             * @param sizeInMB size in MB
             * @return the next stage of topic update
             */
            Update withSizeInMB(long sizeInMB);
        }

        /**
         * The stage of the topic definition allowing to define auto delete behaviour.
         */
        interface WithDeleteOnIdle {
            /**
             * The idle interval after which the topic is automatically deleted.
             *
             * @param durationInMinutes idle duration in minutes
             * @return the next stage of topic update
             */
            Update withDeleteOnIdleDurationInMinutes(int durationInMinutes);
        }

        /**
         * The stage of the topic definition allowing to define default TTL for messages.
         */
        interface WithDefaultMessageTTL {
            /**
             * Specifies the duration after which the message expires.
             *
             * @param ttl time to live duration
             * @return the next stage of topic update
             */
            Update withDefaultMessageTTL(Duration ttl);
        }

        /**
         * The stage of the topic definition allowing to mark it as either holding regular or express
         * messages.
         */
        interface WithExpressMessage {
            /**
             * Specifies that messages in this topic are express hence they can be cached in memory
             * for some time before storing it in messaging store.
             *
             * @return the next stage of topic update
             */
            Update withExpressMessage();

            /**
             * Specifies that messages in this topic are not express hence they should be cached in memory.
             *
             * @return the next stage of topic update
             */
            Update withoutExpressMessage();
        }

        /**
         * The stage of the topic definition allowing configure message batching behaviour.
         */
        interface WithMessageBatching {
            /**
             * Specifies that service bus can batch multiple message when it write messages to or delete
             * messages from it's internal store. This increases the throughput.
             *
             * @return the next stage of topic update
             */
            Update withMessageBatching();

            /**
             * Specifies that batching of messages should be disabled when Service Bus write messages to
             * or delete messages from it's internal store.
             *
             * @return the next stage of topic update
             */
            Update withoutMessageBatching();
        }

        /**
         * The stage of the topic definition allowing to specify duration of the duplicate message
         * detection history.
         */
        interface WithDuplicateMessageDetection {
            /**
             * Specifies the duration of the duplicate message detection history.
             *
             * @param duration duration of the history
             * @return the next stage of topic update
             */
            Update withDuplicateMessageDetectionHistoryDuration(Duration duration);

            /**
             * Specifies that duplicate message detection needs to be disabled.
             *
             * @return the next stage of topic update
             */
            Update withoutDuplicateMessageDetection();
        }

        /**
         * The stage of the Service Bus namespace update allowing to manage subscriptions for the topic.
         */
        interface WithSubscription {
            /**
             * Creates a subscription entity for the Service Bus topic.
             *
             * @param name queue name
             * @return next stage of the Service Bus topic update
             */
            Update withNewSubscription(String name);

            /**
             * Removes a subscription entity associated with the Service Bus topic.
             *
             * @param name subscription name
             * @return next stage of the Service Bus topic update
             */
            Update withoutSubscription(String name);
        }

        /**
         * The stage of the topic definition allowing to add an authorization rule for accessing
         * the topic.
         */
        interface WithAuthorizationRule {
            /**
             * Creates a send authorization rule for the topic.
             *
             * @param name rule name
             * @return next stage of the topic update
             */
            Update withNewSendRule(String name);
            /**
             * Creates a listen authorization rule for the topic.
             *
             * @param name rule name
             * @return next stage of the topic update
             */
            Update withNewListenRule(String name);
            /**
             * Creates a manage authorization rule for the topic.
             *
             * @param name rule name
             * @return next stage of the topic update
             */
            Update withNewManageRule(String name);

            /**
             * Removes an authorization rule for the topic.
             *
             * @param name rule name
             * @return next stage of the topic update
             */
            Update withoutAuthorizationRule(String name);
        }
    }
}
