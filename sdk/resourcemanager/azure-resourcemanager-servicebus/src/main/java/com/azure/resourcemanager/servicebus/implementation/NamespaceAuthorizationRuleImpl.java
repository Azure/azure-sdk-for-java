// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.ResourceListKeysInner;
import com.azure.resourcemanager.servicebus.fluent.models.SharedAccessAuthorizationRuleResourceInner;
import com.azure.resourcemanager.servicebus.models.NamespaceAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.Policykey;
import reactor.core.publisher.Mono;

/**
 * Implementation for NamespaceAuthorizationRule.
 */
class NamespaceAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<NamespaceAuthorizationRule,
    ServiceBusNamespaceImpl,
    SharedAccessAuthorizationRuleResourceInner,
    NamespaceAuthorizationRuleImpl,
    ServiceBusManager>
    implements
        NamespaceAuthorizationRule,
        NamespaceAuthorizationRule.Definition,
        NamespaceAuthorizationRule.Update {
    private final Region region;

    NamespaceAuthorizationRuleImpl(String resourceGroupName,
                                   String namespaceName,
                                   String name,
                                   Region region,
                                   SharedAccessAuthorizationRuleResourceInner inner,
                                   ServiceBusManager manager) {
        super(name, inner, manager);
        this.region = region;
        this.withExistingParentResource(resourceGroupName, namespaceName);
        if (inner.location() == null) {
            inner.withLocation(this.region.toString());
        }
    }

    @Override
    public String namespaceName() {
        return this.parentName;
    }

    @Override
    protected Mono<SharedAccessAuthorizationRuleResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getNamespaces()
                .getAuthorizationRuleAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.name());
    }

    @Override
    protected Mono<NamespaceAuthorizationRule> createChildResourceAsync() {
        final NamespaceAuthorizationRule self = this;
        return this.manager().serviceClient().getNamespaces()
            .createOrUpdateAuthorizationRuleAsync(
                this.resourceGroupName(),
                this.namespaceName(),
                this.name(),
                prepareForCreate(this.innerModel()))
            .map(inner -> {
                setInner(inner);
                return self;
            });
    }

    @Override
    protected Mono<ResourceListKeysInner> getKeysInnerAsync() {
        return this.manager().serviceClient().getNamespaces()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.name());
    }

    @Override
    protected Mono<ResourceListKeysInner> regenerateKeysInnerAsync(Policykey policykey) {
        return this.manager().serviceClient().getNamespaces()
                .regenerateKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.name(),
                        policykey);
    }
}
