/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.eventhub.implementation.EventHubsInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Completable;
import rx.Observable;

/**
 * Entry point to manage event hubs.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubs extends
        SupportsCreating<EventHub.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<EventHub>,
        HasInner<EventHubsInner>,
        HasManager<EventHubManager> {
    /**
     * @return entry point to manage authorization rules of event hubs.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubAuthorizationRules authorizationRules();
    /**
     * @return entry point to manage consumer group of event hubs.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubConsumerGroups consumerGroups();
    /**
     * Lists the event hubs in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return list of event hubs
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<EventHub> listByNamespace(String resourceGroupName, String namespaceName);
    /**
     * Lists the event hubs in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return observable that emits the event hubs
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHub> listByNamespaceAsync(String resourceGroupName, String namespaceName);
    /**
     * Gets an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name event hub name
     * @return observable that emits the event hubs
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHub> getByNameAsync(String resourceGroupName, String namespaceName, String name);
    /**
     * Gets an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name event hub name
     * @return the event hubs
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHub getByName(String resourceGroupName, String namespaceName, String name);
    /**
     * Deletes an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name event hub name
     * @return the completable representing the task
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Completable deleteByNameAsync(String resourceGroupName, String namespaceName, String name);
    /**
     * Deletes an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name event hub name
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    void deleteByName(String resourceGroupName, String namespaceName, String name);
}
