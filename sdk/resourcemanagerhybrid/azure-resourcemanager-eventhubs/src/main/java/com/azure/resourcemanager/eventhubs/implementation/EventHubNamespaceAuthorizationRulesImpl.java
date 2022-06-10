// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.NamespacesClient;
import com.azure.resourcemanager.eventhubs.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRules;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import reactor.core.publisher.Mono;

import java.util.Objects;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation for {@link EventHubNamespaceAuthorizationRules}.
 */
public final class EventHubNamespaceAuthorizationRulesImpl
    extends AuthorizationRulesBaseImpl<NamespacesClient,
        EventHubNamespaceAuthorizationRule,
        EventHubNamespaceAuthorizationRuleImpl>
    implements EventHubNamespaceAuthorizationRules {

    public EventHubNamespaceAuthorizationRulesImpl(EventHubsManager manager) {
        super(manager, manager.serviceClient().getNamespaces());
    }

    @Override
    public EventHubNamespaceAuthorizationRuleImpl define(String name) {
        return new EventHubNamespaceAuthorizationRuleImpl(name, this.manager);
    }

    @Override
    public Mono<EventHubNamespaceAuthorizationRule> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        ResourceId resourceId = ResourceId.fromString(id);
        return getByNameAsync(resourceId.resourceGroupName(),
                resourceId.parent().name(),
                resourceId.name());
    }

    @Override
    public EventHubNamespaceAuthorizationRule getByName(String resourceGroupName, String namespaceName, String name) {
        return getByNameAsync(resourceGroupName, namespaceName, name).block();
    }

    @Override
    public Mono<EventHubNamespaceAuthorizationRule> getByNameAsync(
        String resourceGroupName, String namespaceName, String name) {
        return this.innerModel().getAuthorizationRuleAsync(resourceGroupName,
            namespaceName,
            name)
            .map(this::wrapModel);
    }

    @Override
    public PagedIterable<EventHubNamespaceAuthorizationRule> listByNamespace(
        final String resourceGroupName, final String namespaceName) {
        return PagedConverter.mapPage(innerModel()
            .listAuthorizationRules(resourceGroupName, namespaceName),
            this::wrapModel);
    }

    @Override
    public PagedFlux<EventHubNamespaceAuthorizationRule> listByNamespaceAsync(
        String resourceGroupName, String namespaceName) {
        return PagedConverter.mapPage(this.innerModel()
            .listAuthorizationRulesAsync(resourceGroupName, namespaceName),
            this::wrapModel);
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
        return this.innerModel().deleteAuthorizationRuleAsync(resourceGroupName,
                namespaceName,
                name);
    }

    @Override
    public void deleteByName(String resourceGroupName, String namespaceName, String name) {
        deleteByNameAsync(resourceGroupName, namespaceName, name).block();
    }

    @Override
    protected EventHubNamespaceAuthorizationRuleImpl wrapModel(AuthorizationRuleInner innerModel) {
        return new EventHubNamespaceAuthorizationRuleImpl(innerModel.name(), innerModel, this.manager);
    }
}
