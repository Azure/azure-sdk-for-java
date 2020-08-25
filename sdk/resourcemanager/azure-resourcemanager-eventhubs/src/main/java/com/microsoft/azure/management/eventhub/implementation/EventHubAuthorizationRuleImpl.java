/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationRule;
import com.microsoft.azure.management.eventhub.KeyType;
import com.microsoft.azure.management.eventhub.RegenerateAccessKeyParameters;
import rx.Observable;

import java.util.Objects;

/**
 * Implementation for {@link EventHubAuthorizationRule}.
 */
@LangDefinition
class EventHubAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<EventHubAuthorizationRule,
        EventHubAuthorizationRuleImpl>
        implements
        EventHubAuthorizationRule,
        EventHubAuthorizationRule.Definition,
        EventHubAuthorizationRule.Update {

    private Ancestors.TwoAncestor ancestor;

    EventHubAuthorizationRuleImpl(String name, AuthorizationRuleInner inner, EventHubManager manager) {
        super(name, inner, manager);
        this.ancestor =  new Ancestors().new TwoAncestor(inner.id());
    }

    EventHubAuthorizationRuleImpl(String name, EventHubManager manager) {
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
    public EventHubAuthorizationRuleImpl withExistingEventHub(String resourceGroupName, String namespaceName, String eventHubName) {
        this.ancestor = new Ancestors().new TwoAncestor(resourceGroupName, eventHubName, namespaceName);
        return this;
    }

    @Override
    public EventHubAuthorizationRuleImpl withExistingEventHub(EventHub eventHub) {
        this.ancestor = new Ancestors().new TwoAncestor(selfId(eventHub.id()));
        return this;
    }

    @Override
    protected Observable<AuthorizationRuleInner> getInnerAsync() {
        return this.manager.inner().eventHubs()
                .getAuthorizationRuleAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    @Override
    public Observable<EventHubAuthorizationRule> createResourceAsync() {
        return this.manager.inner().eventHubs()
                .createOrUpdateAuthorizationRuleAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name(),
                        this.inner().rights())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<AccessKeysInner> getKeysInnerAsync() {
        return this.manager.inner().eventHubs()
                .listKeysAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor2Name(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    @Override
    protected Observable<AccessKeysInner> regenerateKeysInnerAsync(KeyType keyType) {
        final RegenerateAccessKeyParameters regenKeyInner = new RegenerateAccessKeyParameters()
                .withKeyType(keyType);
        return this.manager.inner().eventHubs()
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