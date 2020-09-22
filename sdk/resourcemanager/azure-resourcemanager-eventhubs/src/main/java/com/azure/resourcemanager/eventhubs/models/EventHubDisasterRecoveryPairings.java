// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import reactor.core.publisher.Mono;

/**
 * Entry point to manage disaster recovery pairing of event hub namespaces.
 */
@Fluent
public interface EventHubDisasterRecoveryPairings extends
    SupportsCreating<EventHubDisasterRecoveryPairing.DefinitionStages.Blank>,
    SupportsDeletingById,
    SupportsGettingById<EventHubDisasterRecoveryPairing>,
    HasManager<EventHubsManager> {
    /**
     * @return entry point to manage authorization rules of a disaster recovery pairing.
     */
    DisasterRecoveryPairingAuthorizationRules authorizationRules();
    /**
     * Lists the disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return list of disaster recovery pairings
     */
    PagedIterable<EventHubDisasterRecoveryPairing> listByNamespace(String resourceGroupName, String namespaceName);
    /**
     * Lists the disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return observable that emits disaster recovery pairings
     */
    PagedFlux<EventHubDisasterRecoveryPairing> listByNamespaceAsync(String resourceGroupName, String namespaceName);
    /**
     * Gets a disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name disaster recovery pairing name
     * @return observable that emits disaster recovery pairings
     */
    Mono<EventHubDisasterRecoveryPairing> getByNameAsync(String resourceGroupName, String namespaceName, String name);

    /**
     * Gets a disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name disaster recovery pairing name
     * @return the disaster recovery pairing
     */
    EventHubDisasterRecoveryPairing getByName(String resourceGroupName, String namespaceName, String name);

    /**
     * Deletes a disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name disaster recovery pairing name
     * @return the completable representing the task
     */
    Mono<Void> deleteByNameAsync(String resourceGroupName, String namespaceName, String name);

    /**
     * Deletes a disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name disaster recovery pairing
     */
    void deleteByName(String resourceGroupName, String namespaceName, String name);
}
