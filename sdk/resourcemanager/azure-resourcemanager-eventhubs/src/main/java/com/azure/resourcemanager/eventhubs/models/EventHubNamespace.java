// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.fluent.inner.EHNamespaceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;

import java.time.OffsetDateTime;

/**
 *  Type representing an Azure EventHub namespace.
 */
@Fluent
public interface EventHubNamespace extends
    GroupableResource<EventHubsManager, EHNamespaceInner>,
    Refreshable<EventHubNamespace>,
    Updatable<EventHubNamespace.Update> {
    /**
     * @return namespace sku
     */
    EventHubNamespaceSkuType sku();

    /**
     * @return resource id of the Azure Insights metrics associated with the namespace
     */
    String azureInsightMetricId();

    /**
     * @return the service bus endpoint associated with the namespace
     */
    String serviceBusEndpoint();

    /**
     * @return namespace created time
     */
    OffsetDateTime createdAt();

    /**
     * @return namespace last modified time
     */
    OffsetDateTime updatedAt();

    /**
     * @return provisioning state of the namespace
     */
    String provisioningState();

    /**
     * @return true if auto-scale is enabled for the namespace, false otherwise
     */
    boolean isAutoScaleEnabled();

    /**
     * @return current throughput units set for the namespace
     */
    int currentThroughputUnits();

    /**
     * @return maximum throughput unit that auto-scalar is allowed to set
     */
    int throughputUnitsUpperLimit();

    /**
     * @return the event hubs in the namespace
     */
    PagedFlux<EventHub> listEventHubsAsync();

     /**
     * @return the authorization rules for the event hub namespace
     */
    PagedFlux<EventHubNamespaceAuthorizationRule> listAuthorizationRulesAsync();

    /**
     * @return list of event hubs in the namespace
     */
    PagedIterable<EventHub> listEventHubs();

    /**
     * @return list of authorization rules for the event hub namespace
     */
    PagedIterable<EventHubNamespaceAuthorizationRule> listAuthorizationRules();

    /**
     * The entirety of the event hub namespace definition.
     */
    interface Definition extends
            EventHubNamespace.DefinitionStages.Blank,
            EventHubNamespace.DefinitionStages.WithGroup,
            EventHubNamespace.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of event hub namespace definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a event hub namespace definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the event hub namespace definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the event hub namespace definition allowing to specify the sku.
         */
        interface WithSku {
            /**
             * Specifies the namespace sku.
             *
             * @param namespaceSku the sku
             * @return next stage of the event hub namespace definition
             */
            WithCreate withSku(EventHubNamespaceSkuType namespaceSku);
        }

        /**
         * The stage of the event hub namespace definition allowing to add new event hub in the namespace.
         */
        interface WithEventHub {
            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @return next stage of the event hub namespace definition
             */
            WithCreate withNewEventHub(String eventHubName);

            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @param partitionCount the number of partitions in the event hub
             * @return next stage of the event hub namespace definition
             */
            WithCreate withNewEventHub(String eventHubName, int partitionCount);

            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @param partitionCount the number of partitions in the event hub
             * @param retentionPeriodInDays the retention period for events in days
             * @return next stage of the event hub namespace definition
             */
            WithCreate withNewEventHub(String eventHubName, int partitionCount, int retentionPeriodInDays);
        }

        /**
         * The stage of the event hub namespace definition allowing to add authorization rule for accessing
         * the event hub.
         */
        interface WithAuthorizationRule {
            /**
             * Specifies that a new authorization rule should be created
             * that has send access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace definition
             */
            WithCreate withNewSendRule(String ruleName);

            /**
             * Specifies that a new authorization rule should be created
             * that has listen access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace definition
             */
            WithCreate withNewListenRule(String ruleName);

            /**
             * Specifies that a new authorization rule should be created
             * that has manage access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace definition
             */
            WithCreate withNewManageRule(String ruleName);
        }

        /**
         * The stage of the event hub namespace definition
         * allowing to specify the throughput unit settings.
         */
        interface WithThroughputConfiguration {
            /**
             * Enables the scaling up the throughput units automatically based on load.
             *
             * @return next stage of the event hub namespace definition
             */
            WithCreate withAutoScaling();
            /**
             * Specifies the current throughput units.
             *
             * @param units throughput units
             * @return next stage of the event hub namespace definition
             */
            WithCreate withCurrentThroughputUnits(int units);

            /**
             * Specifies the maximum throughput units that auto-scalar is allowed to scale-up.
             *
             * @param units throughput units
             * @return next stage of the event hub namespace definition
             */
            WithCreate withThroughputUnitsUpperLimit(int units);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<EventHubNamespace>,
            Resource.DefinitionWithTags<WithCreate>,
            EventHubNamespace.DefinitionStages.WithSku,
            EventHubNamespace.DefinitionStages.WithEventHub,
            EventHubNamespace.DefinitionStages.WithAuthorizationRule,
            EventHubNamespace.DefinitionStages.WithThroughputConfiguration {
        }
    }

    /**
     * The template for a event hub namespace update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<EventHubNamespace>,
        Resource.UpdateWithTags<Update>,
        EventHubNamespace.UpdateStages.WithSku,
        EventHubNamespace.UpdateStages.WithEventHub,
        EventHubNamespace.UpdateStages.WithAuthorizationRule,
        EventHubNamespace.UpdateStages.WithThroughputConfiguration {
    }

    /**
     * Grouping of all the event hub namespace update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the event hub namespace update allowing to change the sku.
         */
        interface WithSku {
            /**
             * Specifies the namespace sku.
             *
             * @param namespaceSku the sku
             * @return next stage of the event hub namespace update
             */
            Update withSku(EventHubNamespaceSkuType namespaceSku);
        }

        /**
         * The stage of the event hub namespace update allowing to add new event hub in the namespace.
         */
        interface WithEventHub {
            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @return next stage of the event hub namespace update
             */
            Update withNewEventHub(String eventHubName);

            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @param partitionCount the number of partitions in the event hub
             * @return next stage of the event hub namespace update
             */
            Update withNewEventHub(String eventHubName, int partitionCount);

            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @param partitionCount the number of partitions in the event hub
             * @param retentionPeriodInDays the retention period for events in days
             * @return next stage of the event hub namespace update
             */
            Update withNewEventHub(String eventHubName, int partitionCount, int retentionPeriodInDays);

            /**
             * Deletes an event hub in the event hub namespace.
             *
             * @param eventHubName event hub name
             * @return next stage of the event hub namespace update
             */
            Update withoutEventHub(String eventHubName);
        }

        /**
         * The stage of the event hub namespace update allowing to add authorization rule for accessing
         * the event hub.
         */
        interface WithAuthorizationRule {
            /**
             * Specifies that a new authorization rule should be created
             * that has send access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace update
             */
            Update withNewSendRule(String ruleName);

            /**
             * Specifies that a new authorization rule should be created
             * that has listen access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace update
             */
            Update withNewListenRule(String ruleName);

            /**
             * Specifies that a new authorization rule should be created
             * that has manage access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace update
             */
            Update withNewManageRule(String ruleName);

            /**
             * Deletes an authorization rule associated with the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace update
             */
            Update withoutAuthorizationRule(String ruleName);
        }

        /**
         * The stage of the event hub namespace update allowing to specify the throughput unit settings.
         */
        interface WithThroughputConfiguration {
            /**
             * Enables the scaling up the throughput units automatically based on load.
             *
             * @return next stage of the event hub namespace update
             */
            Update withAutoScaling();

            /**
             * Specifies the current throughput units.
             *
             * @param units throughput units
             * @return next stage of the event hub namespace update
             */
            Update withCurrentThroughputUnits(int units);

            /**
             * Specifies the maximum throughput units that auto-scalar is allowed to scale-up.
             *
             * @param units throughput units
             * @return next stage of the event hub namespace update
             */
            Update withThroughputUnitsUpperLimit(int units);
        }
    }
}
