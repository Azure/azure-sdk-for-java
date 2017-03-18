/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.*;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for QueueAuthorizationRule.
 */
@LangDefinition
class QueueAuthorizationRuleImpl extends IndependentChildResourceImpl<QueueAuthorizationRule,
        QueueImpl,
        SharedAccessAuthorizationRuleInner,
        QueueAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        QueueAuthorizationRule,
        QueueAuthorizationRule.Definition,
        QueueAuthorizationRule.Update {
    private final String namespaceName;

    QueueAuthorizationRuleImpl(String resourceGroupName,
                               String namespaceName,
                               String queueName,
                               String name,
                               SharedAccessAuthorizationRuleInner inner,
                               ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.withExistingParentResource(resourceGroupName, queueName);
    }

    @Override
    public Queue parent() {
        return null;
    }

    @Override
    public String namespaceName() {
        return this.namespaceName;
    }

    @Override
    public String queueName() {
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
        return this.manager().inner().queues()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.queueName(),
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
        return this.manager().inner().queues()
                .regenerateKeysAsync(this.resourceGroupName(),
                    this.namespaceName(),
                    this.queueName(),
                    this.name())
                .map(new Func1<ResourceListKeysInner, AuthorizationKeysImpl>() {
                    @Override
                    public AuthorizationKeysImpl call(ResourceListKeysInner inner) {
                        return new AuthorizationKeysImpl(inner);
                    }
                }).toBlocking().last();
    }

    @Override
    public QueueAuthorizationRuleImpl withAccessRight(AccessRights rights) {
        if (this.inner().rights() == null) {
            this.inner().withRights(new ArrayList<AccessRights>());
        }
        if (!this.inner().rights().contains(rights)) {
            this.inner().rights().add(rights);
        }
        return this;
    }

    @Override
    public QueueAuthorizationRuleImpl withoutAccessRight(AccessRights rights) {
        if (this.inner().rights() != null
                && this.inner().rights().contains(rights)) {
            this.inner().rights().remove(rights);
        }
        return this;
    }

    @Override
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerAsync() {
        return this.manager().inner().queues()
                .getAuthorizationRuleAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.queueName(),
                        this.name());
    }

    @Override
    protected Observable<QueueAuthorizationRule> createChildResourceAsync() {
        return null;
    }
}
