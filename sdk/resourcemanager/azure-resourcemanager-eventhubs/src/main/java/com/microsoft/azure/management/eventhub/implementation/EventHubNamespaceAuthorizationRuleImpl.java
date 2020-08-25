/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.eventhub.EventHubNamespaceAuthorizationRule;
import com.microsoft.azure.management.eventhub.KeyType;
import com.microsoft.azure.management.eventhub.RegenerateAccessKeyParameters;
import rx.Observable;

import java.util.Objects;

/**
 * Implementation for {@link EventHubNamespaceAuthorizationRule}.
 */
@LangDefinition
class EventHubNamespaceAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<EventHubNamespaceAuthorizationRule,
        EventHubNamespaceAuthorizationRuleImpl>
        implements
        EventHubNamespaceAuthorizationRule,
        EventHubNamespaceAuthorizationRule.Definition,
        EventHubNamespaceAuthorizationRule.Update {

    private Ancestors.OneAncestor ancestor;

    EventHubNamespaceAuthorizationRuleImpl(String name, AuthorizationRuleInner inner, EventHubManager manager) {
        super(name, inner, manager);
        this.ancestor = new Ancestors().new OneAncestor(inner.id());
    }

    EventHubNamespaceAuthorizationRuleImpl(String name, EventHubManager manager) {
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
    public EventHubNamespaceAuthorizationRuleImpl withExistingNamespace(String resourceGroupName, String namespaceName) {
        this.ancestor = new Ancestors().new OneAncestor(resourceGroupName, namespaceName);
        return this;
    }

    @Override
    public EventHubNamespaceAuthorizationRuleImpl withExistingNamespace(EventHubNamespace namespace) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespace.id()));
        return this;
    }

    @Override
    protected Observable<AuthorizationRuleInner> getInnerAsync() {
        return this.manager.inner().namespaces()
                .getAuthorizationRuleAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    @Override
    public Observable<EventHubNamespaceAuthorizationRule> createResourceAsync() {
        return this.manager.inner().namespaces()
                .createOrUpdateAuthorizationRuleAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor1Name(),
                        this.name(),
                        this.inner().rights())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<AccessKeysInner> getKeysInnerAsync() {
        return this.manager.inner().namespaces()
                .listKeysAsync(this.ancestor().resourceGroupName(),
                        this.ancestor().ancestor1Name(),
                        this.name());
    }

    @Override
    protected Observable<AccessKeysInner> regenerateKeysInnerAsync(KeyType keyType) {
        final RegenerateAccessKeyParameters regenKeyInner = new RegenerateAccessKeyParameters()
                .withKeyType(keyType);
        return this.manager.inner().namespaces()
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