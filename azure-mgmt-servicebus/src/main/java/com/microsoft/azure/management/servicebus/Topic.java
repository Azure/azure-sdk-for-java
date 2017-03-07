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
import com.microsoft.azure.management.servicebus.implementation.TopicResourceInner;
import org.joda.time.DateTime;

/**
 */
@Fluent
public interface Topic extends
        IndependentChildResource<ServiceBusManager, TopicResourceInner>,
    Refreshable<Topic>,
    Updatable<Topic.Update> {

    /**
     * Last time the message was sent, or a request was received, for this topic.
     */
    DateTime accessedAt();
    /**
     * TimeSpan idle interval after which the topic is automatically deleted. The minimum duration is 5 minutes. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String autoDeleteOnIdle();
    /**
     * Exact time the message was created.
     */
    DateTime createdAt();
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
     * Default message time to live value. This is the duration after which the message expires, starting from when the message is sent to Service Bus. This is the default value used when TimeToLive is not set on a message itself. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String defaultMessageTimeToLive();
    /**
     * TimeSpan structure that defines the duration of the duplicate detection history. The default value is 10 minutes. The service accepts a C# Standard TimeSpan Format for loc duration https://msdn.microsoft.com/en-us/library/ee372286(v=vs.110).aspx
     */
    String duplicateDetectionHistoryTimeWindow();
    /**
     * Value that indicates whether server-side batched operations are enabled.
     */
    boolean enableBatchedOperations();
    /**
     * Value that indicates whether Express Entities are enabled. An express topic holds a message in memory temporarily before writing it to persistent storage.
     */
    boolean enableExpress();
    /**
     * Value that indicates whether the topic to be partitioned across multiple message brokers is enabled.
     */
    boolean enablePartitioning();
    /**
     * Maximum size of the topic in megabytes, which is the size of the memory allocated for the topic.
     */
    int maxSizeInMegabytes();
    /**
     * Value indicating if this topic requires duplicate detection.
     */
    boolean requiresDuplicateDetection();
    /**
     * Size of the topic, in bytes.
     */
    int sizeInBytes();
    /**
     * Enumerates the possible values for the status of a messaging entity.
     */
    EntityStatus status();
    /**
     * Number of subscriptions.
     */
    int subscriptionCount();
    /**
     * Value that indicates whether the topic supports ordering.
     */
    boolean supportOrdering();
    /**
     * The exact time the message was updated.
     */
    DateTime updatedAt();


    interface Definition extends
        Topic.DefinitionStages.Blank,
        Topic.DefinitionStages.WithGroup,
        Topic.DefinitionStages.WithCreate{
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

        interface WithAutoDeleteOnIdle {
            Definition withAutoDeleteOnIdle(String autoDeleteOnIdle);
        }

        interface WithDefaultMessageTimeToLive {
            Definition withDefaultMessageTimeToLive(String defaultMessageTimeToLive);
        }

        interface WithDuplicateDetectionHistoryTimeWindow {
            Definition withDuplicateDetectionHistoryTimeWindow(String duplicateDetectionHistoryTimeWindow);
        }

        interface WithEnableBatchedOperations {
            Definition withTopicProperties();
            Definition withoutTopicProperties();
        }

        interface WithEnableExpress {
            Definition withTopicProperties();
            Definition withoutTopicProperties();
        }

        interface WithEnablePartitioning {
            Definition withTopicProperties();
            Definition withoutTopicProperties();
        }

        interface WithMaxSizeInMegabytes {
            Definition withMaxSizeInMegabytes(int maxSizeInMegabytes);
        }

        interface WithRequiresDuplicateDetection {
            Definition withTopicProperties();
            Definition withoutTopicProperties();
        }

        interface WithStatus {
            /**
             * Entity status.
             *
             * @return the next stage
             */
            Definition withStatus(EntityStatus status);
        }

        interface WithSupportOrdering {
            Definition withTopicProperties();
            Definition withoutTopicProperties();
        }

        interface WithCreate extends
            Creatable<Topic>,
            Topic.DefinitionStages.WithResourceGroupNameParameter,
            Topic.DefinitionStages.WithNamespaceNameParameter,
            Topic.DefinitionStages.WithTopicNameParameter,
            Topic.DefinitionStages.WithAutoDeleteOnIdle,
            Topic.DefinitionStages.WithDefaultMessageTimeToLive,
            Topic.DefinitionStages.WithDuplicateDetectionHistoryTimeWindow,
            Topic.DefinitionStages.WithEnableBatchedOperations,
            Topic.DefinitionStages.WithEnableExpress,
            Topic.DefinitionStages.WithEnablePartitioning,
            Topic.DefinitionStages.WithMaxSizeInMegabytes,
            Topic.DefinitionStages.WithRequiresDuplicateDetection,
            Topic.DefinitionStages.WithStatus,
            Topic.DefinitionStages.WithSupportOrdering{
        }

        interface Blank extends
            GroupableResource.DefinitionWithRegion<WithGroup>{
        }

        interface WithGroup extends
            GroupableResource.DefinitionStages.WithGroup<WithCreate>{
        }
    }

    interface Update extends
        Topic.UpdateStages.WithResourceGroupNameParameter,
        Topic.UpdateStages.WithNamespaceNameParameter,
        Topic.UpdateStages.WithTopicNameParameter,
        Topic.UpdateStages.WithAutoDeleteOnIdle,
        Topic.UpdateStages.WithDefaultMessageTimeToLive,
        Topic.UpdateStages.WithDuplicateDetectionHistoryTimeWindow,
        Topic.UpdateStages.WithEnableBatchedOperations,
        Topic.UpdateStages.WithEnableExpress,
        Topic.UpdateStages.WithEnablePartitioning,
        Topic.UpdateStages.WithMaxSizeInMegabytes,
        Topic.UpdateStages.WithRequiresDuplicateDetection,
        Topic.UpdateStages.WithStatus,
        Topic.UpdateStages.WithSupportOrdering{
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

        interface WithAutoDeleteOnIdle {
            Update withAutoDeleteOnIdle(String autoDeleteOnIdle);
        }

        interface WithDefaultMessageTimeToLive {
            Update withDefaultMessageTimeToLive(String defaultMessageTimeToLive);
        }

        interface WithDuplicateDetectionHistoryTimeWindow {
            Update withDuplicateDetectionHistoryTimeWindow(String duplicateDetectionHistoryTimeWindow);
        }

        interface WithEnableBatchedOperations {
            Update withTopicProperties();
            Update withoutTopicProperties();
        }

        interface WithEnableExpress {
            Update withTopicProperties();
            Update withoutTopicProperties();
        }

        interface WithEnablePartitioning {
            Update withTopicProperties();
            Update withoutTopicProperties();
        }

        interface WithMaxSizeInMegabytes {
            Update withMaxSizeInMegabytes(int maxSizeInMegabytes);
        }

        interface WithRequiresDuplicateDetection {
            Update withTopicProperties();
            Update withoutTopicProperties();
        }

        interface WithStatus {
            /**
             * Entity status.
             *
             * @return the next stage
             */
            Update withStatus(EntityStatus status);
        }

        interface WithSupportOrdering {
            Update withTopicProperties();
            Update withoutTopicProperties();
        }
    }

}
