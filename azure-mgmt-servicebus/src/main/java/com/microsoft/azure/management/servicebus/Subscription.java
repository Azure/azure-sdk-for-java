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
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.azure.management.servicebus.implementation.SubscriptionResourceInner;
import org.joda.time.DateTime;

/**
 * Type represents service bus topic subscription.
 */
@Fluent
public interface Subscription extends
        IndependentChildResource<ServiceBusManager, SubscriptionResourceInner>,
    Refreshable<Subscription>,
    Updatable<Subscription.Update> {

    /**
     * Last time there was a receive request to this subscription.
     */
    DateTime accessedAt();
    /**
     * TimeSpan idle interval after which the topic is automatically deleted. The minimum duration is 5 minutes. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String autoDeleteOnIdle();
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
     * Exact time the message was created.
     */
    DateTime createdAt();
    /**
     * Default message time to live value. This is the duration after which the message expires, starting from when the message is sent to Service Bus. This is the default value used when TimeToLive is not set on a message itself. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String defaultMessageTimeToLive();
    /**
     * Value that indicates whether a subscription has dead letter support on filter evaluation exceptions.
     */
    boolean deadLetteringOnFilterEvaluationExceptions();
    /**
     * Value that indicates whether a subscription has dead letter support when a message expires.
     */
    boolean deadLetteringOnMessageExpiration();
    /**
     * Value that indicates whether server-side batched operations are enabled.
     */
    boolean enableBatchedOperations();
    /**
     * The lock duration time span for the subscription. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String lockDuration();
    /**
     * Number of maximum deliveries.
     */
    int maxDeliveryCount();
    /**
     * Number of messages.
     */
    int messageCount();
    /**
     * Value indicating if a subscription supports the concept of sessions.
     */
    boolean requiresSession();
    /**
     * Enumerates the possible values for the status of a messaging entity.
     */
    EntityStatus status();
    /**
     * The exact time the message was updated.
     */
    DateTime updatedAt();


    interface Definition extends
        Subscription.DefinitionStages.Blank,
        Subscription.DefinitionStages.WithGroup,
        Subscription.DefinitionStages.WithCreate{
    }

    interface DefinitionStages {

        interface WithResourceGroupNameParameter {
            /**
             * Name of the Resource group within the Azure subscription.
             *
             * @param resourceGroupNameParameter
             * @return the next stage
             */
            Definition withResourceGroupNameParameter(String resourceGroupNameParameter);
        }

        interface WithNamespaceNameParameter {
            /**
             * The namespace name
             *
             * @param namespaceNameParameter
             * @return the next stage
             */
            Definition withNamespaceNameParameter(String namespaceNameParameter);
        }

        interface WithTopicNameParameter {
            /**
             * The topic name.
             *
             * @param topicNameParameter
             * @return the next stage
             */
            Definition withTopicNameParameter(String topicNameParameter);
        }

        interface WithSubscriptionNameParameter {
            /**
             * The subscription name.
             *
             * @param subscriptionNameParameter
             * @return the next stage
             */
            Definition withSubscriptionNameParameter(String subscriptionNameParameter);
        }

        interface WithAutoDeleteOnIdle {
            Definition withAutoDeleteOnIdle(String autoDeleteOnIdle);
        }

        interface WithDefaultMessageTimeToLive {
            Definition withDefaultMessageTimeToLive(String defaultMessageTimeToLive);
        }

        interface WithDeadLetteringOnFilterEvaluationExceptions {
            Definition withSubscriptionProperties();
            Definition withoutSubscriptionProperties();
        }

        interface WithDeadLetteringOnMessageExpiration {
            Definition withSubscriptionProperties();
            Definition withoutSubscriptionProperties();
        }

        interface WithEnableBatchedOperations {
            Definition withSubscriptionProperties();
            Definition withoutSubscriptionProperties();
        }

        interface WithLockDuration {
            Definition withLockDuration(String lockDuration);
        }

        interface WithMaxDeliveryCount {
            Definition withMaxDeliveryCount(int maxDeliveryCount);
        }

        interface WithRequiresSession {
            Definition withSubscriptionProperties();
            Definition withoutSubscriptionProperties();
        }

        interface WithStatus {
            /**
             * Entity status.
             *
             * @return the next stage
             */
            Definition withStatus(EntityStatus status);
        }

        interface WithCreate extends
            Creatable<Subscription>,
            Subscription.DefinitionStages.WithResourceGroupNameParameter,
            Subscription.DefinitionStages.WithNamespaceNameParameter,
            Subscription.DefinitionStages.WithTopicNameParameter,
            Subscription.DefinitionStages.WithSubscriptionNameParameter,
            Subscription.DefinitionStages.WithAutoDeleteOnIdle,
            Subscription.DefinitionStages.WithDefaultMessageTimeToLive,
            Subscription.DefinitionStages.WithDeadLetteringOnFilterEvaluationExceptions,
            Subscription.DefinitionStages.WithDeadLetteringOnMessageExpiration,
            Subscription.DefinitionStages.WithEnableBatchedOperations,
            Subscription.DefinitionStages.WithLockDuration,
            Subscription.DefinitionStages.WithMaxDeliveryCount,
            Subscription.DefinitionStages.WithRequiresSession,
            Subscription.DefinitionStages.WithStatus{
        }

        interface Blank extends
            GroupableResource.DefinitionWithRegion<WithGroup>{
        }

        interface WithGroup extends
            GroupableResource.DefinitionStages.WithGroup<WithCreate>{
        }
    }

    interface Update extends
        Subscription.UpdateStages.WithResourceGroupNameParameter,
        Subscription.UpdateStages.WithNamespaceNameParameter,
        Subscription.UpdateStages.WithTopicNameParameter,
        Subscription.UpdateStages.WithSubscriptionNameParameter,
        Subscription.UpdateStages.WithAutoDeleteOnIdle,
        Subscription.UpdateStages.WithDefaultMessageTimeToLive,
        Subscription.UpdateStages.WithDeadLetteringOnFilterEvaluationExceptions,
        Subscription.UpdateStages.WithDeadLetteringOnMessageExpiration,
        Subscription.UpdateStages.WithEnableBatchedOperations,
        Subscription.UpdateStages.WithLockDuration,
        Subscription.UpdateStages.WithMaxDeliveryCount,
        Subscription.UpdateStages.WithRequiresSession,
        Subscription.UpdateStages.WithStatus{
    }

    interface UpdateStages {

        interface WithResourceGroupNameParameter {
            /**
             * Name of the Resource group within the Azure subscription.
             *
             * @param resourceGroupNameParameter
             * @return the next stage
             */
            Update withResourceGroupNameParameter(String resourceGroupNameParameter);
        }

        interface WithNamespaceNameParameter {
            /**
             * The namespace name
             *
             * @param namespaceNameParameter
             * @return the next stage
             */
            Update withNamespaceNameParameter(String namespaceNameParameter);
        }

        interface WithTopicNameParameter {
            /**
             * The topic name.
             *
             * @param topicNameParameter
             * @return the next stage
             */
            Update withTopicNameParameter(String topicNameParameter);
        }

        interface WithSubscriptionNameParameter {
            /**
             * The subscription name.
             *
             * @param subscriptionNameParameter
             * @return the next stage
             */
            Update withSubscriptionNameParameter(String subscriptionNameParameter);
        }

        interface WithAutoDeleteOnIdle {
            Update withAutoDeleteOnIdle(String autoDeleteOnIdle);
        }

        interface WithDefaultMessageTimeToLive {
            Update withDefaultMessageTimeToLive(String defaultMessageTimeToLive);
        }

        interface WithDeadLetteringOnFilterEvaluationExceptions {
            Update withSubscriptionProperties();
            Update withoutSubscriptionProperties();
        }

        interface WithDeadLetteringOnMessageExpiration {
            Update withSubscriptionProperties();
            Update withoutSubscriptionProperties();
        }

        interface WithEnableBatchedOperations {
            Update withSubscriptionProperties();
            Update withoutSubscriptionProperties();
        }

        interface WithLockDuration {
            Update withLockDuration(String lockDuration);
        }

        interface WithMaxDeliveryCount {
            Update withMaxDeliveryCount(int maxDeliveryCount);
        }

        interface WithRequiresSession {
            Update withSubscriptionProperties();
            Update withoutSubscriptionProperties();
        }

        interface WithStatus {
            /**
             * Entity status.
             *
             * @return the next stage
             */
            Update withStatus(EntityStatus status);
        }
    }

}
