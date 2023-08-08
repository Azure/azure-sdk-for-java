// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import reactor.core.publisher.Mono;

/**
 * Entry point to manage event hubs.
 */
@Fluent
public interface EventHubs extends
    SupportsCreating<EventHub.DefinitionStages.Blank>,
    SupportsDeletingById,
    SupportsGettingById<EventHub>,
    HasManager<EventHubsManager> {
    /**
     * @return entry point to manage authorization rules of event hubs.
     */
    EventHubAuthorizationRules authorizationRules();

    /**
     * @return entry point to manage consumer group of event hubs.
     */

    EventHubConsumerGroups consumerGroups();
    /**
     * Lists the event hubs in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return list of event hubs
     */
    PagedIterable<EventHub> listByNamespace(String resourceGroupName, String namespaceName);

    /**
     * Lists the event hubs in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return observable that emits the event hubs
     */
    PagedFlux<EventHub> listByNamespaceAsync(String resourceGroupName, String namespaceName);

    /**
     * Gets an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name event hub name
     * @return observable that emits the event hubs
     */
    Mono<EventHub> getByNameAsync(String resourceGroupName, String namespaceName, String name);

    /**
     * Gets an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name event hub name
     * @return the event hubs
     */
    EventHub getByName(String resourceGroupName, String namespaceName, String name);

    /**
     * Deletes an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name event hub name
     * @return the completable representing the task
     */
    Mono<Void> deleteByNameAsync(String resourceGroupName, String namespaceName, String name);

    /**
     * Deletes an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name event hub name
     */
    void deleteByName(String resourceGroupName, String namespaceName, String name);
}
