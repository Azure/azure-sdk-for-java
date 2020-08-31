// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.DisasterRecoveryConfigsClient;
import com.azure.resourcemanager.eventhubs.fluent.inner.ArmDisasterRecoveryInner;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRules;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairing;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairings;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation for {@link EventHubDisasterRecoveryPairings}.
 */
public final class EventHubDisasterRecoveryPairingsImpl
    extends WrapperImpl<DisasterRecoveryConfigsClient>
    implements EventHubDisasterRecoveryPairings {
    private EventHubsManager manager;

    public EventHubDisasterRecoveryPairingsImpl(EventHubsManager manager) {
        super(manager.inner().getDisasterRecoveryConfigs());
        this.manager = manager;
    }

    @Override
    public EventHubsManager manager() {
        return this.manager;
    }

    @Override
    public EventHubDisasterRecoveryPairingImpl define(String name) {
        return new EventHubDisasterRecoveryPairingImpl(name, this.manager);
    }

    @Override
    public DisasterRecoveryPairingAuthorizationRules authorizationRules() {
        return null;
    }

    @Override
    public EventHubDisasterRecoveryPairing getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<EventHubDisasterRecoveryPairing> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);
        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public Mono<EventHubDisasterRecoveryPairing> getByNameAsync(
        String resourceGroupName, String namespaceName, String name) {
        return this.inner().getAsync(resourceGroupName,
            namespaceName,
            name)
            .map(this::wrapModel);
    }

    @Override
    public EventHubDisasterRecoveryPairing getByName(
        String resourceGroupName, String namespaceName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, name).block();
    }

    @Override
    public PagedIterable<EventHubDisasterRecoveryPairing> listByNamespace(
        String resourceGroupName, String namespaceName) {
        return inner()
            .list(resourceGroupName, namespaceName)
            .mapPage(this::wrapModel);
    }

    @Override
    public PagedFlux<EventHubDisasterRecoveryPairing> listByNamespaceAsync(
        String resourceGroupName, String namespaceName) {
        return this.inner().listAsync(resourceGroupName, namespaceName)
            .mapPage(this::wrapModel);
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);
        return deleteByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public Mono<Void> deleteByNameAsync(String resourceGroupName, String namespaceName, String name) {
        return this.inner().deleteAsync(resourceGroupName,
                namespaceName,
                name);
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, name).block();
    }

    private EventHubDisasterRecoveryPairingImpl wrapModel(ArmDisasterRecoveryInner innerModel) {
        return new EventHubDisasterRecoveryPairingImpl(innerModel.name(), innerModel, this.manager);
    }
}
