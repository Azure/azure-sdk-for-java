// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.DisasterRecoveryConfigsClient;
import com.azure.resourcemanager.eventhubs.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRules;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation for {@link DisasterRecoveryPairingAuthorizationRules}.
 */
public final class DisasterRecoveryPairingAuthorizationRulesImpl
    extends ReadableWrappersImpl<DisasterRecoveryPairingAuthorizationRule,
        DisasterRecoveryPairingAuthorizationRuleImpl,
        AuthorizationRuleInner>
    implements DisasterRecoveryPairingAuthorizationRules {

    private final EventHubsManager manager;

    public DisasterRecoveryPairingAuthorizationRulesImpl(EventHubsManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedIterable<DisasterRecoveryPairingAuthorizationRule> listByDisasterRecoveryPairing(
        String resourceGroupName, String namespaceName, String pairingName) {
        return PagedConverter.mapPage(inner()
            .listAuthorizationRules(resourceGroupName, namespaceName, pairingName),
            this::wrapModel);
    }

    @Override
    public PagedFlux<DisasterRecoveryPairingAuthorizationRule> listByDisasterRecoveryPairingAsync(
        String resourceGroupName, String namespaceName, String pairingName) {
        return PagedConverter.mapPage(inner()
             .listAuthorizationRulesAsync(resourceGroupName, namespaceName, pairingName),
             this::wrapModel);
    }

    @Override
    public Mono<DisasterRecoveryPairingAuthorizationRule> getByNameAsync(
        String resourceGroupName, String namespaceName, String pairingName, String name) {
        return this.manager.serviceClient().getDisasterRecoveryConfigs().getAuthorizationRuleAsync(resourceGroupName,
            namespaceName,
            pairingName,
            name)
            .map(this::wrapModel);
    }

    @Override
    public DisasterRecoveryPairingAuthorizationRule getByName(
        String resourceGroupName, String namespaceName, String pairingName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, pairingName, name).block();
    }

    @Override
    public DisasterRecoveryPairingAuthorizationRule getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<DisasterRecoveryPairingAuthorizationRule> getByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return this.getByNameAsync(resourceId.resourceGroupName(),
            resourceId.parent().name(),
            resourceId.parent().parent().name(),
            resourceId.name());
    }

    @Override
    public EventHubsManager manager() {
        return this.manager;
    }

    public DisasterRecoveryConfigsClient inner() {
        return this.manager.serviceClient().getDisasterRecoveryConfigs();
    }

    @Override
    protected DisasterRecoveryPairingAuthorizationRuleImpl wrapModel(AuthorizationRuleInner inner) {
        return new DisasterRecoveryPairingAuthorizationRuleImpl(inner, manager);
    }
}
