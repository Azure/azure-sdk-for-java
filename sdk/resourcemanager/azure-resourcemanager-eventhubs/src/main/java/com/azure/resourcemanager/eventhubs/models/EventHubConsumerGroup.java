// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.inner.ConsumerGroupInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.time.OffsetDateTime;

/**
 * Type representing consumer group of an event hub.
 */
@Fluent
public interface EventHubConsumerGroup extends
    NestedResource,
    HasManager<EventHubsManager>,
    Refreshable<EventHubConsumerGroup>,
    HasInner<ConsumerGroupInner>,
    Updatable<EventHubConsumerGroup.Update> {
    /**
     * @return the resource group of the namespace where parent event hub resides
     */
    String namespaceResourceGroupName();

    /**
     * @return the namespace name of parent event hub
     */
    String namespaceName();

    /**
     * @return the name of the parent event hub
     */
    String eventHubName();

    /**
     * @return creation time of the consumer group
     */
    OffsetDateTime createdAt();

    /**
     * @return last modified time of the consumer group
     */
    OffsetDateTime updatedAt();

    /**
     * @return user metadata associated with the consumer group
     */
    String userMetadata();

    /**
     * The entirety of the consumer group definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithEventHub,
            DefinitionStages.WithUserMetadata,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of consumer group definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a event hub definition.
         */
        interface Blank extends WithEventHub {
        }

        /**
         * The stage of the consumer group definition allowing to specify the event
         * hub to be associated with it.
         */
        interface WithEventHub {
            /**
             * Specifies the event hub for which consumer group needs to be created.
             *
             * @param eventHub event hub
             * @return next stage of the consumer group definition
             */
            WithCreate withExistingEventHub(EventHub eventHub);

            /**
             * Specifies the event hub for which consumer group needs to be created.
             *
             * @param eventHubId ARM resource id of event hub
             * @return next stage of the consumer group definition
             */
            WithCreate withExistingEventHubId(String eventHubId);

            /**
             * Specifies the event hub for which consumer group needs to be created.
             *
             * @param resourceGroupName event hub namespace resource group name
             * @param namespaceName event hub namespace name
             * @param eventHubName event hub name
             * @return next stage of the consumer group definition
             */
            WithCreate withExistingEventHub(String resourceGroupName, String namespaceName, String eventHubName);
        }

        /**
         * The stage of the consumer group definition allowing to specify user metadata.
         */
        interface WithUserMetadata {
            /**
             * Specifies user metadata.
             *
             * @param metadata the metadata
             * @return next stage of the consumer group definition
             */
            WithCreate withUserMetadata(String metadata);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<EventHubConsumerGroup>,
            DefinitionStages.WithUserMetadata {
        }
    }

    /**
     * The template for a consumer group update operation, containing all the settings
     * that can be modified.
     */
    interface Update extends
        Appliable<EventHubConsumerGroup>,
        UpdateStages.WithUserMetadata {
    }

    /**
     * Grouping of consumer group update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the consumer group update allowing to specify user metadata.
         */
        interface WithUserMetadata {
            /**
             * Specifies user metadata.
             *
             * @param metadata the metadata
             * @return next stage of the consumer group update
             */
            Update withUserMetadata(String metadata);
        }
    }
}
