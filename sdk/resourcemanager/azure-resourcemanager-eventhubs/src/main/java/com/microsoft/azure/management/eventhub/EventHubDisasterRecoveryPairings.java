/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.eventhub.implementation.DisasterRecoveryConfigsInner;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Completable;
import rx.Observable;

/**
 * Entry point to manage disaster recovery pairing of event hub namespaces.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubDisasterRecoveryPairings extends
        SupportsCreating<EventHubDisasterRecoveryPairing.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<EventHubDisasterRecoveryPairing>,
        HasInner<DisasterRecoveryConfigsInner>,
        HasManager<EventHubManager> {
    /**
     * @return entry point to manage authorization rules of a disaster recovery pairing.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    DisasterRecoveryPairingAuthorizationRules authorizationRules();
    /**
     * Lists the disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return list of disaster recovery pairings
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<EventHubDisasterRecoveryPairing> listByNamespace(String resourceGroupName, String namespaceName);
    /**
     * Lists the disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @return observable that emits disaster recovery pairings
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubDisasterRecoveryPairing> listByNamespaceAsync(String resourceGroupName, String namespaceName);
    /**
     * Gets a disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name disaster recovery pairing name
     * @return observable that emits disaster recovery pairings
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<EventHubDisasterRecoveryPairing> getByNameAsync(String resourceGroupName, String namespaceName, String name);
    /**
     * Gets a disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name disaster recovery pairing name
     * @return the disaster recovery pairing
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    EventHubDisasterRecoveryPairing getByName(String resourceGroupName, String namespaceName, String name);
    /**
     * Deletes a disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name disaster recovery pairing name
     * @return the completable representing the task
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Completable deleteByNameAsync(String resourceGroupName, String namespaceName, String name);
    /**
     * Deletes a disaster recovery pairings of a namespace under a resource group.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName namespace name
     * @param name disaster recovery pairing
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    void deleteByName(String resourceGroupName, String namespaceName, String name);
}
