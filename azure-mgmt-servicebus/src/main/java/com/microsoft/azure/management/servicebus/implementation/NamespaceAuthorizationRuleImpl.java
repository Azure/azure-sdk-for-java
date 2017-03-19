/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.*;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for NamespaceAuthorizationRule.
 */
class NamespaceAuthorizationRuleImpl extends IndependentChildResourceImpl<NamespaceAuthorizationRule,
        NamespaceImpl,
        SharedAccessAuthorizationRuleInner,
        NamespaceAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        NamespaceAuthorizationRule,
        NamespaceAuthorizationRule.Definition,
        NamespaceAuthorizationRule.Update {
    NamespaceAuthorizationRuleImpl(String resourceGroupName,
                                   String namespaceName,
                                   String name,
                                   SharedAccessAuthorizationRuleInner inner,
                                   ServiceBusManager manager) {
        super(name, inner, manager);
        this.withExistingParentResource(resourceGroupName, namespaceName);
    }

    @Override
    public String namespaceName() {
        return this.parentName;
    }

    @Override
    public List<AccessRights> rights() {
        if (this.inner().rights() == null) {
            return Collections.unmodifiableList(new ArrayList<AccessRights>());
        }
        return Collections.unmodifiableList(this.inner().rights());
    }

    @Override
    public AuthorizationKeys getKeys() {
        return this.manager().inner().namespaces()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.name())
                .map(new Func1<ResourceListKeysInner, AuthorizationKeysImpl>() {
                    @Override
                    public AuthorizationKeysImpl call(ResourceListKeysInner inner) {
                        return new AuthorizationKeysImpl(inner);
                    }
                }).toBlocking().last();
    }

    @Override
    public AuthorizationKeys regenerateKey(Policykey policykey) {
        return this.manager().inner().namespaces()
                .regenerateKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.name())
                .map(new Func1<ResourceListKeysInner, AuthorizationKeysImpl>() {
                    @Override
                    public AuthorizationKeysImpl call(ResourceListKeysInner inner) {
                        return new AuthorizationKeysImpl(inner);
                    }
                }).toBlocking().last();
    }

    @Override
    public NamespaceAuthorizationRuleImpl withAccessRight(AccessRights rights) {
        if (this.inner().rights() == null) {
            this.inner().withRights(new ArrayList<AccessRights>());
        }
        if (!this.inner().rights().contains(rights)) {
            this.inner().rights().add(rights);
        }
        return this;
    }

    @Override
    public NamespaceAuthorizationRuleImpl withAccessRights(AccessRights... rights) {
        if (rights == null) {
            return this;
        }
        for (AccessRights r : rights) {
            withAccessRight(r);
        }
        return this;
    }

    @Override
    public NamespaceAuthorizationRuleImpl withoutAccessRight(AccessRights rights) {
        if (this.inner().rights() != null
                && this.inner().rights().contains(rights)) {
            this.inner().rights().remove(rights);
        }
        return this;
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
                this.inner()).map(new Func1<SharedAccessAuthorizationRuleInner, NamespaceAuthorizationRule>() {
            @Override
            public NamespaceAuthorizationRule call(SharedAccessAuthorizationRuleInner inner) {
                setInner(inner);
                return self;
            }
        });
    }
}
