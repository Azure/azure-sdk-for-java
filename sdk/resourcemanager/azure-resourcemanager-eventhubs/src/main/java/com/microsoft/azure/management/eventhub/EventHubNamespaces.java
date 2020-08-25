/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;


import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.eventhub.implementation.NamespacesInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to manage EventHub namespaces.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubNamespaces extends
        SupportsCreating<EventHubNamespace.DefinitionStages.Blank>,
        SupportsListing<EventHubNamespace>,
        SupportsListingByResourceGroup<EventHubNamespace>,
        SupportsGettingByResourceGroup<EventHubNamespace>,
        SupportsGettingById<EventHubNamespace>,
        SupportsDeletingById,
        SupportsDeletingByResourceGroup,
        SupportsBatchCreation<EventHubNamespace>,
        SupportsBatchDeletion,
        HasManager<EventHubManager>,
        HasInner<NamespacesInner> {
    /**
     * @return entry point to manage authorization rules of event hub namespaces.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubNamespaceAuthorizationRules authorizationRules();

    /**
     * @return entry point to manage event hubs of event hub namespaces.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubs eventHubs();
}
