// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;


import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsBatchCreation;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.eventhubs.EventHubsManager;

/**
 * Entry point to manage EventHub namespaces.
 */
@Fluent
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
    HasManager<EventHubsManager> {
    /**
     * @return entry point to manage authorization rules of event hub namespaces.
     */
    EventHubNamespaceAuthorizationRules authorizationRules();

    /**
     * @return entry point to manage event hubs of event hub namespaces.
     */
    EventHubs eventHubs();
}
