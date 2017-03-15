/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.AccessRights;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRule;
import rx.Observable;

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
    public void listKeys() {
    }

    @Override
    public void regenerateKeys() {
    }

    @Override
    public NamespaceAuthorizationRuleImpl withAccessRight(AccessRights rights) {
        return null;
    }

    @Override
    public NamespaceAuthorizationRuleImpl withoutAccessRight(AccessRights rights) {
        return null;
    }

    @Override
    protected Observable<NamespaceAuthorizationRule> createChildResourceAsync() {
        return null;
    }

    @Override
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerAsync() {
        return null;
    }
}
