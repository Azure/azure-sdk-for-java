// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.models.AccessKeysInner;
import com.azure.resourcemanager.eventhubs.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.KeyType;
import com.azure.resourcemanager.eventhubs.models.RegenerateAccessKeyParameters;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation for {@link EventHubAuthorizationRule}.
 */
class EventHubAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<EventHubAuthorizationRule,
        EventHubAuthorizationRuleImpl>
        implements
        EventHubAuthorizationRule,
        EventHubAuthorizationRule.Definition,
        EventHubAuthorizationRule.Update {

    private Ancestors.TwoAncestor ancestor;

    EventHubAuthorizationRuleImpl(String name, AuthorizationRuleInner inner, EventHubsManager manager) {
        super(name, inner, manager);
        this.ancestor =  new Ancestors().new TwoAncestor(inner.id());
    }

    EventHubAuthorizationRuleImpl(String name, EventHubsManager manager) {
        super(name, new AuthorizationRuleInner(), manager);
    }

    @Override
    public String namespaceResourceGroupName() {
        return this.ancestor().resourceGroupName();
    }

    @Override
    public String namespaceName() {
        return this.ancestor().ancestor2Name();
    }

    @Override
    public String eventHubName() {
        return this.ancestor().ancestor1Name();
    }

    @Override
    public EventHubAuthorizationRuleImpl withExistingEventHubId(String eventHubResourceId) {
        this.ancestor = new Ancestors().new TwoAncestor(selfId(eventHubResourceId));
        return this;
    }

    @Override
    public EventHubAuthorizationRuleImpl withExistingEventHub(
        String resourceGroupName, String namespaceName, String eventHubName) {
        this.ancestor = new Ancestors().new TwoAncestor(resourceGroupName, eventHubName, namespaceName);
        return this;
    }

    @Override
    public EventHubAuthorizationRuleImpl withExistingEventHub(EventHub eventHub) {
        this.ancestor = new Ancestors().new TwoAncestor(selfId(eventHub.id()));
        return this;
    }

    @Override
    protected Mono<AuthorizationRuleInner> getInnerAsync() {
        return this.manager.serviceClient().getEventHubs()
                .getAuthorizationRuleAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    @Override
    public Mono<EventHubAuthorizationRule> createResourceAsync() {
        return this.manager.serviceClient().getEventHubs()
                .createOrUpdateAuthorizationRuleAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name(),
                        new AuthorizationRuleInner().withRights(this.innerModel().rights()))
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<AccessKeysInner> getKeysInnerAsync() {
        return this.manager.serviceClient().getEventHubs()
                .listKeysAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    @Override
    protected Mono<AccessKeysInner> regenerateKeysInnerAsync(KeyType keyType) {
        final RegenerateAccessKeyParameters regenKeyInner = new RegenerateAccessKeyParameters()
                .withKeyType(keyType);
        return this.manager.serviceClient().getEventHubs()
                .regenerateKeysAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name(),
                        regenKeyInner);
    }

    private Ancestors.TwoAncestor ancestor() {
        Objects.requireNonNull(this.ancestor);
        return this.ancestor;
    }

    private String selfId(String parentId) {
        return String.format("%s/authorizationRules/%s", parentId, this.name());
    }
}
