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
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Observable;

/**
 * Entry point to manage disaster recovery pairing authorization rules.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface DisasterRecoveryPairingAuthorizationRules extends
        SupportsGettingById<DisasterRecoveryPairingAuthorizationRule>,
        HasInner<DisasterRecoveryConfigsInner>,
        HasManager<EventHubManager> {
    /**
     * Lists the authorization rules that can be used to access the disaster recovery pairing.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName     primary namespace name
     * @param pairingName       pairing name
     * @return list of authorization rules
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<DisasterRecoveryPairingAuthorizationRule> listByDisasterRecoveryPairing(String resourceGroupName, String namespaceName, String pairingName);

    /**
     * Lists the authorization rules that can be used to access the disaster recovery pairing.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName     primary namespace name
     * @param pairingName       pairing name
     * @return observable that emits the authorization rules
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<DisasterRecoveryPairingAuthorizationRule> listByDisasterRecoveryPairingAsync(String resourceGroupName, String namespaceName, String pairingName);

    /**
     * Gets an authorization rule that can be used to access the disaster recovery pairing.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName     primary namespace name
     * @param pairingName       pairing name
     * @param name              rule name
     * @return observable that emits the authorization rule
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<DisasterRecoveryPairingAuthorizationRule> getByNameAsync(String resourceGroupName, String namespaceName, String pairingName, String name);

    /**
     * Gets an authorization rule that can be used to access the disaster recovery pairing.
     *
     * @param resourceGroupName resource group name
     * @param namespaceName     primary namespace name
     * @param pairingName       pairing name
     * @param name              rule name
     * @return the authorization rule
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    DisasterRecoveryPairingAuthorizationRule getByName(String resourceGroupName, String namespaceName, String pairingName, String name);
}