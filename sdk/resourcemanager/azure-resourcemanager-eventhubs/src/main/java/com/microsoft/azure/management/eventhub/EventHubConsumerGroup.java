/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.ConsumerGroupInner;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import org.joda.time.DateTime;

/**
 * Type representing consumer group of an event hub.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubConsumerGroup extends
        NestedResource,
        HasManager<EventHubManager>,
        Refreshable<EventHubConsumerGroup>,
        HasInner<ConsumerGroupInner>,
        Updatable<EventHubConsumerGroup.Update> {
    /**
     * @return the resource group of the namespace where parent event hub resides
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String namespaceResourceGroupName();
    /**
     * @return the namespace name of parent event hub
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String namespaceName();
    /**
     * @return the name of the parent event hub
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String eventHubName();
    /**
     * @return creation time of the consumer group
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    DateTime createdAt();
    /**
     * @return last modified time of the consumer group
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    DateTime updatedAt();
    /**
     * @return user metadata associated with the consumer group
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String userMetadata();

    /**
     * The entirety of the consumer group definition.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithEventHub,
            DefinitionStages.WithUserMetadata,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of consumer group definition stages.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface DefinitionStages {
        /**
         * The first stage of a event hub definition.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface Blank extends WithEventHub {
        }

        /**
         * The stage of the consumer group definition allowing to specify the event
         * hub to be associated with it.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithEventHub {
            /**
             * Specifies the event hub for which consumer group needs to be created.
             *
             * @param eventHub event hub
             * @return next stage of the consumer group definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withExistingEventHub(EventHub eventHub);

            /**
             * Specifies the event hub for which consumer group needs to be created.
             *
             * @param eventHubId ARM resource id of event hub
             * @return next stage of the consumer group definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withExistingEventHubId(String eventHubId);

            /**
             * Specifies the event hub for which consumer group needs to be created.
             *
             * @param resourceGroupName event hub namespace resource group name
             * @param namespaceName event hub namespace name
             * @param eventHubName event hub name
             * @return next stage of the consumer group definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withExistingEventHub(String resourceGroupName, String namespaceName, String eventHubName);
        }

        /**
         * The stage of the consumer group definition allowing to specify user metadata.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithUserMetadata {
            /**
             * Specifies user metadata.
             *
             * @param metadata the metadata
             * @return next stage of the consumer group definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withUserMetadata(String metadata);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithCreate extends
                Creatable<EventHubConsumerGroup>,
                DefinitionStages.WithUserMetadata {
        }
    }

    /**
     * The template for a consumer group update operation, containing all the settings
     * that can be modified.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Update extends
            Appliable<EventHubConsumerGroup>,
            UpdateStages.WithUserMetadata {
    }

    /**
     * Grouping of consumer group update stages.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface UpdateStages {
        /**
         * The stage of the consumer group update allowing to specify user metadata.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithUserMetadata {
            /**
             * Specifies user metadata.
             *
             * @param metadata the metadata
             * @return next stage of the consumer group update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withUserMetadata(String metadata);
        }
    }
}
