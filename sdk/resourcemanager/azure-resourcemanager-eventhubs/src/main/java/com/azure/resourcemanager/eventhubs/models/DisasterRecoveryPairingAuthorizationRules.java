// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.fluent.DisasterRecoveryConfigsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import reactor.core.publisher.Mono;

/**
 * Entry point to manage disaster recovery pairing authorization rules.
 */
@Fluent
public interface DisasterRecoveryPairingAuthorizationRules extends
    SupportsGettingById<DisasterRecoveryPairingAuthorizationRule>,
    HasInner<DisasterRecoveryConfigsClient>,
    HasManager<EventHubsManager> {
    /**
     * Lists the authorization rules that can be used to access the disaster recovery pairing.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName primary namespace name
     * @param pairingName pairing name
     * @return list of authorization rules
     */
    PagedIterable<DisasterRecoveryPairingAuthorizationRule> listByDisasterRecoveryPairing(
        String resourceGroupName, String namespaceName, String pairingName);

    /**
     * Lists the authorization rules that can be used to access the disaster recovery pairing.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName primary namespace name
     * @param pairingName pairing name
     * @return observable that emits the authorization rules
     */
    PagedFlux<DisasterRecoveryPairingAuthorizationRule> listByDisasterRecoveryPairingAsync(
        String resourceGroupName, String namespaceName, String pairingName);

    /**
     * Gets an authorization rule that can be used to access the disaster recovery pairing.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName primary namespace name
     * @param pairingName pairing name
     * @param name rule name
     * @return observable that emits the authorization rule
     */
    Mono<DisasterRecoveryPairingAuthorizationRule> getByNameAsync(
        String resourceGroupName, String namespaceName, String pairingName, String name);

    /**
     * Gets an authorization rule that can be used to access the disaster recovery pairing.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName primary namespace name
     * @param pairingName pairing name
     * @param name rule name
     * @return the authorization rule
     */
    DisasterRecoveryPairingAuthorizationRule getByName(
        String resourceGroupName, String namespaceName, String pairingName, String name);
}
