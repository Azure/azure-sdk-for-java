// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.models.AccessKeysInner;
import com.azure.resourcemanager.eventhubs.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.KeyType;
import com.azure.resourcemanager.eventhubs.models.RegenerateAccessKeyParameters;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation for {@link EventHubNamespaceAuthorizationRule}.
 */
class EventHubNamespaceAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<EventHubNamespaceAuthorizationRule,
        EventHubNamespaceAuthorizationRuleImpl>
        implements
        EventHubNamespaceAuthorizationRule,
        EventHubNamespaceAuthorizationRule.Definition,
        EventHubNamespaceAuthorizationRule.Update {

    private Ancestors.OneAncestor ancestor;

    EventHubNamespaceAuthorizationRuleImpl(String name, AuthorizationRuleInner inner, EventHubsManager manager) {
        super(name, inner, manager);
        this.ancestor = new Ancestors().new OneAncestor(inner.id());
    }

    EventHubNamespaceAuthorizationRuleImpl(String name, EventHubsManager manager) {
        super(name, new AuthorizationRuleInner(), manager);
    }

    @Override
    public String namespaceResourceGroupName() {
        return this.ancestor().resourceGroupName();
    }

    @Override
    public String namespaceName() {
        return this.ancestor().ancestor1Name();
    }

    @Override
    public EventHubNamespaceAuthorizationRuleImpl withExistingNamespaceId(String namespaceResourceId) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespaceResourceId));
        return this;
    }

    @Override
    public EventHubNamespaceAuthorizationRuleImpl withExistingNamespace(
        String resourceGroupName, String namespaceName) {
        this.ancestor = new Ancestors().new OneAncestor(resourceGroupName, namespaceName);
        return this;
    }

    @Override
    public EventHubNamespaceAuthorizationRuleImpl withExistingNamespace(EventHubNamespace namespace) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespace.id()));
        return this;
    }

    @Override
    protected Mono<AuthorizationRuleInner> getInnerAsync() {
        return this.manager.serviceClient().getNamespaces()
                .getAuthorizationRuleAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    @Override
    public Mono<EventHubNamespaceAuthorizationRule> createResourceAsync() {
        return this.manager.serviceClient().getNamespaces()
                .createOrUpdateAuthorizationRuleAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor1Name(),
                        this.name(),
                        this.innerModel().rights())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<AccessKeysInner> getKeysInnerAsync() {
        return this.manager.serviceClient().getNamespaces()
                .listKeysAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    @Override
    protected Mono<AccessKeysInner> regenerateKeysInnerAsync(KeyType keyType) {
        final RegenerateAccessKeyParameters regenKeyInner = new RegenerateAccessKeyParameters()
                .withKeyType(keyType);
        return this.manager.serviceClient().getNamespaces()
                .regenerateKeysAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor1Name(),
                        this.name(),
                        regenKeyInner);
    }

    private Ancestors.OneAncestor ancestor() {
        Objects.requireNonNull(this.ancestor);
        return this.ancestor;
    }

    private String selfId(String parentId) {
        return String.format("%s/authorizationRules/%s", parentId, this.name());
    }
}
