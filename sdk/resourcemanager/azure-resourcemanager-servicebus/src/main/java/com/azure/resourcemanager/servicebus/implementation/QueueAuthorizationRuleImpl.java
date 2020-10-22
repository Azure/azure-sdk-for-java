// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.ResourceListKeysInner;
import com.azure.resourcemanager.servicebus.fluent.models.SharedAccessAuthorizationRuleResourceInner;
import com.azure.resourcemanager.servicebus.models.Policykey;
import com.azure.resourcemanager.servicebus.models.QueueAuthorizationRule;
import reactor.core.publisher.Mono;

/**
 * Implementation for QueueAuthorizationRule.
 */
class QueueAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<QueueAuthorizationRule,
        QueueImpl,
        SharedAccessAuthorizationRuleResourceInner,
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
                               SharedAccessAuthorizationRuleResourceInner inner,
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
    protected Mono<SharedAccessAuthorizationRuleResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getQueues()
                .getAuthorizationRuleAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.queueName(),
                        this.name());
    }

    @Override
    protected Mono<QueueAuthorizationRule> createChildResourceAsync() {
        final QueueAuthorizationRule self = this;
        return this.manager().serviceClient().getQueues().createOrUpdateAuthorizationRuleAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.queueName(),
                this.name(),
                prepareForCreate(this.innerModel()))
            .map(inner -> {
                setInner(inner);
                return self;
            });
    }

    @Override
    protected Mono<ResourceListKeysInner> getKeysInnerAsync() {
        return this.manager().serviceClient().getQueues()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.queueName(),
                        this.name());
    }

    @Override
    protected Mono<ResourceListKeysInner> regenerateKeysInnerAsync(Policykey policykey) {
        return this.manager().serviceClient().getQueues()
                .regenerateKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.queueName(),
                        this.name(),
                        policykey);
    }
}
