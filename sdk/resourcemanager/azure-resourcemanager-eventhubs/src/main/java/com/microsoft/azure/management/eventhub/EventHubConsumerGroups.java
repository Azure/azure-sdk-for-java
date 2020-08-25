/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.ConsumerGroupsInner;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Completable;
import rx.Observable;

/**
 * Entry point to manage event hub consumer groups.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubConsumerGroups extends
        SupportsCreating<EventHubConsumerGroup.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<EventHubConsumerGroup>,
        HasInner<ConsumerGroupsInner>,
        HasManager<EventHubManager> {
    /**
     * Lists the consumer groups of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @return list of consumer groups
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<EventHubConsumerGroup> listByEventHub(String resourceGroupName, String namespaceName, String eventHubName);
    /**
     * Lists the consumer groups of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @return observable that emits the consumer groups
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubConsumerGroup> listByEventHubAsync(String resourceGroupName, String namespaceName, String eventHubName);
    /**
     * Gets a consumer group of an event hub in a namespace in a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name consumer group name
     * @return observable that emits the consumer group
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubConsumerGroup> getByNameAsync(String resourceGroupName, String namespaceName, String eventHubName, String name);
    /**
     * Gets a consumer group of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name consumer group name
     * @return the consumer group
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubConsumerGroup getByName(String resourceGroupName, String namespaceName, String eventHubName, String name);
    /**
     * Deletes a consumer group of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name consumer group name
     * @return the completable representing the task
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Completable deleteByNameAsync(String resourceGroupName, String namespaceName, String eventHubName, String name);
    /**
     * Deletes a consumer group of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name consumer group name
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    void deleteByName(String resourceGroupName, String namespaceName, String eventHubName, String name);
}
