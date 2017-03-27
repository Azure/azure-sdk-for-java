/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.servicebus.Policykey;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRule;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for QueueAuthorizationRule.
 */
@LangDefinition
class QueueAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<QueueAuthorizationRule,
        QueueImpl,
        SharedAccessAuthorizationRuleInner,
        QueueAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        QueueAuthorizationRule,
        QueueAuthorizationRule.Definition,
        QueueAuthorizationRule.Update {
    private final String namespaceName;
    private final Region region;

    QueueAuthorizationRuleImpl(String resourceGroupName,
                               String namespaceName,
                               String queueName,
                               String name,
                               Region region,
                               SharedAccessAuthorizationRuleInner inner,
                               ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.region = region;
        this.withExistingParentResource(resourceGroupName, queueName);
        if (inner.location() == null) {
            inner.withLocation(this.region.toString());
        }
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
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerAsync() {
        return this.manager().inner().queues()
                .getAuthorizationRuleAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.queueName(),
                        this.name());
    }

    @Override
    protected Observable<QueueAuthorizationRule> createChildResourceAsync() {
        final QueueAuthorizationRule self = this;
        return this.manager().inner().queues().createOrUpdateAuthorizationRuleAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.queueName(),
                this.name(),
                this.inner().rights()).map(new Func1<SharedAccessAuthorizationRuleInner, QueueAuthorizationRule>() {
            @Override
            public QueueAuthorizationRule call(SharedAccessAuthorizationRuleInner inner) {
                setInner(inner);
                return self;
            }
        });
    }

    @Override
    protected Observable<ResourceListKeysInner> getKeysInnerAsync() {
        return this.manager().inner().queues()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.queueName(),
                        this.name());
    }

    @Override
    protected Observable<ResourceListKeysInner> regenerateKeysInnerAsync(Policykey policykey) {
        return this.manager().inner().queues()
                .regenerateKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.queueName(),
                        this.name(),
                        policykey);
    }
}