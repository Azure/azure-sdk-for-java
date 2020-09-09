// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.EventHubsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * Entry point to manage event hub authorization rules.
 */
@Fluent
public interface EventHubAuthorizationRules extends
    SupportsCreating<EventHubAuthorizationRule.DefinitionStages.Blank>,
    SupportsDeletingById,
    SupportsGettingById<EventHubAuthorizationRule>,
    HasInner<EventHubsClient>,
    HasManager<EventHubsManager> {
    /**
     * Lists the authorization rules of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @return list of authorization rules
     */
    PagedIterable<EventHubAuthorizationRule> listByEventHub(
        String resourceGroupName, String namespaceName, String eventHubName);
    /**
     * Lists the authorization rules of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @return observable that emits the authorization rules
     */
    PagedFlux<EventHubAuthorizationRule> listByEventHubAsync(
        String resourceGroupName, String namespaceName, String eventHubName);
    /**
     * Gets an authorization rule of an event hub in a namespace in a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name authorization rule name
     * @return observable that emits the authorization rule
     */
    Mono<EventHubAuthorizationRule> getByNameAsync(
        String resourceGroupName, String namespaceName, String eventHubName, String name);
    /**
     * Gets an authorization rule of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name authorization rule name
     * @return the authorization rule
     */
    EventHubAuthorizationRule getByName(
        String resourceGroupName, String namespaceName, String eventHubName, String name);

    /**
     * Deletes an authorization rule of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name authorization rule name
     * @return the completable representing the task
     */
    Mono<Void> deleteByNameAsync(
        String resourceGroupName, String namespaceName, String eventHubName, String name);

    /**
     * Deletes an authorization rule of an event hub in a namespace under a resource group.
     *
     * @param resourceGroupName namespace resource group name
     * @param namespaceName event hub parent namespace name
     * @param eventHubName event hub name
     * @param name authorization rule name
     */
    void deleteByName(String resourceGroupName, String namespaceName, String eventHubName, String name);
}
