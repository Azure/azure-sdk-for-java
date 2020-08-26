// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.ConsumerGroupsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * Entry point to manage event hub consumer groups.
 */
@Fluent
public interface EventHubConsumerGroups extends
    SupportsCreating<EventHubConsumerGroup.DefinitionStages.Blank>,
    SupportsDeletingById,
    SupportsGettingById<EventHubConsumerGroup>,
    HasInner<ConsumerGroupsClient>,
    HasManager<EventHubsManager> {
    /**
     * Lists the consumer groups of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @return list of consumer groups
     */
    PagedIterable<EventHubConsumerGroup> listByEventHub(
        String resourceGroupName, String namespaceName, String eventHubName);

    /**
     * Lists the consumer groups of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @return observable that emits the consumer groups
     */
    PagedFlux<EventHubConsumerGroup> listByEventHubAsync(
        String resourceGroupName, String namespaceName, String eventHubName);

    /**
     * Gets a consumer group of an event hub in a namespace in a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name consumer group name
     * @return observable that emits the consumer group
     */
    Mono<EventHubConsumerGroup> getByNameAsync(
        String resourceGroupName, String namespaceName, String eventHubName, String name);

    /**
     * Gets a consumer group of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name consumer group name
     * @return the consumer group
     */
    EventHubConsumerGroup getByName(
        String resourceGroupName, String namespaceName, String eventHubName, String name);

    /**
     * Deletes a consumer group of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name consumer group name
     * @return the completable representing the task
     */
    Mono<Void> deleteByNameAsync(
        String resourceGroupName, String namespaceName, String eventHubName, String name);

    /**
     * Deletes a consumer group of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name consumer group name
     */
    void deleteByName(
        String resourceGroupName, String namespaceName, String eventHubName, String name);
}
