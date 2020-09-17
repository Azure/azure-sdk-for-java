// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.EventHubsClient;
import com.azure.resourcemanager.eventhubs.fluent.inner.AuthorizationRuleInner;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationRules;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation for {@link EventHubAuthorizationRules}.
 */
public final class EventHubAuthorizationRulesImpl
    extends AuthorizationRulesBaseImpl<EventHubsClient,
        EventHubAuthorizationRule,
        EventHubAuthorizationRuleImpl>
    implements EventHubAuthorizationRules {

    public EventHubAuthorizationRulesImpl(EventHubsManager manager) {
        super(manager, manager.inner().getEventHubs());
    }

    @Override
    public EventHubAuthorizationRuleImpl define(String name) {
        return new EventHubAuthorizationRuleImpl(name, this.manager);
    }

    @Override
    public Mono<EventHubAuthorizationRule> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);

        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().parent().name(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public EventHubAuthorizationRule getByName(
        String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, eventHubName, name).block();
    }

    @Override
    public Mono<EventHubAuthorizationRule> getByNameAsync(
        String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return this.inner().getAuthorizationRuleAsync(resourceGroupName, namespaceName, eventHubName, name)
            .map(this::wrapModel);
    }

    @Override
    public PagedIterable<EventHubAuthorizationRule> listByEventHub(
        final String resourceGroupName, final String namespaceName, final String eventHubName) {
        return inner()
            .listAuthorizationRules(resourceGroupName, namespaceName, eventHubName)
            .mapPage(this::wrapModel);
    }

    @Override
    public PagedFlux<EventHubAuthorizationRule> listByEventHubAsync(
        String resourceGroupName, String namespaceName, final String eventHubName) {
        return this.inner()
            .listAuthorizationRulesAsync(resourceGroupName, namespaceName, eventHubName)
            .mapPage(this::wrapModel);
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);
        return deleteByNameAsync(resourceId.resourceGroupName(),
            resourceId.parent().parent().name(),
            resourceId.parent().name(),
            resourceId.name());
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String eventHubName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, eventHubName, name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(
        String resourceGroupName, String namespaceName, String eventHubName, String name) {
        return this.inner().deleteAuthorizationRuleAsync(resourceGroupName,
            namespaceName,
            eventHubName,
            name);
    }

    @Override
    protected EventHubAuthorizationRuleImpl wrapModel(AuthorizationRuleInner innerModel) {
        return new EventHubAuthorizationRuleImpl(innerModel.name(), innerModel, this.manager);
    }
}
