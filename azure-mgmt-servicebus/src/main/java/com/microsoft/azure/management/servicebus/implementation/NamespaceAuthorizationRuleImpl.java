/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRule;
import com.microsoft.azure.management.servicebus.Policykey;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for NamespaceAuthorizationRule.
 */
@LangDefinition
class NamespaceAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<NamespaceAuthorizationRule,
        ServiceBusNamespaceImpl,
        SharedAccessAuthorizationRuleInner,
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
                                   SharedAccessAuthorizationRuleInner inner,
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
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerAsync() {
        return this.manager().inner().namespaces()
                .getAuthorizationRuleAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.name());
    }

    @Override
    protected Observable<NamespaceAuthorizationRule> createChildResourceAsync() {
        final NamespaceAuthorizationRule self = this;
        return this.manager().inner().namespaces().createOrUpdateAuthorizationRuleAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.name(),
                this.inner().rights()).map(new Func1<SharedAccessAuthorizationRuleInner, NamespaceAuthorizationRule>() {
            @Override
            public NamespaceAuthorizationRule call(SharedAccessAuthorizationRuleInner inner) {
                setInner(inner);
                return self;
            }
        });
    }

    @Override
    protected Observable<ResourceListKeysInner> getKeysInnerAsync() {
        return this.manager().inner().namespaces()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.name());
    }

    @Override
    protected Observable<ResourceListKeysInner> regenerateKeysInnerAsync(Policykey policykey) {
        return this.manager().inner().namespaces()
                .regenerateKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.name(),
                        policykey);
    }
}