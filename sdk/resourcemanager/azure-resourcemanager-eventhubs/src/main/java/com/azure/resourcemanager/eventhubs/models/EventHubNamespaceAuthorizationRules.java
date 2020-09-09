// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.fluent.NamespacesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import reactor.core.publisher.Mono;

/**
 * Entry point to manage event hub namespace authorization rules.
 */
@Fluent
public interface EventHubNamespaceAuthorizationRules extends
    SupportsCreating<EventHubNamespaceAuthorizationRule.DefinitionStages.Blank>,
    SupportsDeletingById,
    SupportsGettingById<EventHubNamespaceAuthorizationRule>,
    HasInner<NamespacesClient>,
    HasManager<EventHubsManager> {
    /**
     * Lists the authorization rules under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return list of authorization rules
     */
    PagedIterable<EventHubNamespaceAuthorizationRule> listByNamespace(String resourceGroupName, String namespaceName);

    /**
     * Lists the authorization rules under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return observable that emits the authorization rules
     */
    PagedFlux<EventHubNamespaceAuthorizationRule> listByNamespaceAsync(String resourceGroupName, String namespaceName);

    /**
     * Gets an authorization rule under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name authorization rule name
     * @return observable that emits the authorization rule
     */
    Mono<EventHubNamespaceAuthorizationRule> getByNameAsync(
        String resourceGroupName, String namespaceName, String name);

    /**
     * Gets an authorization rule under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name authorization rule name
     * @return the authorization rule
     */
    EventHubNamespaceAuthorizationRule getByName(String resourceGroupName, String namespaceName, String name);

    /**
     * Deletes an authorization rule under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name authorization rule name
     * @return the completable representing the task
     */
    Mono<Void> deleteByNameAsync(String resourceGroupName, String namespaceName, String name);

    /**
     * Deletes an authorization rule under a namespace in a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name authorization rule name
     */
    void deleteByName(String resourceGroupName, String namespaceName, String name);
}
