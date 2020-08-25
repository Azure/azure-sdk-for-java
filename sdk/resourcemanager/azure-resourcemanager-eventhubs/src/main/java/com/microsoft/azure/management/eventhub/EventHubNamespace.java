/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.EHNamespaceInner;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import org.joda.time.DateTime;
import rx.Observable;

/**
 *  Type representing an Azure EventHub namespace.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubNamespace extends
        GroupableResource<EventHubManager, EHNamespaceInner>,
        Refreshable<EventHubNamespace>,
        Updatable<EventHubNamespace.Update> {
    /**
     * @return namespace sku
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubNamespaceSkuType sku();
    /**
     * @return resource id of the Azure Insights metrics associated with the namespace
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String azureInsightMetricId();
    /**
     * @return the service bus endpoint associated with the namespace
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String serviceBusEndpoint();
    /**
     * @return namespace created time
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    DateTime createdAt();
    /**
     * @return namespace last modified time
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    DateTime updatedAt();
    /**
     * @return provisioning state of the namespace
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String provisioningState();
    /**
     * @return true if auto-scale is enabled for the namespace, false otherwise
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    boolean isAutoScaleEnabled();
    /**
     * @return current throughput units set for the namespace
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    int currentThroughputUnits();
    /**
     * @return maximum throughput unit that auto-scalar is allowed to set
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    int throughputUnitsUpperLimit();
    /**
     * @return the event hubs in the namespace
     */
    @Beta(Beta.SinceVersion.V1_7_0)
     Observable<EventHub> listEventHubsAsync();
    /**
     * @return the authorization rules for the event hub namespace
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubNamespaceAuthorizationRule> listAuthorizationRulesAsync();
    /**
     * @return list of event hubs in the namespace
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<EventHub> listEventHubs();
    /**
     * @return list of authorization rules for the event hub namespace
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<EventHubNamespaceAuthorizationRule> listAuthorizationRules();

    /**
     * The entirety of the event hub namespace definition.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Definition extends
            EventHubNamespace.DefinitionStages.Blank,
            EventHubNamespace.DefinitionStages.WithGroup,
            EventHubNamespace.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of event hub namespace definition stages.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface DefinitionStages {
        /**
         * The first stage of a event hub namespace definition.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the event hub namespace definition allowing to specify the resource group.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the event hub namespace definition allowing to specify the sku.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
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
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithEventHub {
            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withNewEventHub(String eventHubName);
            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @param partitionCount the number of partitions in the event hub
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withNewEventHub(String eventHubName, int partitionCount);
            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @param partitionCount the number of partitions in the event hub
             * @param retentionPeriodInDays the retention period for events in days
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withNewEventHub(String eventHubName, int partitionCount, int retentionPeriodInDays);
        }

        /**
         * The stage of the event hub namespace definition allowing to add authorization rule for accessing
         * the event hub.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithAuthorizationRule {
            /**
             * Specifies that a new authorization rule should be created that has send access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withNewSendRule(String ruleName);
            /**
             * Specifies that a new authorization rule should be created that has listen access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withNewListenRule(String ruleName);
            /**
             * Specifies that a new authorization rule should be created that has manage access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withNewManageRule(String ruleName);
        }

        /**
         * The stage of the event hub namespace definition allowing to specify the throughput unit settings.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithThroughputConfiguration {
            /**
             * Enables the scaling up the throughput units automatically based on load.
             *
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withAutoScaling();
            /**
             * Specifies the current throughput units.
             *
             * @param units throughput units
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withCurrentThroughputUnits(int units);

            /**
             * Specifies the maximum throughput units that auto-scalar is allowed to scale-up.
             *
             * @param units throughput units
             * @return next stage of the event hub namespace definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withThroughputUnitsUpperLimit(int units);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
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
    @Beta(Beta.SinceVersion.V1_7_0)
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
    @Beta(Beta.SinceVersion.V1_7_0)
    interface UpdateStages {
        /**
         * The stage of the event hub namespace update allowing to change the sku.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithSku {
            /**
             * Specifies the namespace sku.
             *
             * @param namespaceSku the sku
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withSku(EventHubNamespaceSkuType namespaceSku);
        }

        /**
         * The stage of the event hub namespace update allowing to add new event hub in the namespace.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithEventHub {
            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withNewEventHub(String eventHubName);
            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @param partitionCount the number of partitions in the event hub
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withNewEventHub(String eventHubName, int partitionCount);
            /**
             * Specifies that a new event hub should be created in the namespace.
             *
             * @param eventHubName event hub name
             * @param partitionCount the number of partitions in the event hub
             * @param retentionPeriodInDays the retention period for events in days
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withNewEventHub(String eventHubName, int partitionCount, int retentionPeriodInDays);
            /**
             * Deletes an event hub in the event hub namespace.
             *
             * @param eventHubName event hub name
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withoutEventHub(String eventHubName);
        }

        /**
         * The stage of the event hub namespace update allowing to add authorization rule for accessing
         * the event hub.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithAuthorizationRule {
            /**
             * Specifies that a new authorization rule should be created that has send access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withNewSendRule(String ruleName);
            /**
             * Specifies that a new authorization rule should be created that has listen access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withNewListenRule(String ruleName);
            /**
             * Specifies that a new authorization rule should be created that has manage access to the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withNewManageRule(String ruleName);
            /**
             * Deletes an authorization rule associated with the event hub namespace.
             *
             * @param ruleName rule name
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withoutAuthorizationRule(String ruleName);
        }

        /**
         * The stage of the event hub namespace update allowing to specify the throughput unit settings.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithThroughputConfiguration {
            /**
             * Enables the scaling up the throughput units automatically based on load.
             *
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withAutoScaling();

            /**
             * Specifies the current throughput units.
             *
             * @param units throughput units
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withCurrentThroughputUnits(int units);

            /**
             * Specifies the maximum throughput units that auto-scalar is allowed to scale-up.
             *
             * @param units throughput units
             * @return next stage of the event hub namespace update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withThroughputUnitsUpperLimit(int units);
        }
    }
}